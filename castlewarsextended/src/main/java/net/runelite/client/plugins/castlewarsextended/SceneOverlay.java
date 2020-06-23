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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Shape;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.NPC;
import net.runelite.api.NPCDefinition;
import net.runelite.api.NpcID;
import net.runelite.api.ObjectID;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

public class SceneOverlay extends Overlay
{
	private final Client client;
	private final CastleWarsExtendedConfig config;
	private final CastleWarsExtendedPlugin plugin;

	@Inject
	SceneOverlay(Client client, CastleWarsExtendedConfig config, CastleWarsExtendedPlugin plugin)
	{
		this.client = client;
		this.config = config;
		this.plugin = plugin;
		setPosition(OverlayPosition.DYNAMIC);
		determineLayer();
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{

		for (NPC npc : plugin.getHighlightBarricades())
		{
			switch (npc.getId())
			{
				case NpcID.BARRICADE: //Saradomin Barricade
				case NpcID.BARRICADE_5723: //Saradomin Barricade Lit
					renderNpcSceneOverlay(graphics, npc, config.getSaradominHighlightColor());
					break;
				case NpcID.BARRICADE_5724: //Zamorak Barricade
				case NpcID.BARRICADE_5725: //Zamorak Barricade Lit
					renderNpcSceneOverlay(graphics, npc, config.getZamorakHighlightColor());
					break;
			}
		}

		for (GameObject gameObject : plugin.getHighlightRocks())
		{
			switch (gameObject.getId())
			{

				case ObjectID.ROCKS_4437: //Underground rocks full
				case ObjectID.ROCKS_4438: //Underground rocks half
					renderGameObjectSceneOverlay(graphics, gameObject, config.getRocksHighlightColor());
					break;
			}
		}

		return null;
	}

	private void renderNpcSceneOverlay(Graphics2D graphics, NPC actor, Color color)
	{
		NPCDefinition npcDefinition = actor.getTransformedDefinition();
		if (npcDefinition == null || !npcDefinition.isFollower())
		{
			return;
		}

		Shape objectClickbox = actor.getConvexHull();
		renderPoly(graphics, color, objectClickbox);

	}

	private void renderGameObjectSceneOverlay(Graphics2D graphics, GameObject actor, Color color)
	{
		if (actor == null)
		{
			return;
		}

		Shape objectClickbox = actor.getConvexHull();
		renderPoly(graphics, color, objectClickbox);

	}

	private void renderPoly(Graphics2D graphics, Color color, Shape polygon)
	{
		if (polygon != null)
		{
			graphics.setColor(color);
			graphics.setStroke(new BasicStroke(2));
			graphics.draw(polygon);
			graphics.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 20));
			graphics.fill(polygon);
		}
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