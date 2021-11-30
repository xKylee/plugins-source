/*
 * BSD 2-Clause License
 *
 * Copyright (c) 2020, dutta64 <https://github.com/dutta64>
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

package net.runelite.client.plugins.playerattacktimer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.api.VarClientInt;
import net.runelite.api.widgets.Widget;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.OverlayUtil;

@Singleton
class PrayerOverlay extends Overlay
{
	private final Client client;
	private final PlayerAttackTimerPlugin plugin;
	private final PlayerAttackTimerConfig config;

	private Player player;

	@Inject
	PrayerOverlay(final Client client, final PlayerAttackTimerPlugin plugin, final PlayerAttackTimerConfig config)
	{
		this.client = client;
		this.plugin = plugin;
		this.config = config;

		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
		setPriority(OverlayPriority.HIGHEST);
	}

	@Override
	public Dimension render(final Graphics2D graphics2D)
	{
		player = client.getLocalPlayer();

		if (player == null)
		{
			return null;
		}

		if (config.drawPrayerHelper())
		{
			renderPrayer(graphics2D);
		}

		return null;
	}

	private void renderPrayer(final Graphics2D graphics2D)
	{
		if (!config.alwaysShow() && client.getVar(VarClientInt.INVENTORY_TAB) != 5)
		{
			return;
		}

		int ticksUntilNextAnimation = plugin.getTicksUntilNextAnimation();
		if (ticksUntilNextAnimation == 0)
		{
			return;
		}

		var currentPrayer = plugin.getCurrentPrayer();
		if (currentPrayer == null)
		{
			return;
		}

		Widget prayerWidget = client.getWidget(currentPrayer.getWidgetInfo());
		if (prayerWidget == null)
		{
			return;
		}

		final Rectangle boxRectangle = prayerWidget.getBounds();
		final String str = String.valueOf(ticksUntilNextAnimation);
		final Point point = new Point(boxRectangle.x + boxRectangle.width / 2 - 4, boxRectangle.y + boxRectangle.height / 2 + 5);

		graphics2D.setFont(new Font("Arial", Font.BOLD, 16));
		OverlayUtil.renderTextLocation(graphics2D, point, str, ticksUntilNextAnimation == 1 ? Color.WHITE : config.fontColor());
		if (config.drawBoundingBox())
		{
			OverlayUtil.renderPolygon(graphics2D, boxRectangle, ticksUntilNextAnimation == 1 ? Color.white : Color.LIGHT_GRAY, new Color(0, 0, 0, 0), new BasicStroke(1.5f));
		}
	}
}
