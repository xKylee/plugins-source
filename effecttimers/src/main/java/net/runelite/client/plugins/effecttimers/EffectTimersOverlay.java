/*
 * Copyright (c) 2020 ThatGamerBlue
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
package net.runelite.client.plugins.effecttimers;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.inject.Inject;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.Point;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

public class EffectTimersOverlay extends Overlay
{
	@Inject
	private TimerManager timerManager;

	@Inject
	private Client client;

	@Inject
	private EffectTimersConfig config;

	@Inject
	private ConfigManager configManager;

	private final Font timerFont = FontManager.getRunescapeBoldFont().deriveFont(14.0f);

	@Inject
	public EffectTimersOverlay(final EffectTimersConfig config)
	{
		super();
		this.config = config;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.UNDER_WIDGETS);
	}

	@Override
	public Dimension render(Graphics2D g)
	{
		Font oldFont = g.getFont();
		Color oldColor = g.getColor();
		g.setFont(timerFont);

		if (config.showNpcs())
		{
			client.getNpcs().forEach((a) -> renderActor(g, a));
		}

		if (config.showPlayers())
		{
			client.getPlayers().forEach((a) -> renderActor(g, a));
		}

		g.setFont(oldFont);
		g.setColor(oldColor);

		return null;
	}

	private void renderActor(Graphics2D g, Actor actor)
	{
		int offset = 0;
		for (TimerType timerType : TimerType.values())
		{
			if (timerType.shouldRender(configManager) && !actor.isDead())
			{
				if (timerManager.isTimerValid(actor, timerType) && timerManager.hasTimerActive(actor, timerType))
				{
					if (renderTimer(g, actor, timerType, offset))
					{
						offset++;
					}
				}
			}
		}
	}

	private boolean renderTimer(Graphics2D g, Actor actor, TimerType timerType, int offset)
	{
		String text;
		Timer timer = timerManager.getTimerFor(actor, timerType);
		switch (config.timeMode())
		{

			case MINS_SECONDS_MILLI:
				long minSecondsMillisRemaining = timer.getMillisForRender();
				if (minSecondsMillisRemaining == -1)
				{
					return false; // this shouldnt happen but just in case
				}
				text = formatTimeMMSSMS(minSecondsMillisRemaining);
				break;
			case SECONDS:
				long secondsRemaining = timer.getMillisForRender();
				if (secondsRemaining == -1)
				{
					return false; // this shouldnt happen but just in case
				}
				text = formatTimeSS(secondsRemaining);
				break;
			case TICKS:
				int tfr = timer.getTicksForRender();
				if (tfr == -1)
				{
					return false;
				}
				text = Integer.toString(tfr);
				break;
			default:
				return false;
		}

		Point canvasLocation = actor.getCanvasTextLocation(g, text, 0);

		if (canvasLocation == null)
		{
			return false;
		}

		int yOffset = (offset * (g.getFontMetrics().getHeight() + 2));
		int xOffset = config.xOffset();

		BufferedImage image = timer.getIcon();
		Point actorCIL = actor.getCanvasImageLocation(image, 0);
		Point textLocation = new Point(actorCIL.getX() + xOffset, actorCIL.getY() + yOffset);

		if (config.showIcons())
		{
			g.drawImage(image, textLocation.getX(), textLocation.getY(), null);
			xOffset = image.getWidth() + 1;
			yOffset = (image.getHeight() - g.getFontMetrics().getHeight());
			textLocation = new Point(textLocation.getX() + xOffset, textLocation.getY() + image.getHeight() - yOffset);
		}

		renderTextLocation(g, text, config.textSize(), config.fontStyle().getFont(), timer.determineColor(), textLocation, false, 0);

		return true;
	}

	@VisibleForTesting
	/*
		TODO: if the game lags this will go mental
	 */
	public String formatTimeMMSSMS(long time)
	{
		if (time > 59999)
		{
			return ((int) time / 60000) + ":" + formatSeconds(((int) (time % 60000) / 1000));
		}
		else if (time > 9999)
		{
			return ((int) time / 1000) + "";
		}
		else if (time > 999)
		{
			return ((int) time / 1000) + "." + ((int) (time % 1000) / 100);
		}
		return "0." + ((int) time / 100);
	}

	@VisibleForTesting
	/*
		TODO: if the game lags this will go mental
	 */
	public String formatTimeSS(long time)
	{
		if (time > 59999)
		{
			return formatSeconds(((int) (time % 60000) / 1000)) + "";
		}
		else if (time > 9999)
		{
			return ((int) time / 1000) + "";
		}
		else if (time > 999)
		{
			return ((int) time / 1000) + "";
		}
		return "0";
	}

	private String formatSeconds(int seconds)
	{
		return String.format("%02d", seconds);
	}

	public static void renderTextLocation(Graphics2D graphics, String txtString, int fontSize, int fontStyle, Color fontColor, Point canvasPoint, boolean shadows, int yOffset)
	{
		graphics.setFont(new Font("Arial", fontStyle, fontSize));
		if (canvasPoint != null)
		{
			final Point canvasCenterPoint = new Point(
				canvasPoint.getX(),
				canvasPoint.getY() + yOffset);
			final Point canvasCenterPoint_shadow = new Point(
				canvasPoint.getX() + 1,
				canvasPoint.getY() + 1 + yOffset);
			if (shadows)
			{
				renderTextLocation(graphics, canvasCenterPoint_shadow, txtString, Color.BLACK);
			}
			renderTextLocation(graphics, canvasCenterPoint, txtString, fontColor);
		}
	}

	public static void renderTextLocation(Graphics2D graphics, Point txtLoc, String text, Color color)
	{
		if (Strings.isNullOrEmpty(text))
		{
			return;
		}

		int x = txtLoc.getX();
		int y = txtLoc.getY();

		graphics.setColor(Color.BLACK);
		graphics.drawString(text, x + 1, y + 1);

		graphics.setColor(color);
		graphics.drawString(text, x, y);
	}
}
