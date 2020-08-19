/*
 * Copyright (c) 2020 dutta64 <https://github.com/dutta64>
 * Copyright (c) 2019, Lucas <https://github.com/lucwousin>
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
package net.runelite.client.plugins.alchemicalhydra.overlay;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.geom.Area;
import java.util.Collection;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import static net.runelite.api.Perspective.getCanvasTileAreaPoly;
import net.runelite.api.Point;
import net.runelite.api.Projectile;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.graphics.ModelOutlineRenderer;
import net.runelite.client.plugins.alchemicalhydra.AlchemicalHydraConfig;
import net.runelite.client.plugins.alchemicalhydra.AlchemicalHydraPlugin;
import net.runelite.client.plugins.alchemicalhydra.entity.Hydra;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.OverlayUtil;

@Singleton
public class SceneOverlay extends Overlay
{
	private static final Color TRANSPARENT = new Color(0, 0, 0, 0);

	private static final BasicStroke FOUNTAINE_OUTLINE_STROKE = new BasicStroke(1);

	private static final Area POISON_AREA = new Area();

	private static final int POISON_AOE_AREA_SIZE = 3;

	private static final int HYDRA_HULL_OUTLINE_STROKE_SIZE = 1;

	private final Client client;
	private final AlchemicalHydraPlugin plugin;
	private final AlchemicalHydraConfig config;
	private final ModelOutlineRenderer modelOutlineRenderer;

	private Hydra hydra;

	@Inject
	public SceneOverlay(final Client client, final AlchemicalHydraPlugin plugin, final AlchemicalHydraConfig config,
						final ModelOutlineRenderer modelOutlineRenderer)
	{
		this.client = client;
		this.plugin = plugin;
		this.config = config;
		this.modelOutlineRenderer = modelOutlineRenderer;

		setPriority(OverlayPriority.HIGH);
		setPosition(OverlayPosition.DYNAMIC);
		determineLayer();
	}

	@Override
	public Dimension render(final Graphics2D graphics2D)
	{
		hydra = plugin.getHydra();

		if (hydra == null)
		{
			return null;
		}

		renderPoisonProjectileAreaTiles(graphics2D);
		renderHydraImmunityOutline();
		renderFountainOutline(graphics2D);
		renderHpUntilPhaseChange(graphics2D);

		return null;
	}

	public void determineLayer()
	{
		setLayer(config.mirrorMode() ? OverlayLayer.AFTER_MIRROR : OverlayLayer.UNDER_WIDGETS);
	}

	private void renderPoisonProjectileAreaTiles(final Graphics2D graphics)
	{
		final Map<LocalPoint, Projectile> poisonProjectiles = plugin.getPoisonProjectiles();

		if (poisonProjectiles.isEmpty())
		{
			return;
		}

		POISON_AREA.reset();

		for (final Map.Entry<LocalPoint, Projectile> entry : poisonProjectiles.entrySet())
		{
			if (entry.getValue().getEndCycle() < client.getGameCycle())
			{
				continue;
			}

			final LocalPoint localPoint = entry.getKey();

			final Polygon polygon = getCanvasTileAreaPoly(client, localPoint, POISON_AOE_AREA_SIZE);

			if (polygon != null)
			{
				POISON_AREA.add(new Area(polygon));
			}
		}

		graphics.setPaintMode();
		graphics.setColor(config.poisonOutlineColor());
		graphics.draw(POISON_AREA);
		graphics.setColor(config.poisonFillColor());
		graphics.fill(POISON_AREA);
	}

	private void renderHydraImmunityOutline()
	{
		final NPC npc = hydra.getNpc();

		if (!config.hydraImmunityOutline() || !hydra.isImmunity() || npc == null || npc.isDead())
		{
			return;
		}

		final WorldPoint fountainWorldPoint = hydra.getPhase().getFountainWorldPoint();

		if (fountainWorldPoint != null)
		{
			final Collection<WorldPoint> fountainWorldPoints = WorldPoint.toLocalInstance(client, fountainWorldPoint);

			if (fountainWorldPoints.size() == 1)
			{
				WorldPoint worldPoint = null;

				for (final WorldPoint wp : fountainWorldPoints)
				{
					worldPoint = wp;
				}

				final LocalPoint localPoint = LocalPoint.fromWorld(client, worldPoint);

				if (localPoint != null)
				{
					final Polygon polygon = getCanvasTileAreaPoly(client, localPoint, 3);

					if (polygon != null)
					{
						int stroke = HYDRA_HULL_OUTLINE_STROKE_SIZE;

						if (npc.getWorldArea().intersectsWith(new WorldArea(worldPoint, 1, 1)))
						{
							stroke++;
						}

						modelOutlineRenderer.drawOutline(npc, stroke, hydra.getPhase().getPhaseColor(), TRANSPARENT);
						return;
					}
				}
			}

		}

		modelOutlineRenderer.drawOutline(npc, HYDRA_HULL_OUTLINE_STROKE_SIZE, hydra.getPhase().getPhaseColor(), TRANSPARENT);
	}

	private void renderFountainOutline(final Graphics2D graphics2D)
	{
		final NPC npc = hydra.getNpc();
		final WorldPoint fountainWorldPoint = hydra.getPhase().getFountainWorldPoint();

		if (!config.fountainOutline() || !hydra.isImmunity() || fountainWorldPoint == null || npc == null || npc.isDead())
		{
			return;
		}

		final Collection<WorldPoint> fountainWorldPoints = WorldPoint.toLocalInstance(client, fountainWorldPoint);

		if (fountainWorldPoints.size() != 1)
		{
			return;
		}

		WorldPoint worldPoint = null;

		for (final WorldPoint wp : fountainWorldPoints)
		{
			worldPoint = wp;
		}

		final LocalPoint localPoint = LocalPoint.fromWorld(client, worldPoint);

		if (localPoint == null)
		{
			return;
		}

		final Polygon polygon = getCanvasTileAreaPoly(client, localPoint, 3);

		if (polygon == null)
		{
			return;
		}

		Color color = hydra.getPhase().getFountainColor();

		if (!npc.getWorldArea().intersectsWith(new WorldArea(worldPoint, 1, 1)))
		{
			color = color.darker();
		}

		graphics2D.setColor(color);
		graphics2D.setStroke(FOUNTAINE_OUTLINE_STROKE);
		graphics2D.draw(polygon);
	}

	private void renderHpUntilPhaseChange(final Graphics2D graphics2D)
	{
		final NPC npc = hydra.getNpc();

		if (!config.showHpUntilPhaseChange() || npc == null || npc.isDead())
		{
			return;
		}

		final int hpUntilPhaseChange = hydra.getHpUntilPhaseChange();

		if (hpUntilPhaseChange == 0)
		{
			return;
		}

		final String text = String.valueOf(hpUntilPhaseChange);

		final Point point = npc.getCanvasTextLocation(graphics2D, text, 0);

		if (point == null)
		{
			return;
		}

		OverlayUtil.renderTextLocation(
			graphics2D,
			text,
			config.fontSize(),
			config.fontStyle().getFont(),
			config.fontColor(),
			point,
			config.fontShadow(),
			config.fontZOffset() * -1
		);
	}
}
