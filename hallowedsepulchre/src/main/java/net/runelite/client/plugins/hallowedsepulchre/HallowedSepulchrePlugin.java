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
import net.runelite.api.Entity;
import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.NPC;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginType;
import static net.runelite.client.plugins.hallowedsepulchre.HallowedSepulchreIDs.ARROW_9672;
import static net.runelite.client.plugins.hallowedsepulchre.HallowedSepulchreIDs.ARROW_9673;
import static net.runelite.client.plugins.hallowedsepulchre.HallowedSepulchreIDs.ARROW_9674;
import static net.runelite.client.plugins.hallowedsepulchre.HallowedSepulchreIDs.CROSSBOW_STATUE_38444;
import static net.runelite.client.plugins.hallowedsepulchre.HallowedSepulchreIDs.CROSSBOW_STATUE_38445;
import static net.runelite.client.plugins.hallowedsepulchre.HallowedSepulchreIDs.CROSSBOW_STATUE_38446;
import static net.runelite.client.plugins.hallowedsepulchre.HallowedSepulchreIDs.REGION_IDS;
import static net.runelite.client.plugins.hallowedsepulchre.HallowedSepulchreIDs.SWORD_9669;
import static net.runelite.client.plugins.hallowedsepulchre.HallowedSepulchreIDs.SWORD_9670;
import static net.runelite.client.plugins.hallowedsepulchre.HallowedSepulchreIDs.SWORD_9671;
import static net.runelite.client.plugins.hallowedsepulchre.HallowedSepulchreIDs.WIZARD_STATUE_38409;
import static net.runelite.client.plugins.hallowedsepulchre.HallowedSepulchreIDs.WIZARD_STATUE_38410;
import static net.runelite.client.plugins.hallowedsepulchre.HallowedSepulchreIDs.WIZARD_STATUE_38411;
import static net.runelite.client.plugins.hallowedsepulchre.HallowedSepulchreIDs.WIZARD_STATUE_38412;
import static net.runelite.client.plugins.hallowedsepulchre.HallowedSepulchreIDs.WIZARD_STATUE_38416;
import static net.runelite.client.plugins.hallowedsepulchre.HallowedSepulchreIDs.WIZARD_STATUE_38417;
import static net.runelite.client.plugins.hallowedsepulchre.HallowedSepulchreIDs.WIZARD_STATUE_38418;
import static net.runelite.client.plugins.hallowedsepulchre.HallowedSepulchreIDs.WIZARD_STATUE_38419;
import static net.runelite.client.plugins.hallowedsepulchre.HallowedSepulchreIDs.WIZARD_STATUE_38420;
import static net.runelite.client.plugins.hallowedsepulchre.HallowedSepulchreIDs.WIZARD_STATUE_38421;
import static net.runelite.client.plugins.hallowedsepulchre.HallowedSepulchreIDs.WIZARD_STATUE_38422;
import static net.runelite.client.plugins.hallowedsepulchre.HallowedSepulchreIDs.WIZARD_STATUE_38423;
import static net.runelite.client.plugins.hallowedsepulchre.HallowedSepulchreIDs.WIZARD_STATUE_38424;
import static net.runelite.client.plugins.hallowedsepulchre.HallowedSepulchreIDs.WIZARD_STATUE_38425;
import static net.runelite.client.plugins.hallowedsepulchre.HallowedSepulchreIDs.WIZARD_STATUE_ANIM_FIRE;
import static net.runelite.client.plugins.hallowedsepulchre.HallowedSepulchreIDs.WIZARD_STATUE_ANIM_SPEED_2;
import static net.runelite.client.plugins.hallowedsepulchre.HallowedSepulchreIDs.WIZARD_STATUE_ANIM_SPEED_3;
import static net.runelite.client.plugins.hallowedsepulchre.HallowedSepulchreIDs.WIZARD_STATUE_ANIM_SPEED_4;
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
	@Inject
	private Client client;

	@Inject
	private HallowedSepulchreConfig config;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private HallowedSepulchreOverlay hallowedSepulchreOverlay;

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
		overlayManager.add(hallowedSepulchreOverlay);

		resetHallowedSepulchre();
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(hallowedSepulchreOverlay);

		resetHallowedSepulchre();
	}

	@Subscribe
	private void onConfigChanged(ConfigChanged event)
	{
		if (!event.getGroup().equals("hallowedsepulchre"))
		{
			return;
		}

		switch (event.getKey())
		{
			case "mirrorMode":
				hallowedSepulchreOverlay.determineLayer();
				overlayManager.remove(hallowedSepulchreOverlay);
				overlayManager.add(hallowedSepulchreOverlay);
				break;
			default:
				break;
		}
	}

	@Subscribe
	private void onGameTick(GameTick event)
	{
		if (!isInSepulchreRegion())
		{
			if (playerInSepulchre)
			{
				resetHallowedSepulchre();
				playerInSepulchre = false;
			}

			return;
		}

		playerInSepulchre = true;

		if (config.highlightWizardStatues() && !wizardStatues.isEmpty())
		{
			for (HallowedSepulchreGameObject wizardStatue : wizardStatues)
			{
				int ticks = wizardStatue.getTicksUntilNextAnimation();

				if (ticks >= 0)
				{
					wizardStatue.setTicksUntilNextAnimation(--ticks);
				}
				else
				{
					Entity entity = wizardStatue.getGameObject().getEntity();

					if (!(entity instanceof DynamicObject))
					{
						return;
					}

					if (((DynamicObject) entity).getAnimationID() == wizardStatue.getAnimationId())
					{
						wizardStatue.setTicksUntilNextAnimation(wizardStatue.getAnimationSpeed());
					}
				}
			}
		}
	}

	@Subscribe
	private void onNpcSpawned(NpcSpawned event)
	{
		if (!playerInSepulchre)
		{
			return;
		}

		NPC npc = event.getNpc();

		addNpc(npc);
	}

	@Subscribe
	private void onNpcDespawned(NpcDespawned event)
	{
		if (!playerInSepulchre)
		{
			return;
		}

		NPC npc = event.getNpc();

		removeNpc(npc);
	}

	@Subscribe
	private void onGameObjectSpawned(GameObjectSpawned event)
	{
		if (!playerInSepulchre)
		{
			return;
		}

		GameObject gameObject = event.getGameObject();

		addGameObject(gameObject);
	}

	@Subscribe
	private void onGameObjectDespawned(GameObjectDespawned event)
	{
		if (!playerInSepulchre)
		{
			return;
		}

		GameObject gameObject = event.getGameObject();

		removeGameObject(gameObject);
	}

	@Subscribe
	private void onGameStateChanged(GameStateChanged event)
	{
		if (!playerInSepulchre)
		{
			return;
		}

		if (event.getGameState() != GameState.LOGGED_IN)
		{
			resetHallowedSepulchre();
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
			case "You make your way back to the lobby of the Hallowed Sepulchre.":
			case "The obelisk teleports you back to the lobby of the Hallowed Sepulchre.":
				resetHallowedSepulchre();
				break;
			default:
				break;
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
				wizardStatues.add(new HallowedSepulchreGameObject(gameObject, WIZARD_STATUE_ANIM_FIRE, WIZARD_STATUE_ANIM_SPEED_4));
				break;
			case WIZARD_STATUE_38416:
			case WIZARD_STATUE_38417:
			case WIZARD_STATUE_38418:
			case WIZARD_STATUE_38419:
			case WIZARD_STATUE_38420:
				wizardStatues.add(new HallowedSepulchreGameObject(gameObject, WIZARD_STATUE_ANIM_FIRE, WIZARD_STATUE_ANIM_SPEED_3));
				break;
			case WIZARD_STATUE_38421:
			case WIZARD_STATUE_38422:
			case WIZARD_STATUE_38423:
			case WIZARD_STATUE_38424:
			case WIZARD_STATUE_38425:
				wizardStatues.add(new HallowedSepulchreGameObject(gameObject, WIZARD_STATUE_ANIM_FIRE, WIZARD_STATUE_ANIM_SPEED_2));
				break;
			default:
				break;
		}
	}

	private void removeGameObject(GameObject gameObject)
	{
		switch (gameObject.getId())
		{
			case CROSSBOW_STATUE_38444:
			case CROSSBOW_STATUE_38445:
			case CROSSBOW_STATUE_38446:
				crossbowStatues.remove(gameObject);
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
			case WIZARD_STATUE_38421:
			case WIZARD_STATUE_38422:
			case WIZARD_STATUE_38423:
			case WIZARD_STATUE_38424:
			case WIZARD_STATUE_38425:
				wizardStatues.removeIf(o -> o.getGameObject() == gameObject);
				break;
			default:
				break;
		}
	}

	private void resetHallowedSepulchre()
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
}