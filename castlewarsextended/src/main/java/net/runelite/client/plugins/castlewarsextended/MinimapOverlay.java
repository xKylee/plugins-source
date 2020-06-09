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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.NPC;
import net.runelite.api.NPCDefinition;
import net.runelite.api.NpcID;
import net.runelite.api.ObjectID;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

public class MinimapOverlay extends Overlay
{
	private final Client client;
	private final CastleWarsExtendedConfig config;
	private final CastleWarsExtendedPlugin plugin;

	@Inject
	MinimapOverlay(Client client, CastleWarsExtendedConfig config, CastleWarsExtendedPlugin plugin)
	{
		this.client = client;
		this.config = config;
		this.plugin = plugin;
		setPosition(OverlayPosition.DYNAMIC);
		determineLayer();
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{

		for (NPC npc : plugin.getHighlightBarricades())
		{
			switch (npc.getId())
			{
				case NpcID.BARRICADE: //Saradomin Barricade
				case NpcID.BARRICADE_5723: //Saradomin Barricade Lit
					renderNpcMinimapOverlay(graphics, npc, npc.getName(), config.getSaradominHighlightColor());
					break;
				case NpcID.BARRICADE_5724: //Zamorak Barricade
				case NpcID.BARRICADE_5725: //Zamorak Barricade Lit
					renderNpcMinimapOverlay(graphics, npc, npc.getName(), config.getZamorakHighlightColor());
					break;
			}
		}

		for (GameObject gameObject : plugin.getHighlightRocks())
		{
			switch (gameObject.getId())
			{
				case ObjectID.ROCKS_4437: //Underground rocks full
				case ObjectID.ROCKS_4438: //Underground rocks half
					renderGameObjectMinimapOverlay(graphics, gameObject, gameObject.getId(), config.getRocksHighlightColor());
					break;
			}
		}

		if (config.displayOpenTunnels())
		{
			renderTunnelMinimapOverlay(graphics);
		}

		return null;
	}


	private void renderNpcMinimapOverlay(Graphics2D graphics, NPC actor, String name, Color color)
	{
		NPCDefinition npcDefinition = actor.getTransformedDefinition();
		if (npcDefinition == null || !npcDefinition.isFollower())
		{
			return;
		}

		Point minimapLocation = actor.getMinimapLocation();
		if (minimapLocation != null)
		{
			OverlayUtil.renderMinimapLocation(graphics, minimapLocation, color.darker());
		}
	}

	private void renderGameObjectMinimapOverlay(Graphics2D graphics, GameObject actor, int ID, Color color)
	{
		if (actor == null)
		{
			return;
		}

		Point minimapLocation = actor.getMinimapLocation();
		if (minimapLocation != null)
		{
			OverlayUtil.renderMinimapLocation(graphics, minimapLocation, color.darker());
		}
	}

	private void renderTunnelMinimapOverlay(Graphics2D graphics)
	{
		for (WorldPoint deSpawnedRock : plugin.getDeSpawnedRocks())
		{

			if (!config.displayOpenTunnels() || deSpawnedRock == null)
			{
				return;
			}
			String Text = "Open";

			/*
			 * Used to set text on deSpawnedRocks
			 */
			LocalPoint deSpawnedRocksLocation = LocalPoint.fromWorld(client, deSpawnedRock);
			if (deSpawnedRocksLocation == null)
			{
				continue;
			}
			net.runelite.api.Point deSpawnedRocksMinimapText = Perspective.getCanvasTextMiniMapLocation(client, graphics,
				deSpawnedRocksLocation, Text);
			if (deSpawnedRocksMinimapText != null)
			{
				graphics.setColor(Color.GREEN);
				graphics.drawString(Text, deSpawnedRocksMinimapText.getX() + 1, deSpawnedRocksMinimapText.getY() + 1);
			}
			if (deSpawnedRock.getX() == 2409 && deSpawnedRock.getY() == 9503)
			{
				LocalPoint saradominTunnelNorthLocation = LocalPoint.fromWorld(client, 2424, 9493);
				if (saradominTunnelNorthLocation == null)
				{
					continue;
				}
				net.runelite.api.Point saradominTunnelMinimapTextNorth = Perspective.getCanvasTextMiniMapLocation(client, graphics,
					saradominTunnelNorthLocation, Text);
				if (saradominTunnelMinimapTextNorth != null)
				{
					graphics.setColor(Color.GREEN);
					graphics.drawString(Text, saradominTunnelMinimapTextNorth.getX() + 1, saradominTunnelMinimapTextNorth.getY() + 1);
				}
			}
			if (deSpawnedRock.getX() == 2401 && deSpawnedRock.getY() == 9494)
			{
				LocalPoint saradominTunnelWestLocation = LocalPoint.fromWorld(client, 2418, 9483);
				if (saradominTunnelWestLocation == null)
				{
					continue;
				}
				net.runelite.api.Point saradominTunnelMinimapTextWest = Perspective.getCanvasTextMiniMapLocation(client, graphics,
					saradominTunnelWestLocation, Text);
				if (saradominTunnelMinimapTextWest != null)
				{
					graphics.setColor(Color.GREEN);
					graphics.drawString(Text, saradominTunnelMinimapTextWest.getX() + 1, saradominTunnelMinimapTextWest.getY() + 1);
				}
			}
			if (deSpawnedRock.getX() == 2391 && deSpawnedRock.getY() == 9501)
			{
				LocalPoint zamorakTunnelSouthLocation = LocalPoint.fromWorld(client, 2371, 9516);
				if (zamorakTunnelSouthLocation == null)
				{
					continue;
				}
				net.runelite.api.Point zamorakTunnelMinimapTextSouth = Perspective.getCanvasTextMiniMapLocation(client, graphics,
					zamorakTunnelSouthLocation, Text);
				if (zamorakTunnelMinimapTextSouth != null)
				{
					graphics.setColor(Color.GREEN);
					graphics.drawString(Text, zamorakTunnelMinimapTextSouth.getX() + 1, zamorakTunnelMinimapTextSouth.getY() + 1);
				}
			}
			if (deSpawnedRock.getX() == 2400 && deSpawnedRock.getY() == 9512)
			{
				LocalPoint zamorakTunnelEastLocation = LocalPoint.fromWorld(client, 2382, 9527);
				if (zamorakTunnelEastLocation == null)
				{
					continue;
				}
				net.runelite.api.Point zamorakTunnelMinimapTextEast = Perspective.getCanvasTextMiniMapLocation(client, graphics,
					zamorakTunnelEastLocation, Text);
				if (zamorakTunnelMinimapTextEast != null)
				{
					graphics.setColor(Color.GREEN);
					graphics.drawString(Text, zamorakTunnelMinimapTextEast.getX() + 1, zamorakTunnelMinimapTextEast.getY() + 1);
				}
			}

		}
	}

	public void determineLayer()
	{
		if (config.mirrorMode())
		{
			setLayer(OverlayLayer.AFTER_MIRROR);
		}
		if (!config.mirrorMode())
		{
			setLayer(OverlayLayer.ABOVE_WIDGETS);
		}
	}
}