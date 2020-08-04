/*
 * BSD 2-Clause License
 *
 * Copyright (c) 2020, dutta64 <https://github.com/dutta64>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.runelite.client.plugins.armadyl;

import com.google.inject.Provides;
import java.awt.Color;
import java.util.HashSet;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.MenuOpcode;
import static net.runelite.api.MenuOpcode.MENU_ACTION_DEPRIORITIZE_OFFSET;
import net.runelite.api.NPC;
import net.runelite.api.NpcID;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.util.Text;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginType;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ColorUtil;
import org.pf4j.Extension;

@Extension
@PluginDescriptor(
	name = "Armadyl",
	enabledByDefault = false,
	description = "Armadyl god wars dungeon plugin.",
	tags = {"armadyl", "god", "wars", "dungeon", "gwd", "kree'arra", "kreearra", "boss", "tick", "timer"},
	type = PluginType.PVM
)
@Singleton
@Slf4j
public class ArmadylPlugin extends Plugin
{
	private static final int ARMADYL_REGION = 11089;

	private static final Set<MenuOpcode> NPC_MENU_ACTIONS = Set.of(
		MenuOpcode.NPC_FIRST_OPTION,
		MenuOpcode.NPC_SECOND_OPTION,
		MenuOpcode.NPC_THIRD_OPTION,
		MenuOpcode.NPC_FOURTH_OPTION,
		MenuOpcode.NPC_FIFTH_OPTION,
		MenuOpcode.SPELL_CAST_ON_NPC,
		MenuOpcode.ITEM_USE_ON_NPC
	);

	@Inject
	private Client client;

	@Inject
	private EventBus eventBus;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private ArmadylConfig config;

	@Inject
	private ArmadylSceneOverlay sceneOverlay;

	@Inject
	private ArmadylPrayerOverlay prayerOverlay;

	@Getter
	private final Set<ArmadylNpc> armadylNpcs = new HashSet<>();

	@Getter
	private NPC kreearra;

	private boolean atArmadyl;

	@Getter(AccessLevel.PACKAGE)
	private long lastTickTime;

	@Provides
	ArmadylConfig getConfig(final ConfigManager configManager)
	{
		return configManager.getConfig(ArmadylConfig.class);
	}

	@Override
	protected void startUp()
	{
		if (client.getGameState() != GameState.LOGGED_IN || !atArmadyl())
		{
			return;
		}

		init();
	}

	@Override
	protected void shutDown()
	{
		eventBus.unregister(this);

		kreearra = null;
		atArmadyl = false;

		overlayManager.remove(sceneOverlay);
		overlayManager.remove(prayerOverlay);

		armadylNpcs.clear();
	}

	@Subscribe
	private void onGameStateChanged(final GameStateChanged event)
	{
		final GameState gameState = event.getGameState();

		switch (gameState)
		{
			case LOGGED_IN:
				if (atArmadyl())
				{
					if (!atArmadyl)
					{
						init();
					}
				}
				else
				{
					if (atArmadyl)
					{
						shutDown();
					}
				}
				break;
			case LOGIN_SCREEN:
			case HOPPING:
				shutDown();
				break;
		}
	}

	private void onGameTick(final GameTick event)
	{
		lastTickTime = System.currentTimeMillis();

		if (armadylNpcs.isEmpty())
		{
			return;
		}

		for (final ArmadylNpc armadylNpc : armadylNpcs)
		{
			armadylNpc.updateTicksUntilNextAnimation();
		}
	}

	private void onMenuEntryAdded(final MenuEntryAdded event)
	{
		if (!config.colorNpcMenuEntries())
		{
			return;
		}

		int type = event.getOpcode();

		if (type >= MENU_ACTION_DEPRIORITIZE_OFFSET)
		{
			type -= MENU_ACTION_DEPRIORITIZE_OFFSET;
		}

		final MenuOpcode menuOpcode = MenuOpcode.of(type);

		if (NPC_MENU_ACTIONS.contains(menuOpcode))
		{
			final int npcId = client.getCachedNPCs()[event.getIdentifier()].getId();

			Color color = null;

			for (final ArmadylNpc armadylNpc : armadylNpcs)
			{
				if (npcId == armadylNpc.getNpcId())
				{
					color = armadylNpc.getColor();
				}
			}

			if (color != null)
			{
				final String target = ColorUtil.prependColorTag(Text.removeTags(event.getTarget()), color);
				event.setTarget(target);
				event.setModified();
			}
		}
	}

	private void onNpcSpawned(final NpcSpawned event)
	{
		addNpc(event.getNpc());
	}

	private void onNpcDespawned(final NpcDespawned event)
	{
		removeNpc(event.getNpc());
	}

	private void addNpc(final NPC npc)
	{
		switch (npc.getId())
		{
			case NpcID.KREEARRA:
				kreearra = npc;
			case NpcID.FLIGHT_KILISA:
			case NpcID.FLOCKLEADER_GEERIN:
			case NpcID.WINGMAN_SKREE:
				armadylNpcs.add(new ArmadylNpc(npc));
				break;
			default:
				break;
		}
	}

	private void removeNpc(final NPC npc)
	{
		switch (npc.getId())
		{
			case NpcID.KREEARRA:
				kreearra = null;
			case NpcID.FLIGHT_KILISA:
			case NpcID.FLOCKLEADER_GEERIN:
			case NpcID.WINGMAN_SKREE:
				armadylNpcs.removeIf(armadylNpc -> armadylNpc.getNpcId() == npc.getId());
				break;
			default:
				break;
		}
	}

	private void init()
	{
		atArmadyl = true;

		overlayManager.add(sceneOverlay);
		overlayManager.add(prayerOverlay);

		for (final NPC npc : client.getNpcs())
		{
			addNpc(npc);
		}

		eventBus.subscribe(GameTick.class, this, this::onGameTick);
		eventBus.subscribe(NpcSpawned.class, this, this::onNpcSpawned);
		eventBus.subscribe(NpcDespawned.class, this, this::onNpcDespawned);
		eventBus.subscribe(MenuEntryAdded.class, this, this::onMenuEntryAdded);
	}

	private boolean atArmadyl()
	{
		return client.getMapRegions()[0] == ARMADYL_REGION;
	}
}
