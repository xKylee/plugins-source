/*
 * Copyright (c) 2020, dutta64 <https://github.com/dutta64>
 * Copyright (c) 2019, kThisIsCvpv <https://github.com/kThisIsCvpv>
 * Copyright (c) 2019, ganom <https://github.com/Ganom>
 * Copyright (c) 2019, kyle <https://github.com/Kyleeld>
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

package net.runelite.client.plugins.gauntlet;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.Model;
import net.runelite.api.NPC;
import net.runelite.api.NPCDefinition;
import net.runelite.api.Perspective;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.api.Prayer;
import net.runelite.api.Projectile;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.model.Jarvis;
import net.runelite.api.model.Vertex;
import net.runelite.client.graphics.ModelOutlineRenderer;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.OverlayUtil;

class GauntletOverlay extends Overlay
{
	private static final Color[] COLORS = new Color[]{
		Color.BLUE,
		Color.RED,
		Color.GREEN,
		Color.ORANGE,
		Color.WHITE,
		Color.CYAN,
		Color.MAGENTA,
		Color.PINK,
		Color.YELLOW,
		Color.DARK_GRAY,
		Color.LIGHT_GRAY
	};

	private static final Color TRANSPARENT = new Color(0, 0, 0, 0);

	private static final int COLOR_DURATION = 10;

	private final Client client;
	private final GauntletPlugin plugin;
	private final GauntletConfig config;
	private final ModelOutlineRenderer modelOutlineRenderer;

	private Player player;

	private int timeout;
	private int idx;

	@Inject
	private GauntletOverlay(final Client client, final GauntletPlugin plugin, final GauntletConfig config,
							final ModelOutlineRenderer modelOutlineRenderer)
	{
		this.client = client;
		this.plugin = plugin;
		this.config = config;
		this.modelOutlineRenderer = modelOutlineRenderer;

		setPosition(OverlayPosition.DYNAMIC);
		setPriority(OverlayPriority.HIGH);
		determineLayer();
	}

	@Override
	public Dimension render(final Graphics2D graphics2D)
	{
		if (!plugin.isInGauntlet())
		{
			return null;
		}

		if (plugin.isInHunllefRoom())
		{
			final GauntletHunllef hunllef = plugin.getHunllef();

			if (hunllef == null)
			{
				return null;
			}

			final NPC npc = hunllef.getNpc();

			if (npc == null)
			{
				return null;
			}

			if (npc.isDead())
			{
				renderDiscoMode();
				return null;
			}

			renderTornadoes(graphics2D);

			renderProjectiles(graphics2D);

			renderHunllefWrongPrayerOutline();

			renderHunllefAttackCounter(graphics2D);

			renderHunllefAttackStyleIcon(graphics2D);

			renderHunllefTile(graphics2D);

			renderFlash(graphics2D);
		}
		else
		{
			player = client.getLocalPlayer();

			if (player == null)
			{
				return null;
			}

			renderResources(graphics2D);
			renderUtilities();
			renderDemibosses();
			renderStrongNpcs();
			renderWeakNpcs();
		}

		return null;
	}

	void determineLayer()
	{
		setLayer(config.mirrorMode() ? OverlayLayer.AFTER_MIRROR : OverlayLayer.UNDER_WIDGETS);
	}

	private void renderTornadoes(final Graphics2D graphics2D)
	{
		if ((!config.tornadoTickCounter() && !config.tornadoTileOutline()) || plugin.getTornados().isEmpty())
		{
			return;
		}

		for (final GauntletTornado tornado : plugin.getTornados())
		{
			final int timeLeft = tornado.getTimeLeft();

			if (timeLeft < 0)
			{
				continue;
			}

			final NPC npc = tornado.getNpc();

			if (config.tornadoTileOutline())
			{

				final Polygon polygon = Perspective.getCanvasTilePoly(client, npc.getLocalLocation());

				if (polygon == null)
				{
					continue;
				}

				drawStrokeAndFill(graphics2D, config.tornadoOutlineColor(), config.tornadoFillColor(),
					config.tornadoTileOutlineWidth(), polygon);
			}

			if (config.tornadoTickCounter())
			{
				final String ticksLeftStr = String.valueOf(timeLeft);

				final Point point = npc.getCanvasTextLocation(graphics2D, ticksLeftStr, 0);

				if (point == null)
				{
					return;
				}

				OverlayUtil.renderTextLocation(graphics2D, ticksLeftStr, config.tornadoFontSize(),
					config.tornadoFontStyle().getFont(), config.tornadoFontColor(), point,
					config.tornadoFontShadow(), 0);
			}
		}
	}

	private void renderProjectiles(final Graphics2D graphics2D)
	{
		if ((!config.outlineProjectile() && !config.overlayProjectileIcon())
			|| plugin.getProjectiles().isEmpty())
		{
			return;
		}

		for (final GauntletProjectile projectile : plugin.getProjectiles())
		{
			final Polygon polygon = getProjectilePolygon(projectile.getProjectile());

			if (polygon == null)
			{
				continue;
			}

			if (config.outlineProjectile())
			{
				final Color originalColor = graphics2D.getColor();

				graphics2D.setColor(projectile.getOutlineColor());
				graphics2D.draw(polygon);

				graphics2D.setColor(projectile.getFillColor());
				graphics2D.fill(polygon);

				graphics2D.setColor(originalColor);
			}

			if (config.overlayProjectileIcon())
			{
				final BufferedImage icon = projectile.getIcon();

				final Rectangle bounds = polygon.getBounds();

				final int x = (int) bounds.getCenterX() - (icon.getWidth() / 2);
				final int y = (int) bounds.getCenterY() - (icon.getHeight() / 2);

				graphics2D.drawImage(icon, x, y, null);
			}
		}
	}

	private void renderHunllefWrongPrayerOutline()
	{
		if (!config.hunllefOverlayWrongPrayerOutline())
		{
			return;
		}

		final GauntletHunllef hunllef = plugin.getHunllef();

		final GauntletHunllef.BossAttackPhase phase = hunllef.getCurrentPhase();

		if (client.isPrayerActive(phase.getPrayer()))
		{
			return;
		}

		modelOutlineRenderer.drawOutline(hunllef.getNpc(), config.hunllefWrongPrayerOutlineWidth(), phase.getColor(),
			TRANSPARENT);
	}

	private void renderHunllefAttackCounter(final Graphics2D graphics2D)
	{
		if (!config.hunllefOverlayAttackCounter())
		{
			return;
		}

		final GauntletHunllef hunllef = plugin.getHunllef();

		final NPC npc = hunllef.getNpc();

		final String text = String.format("%d | %d", hunllef.getBossAttacks(),
			hunllef.getPlayerAttacks());

		// offset value is height above the npc tile
		final Point point = npc.getCanvasTextLocation(graphics2D, text, 0);

		if (point == null)
		{
			return;
		}

		final Font originalFont = graphics2D.getFont();

		graphics2D.setFont(new Font(Font.DIALOG,
			config.hunllefAttackCounterFontStyle().getFont(), config.hunllefAttackCounterFontSize()));

		OverlayUtil.renderTextLocation(graphics2D, point, text, hunllef.getCurrentPhase().getColor());

		graphics2D.setFont(originalFont);
	}

	private void renderHunllefAttackStyleIcon(final Graphics2D graphics2D)
	{
		if (!config.hunllefOverlayAttackStyleIcon())
		{
			return;
		}

		final GauntletHunllef hunllef = plugin.getHunllef();

		final NPC npc = hunllef.getNpc();

		final BufferedImage icon = hunllef.getAttackStyleIcon();

		// offset value is height above the npc tile
		final Point point = Perspective.getCanvasImageLocation(client, npc.getLocalLocation(), icon,
			npc.getLogicalHeight() - 100);

		if (point == null)
		{
			return;
		}

		graphics2D.drawImage(icon, point.getX(), point.getY(), null);
	}

	private void renderHunllefTile(final Graphics2D graphics2D)
	{
		if (!config.hunllefOutlineTile())
		{
			return;
		}

		final NPC npc = plugin.getHunllef().getNpc();

		final NPCDefinition npcDefinition = npc.getDefinition();

		if (npcDefinition == null)
		{
			return;
		}

		final Polygon polygon = Perspective.getCanvasTileAreaPoly(client, npc.getLocalLocation(),
			npcDefinition.getSize());

		if (polygon == null)
		{
			return;
		}

		drawStrokeAndFill(graphics2D, config.hunllefOutlineColor(), config.hunllefFillColor(),
			config.hunllefTileOutlineWidth(), polygon);
	}

	private void renderFlash(final Graphics2D graphics2D)
	{
		if (!config.flashOnWrongAttack() || !plugin.isFlash())
		{
			return;
		}

		final Color originalColor = graphics2D.getColor();

		graphics2D.setColor(config.flashColor());

		graphics2D.fill(client.getCanvas().getBounds());

		graphics2D.setColor(originalColor);

		if (++timeout >= config.flashOnWrongAttackDuration())
		{
			timeout = 0;
			plugin.setFlash(false);
		}
	}

	private void renderResources(final Graphics2D graphics2D)
	{
		if (!config.resourceOverlay() || plugin.getResources().isEmpty())
		{
			return;
		}

		final LocalPoint playerLocalLocation = player.getLocalLocation();

		for (final GauntletResource resource : plugin.getResources())
		{
			final LocalPoint gameObjectLocalLocation = resource.getGameObject().getLocalLocation();

			if (isOutsideRenderDistance(gameObjectLocalLocation, playerLocalLocation))
			{
				continue;
			}

			final Polygon polygon = Perspective.getCanvasTilePoly(client, gameObjectLocalLocation);

			if (polygon == null)
			{
				continue;
			}

			drawStrokeAndFill(graphics2D, config.resourceOutlineColor(), config.resourceFillColor(),
				config.resourceTileOutlineWidth(), polygon);

			OverlayUtil.renderImageLocation(client, graphics2D, gameObjectLocalLocation, resource.getIcon(), 0);
		}
	}

	private void renderUtilities()
	{
		if (!config.utilitiesOutline() || plugin.getUtilities().isEmpty())
		{
			return;
		}

		final LocalPoint localPoint = player.getLocalLocation();

		for (final GameObject gameObject : plugin.getUtilities())
		{
			if (!gameObject.getWorldLocation().isInScene(client)
				|| isOutsideRenderDistance(gameObject.getLocalLocation(), localPoint))
			{
				continue;
			}

			final Shape shape = gameObject.getConvexHull();

			if (shape == null)
			{
				continue;
			}

			modelOutlineRenderer.drawOutline(gameObject, config.utilitiesOutlineWidth(),
				config.utilitiesOutlineColor(), TRANSPARENT);
		}
	}

	private void renderDemibosses()
	{
		if (!config.demibossOutline() || plugin.getDemibosses().isEmpty())
		{
			return;
		}

		final LocalPoint localPointPlayer = player.getLocalLocation();

		for (final GauntletDemiboss demiboss : plugin.getDemibosses())
		{
			final NPC npc = demiboss.getNpc();

			final LocalPoint localPointNpc = npc.getLocalLocation();

			if (localPointNpc == null || npc.isDead() || isOutsideRenderDistance(localPointNpc, localPointPlayer))
			{
				continue;
			}

			modelOutlineRenderer.drawOutline(npc, config.demibossOutlineWidth(),
				demiboss.getDemiboss().getOutlineColor(), TRANSPARENT);
		}
	}

	private void renderStrongNpcs()
	{
		if (!config.strongNpcOutline() || plugin.getStrongNpcs().isEmpty())
		{
			return;
		}

		final LocalPoint localPointPLayer = player.getLocalLocation();

		for (final NPC npc : plugin.getStrongNpcs())
		{
			final LocalPoint localPointNpc = npc.getLocalLocation();

			if (localPointNpc == null || npc.isDead() || isOutsideRenderDistance(localPointNpc, localPointPLayer))
			{
				continue;
			}

			modelOutlineRenderer.drawOutline(npc, config.strongNpcOutlineWidth(), config.strongNpcOutlineColor(),
				TRANSPARENT);
		}
	}

	private void renderWeakNpcs()
	{
		if (!config.weakNpcOutline() || plugin.getWeakNpcs().isEmpty())
		{
			return;
		}

		final LocalPoint localPointPlayer = player.getLocalLocation();

		for (final NPC npc : plugin.getWeakNpcs())
		{
			final LocalPoint localPointNpc = npc.getLocalLocation();

			if (localPointNpc == null || npc.isDead() || isOutsideRenderDistance(localPointNpc, localPointPlayer))
			{
				continue;
			}

			modelOutlineRenderer.drawOutline(npc, config.weakNpcOutlineWidth(), config.weakNpcOutlineColor(),
				TRANSPARENT);
		}
	}

	private void renderDiscoMode()
	{
		if (!config.discoMode())
		{
			return;
		}

		if (++timeout > COLOR_DURATION)
		{
			timeout = 0;
			idx = idx >= COLORS.length - 1 ? 0 : idx + 1;
		}

		modelOutlineRenderer.drawOutline(plugin.getHunllef().getNpc(), 12, COLORS[idx], TRANSPARENT);
	}

	private Polygon getProjectilePolygon(final Projectile projectile)
	{
		if (projectile == null || projectile.getModel() == null)
		{
			return null;
		}

		final Model model = projectile.getModel();

		final LocalPoint localPoint = new LocalPoint((int) projectile.getX(), (int) projectile.getY());

		final int tileHeight = Perspective.getTileHeight(client, localPoint, client.getPlane());

		double angle = Math.atan(projectile.getVelocityY() / projectile.getVelocityX());
		angle = Math.toDegrees(angle) + (projectile.getVelocityX() < 0 ? 180 : 0);
		angle = angle < 0 ? angle + 360 : angle;
		angle = 360 - angle - 90;

		double ori = angle * (512d / 90d);
		ori = ori < 0 ? ori + 2048 : ori;

		final int orientation = (int) Math.round(ori);

		final List<Vertex> vertices = model.getVertices();

		for (int i = 0; i < vertices.size(); ++i)
		{
			vertices.set(i, vertices.get(i).rotate(orientation));
		}

		final List<Point> list = new ArrayList<>();

		for (final Vertex vertex : vertices)
		{
			final Point point = Perspective.localToCanvas(client, localPoint.getX() - vertex.getX(),
				localPoint.getY() - vertex.getZ(), tileHeight + vertex.getY() + (int) projectile.getZ());

			if (point == null)
			{
				continue;
			}

			list.add(point);
		}

		final List<Point> convexHull = Jarvis.convexHull(list);

		if (convexHull == null)
		{
			return null;
		}

		final Polygon polygon = new Polygon();

		for (final Point point : convexHull)
		{
			polygon.addPoint(point.getX(), point.getY());
		}

		return polygon;
	}

	private boolean isOutsideRenderDistance(final LocalPoint localPoint, final LocalPoint playerLocation)
	{
		final int maxDistance = config.resourceRenderDistance().getDistance();

		if (maxDistance == 0)
		{
			return false;
		}

		return localPoint.distanceTo(playerLocation) >= maxDistance;
	}

	private static void drawStrokeAndFill(final Graphics2D graphics2D, final Color outlineColor, final Color fillColor,
										  final float strokeWidth, final Shape shape)
	{
		final Color originalColor = graphics2D.getColor();
		final Stroke originalStroke = graphics2D.getStroke();

		graphics2D.setStroke(new BasicStroke(strokeWidth));
		graphics2D.setColor(outlineColor);
		graphics2D.draw(shape);

		graphics2D.setColor(fillColor);
		graphics2D.fill(shape);

		graphics2D.setColor(originalColor);
		graphics2D.setStroke(originalStroke);
	}
}
