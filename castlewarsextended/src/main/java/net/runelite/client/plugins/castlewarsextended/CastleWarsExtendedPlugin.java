/*
 * Copyright (c) 2020, T7x <https://github.com/T7x>
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
package net.runelite.client.plugins.castlewarsextended;

import com.google.inject.Provides;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.MenuEntry;
import net.runelite.api.NPC;
import net.runelite.api.NpcID;
import net.runelite.api.ObjectID;
import net.runelite.api.Player;
import net.runelite.api.Tile;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameObjectChanged;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.NpcDefinitionChanged;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.util.Text;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginType;
import net.runelite.client.ui.overlay.OverlayManager;
import org.pf4j.Extension;

@Extension
@PluginDescriptor(
	name = "Castle Wars Extended",
	description = "Overpowered Castle Wars Plugin",
	tags = {"castlewars", "minigame", "barricade", "npc"},
	enabledByDefault = false,
	type = PluginType.MINIGAME
)
@Slf4j
public class CastleWarsExtendedPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private CastleWarsExtendedConfig config;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private SceneOverlay SceneOverlay;

	@Inject
	private MinimapOverlay MinimapOverlay;

	@Inject
	private TindTimerOverlay TindTimerOverlay;

	@Inject
	private GameTimeOverlay GameTimeOverlay;

	@Getter(AccessLevel.PACKAGE)
	private GameObject saradominStandard;

	@Getter(AccessLevel.PACKAGE)
	private GameObject zamorakStandard;

	@Getter(AccessLevel.PACKAGE)
	private final List<WorldPoint> deSpawnedRocks = new ArrayList<>();

	@Getter(AccessLevel.PACKAGE)
	private final Set<GameObject> highlightRocks = new HashSet<>();

	@Getter(AccessLevel.PACKAGE)
	private final Map<WorldPoint, Barricade> litBarricades = new HashMap<>();

	@Getter(AccessLevel.PACKAGE)
	private Instant lastActionTime = Instant.ofEpochMilli(0);

	@Getter(AccessLevel.PACKAGE)
	private final Set<NPC> highlightBarricades = new HashSet<>();

	public static int CASTLE_WARS = 9520;
	public static int CASTLE_WARS_UNDERGROUND = 9620;

	@Provides
	CastleWarsExtendedConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(CastleWarsExtendedConfig.class);
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (!event.getGroup().equals("castlewarsextended"))
		{
			return;
		}

		if (event.getKey().equals("mirrorMode"))
		{
			SceneOverlay.determineLayer();
			MinimapOverlay.determineLayer();
			TindTimerOverlay.determineLayer();
			GameTimeOverlay.determineLayer();
			overlayManager.remove(SceneOverlay);
			overlayManager.remove(MinimapOverlay);
			overlayManager.remove(TindTimerOverlay);
			overlayManager.remove(GameTimeOverlay);
			overlayManager.add(SceneOverlay);
			overlayManager.add(MinimapOverlay);
			overlayManager.add(TindTimerOverlay);
			overlayManager.add(GameTimeOverlay);
		}

		if (!config.rocksHighlight())
		{
			highlightRocks.clear();
		}
		if (!config.useTindTimer())
		{
			litBarricades.clear();
		}
		rebuildAllHighlightBarricades();
		TindTimerOverlay.updateConfig();
	}

	private void rebuildAllHighlightBarricades()
	{
		highlightBarricades.clear();

		if (client.getGameState() != GameState.LOGGED_IN &&
			client.getGameState() != GameState.LOADING)
		{
			return;
		}

		for (NPC npc : client.getNpcs())
		{
			switch (npc.getId())
			{
				case NpcID.BARRICADE:
				case NpcID.BARRICADE_5724:
					if (config.barricadeHighlight())
					{
						highlightBarricades.add(npc);
					}
					break;

			}
		}
	}

	@Override
	protected void startUp()
	{
		overlayManager.add(SceneOverlay);
		overlayManager.add(MinimapOverlay);
		overlayManager.add(TindTimerOverlay);
		overlayManager.add(GameTimeOverlay);
		TindTimerOverlay.updateConfig();
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(SceneOverlay);
		overlayManager.remove(MinimapOverlay);
		overlayManager.remove(TindTimerOverlay);
		overlayManager.remove(GameTimeOverlay);
		lastActionTime = Instant.ofEpochMilli(0);
		litBarricades.clear();
		highlightBarricades.clear();
		highlightRocks.clear();
		deSpawnedRocks.clear();

	}

	@Subscribe
	public void onGameStateChanged(final GameStateChanged event)
	{
		if (event.getGameState() != GameState.LOGGED_IN)
		{
			deSpawnedRocks.clear();
			litBarricades.clear();
			highlightBarricades.clear();
			highlightRocks.clear();
			saradominStandard = null;
			zamorakStandard = null;
		}
	}

	private boolean inCastleWars()
	{
		Player localPlayer = client.getLocalPlayer();
		return localPlayer != null && localPlayer.getWorldLocation().getRegionID() >= CASTLE_WARS  //9520 = Castle Wars
			&& localPlayer.getWorldLocation().getRegionID() <= CASTLE_WARS_UNDERGROUND; //9620 = Castle Wars underground
	}

	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded menuEntryAdded)
	{
		String option = Text.removeTags(menuEntryAdded.getOption()).toLowerCase();
		String target = Text.standardize(menuEntryAdded.getTarget());
		int identifier = menuEntryAdded.getIdentifier();
		MenuEntry[] menuEntries = client.getMenuEntries();

		if (config.hidePlayerOptions() && option.startsWith("use"))
		{

			if (!inCastleWars() || target.contains("bandages"))
			{
				return;
			}

			Player[] players = client.getCachedPlayers();
			Player player = null;

			if (identifier >= 0 && identifier < players.length)
			{
				player = players[identifier];
			}

			if (player == null)
			{
				return;
			}

			if (menuEntries.length > 0 && target.contains(player.getName().toLowerCase()))
			{
				client.setMenuEntries(Arrays.copyOf(menuEntries, menuEntries.length - 1));
			}
		}

		if (config.hideNpcOptions())
		{
			if (saradominStandard == null && zamorakStandard == null)
			{
				return;
			}

			NPC[] npcs = client.getCachedNPCs();
			NPC npc = null;
			if (identifier >= 0 && identifier < npcs.length)
			{
				npc = npcs[identifier];
			}
			if (npc == null)
			{
				return;
			}
			if (saradominStandard.getLocalLocation().equals(npc.getLocalLocation()) || zamorakStandard.getLocalLocation().equals(npc.getLocalLocation()))
			{
				if (menuEntries.length > 0)
				{
					client.setMenuEntries(Arrays.copyOf(menuEntries, menuEntries.length - 1));
				}
			}
		}
	}

	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned event)
	{
		final GameObject gameObject = event.getGameObject();
		final Tile tile = event.getTile();
		WorldPoint location = tile.getWorldLocation();

		switch (gameObject.getId())
		{

			case ObjectID.SARADOMIN_STANDARD:
				this.saradominStandard = gameObject;
				log.debug("Saradomin flag spawn: {}", gameObject);
				break;
			case ObjectID.ZAMORAK_STANDARD:
				this.zamorakStandard = gameObject;
				log.debug("Zamorak flag spawn: {}", gameObject);
				break;
			case ObjectID.ROCKS_4437: //Underground rocks full
			case ObjectID.ROCKS_4438: //Underground rocks half
				if (config.rocksHighlight() && gameObject.getWorldLocation().getRegionID() == CASTLE_WARS_UNDERGROUND)
				{
					highlightRocks.add(gameObject);
				}
				deSpawnedRocks.remove(location);
				log.debug("Rock spawn: {}", gameObject);
				break;
		}
	}

	@Subscribe
	public void onGameObjectChanged(GameObjectChanged event)
	{
		final GameObject gameObject = event.getGameObject();

		int id = gameObject.getId();
		if (id == ObjectID.ROCKS_4437 || id == ObjectID.ROCKS_4438)
		{
			highlightRocks.add(gameObject);
		}

	}

	@Subscribe
	public void onGameObjectDespawned(GameObjectDespawned event)
	{
		final GameObject gameObject = event.getGameObject();
		final Tile tile = event.getTile();
		WorldPoint location = tile.getWorldLocation();

		switch (gameObject.getId())
		{

			case ObjectID.SARADOMIN_STANDARD:
				this.saradominStandard = null;
				break;

			case ObjectID.ZAMORAK_STANDARD:
				this.zamorakStandard = null;
				break;

			case ObjectID.ROCKS_4438:
			case ObjectID.ROCKS_4437:
				deSpawnedRocks.add(location);
				highlightRocks.remove(gameObject);
				break;
		}
	}

	@Subscribe
	public void onGameTick(GameTick tick)
	{
		if (client.getLocalPlayer() == null || client.getLocalPlayer().getWorldLocation().getRegionID() != CASTLE_WARS_UNDERGROUND && !highlightRocks.isEmpty())
		{
			highlightRocks.clear();
		}

		Iterator<Map.Entry<WorldPoint, Barricade>> it = litBarricades.entrySet().iterator();
		Tile[][][] tiles = client.getScene().getTiles();

		Instant expire = Instant.now().plusSeconds(7);

		while (it.hasNext())
		{
			Map.Entry<WorldPoint, Barricade> entry = it.next();
			Barricade cade = entry.getValue();
			WorldPoint world = entry.getKey();
			LocalPoint local = LocalPoint.fromWorld(client, world);

			if (local == null)
			{
				if (cade.getLitOn().isBefore(expire))
				{
					it.remove();
				}
			}
		}
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned npcSpawned)
	{
		if (!config.barricadeHighlight())
		{
			return;
		}

		NPC npc = npcSpawned.getNpc();
		switch (npc.getId())
		{
			case NpcID.BARRICADE:
			case NpcID.BARRICADE_5723:
				highlightBarricades.add(npc);
				log.debug("Saradomin barricade spawn: {}", npc);
				break;

			case NpcID.BARRICADE_5724:
			case NpcID.BARRICADE_5725:
				highlightBarricades.add(npc);
				log.debug("Zamorak barricade spawn: {}", npc);
				break;
		}
	}

	@Subscribe
	public void onNpcChanged(NpcDefinitionChanged npcDefinitionChanged)
	{
		NPC npc = npcDefinitionChanged.getNpc();

		final WorldPoint npcLocation = npc.getWorldLocation();
		switch (npc.getId())
		{
			case NpcID.BARRICADE_5723:
			case NpcID.BARRICADE_5725:
				if (config.barricadeHighlight())
				{
					highlightBarricades.add(npc);
				}
				if (config.useTindTimer())
				{
					litBarricades.put(npcLocation, new Barricade(npc));
					lastActionTime = Instant.now();
				}
				break;
			case NpcID.BARRICADE:
			case NpcID.BARRICADE_5724:
				litBarricades.remove(npcLocation);
				break;
		}
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned npcDespawned)
	{
		NPC npc = npcDespawned.getNpc();

		final WorldPoint npcLocation = npc.getWorldLocation();
		switch (npc.getId())
		{
			case NpcID.BARRICADE:
			case NpcID.BARRICADE_5724:
				highlightBarricades.remove(npc);
				break;
			case NpcID.BARRICADE_5723:
			case NpcID.BARRICADE_5725:
				highlightBarricades.remove(npc);
				litBarricades.remove(npcLocation);
				break;
		}
	}
}