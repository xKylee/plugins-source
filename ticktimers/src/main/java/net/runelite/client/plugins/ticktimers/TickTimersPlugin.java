/*
 * Copyright (c) 2019, Ganom <https://github.com/Ganom>
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
package net.runelite.client.plugins.ticktimers;

import com.google.inject.Provides;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.inject.Inject;
import lombok.AccessLevel;
import lombok.Getter;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.NPC;
import net.runelite.api.NpcID;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.NPCManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginType;
import net.runelite.client.ui.overlay.OverlayManager;
import org.pf4j.Extension;

@Extension
@PluginDescriptor(
	name = "Boss Tick Timers",
	enabledByDefault = false,
	description = "Tick timers for bosses",
	tags = {"pvm", "bossing"},
	type = PluginType.PVM
)
public class TickTimersPlugin extends Plugin
{
	private static final int GENERAL_REGION = 11347;
	private static final int ARMA_REGION = 11346;
	private static final int SARA_REGION = 11601;
	private static final int ZAMMY_REGION = 11603;
	private static final int WATERBITH_REGION = 11589;

	@Inject
	private Client client;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private TimersOverlay timersOverlay;

	@Inject
	private TickTimersConfig config;

	@Inject
	private NPCManager npcManager;

	@Getter(AccessLevel.PACKAGE)
	private Set<NPCContainer> npcContainers = new HashSet<>();
	private boolean validRegion;

	@Getter(AccessLevel.PACKAGE)
	private long lastTickTime;

	@Provides
	TickTimersConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(TickTimersConfig.class);
	}

	@Override
	public void startUp()
	{
		npcContainers.clear();
	}

	@Override
	public void shutDown()
	{
		npcContainers.clear();
		overlayManager.remove(timersOverlay);
		validRegion = false;
	}

	@Subscribe
	private void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}

		if (regionCheck())
		{
			for (NPC npc : client.getNpcs())
			{
				addNpc(npc);
			}
			validRegion = true;
			overlayManager.add(timersOverlay);
		}
		else
		{
			validRegion = false;
			overlayManager.remove(timersOverlay);
			npcContainers.clear();
		}
	}

	@Subscribe
	private void onNpcSpawned(NpcSpawned event)
	{
		if (!validRegion)
		{
			return;
		}

		addNpc(event.getNpc());
	}

	@Subscribe
	private void onNpcDespawned(NpcDespawned event)
	{
		if (!validRegion)
		{
			return;
		}

		removeNpc(event.getNpc());
	}

	@Subscribe
	public void onGameTick(GameTick Event)
	{
		lastTickTime = System.currentTimeMillis();

		if (!validRegion)
		{
			return;
		}

		handleBosses();
	}

	private void handleBosses()
	{
		for (NPCContainer npc : getNpcContainers())
		{
			npc.setNpcInteracting(npc.getNpc().getInteracting());

			if (npc.getTicksUntilAttack() >= 0)
			{
				npc.setTicksUntilAttack(npc.getTicksUntilAttack() - 1);
			}

			for (int animation : npc.getAnimations())
			{
				if (animation == npc.getNpc().getAnimation() && npc.getTicksUntilAttack() < 1)
				{
					npc.setTicksUntilAttack(npc.getAttackSpeed());
				}
			}
		}
	}

	private boolean regionCheck()
	{
		return Arrays.stream(client.getMapRegions()).anyMatch(
			x -> x == ARMA_REGION || x == GENERAL_REGION || x == ZAMMY_REGION || x == SARA_REGION || x == WATERBITH_REGION
		);
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (!event.getGroup().equals("TickTimers"))
		{
			return;
		}

		if (event.getKey().equals("mirrorMode"))
		{
			if (regionCheck())
			{
				timersOverlay.determineLayer();
				overlayManager.remove(timersOverlay);
				overlayManager.add(timersOverlay);
			}
		}
	}

	private void addNpc(NPC npc)
	{
		if (npc == null)
		{
			return;
		}

		switch (npc.getId())
		{
			case NpcID.SERGEANT_STRONGSTACK:
			case NpcID.SERGEANT_STEELWILL:
			case NpcID.SERGEANT_GRIMSPIKE:
			case NpcID.GENERAL_GRAARDOR:
			case NpcID.TSTANON_KARLAK:
			case NpcID.BALFRUG_KREEYATH:
			case NpcID.ZAKLN_GRITCH:
			case NpcID.KRIL_TSUTSAROTH:
			case NpcID.STARLIGHT:
			case NpcID.BREE:
			case NpcID.GROWLER:
			case NpcID.COMMANDER_ZILYANA:
			case NpcID.FLIGHT_KILISA:
			case NpcID.FLOCKLEADER_GEERIN:
			case NpcID.WINGMAN_SKREE:
			case NpcID.KREEARRA:
				if (config.gwd())
				{
					npcContainers.add(new NPCContainer(npc, npcManager.getAttackSpeed(npc.getId())));
				}
				break;
			case NpcID.DAGANNOTH_REX:
			case NpcID.DAGANNOTH_SUPREME:
			case NpcID.DAGANNOTH_PRIME:
				if (config.dks())
				{
					npcContainers.add(new NPCContainer(npc, npcManager.getAttackSpeed(npc.getId())));
				}
				break;
		}
	}

	private void removeNpc(NPC npc)
	{
		if (npc == null)
		{
			return;
		}

		switch (npc.getId())
		{
			case NpcID.SERGEANT_STRONGSTACK:
			case NpcID.SERGEANT_STEELWILL:
			case NpcID.SERGEANT_GRIMSPIKE:
			case NpcID.GENERAL_GRAARDOR:
			case NpcID.TSTANON_KARLAK:
			case NpcID.BALFRUG_KREEYATH:
			case NpcID.ZAKLN_GRITCH:
			case NpcID.KRIL_TSUTSAROTH:
			case NpcID.STARLIGHT:
			case NpcID.BREE:
			case NpcID.GROWLER:
			case NpcID.COMMANDER_ZILYANA:
			case NpcID.FLIGHT_KILISA:
			case NpcID.FLOCKLEADER_GEERIN:
			case NpcID.WINGMAN_SKREE:
			case NpcID.KREEARRA:
			case NpcID.DAGANNOTH_REX:
			case NpcID.DAGANNOTH_SUPREME:
			case NpcID.DAGANNOTH_PRIME:
				npcContainers.removeIf(c -> c.getNpc() == npc);
				break;
		}
	}
}