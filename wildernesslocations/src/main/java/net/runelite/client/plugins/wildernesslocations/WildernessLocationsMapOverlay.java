/*
 * Copyright (c) 2019, St0newall
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

package net.runelite.client.plugins.wildernesslocations;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Area;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Point;
import net.runelite.api.RenderOverview;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.game.WorldLocation;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;

public class WildernessLocationsMapOverlay extends Overlay
{
	private final Client client;
	private final WildernessLocationsPlugin plugin;
	private final WildernessLocationsConfig config;

	@Inject
	private WildernessLocationsMapOverlay(Client client, WildernessLocationsPlugin plugin, WildernessLocationsConfig config)
	{
		this.client = client;
		this.plugin = plugin;
		this.config = config;
		setPosition(OverlayPosition.DYNAMIC);
		determineLayer();
		setPriority(OverlayPriority.HIGH);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!(config.worldMapOverlay() || config.outlineLocations()))
		{
			return null;
		}
		drawWildLocations(graphics);
		return null;
	}


	/**
	 * Draws the names of the wilderness WorldLocations on the World Map
	 *
	 * @param graphics - Overlay graphics
	 */
	private void drawWildLocations(Graphics2D graphics)
	{
		final Widget widget = client.getWidget(WidgetInfo.WORLD_MAP_VIEW);
		if (widget == null)
		{
			return;
		}

		final Rectangle worldMapRectangle = widget.getBounds();
		final Rectangle canvasBounds = client.getCanvas().getBounds();
		canvasBounds.setLocation(0, 0);

		graphics.setClip(worldMapRectangle);

		for (WorldLocation worldLocation : WorldLocation.getWildernessLocations())
		{
			final int x1 = Math.min(worldLocation.getLocation().getX(), worldLocation.getLocation().getX1());
			final int y1 = Math.min(worldLocation.getLocation().getY(), worldLocation.getLocation().getY1());
			final int x2 = Math.max(worldLocation.getLocation().getX(), worldLocation.getLocation().getX1());
			final int y2 = Math.max(worldLocation.getLocation().getY(), worldLocation.getLocation().getY1());

			final int plane = worldLocation.getWorldArea().getPlane();
			final Point point = mapWorldPointToGraphicsPoint(new WorldPoint(x1, y1, plane));
			final Point point1 = mapWorldPointToGraphicsPoint(new WorldPoint(x2, y2, plane));
			if (point == null || point1 == null)
			{
				continue;
			}
			int width = Math.abs(point1.getX() - point.getX());
			int height = Math.abs(point.getY() - point1.getY());


			Rectangle rectangle = new Rectangle(point.getX(), point1.getY(), width, height);

			// These would be unreadable unless font color is black
			if (worldLocation.equals(WorldLocation.ICE_GATE) || worldLocation.equals(WorldLocation.ICE_ROCK))
			{
				graphics.setColor(Color.BLACK);
				if (config.worldMapOverlay())
				{
					graphics.drawString(worldLocation.getName(), point.getX(), point1.getY());
				}
				if (config.outlineLocations())
				{
					graphics.draw(rectangle);
				}
			}
			else
			{
				graphics.setColor(config.mapOverlayColor());
				if (config.worldMapOverlay())
				{
					graphics.drawString(worldLocation.getName(), point.getX(), point1.getY());
				}
				if (config.outlineLocations())
				{
					graphics.draw(rectangle);
				}
			}
		}
	}


	/**
	 * Get the screen coordinates for a WorldPoint on the world map
	 *
	 * @param worldPoint WorldPoint to get screen coordinates of
	 * @return Point of screen coordinates of the center of the world point
	 */
	private Point mapWorldPointToGraphicsPoint(WorldPoint worldPoint)
	{
		RenderOverview ro = client.getRenderOverview();

		if (!ro.getWorldMapData().surfaceContainsPosition(worldPoint.getX(), worldPoint.getY()))
		{
			return null;
		}

		float pixelsPerTile = ro.getWorldMapZoom();

		Widget map = client.getWidget(WidgetInfo.WORLD_MAP_VIEW);
		if (map != null)
		{
			Rectangle worldMapRect = map.getBounds();

			int widthInTiles = (int) Math.ceil(worldMapRect.getWidth() / pixelsPerTile);
			int heightInTiles = (int) Math.ceil(worldMapRect.getHeight() / pixelsPerTile);

			Point worldMapPosition = ro.getWorldMapPosition();

			//Offset in tiles from anchor sides
			int yTileMax = worldMapPosition.getY() - heightInTiles / 2;
			int yTileOffset = (yTileMax - worldPoint.getY() - 1) * -1;
			int xTileOffset = worldPoint.getX() + widthInTiles / 2 - worldMapPosition.getX();

			int xGraphDiff = ((int) (xTileOffset * pixelsPerTile));
			int yGraphDiff = (int) (yTileOffset * pixelsPerTile);

			//Center on tile.
			yGraphDiff -= pixelsPerTile - Math.ceil(pixelsPerTile / 2);
			xGraphDiff += pixelsPerTile - Math.ceil(pixelsPerTile / 2);

			yGraphDiff = worldMapRect.height - yGraphDiff;
			yGraphDiff += (int) worldMapRect.getY();
			xGraphDiff += (int) worldMapRect.getX();

			return new Point(xGraphDiff, yGraphDiff);
		}
		return null;
	}

	/**
	 * Gets a clip area which excludes the area of widgets which overlay the world map.
	 *
	 * @param baseRectangle The base area to clip from
	 * @return An {@link Area} representing <code>baseRectangle</code>, with the area
	 * of visible widgets overlaying the world map clipped from it.
	 */
	public Area getWorldMapClipArea(Rectangle baseRectangle)
	{
		final Widget overview = client.getWidget(WidgetInfo.WORLD_MAP_OVERVIEW_MAP);
		final Widget surfaceSelector = client.getWidget(WidgetInfo.WORLD_MAP_SURFACE_SELECTOR);

		Area clipArea = new Area(baseRectangle);

		if (overview != null && !overview.isHidden())
		{
			clipArea.subtract(new Area(overview.getBounds()));
		}

		if (surfaceSelector != null && !surfaceSelector.isHidden())
		{
			clipArea.subtract(new Area(surfaceSelector.getBounds()));
		}

		return clipArea;
	}

	private Point clipToRectangle(Point drawPoint, Rectangle mapDisplayRectangle)
	{
		int clippedX = drawPoint.getX();

		if (drawPoint.getX() < mapDisplayRectangle.getX())
		{
			clippedX = (int) mapDisplayRectangle.getX();
		}

		if (drawPoint.getX() > mapDisplayRectangle.getX() + mapDisplayRectangle.getWidth())
		{
			clippedX = (int) (mapDisplayRectangle.getX() + mapDisplayRectangle.getWidth());
		}

		int clippedY = drawPoint.getY();

		if (drawPoint.getY() < mapDisplayRectangle.getY())
		{
			clippedY = (int) mapDisplayRectangle.getY();
		}

		if (drawPoint.getY() > mapDisplayRectangle.getY() + mapDisplayRectangle.getHeight())
		{
			clippedY = (int) (mapDisplayRectangle.getY() + mapDisplayRectangle.getHeight());
		}

		return new Point(clippedX, clippedY);
	}

	public void determineLayer()
	{
		if (config.mirrorMode())
		{
			setLayer(OverlayLayer.AFTER_MIRROR);
		}
		if (!config.mirrorMode())
		{
			setLayer(OverlayLayer.ABOVE_MAP);
		}
	}
}