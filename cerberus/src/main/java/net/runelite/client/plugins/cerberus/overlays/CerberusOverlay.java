/*
 * Copyright (c) 2019 Im2be <https://github.com/Im2be>
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


package net.runelite.client.plugins.cerberus.overlays;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.plugins.cerberus.CerberusPlugin;
import net.runelite.client.plugins.cerberus.domain.CerberusArena;
import net.runelite.client.plugins.cerberus.domain.CerberusAttack;
import net.runelite.client.plugins.cerberus.domain.CerberusGhost;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.client.ui.overlay.components.PanelComponent;

@Slf4j
@Singleton
public class CerberusOverlay extends Overlay
{
	private static final int NEXT_ATTACKS = 5;

	private static final int TICK_PIXEL_SIZE = 60;
	private static final int BOX_WIDTH = 10;
	private static final int BOX_HEIGHT = 5;
	final Color color = Color.ORANGE;
	final Color colorOneTick = Color.RED;
	final Map<Widget, Integer> lastBoxBaseYMap = new HashMap<>();
	private final Client client;
	private final CerberusPlugin plugin;
	private final SkillIconManager iconManager;
	private final PanelComponent panelComponent = new PanelComponent();


	@Inject
	CerberusOverlay(final CerberusPlugin plugin, final SkillIconManager iconManager, final Client client)
	{
		this.client = client;
		this.plugin = plugin;
		this.iconManager = iconManager;

		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
		setPriority(OverlayPriority.HIGHEST);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (plugin.getCerberus() == null)
		{
			return null;
		}
		if (!plugin.inCerberusArena())
		{
			return null;
		}

		final Widget meleePrayerWidget = client.getWidget(WidgetInfo.PRAYER_PROTECT_FROM_MELEE);
		final Widget rangePrayerWidget = client.getWidget(WidgetInfo.PRAYER_PROTECT_FROM_MISSILES);
		final Widget magicPrayerWidget = client.getWidget(WidgetInfo.PRAYER_PROTECT_FROM_MAGIC);

		if (plugin.getConfig().drawDescendingBoxes() && meleePrayerWidget != null && !meleePrayerWidget.isHidden()
			&& rangePrayerWidget != null && !rangePrayerWidget.isHidden()
			&& magicPrayerWidget != null && !magicPrayerWidget.isHidden())
		{
			renderDescendingBoxes(graphics);
		}

		if (plugin.getConfig().drawGhostTiles() && plugin.getCerberus().getLastGhostYellTick() != 0 && plugin.getGameTick() - plugin.getCerberus().getLastGhostYellTick() < 17)
		{
			renderGhostTiles(graphics);
		}

		return null;
	}

	private void renderGhostTiles(Graphics2D graphics)
	{
		final int tick = plugin.getGameTick();
		final long time = System.currentTimeMillis();
		final int lastGhostsTick = plugin.getCerberus().getLastGhostYellTick();
		final WorldPoint playerLocation = client.getLocalPlayer().getWorldLocation();
		final CerberusArena arena = CerberusArena.getArena(playerLocation);

		//Ghost 1
		for (int i = 0; i < 3; ++i)
		{
			final WorldPoint ghostLocation = arena.getGhostTile(i);

			final Color fillColor;
			if (plugin.getGhosts().size() >= i + 1)
			{
				fillColor = CerberusGhost.fromNPC(plugin.getGhosts().get(i)).get().getColor();
			}
			else
			{
				fillColor = Color.WHITE;
			}
			OverlayUtil.drawTiles(graphics, client, ghostLocation, playerLocation, fillColor, 2, 255, 20);
			if (ghostLocation.distanceTo(playerLocation) < 32)
			{
				LocalPoint lp = LocalPoint.fromWorld(client, ghostLocation);
				if (lp != null)
				{
					Polygon poly = Perspective.getCanvasTilePoly(client, lp);
					if (poly != null)
					{
						//Update and get the time when the ghosts were summoned
						final long lastGhostsTime = Math.min(plugin.getCerberus().getLastGhostYellTime(), time - (600 * (tick - lastGhostsTick)));
						plugin.getCerberus().setLastGhostYellTime(lastGhostsTime);

						final double ghostActionTime = Math.max((double) ((lastGhostsTime + 600 * (13 + i * 2)) - System.currentTimeMillis()) / 1000, 0);
						final Color textColor;
						if (ghostActionTime <= 2)
						{
							textColor = Color.RED;
						}
						else
						{
							textColor = Color.WHITE;
						}

						final String value = String.format("%.1f", ghostActionTime);
						int fontSize = 12;
						graphics.setFont(new Font("Arial", Font.PLAIN, fontSize));

						final FontMetrics metrics = graphics.getFontMetrics();
						Point centerPoint = centerPoint(poly.getBounds());
						var newPoint = new Point(centerPoint.getX() - (metrics.stringWidth(value) / 2), centerPoint.getY() + (metrics.getHeight() / 2));

						OverlayUtil.renderTextLocation(graphics, value, 12,
							Font.PLAIN, textColor, newPoint, true, 0);
					}
				}
			}
		}
	}

	private void renderDescendingBoxes(Graphics2D graphics)
	{
		final long lastTick = plugin.getLastTick();
		final int gameTick = plugin.getGameTick();
		final List<CerberusAttack> upcomingAttacks = plugin.getUpcomingAttacks();

		boolean first = true;
		for (CerberusAttack attack : upcomingAttacks)
		{
			final int tick = attack.getTick() - gameTick;
			if (tick > Math.min(Math.max(plugin.getConfig().amountOfDescendingBoxes(), 0), 10))
			{
				continue;
			}

			final Widget prayerWidget = client.getWidget(attack.getAttack().getPrayer().getWidgetInfo());

			int baseX = (int) prayerWidget.getBounds().getX();
			baseX += prayerWidget.getBounds().getWidth() / 2;
			baseX -= BOX_WIDTH / 2;

			int baseY = (int) prayerWidget.getBounds().getY() - tick * TICK_PIXEL_SIZE - BOX_HEIGHT;
			baseY += TICK_PIXEL_SIZE - ((lastTick + 600 - System.currentTimeMillis()) / 600.0 * TICK_PIXEL_SIZE);

			if (baseY > (int) prayerWidget.getBounds().getY() - BOX_HEIGHT)
			{
				continue;
			}

			if (System.currentTimeMillis() - lastTick > 600)
			{
				lastBoxBaseYMap.put(prayerWidget, baseY);
			}
			else if (lastBoxBaseYMap.containsKey(prayerWidget))
			{
				if (lastBoxBaseYMap.get(prayerWidget) >= baseY && lastBoxBaseYMap.get(prayerWidget) < (int) (prayerWidget.getBounds().getY() - BOX_HEIGHT))
				{
					baseY = lastBoxBaseYMap.get(prayerWidget) + 1;
					lastBoxBaseYMap.put(prayerWidget, baseY);
				}
				else
				{
					lastBoxBaseYMap.remove(prayerWidget);
				}
			}

			final Rectangle boxRectangle = new Rectangle(BOX_WIDTH, BOX_HEIGHT);
			boxRectangle.translate(baseX, baseY);

			OverlayUtil.renderFilledPolygon(graphics, boxRectangle, color);

			if (first)
			{
				final Rectangle prayerRectangle = new Rectangle(prayerWidget.getBounds().width, prayerWidget.getBounds().height);
				prayerRectangle.translate((int) prayerWidget.getBounds().getX(), (int) prayerWidget.getBounds().getY());

				OverlayUtil.renderOutlinePolygon(graphics, prayerRectangle, tick == 1 ? colorOneTick : color);
				first = false;
			}
		}
	}

	private net.runelite.api.Point centerPoint(Rectangle rect)
	{
		int x = (int) (rect.getX() + rect.getWidth() / 2);
		int y = (int) (rect.getY() + rect.getHeight() / 2);
		return new Point(x, y);
	}
}
