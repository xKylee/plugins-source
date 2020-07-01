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
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;

public class SotetsegOverlay extends Overlay
{

	private final Client client;
	private final SotetsegPlugin plugin;
	private final SotetsegConfig config;

	@Inject
	private SotetsegOverlay(Client client, SotetsegPlugin plugin, SotetsegConfig config)
	{
		this.client = client;
		this.plugin = plugin;
		this.config = config;

		setPosition(OverlayPosition.DYNAMIC);
		setPriority(OverlayPriority.HIGH);
		determineLayer();
	}

	private void determineLayer()
	{
		if (config.mirrorMode())
		{
			setLayer(OverlayLayer.AFTER_MIRROR);
		}
		else
		{
			setLayer(OverlayLayer.ABOVE_SCENE);
		}
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
		}

		return null;
	}
}
