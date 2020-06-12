/*
 * Copyright (c) 2020 Dutta64 <https://github.com/Dutta64>
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
package net.runelite.client.plugins.hallowedsepulchre;

import com.google.inject.Provides;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import lombok.AccessLevel;
import lombok.Getter;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.DynamicObject;
import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.LocatableQueryResults;
import net.runelite.api.NPC;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.queries.GameObjectQuery;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginType;
import static net.runelite.client.plugins.hallowedsepulchre.HallowedSepulchreIDs.*;
import net.runelite.client.ui.overlay.OverlayManager;
import org.pf4j.Extension;

@Extension
@PluginDescriptor(
	name = "Hallowed Sepulchre",
	enabledByDefault = false,
	description = "A plugin for the Hallowed Sepulchre agility minigame.",
	tags = {"sepulchre", "hallowed", "darkmeyer", "agility", "course", "minigame"},
	type = PluginType.MINIGAME
)
public class HallowedSepulchrePlugin extends Plugin
{
	private static final String GAME_MESSAGE_ENTER_LOBBY1 = "You make your way back to the lobby of the Hallowed Sepulchre.";
	private static final String GAME_MESSAGE_ENTER_LOBBY2 = "The obelisk teleports you back to the lobby of the Hallowed Sepulchre.";
	private static final String GAME_MESSAGE_ENTER_SEPULCHRE = "You venture down into the Hallowed Sepulchre.";

	@Inject
	private Client client;

	@Inject
	private HallowedSepulchreConfig config;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private HallowedSepulchreOverlay hallowedSepulchreOverlay;

	@Inject
	private GameObjectQuery gameObjectQuery;

	@Getter(AccessLevel.PACKAGE)
	private List<GameObject> crossbowStatues = new ArrayList<>();

	@Getter(AccessLevel.PACKAGE)
	private List<HallowedSepulchreGameObject> wizardStatues = new ArrayList<>();

	@Getter(AccessLevel.PACKAGE)
	private List<NPC> arrows = new ArrayList<>();

	@Getter(AccessLevel.PACKAGE)
	private List<NPC> swords = new ArrayList<>();

	@Getter(AccessLevel.PACKAGE)
	private boolean playerInSepulchre = false;

	@Provides
	HallowedSepulchreConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(HallowedSepulchreConfig.class);
	}

	@Override
	protected void startUp()
	{
		if (client.getGameState() != GameState.LOGGED_IN || !isInSepulchreRegion())
		{
			return;
		}

		locateSepulchreGameObjects();
		overlayManager.add(hallowedSepulchreOverlay);
		playerInSepulchre = true;
	}

	@Override
	protected void shutDown()
	{
		playerInSepulchre = false;
		overlayManager.remove(hallowedSepulchreOverlay);
		clearSepulchreGameObjects();
	}

	@Subscribe
	private void onConfigChanged(ConfigChanged event)
	{
		if (!event.getGroup().equals("hallowedsepulchre"))
		{
			return;
		}

		if (event.getKey().equals("mirrorMode"))
		{
			hallowedSepulchreOverlay.determineLayer();

			if (overlayManager.anyMatch(o -> o instanceof HallowedSepulchreOverlay))
			{
				overlayManager.remove(hallowedSepulchreOverlay);
				overlayManager.add(hallowedSepulchreOverlay);
			}
		}
	}

	@Subscribe
	private void onGameTick(GameTick event)
	{
		if (!playerInSepulchre)
		{
			return;
		}

		updateWizardStatueTickCounts();
	}

	@Subscribe
	private void onNpcSpawned(NpcSpawned event)
	{
		if (!playerInSepulchre)
		{
			return;
		}

		addNpc(event.getNpc());
	}

	@Subscribe
	private void onNpcDespawned(NpcDespawned event)
	{
		if (!playerInSepulchre)
		{
			return;
		}

		removeNpc(event.getNpc());
	}

	@Subscribe
	private void onGameObjectSpawned(GameObjectSpawned event)
	{
		if (!playerInSepulchre)
		{
			return;
		}

		addGameObject(event.getGameObject());
	}

	@Subscribe
	private void onGameStateChanged(GameStateChanged event)
	{
		final GameState gameState = event.getGameState();

		switch (gameState)
		{
			case LOGGED_IN:
				if (isInSepulchreRegion())
				{
					playerInSepulchre = true;
				}
				else if (playerInSepulchre)
				{
					shutDown();
				}
				break;
			case LOGIN_SCREEN:
				if (playerInSepulchre)
				{
					shutDown();
				}
				break;
			default:
				if (playerInSepulchre)
				{
					clearSepulchreGameObjects();
				}
				break;
		}
	}

	@Subscribe
	private void onChatMessage(ChatMessage message)
	{
		if (!playerInSepulchre || message.getType() != ChatMessageType.GAMEMESSAGE)
		{
			return;
		}

		switch (message.getMessage())
		{
			case GAME_MESSAGE_ENTER_LOBBY1:
			case GAME_MESSAGE_ENTER_LOBBY2:
				clearSepulchreGameObjects();
				break;
			case GAME_MESSAGE_ENTER_SEPULCHRE:
				if (!overlayManager.anyMatch(o -> o instanceof HallowedSepulchreOverlay))
				{
					overlayManager.add(hallowedSepulchreOverlay);
				}
				break;
			default:
				break;
		}
	}

	private void updateWizardStatueTickCounts()
	{
		if (!config.highlightWizardStatues() || wizardStatues.isEmpty())
		{
			return;
		}

		for (HallowedSepulchreGameObject wizardStatue : wizardStatues)
		{
			int ticks = wizardStatue.getTicksUntilNextAnimation();

			if (ticks >= 0)
			{
				wizardStatue.setTicksUntilNextAnimation(--ticks);
			}
			else
			{
				DynamicObject dynamicObject = (DynamicObject) wizardStatue.getGameObject().getEntity();

				if (dynamicObject.getAnimationID() == wizardStatue.getAnimationId())
				{
					wizardStatue.setTicksUntilNextAnimation(wizardStatue.getAnimationSpeed());
				}
			}
		}
	}

	private void addNpc(NPC npc)
	{
		switch (npc.getId())
		{
			case ARROW_9672:
			case ARROW_9673:
			case ARROW_9674:
				arrows.add(npc);
				break;
			case SWORD_9669:
			case SWORD_9670:
			case SWORD_9671:
				swords.add(npc);
				break;
			default:
				break;
		}
	}

	private void removeNpc(NPC npc)
	{
		switch (npc.getId())
		{
			case ARROW_9672:
			case ARROW_9673:
			case ARROW_9674:
				arrows.remove(npc);
				break;
			case SWORD_9669:
			case SWORD_9670:
			case SWORD_9671:
				swords.remove(npc);
				break;
			default:
				break;
		}
	}

	private void addGameObject(GameObject gameObject)
	{
		switch (gameObject.getId())
		{
			case CROSSBOW_STATUE_38444:
			case CROSSBOW_STATUE_38445:
			case CROSSBOW_STATUE_38446:
				crossbowStatues.add(gameObject);
				break;
			case WIZARD_STATUE_38409:
			case WIZARD_STATUE_38410:
			case WIZARD_STATUE_38411:
			case WIZARD_STATUE_38412:
			case WIZARD_STATUE_38416:
			case WIZARD_STATUE_38417:
			case WIZARD_STATUE_38418:
			case WIZARD_STATUE_38419:
			case WIZARD_STATUE_38420:
				wizardStatues.add(new HallowedSepulchreGameObject(gameObject, WIZARD_STATUE_ANIM_FIRE, WIZARD_STATUE_ANIM_SPEED_4));
				break;
			case WIZARD_STATUE_38421:
			case WIZARD_STATUE_38422:
			case WIZARD_STATUE_38423:
			case WIZARD_STATUE_38424:
			case WIZARD_STATUE_38425:
				wizardStatues.add(new HallowedSepulchreGameObject(gameObject, WIZARD_STATUE_ANIM_FIRE, WIZARD_STATUE_ANIM_SPEED_3));
				break;
			default:
				break;
		}
	}

	private void clearSepulchreGameObjects()
	{
		crossbowStatues.clear();
		wizardStatues.clear();
		arrows.clear();
		swords.clear();
	}

	private boolean isInSepulchreRegion()
	{
		return REGION_IDS.contains(client.getMapRegions()[0]);
	}

	private void locateSepulchreGameObjects()
	{
		final LocatableQueryResults<GameObject> locatableQueryResults = gameObjectQuery.result(client);

		for (GameObject gameObject : locatableQueryResults)
		{
			addGameObject(gameObject);
		}

		for (NPC npc : client.getNpcs())
		{
			addNpc(npc);
		}
	}
}