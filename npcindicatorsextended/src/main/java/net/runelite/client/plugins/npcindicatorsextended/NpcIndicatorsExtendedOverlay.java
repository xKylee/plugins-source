/*
 * Copyright (c) 2018, James Swindle <wilingua@gmail.com>
 * Copyright (c) 2018, Adam <Adam@sigterm.info>
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
package net.runelite.client.plugins.npcindicatorsextended;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.NPCComposition;
import net.runelite.api.Perspective;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.plugins.npchighlight.NpcIndicatorsPlugin;
import net.runelite.client.plugins.npchighlight.NpcIndicatorsConfig;

@Slf4j
public class NpcIndicatorsExtendedOverlay extends Overlay
{
	private static final Color TRANSPARENT = new Color(0, 0, 0, 0);

	// Anything but white text is quite hard to see since it is drawn on
	// a dark background
	private static final Color TEXT_COLOR = Color.WHITE;

	private final Client client;
	private final NpcIndicatorsExtendedPlugin plugin;
	private final NpcIndicatorsPlugin basePlugin;
	private final NpcIndicatorsExtendedConfig config;
	private final NpcIndicatorsConfig baseConfig;
	@Inject
	NpcIndicatorsExtendedOverlay(final Client client, final NpcIndicatorsExtendedPlugin plugin, final NpcIndicatorsExtendedConfig config)
	{
		this.client = client;
		this.plugin = plugin;
		this.basePlugin = plugin.getNpcIndicatorsPlugin();
		this.config = config;
		this.baseConfig = plugin.getNpcIndicatorsConfig();
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!config.trueTile())
		{
			return null;
		}

		for (NPC npc : plugin.getHighlightedNpcs())
		{
			renderNpcOverlay(graphics, npc, baseConfig.highlightColor());
		}
		return null;
	}



	private void renderNpcOverlay(Graphics2D graphics, NPC actor, Color color)
	{
		int size = 1;
		NPCComposition composition = actor.getTransformedComposition();

		if (composition != null)
		{
			size = composition.getSize();
		}

		final WorldPoint wp = actor.getWorldLocation();
		final Color squareColor = color;

		drawTile(graphics, wp, size, squareColor, 1, 255, 50);
	}

	private void drawTile(Graphics2D graphics, WorldPoint point, int size, Color color, int strokeWidth, int outlineAlpha, int fillAlpha)
	{
		WorldPoint playerLocation = client.getLocalPlayer().getWorldLocation();

		if (point.distanceTo(playerLocation) >= 32)
		{
			return;
		}

		LocalPoint lp = LocalPoint.fromWorld(client, point);

		if (lp == null)
		{
			return;
		}

		final Polygon poly = getPolygon(point, size);

		if (poly == null)
		{
			return;
		}

		graphics.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), outlineAlpha));
		graphics.setStroke(new BasicStroke(strokeWidth));
		graphics.draw(poly);
		graphics.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), fillAlpha));
		graphics.fill(poly);
	}

	private Polygon getPolygon(WorldPoint point, int size)
	{
		Polygon poly = new Polygon();
		List<Polygon> linePolyList = Perspective.getLinePolyList(client, point, new WorldPoint(point.getX() + size - 1, point.getY() + size - 1, point.getPlane()));
		int listSize = linePolyList.size();
		if (listSize <= 0)
		{
			return null;
		}
		int x = linePolyList.get(listSize - 1).xpoints[0];
		int y = linePolyList.get(listSize - 1).ypoints[0];
		poly.addPoint(x, y);

		x = linePolyList.get(listSize - 1).xpoints[1];
		y = linePolyList.get(listSize - 1).ypoints[1];
		poly.addPoint(x, y);
		linePolyList.remove(listSize - 1);


		while (linePolyList.size() > 0)
		{
			int finalX = x;
			int finalY = y;
			Optional<Polygon> linePolyOpt = linePolyList.stream().filter(p -> (p.xpoints[0] == finalX && p.ypoints[0] == finalY) || (p.xpoints[1] == finalX && p.ypoints[1] == finalY)).findFirst();
			if (linePolyOpt.isEmpty())
			{
				return null;
			}

			final Polygon linePoly = linePolyOpt.get();
			if (linePoly.xpoints[0] == finalX && linePoly.ypoints[0] == finalY)
			{
				x = linePoly.xpoints[1];
				y = linePoly.ypoints[1];
			}
			else
			{
				x = linePoly.xpoints[0];
				y = linePoly.ypoints[0];
			}
			poly.addPoint(x, y);
			linePolyList.remove(linePoly);
		}
		return poly;
	}

}
