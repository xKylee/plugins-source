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
import net.runelite.api.Point;
import net.runelite.api.widgets.Widget;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.OverlayUtil;

public class GameTimeOverlay extends Overlay
{
	private final CastleWarsExtendedConfig config;
	private final Client client;

	@Inject
	private GameTimeOverlay(CastleWarsExtendedConfig config, Client client)
	{
		this.config = config;
		this.client = client;
		determineLayer();
		setPriority(OverlayPriority.HIGH);
		setPosition(OverlayPosition.DYNAMIC);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		Widget saradominTimeWidget = client.getWidget(58, 25); //Castle Wars saradomin time widget
		Widget zamorakTimeWidget = client.getWidget(59, 25); //Castle wars zamorak time widget
		Widget currentWidget = null;
		if (client.isResized())
		{
			if (saradominTimeWidget != null)
			{
				currentWidget = saradominTimeWidget;
			}
			else
			{
				if (zamorakTimeWidget != null)
				{
					currentWidget = zamorakTimeWidget;
				}
			}
			if (currentWidget != null)
			{
				final String text = currentWidget.getText();
				final Point point = new Point(488, 332);
				OverlayUtil.renderTextLocation(graphics, point, text, Color.WHITE);
				if (text.equals("1 Min"))
				{
					OverlayUtil.renderTextLocation(graphics, point, text, Color.RED);
				}
			}
		}
		return null;
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