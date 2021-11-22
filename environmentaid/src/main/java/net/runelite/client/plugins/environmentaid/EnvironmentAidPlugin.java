/*
 * Copyright (c) 2020, <github.com/xKylee> xKylee
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
package net.runelite.client.plugins.environmentaid;

import com.google.common.collect.Sets;
import com.google.inject.Provides;
import java.util.HashSet;
import java.util.Set;
import javax.inject.Inject;
import lombok.AccessLevel;
import lombok.Getter;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.NullObjectID;
import net.runelite.api.ObjectID;
import net.runelite.api.Player;
import net.runelite.api.WallObject;
import net.runelite.api.events.GameObjectChanged;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.WallObjectChanged;
import net.runelite.api.events.WallObjectDespawned;
import net.runelite.api.events.WallObjectSpawned;
import net.runelite.api.events.WidgetHiddenChanged;
import net.runelite.api.widgets.Widget;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import org.pf4j.Extension;

@Extension
@PluginDescriptor(
	name = "Environment Aid",
	enabledByDefault = false,
	description = "Display or Remove Environment Aids.",
	tags = {"Barrows", "Area", "Effects", "environment", "aid"}
)
public class EnvironmentAidPlugin extends Plugin
{
	@Inject
	private OverlayManager overlayManager;

	@Inject
	private Client client;

	@Inject
	private EnvironmentAidConfig config;

	@Inject
	private EnvironmentAidBarrowsOverlay environmentAidBarrowsOverlay;

	@Getter(AccessLevel.PACKAGE)
	public static final int MAX_DISTANCE = 2350;

	@Getter(AccessLevel.PACKAGE)
	private static final Set<Integer> BARROWS_WALLS = Sets.newHashSet
		(
			ObjectID.DOOR_20678, NullObjectID.NULL_20681, NullObjectID.NULL_20682, NullObjectID.NULL_20683, NullObjectID.NULL_20684, NullObjectID.NULL_20685, NullObjectID.NULL_20686, NullObjectID.NULL_20687,
			NullObjectID.NULL_20688, NullObjectID.NULL_20689, NullObjectID.NULL_20690, NullObjectID.NULL_20691, NullObjectID.NULL_20692, NullObjectID.NULL_20693, NullObjectID.NULL_20694, NullObjectID.NULL_20695,
			NullObjectID.NULL_20696, ObjectID.DOOR_20697, NullObjectID.NULL_20700, NullObjectID.NULL_20701, NullObjectID.NULL_20702, NullObjectID.NULL_20703, NullObjectID.NULL_20704, NullObjectID.NULL_20705,
			NullObjectID.NULL_20706, NullObjectID.NULL_20707, NullObjectID.NULL_20708, NullObjectID.NULL_20709, NullObjectID.NULL_20710, NullObjectID.NULL_20711, NullObjectID.NULL_20712, NullObjectID.NULL_20713,
			NullObjectID.NULL_20714, NullObjectID.NULL_20715, NullObjectID.NULL_20728, NullObjectID.NULL_20730
		);

	private static final Set<Integer> BARROWS_LADDERS = Sets.newHashSet(NullObjectID.NULL_20675, NullObjectID.NULL_20676, NullObjectID.NULL_20677);

	private static final int BARROWS_CRYPT_REGION_ID = 14231;
	private static final int ZAMORAK_REGION = 11603;

	private static final Set<Integer> SNOW_REGIONS = Set.of(
		11322, 11323, 11578, 11579,             //  ICE PATH + GWD SURFACE(ENTRANCE)
		10042,                                  //  WATERBIRTH ISLAND
		10810                                   //  FAIRY RING DKS
	);

	private static final Set<Integer> UNDERWATER_REGION = Set.of(
		15008, 15264,                           //  FOSSIL ISLAND UNDERWATER
		11924                                   //  MOGRE CAMP
	);

	private boolean wasInCrypt = false;

	@Getter(AccessLevel.PACKAGE)
	private final Set<WallObject> walls = new HashSet<>();

	@Getter(AccessLevel.PACKAGE)
	private final Set<GameObject> ladders = new HashSet<>();

	@Provides
	EnvironmentAidConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(EnvironmentAidConfig.class);
	}

	@Override
	protected void startUp()
	{
		overlayManager.add(environmentAidBarrowsOverlay);
		onStartUp();
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(environmentAidBarrowsOverlay);
		onShutDown();
	}

	private void onStartUp()
	{
		if (client.getWidget(167, 0) != null)
		{
			client.getWidget(167, 0).setHidden(config.snowEffect());
		}
		if (client.getWidget(406, 3) != null)
		{
			client.getWidget(406, 3).setHidden(config.zamorakEffect());
		}
		if (client.getWidget(169, 0) != null)
		{
			client.getWidget(169, 0).setHidden(config.waterEffect());
		}
		if (client.getWidget(170, 0) != null)
		{
			client.getWidget(170, 0).setHidden(config.waterEffect());
		}
		if (client.getWidget(404, 0) != null)
		{
			client.getWidget(404, 0).setHidden(config.scryPool());
		}
	}

	private void onShutDown()
	{
		wasInCrypt = false;
		walls.clear();
		ladders.clear();

		if (client.getWidget(406, 3) != null)
		{
			client.getWidget(406, 3).setHidden(false);
		}

		if (client.getWidget(167, 0) != null)
		{
			client.getWidget(167, 0).setHidden(false);
		}

		if (client.getWidget(169, 0) != null)
		{
			client.getWidget(169, 0).setHidden(false);
		}

		if (client.getWidget(170, 0) != null)
		{
			client.getWidget(170, 0).setHidden(false);
		}

		if (client.getWidget(404, 0) != null)
		{
			client.getWidget(404, 0).setHidden(false);
		}

	}

	@Subscribe
	public void onGameTick(GameTick gameTick)
	{
		if (isInZamorakRegion() && client.getWidget(406, 3) != null)
		{
			client.getWidget(406, 3).setHidden(config.zamorakEffect());
		}

		if (isInSnowRegions() && client.getWidget(167, 0) != null)
		{
			client.getWidget(167, 0).setHidden(config.snowEffect());
		}

		if (isInWaterRegion())
		{
			if (client.getWidget(169, 0) != null)
			{
				client.getWidget(169, 0).setHidden(config.waterEffect());
			}

			if (client.getWidget(170, 0) != null)
			{
				client.getWidget(170, 0).setHidden(config.waterEffect());
			}
		}
		if (client.getVarbitValue(4606) == 1 && client.getWidget(404, 0) != null)
		{
			client.getWidget(404, 0).setHidden(config.scryPool());
		}
	}

	@Subscribe
	public void widgetEvent(WidgetHiddenChanged widgetHiddenChanged)
	{
		Widget event = widgetHiddenChanged.getWidget();

		if (config.zamorakEffect() && isInZamorakRegion() && client.getWidget(406, 3) != null && event.getId() == client.getWidget(406, 3).getId())
		{
			hideWidget(event, true);
		}

		if (config.snowEffect() && isInSnowRegions() && client.getWidget(167, 0) != null && event.getId() == client.getWidget(167, 0).getId())
		{
			hideWidget(event, true);
		}

		if (config.waterEffect() && isInWaterRegion() && client.getWidget(169, 0) != null && event.getId() == client.getWidget(169, 0).getId())
		{
			hideWidget(event, true);
		}

		if (config.waterEffect() && isInWaterRegion() && client.getWidget(170, 0) != null && event.getId() == client.getWidget(170, 0).getId())
		{
			hideWidget(event, true);
		}

		if (config.scryPool() && client.getVarbitValue(4606) == 1 && client.getWidget(404, 0) != null && event.getId() == client.getWidget(404, 0).getId())
		{
			hideWidget(event, true);
		}
	}

	private void hideWidget(Widget widget, boolean hidden)
	{
		if (widget != null)
		{
			widget.setHidden(true);
		}
	}

	@Subscribe
	private void onWallObjectSpawned(WallObjectSpawned event)
	{
		WallObject wallObject = event.getWallObject();
		if (BARROWS_WALLS.contains(wallObject.getId()))
		{
			walls.add(wallObject);
		}
	}

	@Subscribe
	private void onWallObjectChanged(WallObjectChanged event)
	{
		WallObject previous = event.getPrevious();
		WallObject wallObject = event.getWallObject();

		walls.remove(previous);
		if (BARROWS_WALLS.contains(wallObject.getId()))
		{
			walls.add(wallObject);
		}
	}

	@Subscribe
	private void onWallObjectDespawned(WallObjectDespawned event)
	{
		WallObject wallObject = event.getWallObject();
		walls.remove(wallObject);
	}

	@Subscribe
	private void onGameObjectSpawned(GameObjectSpawned event)
	{
		GameObject gameObject = event.getGameObject();
		if (BARROWS_LADDERS.contains(gameObject.getId()))
		{
			ladders.add(gameObject);
		}
	}

	@Subscribe
	private void onGameObjectChanged(GameObjectChanged event)
	{
		GameObject previous = event.getPrevious();
		GameObject gameObject = event.getGameObject();

		ladders.remove(previous);
		if (BARROWS_LADDERS.contains(gameObject.getId()))
		{
			ladders.add(gameObject);
		}
	}

	@Subscribe
	private void onGameObjectDespawned(GameObjectDespawned event)
	{
		GameObject gameObject = event.getGameObject();
		ladders.remove(gameObject);
	}

	@Subscribe
	private void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOADING)
		{
			wasInCrypt = isInCrypt();
			// on region changes the tiles get set to null
			walls.clear();
			ladders.clear();
		}
	}

	private boolean isInCrypt()
	{
		Player localPlayer = client.getLocalPlayer();
		return localPlayer != null && localPlayer.getWorldLocation().getRegionID() == BARROWS_CRYPT_REGION_ID;
	}

	private boolean isInZamorakRegion()
	{
		Player localPlayer = client.getLocalPlayer();
		return localPlayer != null && localPlayer.getWorldLocation().getRegionID() == ZAMORAK_REGION;
	}

	private boolean isInWaterRegion()
	{
		Player localPlayer = client.getLocalPlayer();
		return localPlayer != null && UNDERWATER_REGION.contains(localPlayer.getWorldLocation().getRegionID());
	}

	private boolean isInSnowRegions()
	{
		Player localPlayer = client.getLocalPlayer();
		return localPlayer != null && SNOW_REGIONS.contains(localPlayer.getWorldLocation().getRegionID());
	}
}
