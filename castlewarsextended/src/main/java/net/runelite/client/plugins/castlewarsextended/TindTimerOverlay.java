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
import java.util.Map;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.ProgressPieComponent;

public class TindTimerOverlay extends Overlay
{
	private static final double TIMER_LOW = 0.25; // When the timer is under a quarter left, if turns red.

	private final Client client;
	private final CastleWarsExtendedPlugin plugin;
	private final CastleWarsExtendedConfig config;

	private Color colorOpen, colorOpenBorder;
	private Color colorEmpty, colorEmptyBorder;

	@Inject
	TindTimerOverlay(Client client, CastleWarsExtendedPlugin plugin, CastleWarsExtendedConfig config)
	{
		this.plugin = plugin;
		this.config = config;
		this.client = client;
		setPosition(OverlayPosition.DYNAMIC);
		determineLayer();
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		drawTimer(graphics);
		return null;
	}

	public void updateConfig()
	{
		colorEmptyBorder = config.getEmptyColor();
		colorEmpty = new Color(colorEmptyBorder.getRed(), colorEmptyBorder.getGreen(), colorEmptyBorder.getBlue(), 100);

		colorOpenBorder = config.getLitColor();
		colorOpen = new Color(colorOpenBorder.getRed(), colorOpenBorder.getGreen(), colorOpenBorder.getBlue(), 100);
	}

	private void drawTimer(Graphics2D graphics)
	{
		for (Map.Entry<WorldPoint, Barricade> entry : plugin.getLitBarricades().entrySet())
		{
			Barricade b = entry.getValue();

			if (b.getState() == Barricade.State.LIT_BARRICADE)
			{
				drawTimerOnBarricade(graphics, b, colorOpen, colorOpenBorder, colorEmpty, colorOpenBorder);
			}
		}
	}

	private void drawTimerOnBarricade(Graphics2D graphics, Barricade barricade, Color fill, Color border, Color fillTimeLow, Color borderTimeLow)
	{
		if (barricade.getWorldLocation().getPlane() != client.getPlane())
		{
			return;
		}
		LocalPoint localLoc = LocalPoint.fromWorld(client, barricade.getWorldLocation());
		if (localLoc == null)
		{
			return;
		}
		net.runelite.api.Point loc = Perspective.localToCanvas(client, localLoc, client.getPlane());

		if (loc == null)
		{
			return;
		}

		double timeLeft = 1 - barricade.getTindTimeRelative();

		ProgressPieComponent pie = new ProgressPieComponent();
		pie.setFill(timeLeft > TIMER_LOW ? fill : fillTimeLow);
		pie.setBorderColor(timeLeft > TIMER_LOW ? border : borderTimeLow);
		pie.setPosition(loc);
		pie.setProgress(timeLeft);
		pie.render(graphics);
	}

	private void drawCircleOnBarricade(Graphics2D graphics, Barricade barricade, Color fill, Color border)
	{
		if (barricade.getWorldLocation().getPlane() != client.getPlane())
		{
			return;
		}
		LocalPoint localLoc = LocalPoint.fromWorld(client, barricade.getWorldLocation());
		if (localLoc == null)
		{
			return;
		}
		net.runelite.api.Point loc = Perspective.localToCanvas(client, localLoc, client.getPlane());

		ProgressPieComponent pie = new ProgressPieComponent();
		pie.setFill(fill);
		pie.setBorderColor(border);
		pie.setPosition(loc);
		pie.setProgress(1);
		pie.render(graphics);
	}

	public void determineLayer()
	{
		if (config.mirrorMode())
		{
			setLayer(OverlayLayer.AFTER_MIRROR);
		}
		if (!config.mirrorMode())
		{
			setLayer(OverlayLayer.ABOVE_SCENE);
		}
	}
}