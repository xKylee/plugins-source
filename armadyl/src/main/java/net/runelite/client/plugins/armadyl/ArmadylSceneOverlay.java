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

package net.runelite.client.plugins.armadyl;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.Stroke;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.NPCDefinition;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.OverlayUtil;

@Slf4j
@Singleton
class ArmadylSceneOverlay extends Overlay
{
	private static final Color TRANSPARENT = new Color(0, 0, 0, 0);

	private final ArmadylPlugin plugin;
	private final ArmadylConfig config;
	private final Client client;

	@Inject
	private ArmadylSceneOverlay(final ArmadylPlugin plugin, final ArmadylConfig config, final Client client)
	{
		super(plugin);

		this.plugin = plugin;
		this.config = config;
		this.client = client;

		setPosition(OverlayPosition.DYNAMIC);
		setPriority(OverlayPriority.HIGH);
		setLayer(OverlayLayer.UNDER_WIDGETS);
	}

	@Override
	public Dimension render(final Graphics2D graphics2D)
	{
		renderNpcTickCount(graphics2D);
		renderNpcTile(graphics2D);

		return null;
	}

	private void renderNpcTickCount(final Graphics2D graphics2D)
	{
		final Set<ArmadylNpc> armadylNpcs = plugin.getArmadylNpcs();
		final Actor player = client.getLocalPlayer();

		final NPC kreearra = plugin.getKreearra();

		if (!config.npcTickCounter() || (config.selectiveTickCount() && kreearra != null && !kreearra.isDead())
			|| armadylNpcs.isEmpty() || player == null)
		{
			return;
		}

		for (final ArmadylNpc armadylNpc : armadylNpcs)
		{
			final NPC npc = armadylNpc.getNpc();

			final int ticksUntilNextAnimation = armadylNpc.getTicksUntilNextAnimation();

			if (npc == null || npc == kreearra || npc.isDead() || ticksUntilNextAnimation <= 0
				|| (config.ignoreNonTargettingNpcs() && armadylNpc.getInteractingActor() != player))
			{
				continue;
			}

			final String text = String.valueOf(ticksUntilNextAnimation);

			final Point npcPoint = npc.getCanvasTextLocation(graphics2D, text, npc.getLogicalHeight() / 2);

			if (npcPoint == null)
			{
				return;
			}

			final Font originalFont = graphics2D.getFont();

			graphics2D.setFont(new Font(Font.SANS_SERIF, config.npcTickCounterFontStyle().getFont(), config.npcTickCounterFontSize()));

			OverlayUtil.renderTextLocation(graphics2D, npcPoint, text, ticksUntilNextAnimation == 1 ? Color.WHITE : armadylNpc.getColor());

			graphics2D.setFont(originalFont);
		}
	}

	private void renderNpcTile(final Graphics2D graphics2D)
	{
		if (!config.npcTileOutline() || plugin.getArmadylNpcs().isEmpty())
		{
			return;
		}

		for (final ArmadylNpc armadylNpc : plugin.getArmadylNpcs())
		{
			drawNpcTile(graphics2D, armadylNpc.getNpc(), armadylNpc.getColor());
		}
	}

	private void drawNpcTile(final Graphics2D graphics2D, final NPC npc, final Color color)
	{
		if (npc == null || npc.isDead())
		{
			return;
		}

		int size = 1;

		final NPCDefinition npcDefinition = npc.getDefinition();

		if (npcDefinition != null)
		{
			size = npcDefinition.getSize();
		}

		final LocalPoint localPoint = npc.getLocalLocation();

		final int x = localPoint.getX() - ((size - 1) * Perspective.LOCAL_TILE_SIZE / 2);
		final int y = localPoint.getY() - ((size - 1) * Perspective.LOCAL_TILE_SIZE / 2);

		final Polygon tile = Perspective.getCanvasTileAreaPoly(client, localPoint, size);

		if (tile == null)
		{
			return;
		}

		final Polygon southWestTile = Perspective.getCanvasTilePoly(client, new LocalPoint(x, y));

		drawOutlineAndFill(graphics2D, color, config.npcTileOutlineWidth(), southWestTile);
		drawOutlineAndFill(graphics2D, color, config.npcTileOutlineWidth(), tile);
	}

	private static void drawOutlineAndFill(final Graphics2D graphics2D, final Color outlineColor, final int strokeWidth, final Shape shape)
	{
		final Color originalColor = graphics2D.getColor();
		final Stroke originalStroke = graphics2D.getStroke();

		graphics2D.setStroke(new BasicStroke(strokeWidth));
		graphics2D.setColor(outlineColor);
		graphics2D.draw(shape);

		graphics2D.setColor(ArmadylSceneOverlay.TRANSPARENT);
		graphics2D.fill(shape);

		graphics2D.setColor(originalColor);
		graphics2D.setStroke(originalStroke);
	}
}
