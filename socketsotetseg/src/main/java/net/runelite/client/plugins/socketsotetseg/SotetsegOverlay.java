/*
 * Copyright (c) 2020, Charles Xu <github.com/kthisiscvpv>
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

package net.runelite.client.plugins.socketsotetseg;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.IntStream;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.OverlayUtil;

public class SotetsegOverlay extends Overlay
{

	private final Client client;
	private final SotetsegPlugin plugin;
	private final SotetsegConfig config;

	private int flashTimeout;

	@Inject
	private SotetsegOverlay(Client client, SotetsegPlugin plugin, SotetsegConfig config)
	{
		this.client = client;
		this.plugin = plugin;
		this.config = config;

		setPosition(OverlayPosition.DYNAMIC);
		setPriority(OverlayPriority.HIGH);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}


	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (plugin.isSotetsegActive())
		{
			for (final WorldPoint next : plugin.getMazePings())
			{
				final LocalPoint localPoint = LocalPoint.fromWorld(client, next);
				if (localPoint != null)
				{
					Polygon poly = Perspective.getCanvasTilePoly(client, localPoint);
					if (poly == null)
					{
						continue;
					}

					Color color = config.getTileOutline();
					graphics.setColor(color);

					Stroke originalStroke = graphics.getStroke();
					int strokeSize = Math.max(config.getTileOutlineSize(), 1);
					graphics.setStroke(new BasicStroke(strokeSize));
					graphics.draw(poly);

					Color fill = config.getTileColor();
					int alpha = Math.min(Math.max(config.getTileTransparency(), 0), 255);
					Color realFill = new Color(fill.getRed(), fill.getGreen(), fill.getBlue(), alpha);
					graphics.setColor(realFill);
					graphics.fill(poly);

					graphics.setStroke(originalStroke);
				}
			}

			if (config.solveMaze())
			{
				drawPoints(graphics, plugin.getMazeSolved(), plugin.getMazeSolvedIndex());
			}

			if (plugin.isMazeActive() && config.showSotetsegInstanceTimer())
			{
				try
				{
					String text = String.valueOf(plugin.getInstanceTime());
					int width = graphics.getFontMetrics().stringWidth(text);
					Point base = Perspective.localToCanvas(client, plugin.getSotetsegNPC().getLocalLocation(), client.getPlane(), plugin.getSotetsegNPC().getLogicalHeight());
					Point actual = new Point(base.getX() - width / 2, base.getY() + 100);
					OverlayUtil.renderTextLocation(graphics, actual, text, Color.GREEN);
				}
				catch (NullPointerException ignored)
				{
				}
			}

			if (plugin.isFlashScreen() && config.flashScreen())
			{
				Color originalColor = graphics.getColor();
				graphics.setColor(new Color(255, 0, 0, 70));
				graphics.fill(client.getCanvas().getBounds());
				graphics.setColor(originalColor);

				if (++flashTimeout >= 15)
				{
					flashTimeout = 0;
					plugin.setFlashScreen(false);
				}
			}
		}

		return null;
	}

	private void drawPoints(Graphics2D graphics, ArrayList<Point> points, int index)
	{
		WorldPoint player = Objects.requireNonNull(client.getLocalPlayer()).getWorldLocation();

		IntStream.range(0, points.size()).forEach(i -> {
			Point p = points.get(i);
			WorldPoint wp = WorldPoint.fromRegion(player.getRegionID(), p.getX(), p.getY(), player.getPlane());
			LocalPoint lp = LocalPoint.fromWorld(client, wp);
			if (config.numbersOn() && index < i)
			{
				try
				{
					Point textPoint = Perspective.getCanvasTextLocation(client, graphics, lp, String.valueOf(i), 0);
					Point canvasCenterPoint = new Point(textPoint.getX(), (int) ((double) textPoint.getY() + Math.floor((double) config.getFontSize() / 2.0D)));
					OverlayUtil.renderTextLocation(graphics, canvasCenterPoint, String.valueOf(i), Color.WHITE);
				}
				catch (NullPointerException ignored)
				{
				}
			}
			if (config.highlightTiles() && index < i)
			{
				try
				{
					Polygon poly = Perspective.getCanvasTilePoly(client, lp);
					if (poly != null)
					{
						Color colorTile = config.getHighlightTileOutline();
						graphics.setColor(colorTile);
						Stroke originalStroke = graphics.getStroke();
						int strokeSize = 1;
						graphics.setStroke(new BasicStroke(strokeSize));
						if (i < points.size())
						{
							graphics.draw(poly);

							int alpha = Math.min(Math.max(35, 0), 255);
							Color realFill = new Color(colorTile.getRed(), colorTile.getGreen(), colorTile.getBlue(), alpha);
							graphics.setColor(realFill);
							graphics.fill(poly);

							graphics.setStroke(originalStroke);
						}
					}
				}
				catch (NullPointerException ignored)
				{
				}
			}
		});

	}
}
