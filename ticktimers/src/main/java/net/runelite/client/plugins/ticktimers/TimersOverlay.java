/*
 * Copyright (c) 2019, Ganom <https://github.com/Ganom>
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
package net.runelite.client.plugins.ticktimers;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.List;
import java.util.TreeMap;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.Point;
import net.runelite.api.Prayer;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.widgets.Widget;
import static net.runelite.client.plugins.ticktimers.NPCContainer.BossMonsters.GENERAL_GRAARDOR;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.OverlayUtil;

@Singleton
public class TimersOverlay extends Overlay
{
	private static final int TICK_PIXEL_SIZE = 60;
	private static final int BOX_WIDTH = 10;
	private static final int BOX_HEIGHT = 5;

	private final TickTimersPlugin plugin;
	private final TickTimersConfig config;
	private final Client client;

	@Inject
	TimersOverlay(final TickTimersPlugin plugin, final TickTimersConfig config, final Client client)
	{
		this.plugin = plugin;
		this.config = config;
		this.client = client;

		setPosition(OverlayPosition.DYNAMIC);
		setPriority(OverlayPriority.HIGHEST);
		determineLayer();
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		// Maps each tick to a set of attacks and their priorities
		TreeMap<Integer, TreeMap<Integer, Prayer>> tickAttackMap = new TreeMap<>();

		for (NPCContainer npc : plugin.getNpcContainers())
		{
			if (npc.getNpc() == null)
			{
				continue;
			}

			int ticksLeft = npc.getTicksUntilAttack();
			final List<WorldPoint> hitSquares = OverlayUtil.getHitSquares(npc.getNpc().getWorldLocation(), npc.getNpcSize(), 1, false);
			NPCContainer.AttackStyle attackStyle = npc.getAttackStyle();

			if (config.showHitSquares() && attackStyle.getName().equals("Melee"))
			{
				for (WorldPoint p : hitSquares)
				{
					OverlayUtil.drawTiles(graphics, client, p, client.getLocalPlayer().getWorldLocation(), attackStyle.getColor(), 0, 0, 50);
				}
			}

			if (ticksLeft <= 0)
			{
				continue;
			}

			if (config.ignoreNonAttacking() && npc.getNpcInteracting() != client.getLocalPlayer() && npc.getMonsterType() != GENERAL_GRAARDOR)
			{
				continue;
			}

			// If you are not tank at bandos, prayer range instead of melee on graardor attack
			if (npc.getMonsterType() == GENERAL_GRAARDOR && npc.getNpcInteracting() != client.getLocalPlayer())
			{
				attackStyle = NPCContainer.AttackStyle.RANGE;
			}

			final String ticksLeftStr = String.valueOf(ticksLeft);
			final int font = config.fontStyle().getFont();
			final boolean shadows = config.shadows();
			Color color = (ticksLeft <= 1 ? Color.WHITE : attackStyle.getColor());

			if (!config.changeTickColor())
			{
				color = attackStyle.getColor();
			}

			final Point canvasPoint = npc.getNpc().getCanvasTextLocation(graphics, ticksLeftStr, 0);
			OverlayUtil.renderTextLocation(graphics, ticksLeftStr, config.textSize(), font, color, canvasPoint, shadows, 0);

			if (config.showPrayerWidgetHelper() && attackStyle.getPrayer() != null)
			{
				Rectangle bounds = OverlayUtil.renderPrayerOverlay(graphics, client, attackStyle.getPrayer(), color);

				if (bounds != null)
				{
					renderTextLocation(graphics, ticksLeftStr, 16, config.fontStyle().getFont(), color, centerPoint(bounds), shadows);
				}
			}

			if (config.guitarHeroMode())
			{
				var attacks = tickAttackMap.computeIfAbsent(ticksLeft, (k) -> new TreeMap<>());

				int priority = 999;
				switch (npc.getMonsterType())
				{
					case SERGEANT_STRONGSTACK:
						priority = 3;
						break;
					case SERGEANT_STEELWILL:
						priority = 1;
						break;
					case SERGEANT_GRIMSPIKE:
						priority = 2;
						break;
					case GENERAL_GRAARDOR:
						priority = 0;
						break;
					default:
						break;
				}

				attacks.putIfAbsent(priority, attackStyle.getPrayer());
			}
		}

		if (!tickAttackMap.isEmpty())
		{
			for (var tickEntry : tickAttackMap.entrySet())
			{
				var attackEntry = tickEntry.getValue().firstEntry();
				Prayer prayer = attackEntry.getValue();
				if (prayer != null)
				{
					renderDescendingBoxes(graphics, prayer, tickEntry.getKey());
				}
			}
		}

		return null;
	}

	private void renderDescendingBoxes(Graphics2D graphics, Prayer prayer, int tick)
	{
		final Color color = tick == 1 ? Color.RED : Color.ORANGE;
		final Widget prayerWidget = client.getWidget(prayer.getWidgetInfo());

		int baseX = (int) prayerWidget.getBounds().getX();
		baseX += prayerWidget.getBounds().getWidth() / 2;
		baseX -= BOX_WIDTH / 2;

		int baseY = (int) prayerWidget.getBounds().getY() - tick * TICK_PIXEL_SIZE - BOX_HEIGHT;
		baseY += TICK_PIXEL_SIZE - ((plugin.getLastTickTime() + 600 - System.currentTimeMillis()) / 600.0 * TICK_PIXEL_SIZE);

		final Rectangle boxRectangle = new Rectangle(BOX_WIDTH, BOX_HEIGHT);
		boxRectangle.translate(baseX, baseY);

		OverlayUtil.renderFilledPolygon(graphics, boxRectangle, color);
	}

	private void renderTextLocation(Graphics2D graphics, String txtString, int fontSize, int fontStyle, Color fontColor, Point canvasPoint, boolean shadows)
	{
		graphics.setFont(new Font("Arial", fontStyle, fontSize));
		if (canvasPoint != null)
		{
			final Point canvasCenterPoint = new Point(
				canvasPoint.getX() - 3,
				canvasPoint.getY() + 6);
			final Point canvasCenterPoint_shadow = new Point(
				canvasPoint.getX() - 2,
				canvasPoint.getY() + 7);
			if (shadows)
			{
				OverlayUtil.renderTextLocation(graphics, canvasCenterPoint_shadow, txtString, Color.BLACK);
			}
			OverlayUtil.renderTextLocation(graphics, canvasCenterPoint, txtString, fontColor);
		}
	}

	private Point centerPoint(Rectangle rect)
	{
		int x = (int) (rect.getX() + rect.getWidth() / 2);
		int y = (int) (rect.getY() + rect.getHeight() / 2);
		return new Point(x, y);
	}

	public void determineLayer()
	{
		if (config.mirrorMode())
		{
			setLayer(OverlayLayer.AFTER_MIRROR);
		}
		if (!config.mirrorMode())
		{
			setLayer(OverlayLayer.ALWAYS_ON_TOP);
		}
	}
}
