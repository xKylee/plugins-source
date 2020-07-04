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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.NPC;
import net.runelite.api.NPCDefinition;
import net.runelite.api.ObjectDefinition;
import net.runelite.api.Player;
import net.runelite.api.WallObject;
import net.runelite.api.coords.LocalPoint;
import static net.runelite.client.plugins.environmentaid.EnvironmentAidPlugin.MAX_DISTANCE;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

@Singleton
public class EnvironmentAidBarrowsOverlay extends Overlay
{
	private final Client client;
	private final EnvironmentAidConfig config;
	private final EnvironmentAidPlugin plugin;

	@Inject
	private EnvironmentAidBarrowsOverlay(final Client client, final EnvironmentAidConfig config, EnvironmentAidPlugin plugin)
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
		Player local = client.getLocalPlayer();
		final Color npcColor = getMinimapDotColor(1);
		final Color playerColor = getMinimapDotColor(2);

		// tunnels are only on z=0
		if (!plugin.getWalls().isEmpty() && client.getPlane() == 0 && config.showBarrowsMinimap())
		{
			// NPC dots
			graphics.setColor(npcColor);
			final List<NPC> npcs = client.getNpcs();
			for (NPC npc : npcs)
			{
				final NPCDefinition composition = npc.getDefinition();

				if (composition != null && !composition.isMinimapVisible())
				{
					continue;
				}

				net.runelite.api.Point minimapLocation = npc.getMinimapLocation();
				if (minimapLocation != null)
				{
					graphics.fillOval(minimapLocation.getX(), minimapLocation.getY(), 4, 4);
				}
			}

			// Player dots
			graphics.setColor(playerColor);
			final List<Player> players = client.getPlayers();
			for (Player player : players)
			{
				if (player == local)
				{
					// Skip local player as we draw square for it later
					continue;
				}

				net.runelite.api.Point minimapLocation = player.getMinimapLocation();
				if (minimapLocation != null)
				{
					graphics.fillOval(minimapLocation.getX(), minimapLocation.getY(), 4, 4);
				}
			}

			// Render barrows walls/doors
			renderObjects(graphics, local);

			// Local player square
			graphics.setColor(playerColor);
			graphics.fillRect(local.getMinimapLocation().getX(), local.getMinimapLocation().getY(), 3, 3);
		}

		return null;
	}

	private void renderObjects(Graphics2D graphics, Player localPlayer)
	{
		LocalPoint localLocation = localPlayer.getLocalLocation();
		for (WallObject wall : plugin.getWalls())
		{
			LocalPoint location = wall.getLocalLocation();
			if (localLocation.distanceTo(location) <= MAX_DISTANCE)
			{
				renderWalls(graphics, wall);
			}
		}

		for (GameObject ladder : plugin.getLadders())
		{
			LocalPoint location = ladder.getLocalLocation();
			if (localLocation.distanceTo(location) <= MAX_DISTANCE)
			{
				renderLadders(graphics, ladder);
			}
		}
	}

	private void renderWalls(Graphics2D graphics, WallObject wall)
	{
		net.runelite.api.Point minimapLocation = wall.getMinimapLocation();

		if (minimapLocation == null)
		{
			return;
		}

		ObjectDefinition objectComp = client.getObjectDefinition(wall.getId());
		ObjectDefinition impostor = objectComp.getImpostorIds() != null ? objectComp.getImpostor() : null;

		if (impostor != null && impostor.getActions()[0] != null)
		{
			graphics.setColor(Color.green);
		}
		else
		{
			graphics.setColor(Color.gray);
		}

		graphics.fillRect(minimapLocation.getX(), minimapLocation.getY(), 3, 3);
	}

	/**
	 * Get minimap dot color from client
	 *
	 * @param typeIndex index of minimap dot type (1 npcs, 2 players)
	 * @return color
	 */
	private Color getMinimapDotColor(int typeIndex)
	{
		final int pixel = client.getMapDots()[typeIndex].getPixels()[1];
		return new Color(pixel);
	}

	private void renderLadders(Graphics2D graphics, GameObject ladder)
	{
		net.runelite.api.Point minimapLocation = ladder.getMinimapLocation();

		if (minimapLocation == null)
		{
			return;
		}

		ObjectDefinition objectComp = client.getObjectDefinition(ladder.getId());

		if (objectComp.getImpostorIds() != null && objectComp.getImpostor() != null)
		{
			graphics.setColor(Color.orange);
			graphics.fillRect(minimapLocation.getX(), minimapLocation.getY(), 6, 6);
		}
	}

	public void determineLayer()
	{
		setLayer(config.mirrorMode() ? OverlayLayer.AFTER_MIRROR : OverlayLayer.ABOVE_WIDGETS);
	}
}
