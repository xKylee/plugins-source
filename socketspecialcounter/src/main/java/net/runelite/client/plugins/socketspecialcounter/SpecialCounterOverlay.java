/*
 * Copyright (c) 2020, Charles <github.com/kthisiscvpv>
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

package net.runelite.client.plugins.socketspecialcounter;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.OverlayUtil;

public class SpecialCounterOverlay extends Overlay
{

	private final Client client;
	private final SpecialCounterExtendedPlugin plugin;
	private final SpecialCounterExtendedConfig config;

	private Map<String, ArrayList<SpecialIcon>> drawings = new HashMap<>();

	@Inject
	private SpecialCounterOverlay(Client client, SpecialCounterExtendedPlugin plugin, SpecialCounterExtendedConfig config)
	{
		this.client = client;
		this.plugin = plugin;
		this.config = config;

		setPosition(OverlayPosition.DYNAMIC);
		setPriority(OverlayPriority.HIGH);
		determineLayer();
	}

	private void determineLayer()
	{
		if (config.mirrorMode())
		{
			setLayer(OverlayLayer.AFTER_MIRROR);
		}
		else
		{
			setLayer(OverlayLayer.ALWAYS_ON_TOP);
		}
	}

	public void addOverlay(String player, SpecialIcon icon)
	{
		if (config.showHitOverlay())
		{
			ArrayList<SpecialIcon> icons = new ArrayList<>();
			if (drawings.containsKey(player))
			{
				icons = drawings.get(player);
			}
			icons.add(icon);
			drawings.put(player, icons);
		}
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		ArrayList<String> removePlayers = new ArrayList<>();

		Map<String, LocalPoint> locations = new HashMap<>();
		for (Player player : client.getPlayers())
		{
			locations.put(player.getName(), player.getLocalLocation());
		}

		for (String playerName : drawings.keySet())
		{
			LocalPoint center = locations.get(playerName);
			if (center != null)
			{
				ArrayList<SpecialIcon> icons = drawings.get(playerName);
				ArrayList<SpecialIcon> removeIcons = new ArrayList<>();
				int currentHeight = 200; // Base height for a player.

				for (int i = (icons.size() - 1); i >= 0; i--)
				{
					SpecialIcon icon = icons.get(i);

					long elapsedTime = System.currentTimeMillis() - icon.getStartTime();
					final int fadeDelay = Math.max(config.getFadeDelay(), 1);
					long timeRemaining = fadeDelay - elapsedTime;

					if (timeRemaining <= 0)
					{
						removeIcons.add(icon);
						continue;
					}

					float opacity = ((float) timeRemaining) / ((float) fadeDelay);
					float thresh = Math.min(opacity + 0.2f, 1.0f);
					graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, thresh));

					final int maxHeight = Math.max(config.getMaxHeight(), 1);
					int updatedHeight = maxHeight - (int) (((float) maxHeight) * thresh);

					Point drawPoint = Perspective.getCanvasImageLocation(client, center, icon.getImage(),
						currentHeight + updatedHeight);
					graphics.drawImage(icon.getImage(), drawPoint.getX(), drawPoint.getY(), null);

					if (icon.getText() != null)
					{
						Point textPoint = Perspective
							.getCanvasTextLocation(client, graphics, center, icon.getText(),
								currentHeight + updatedHeight);
						graphics.setFont(new Font("Arial", Font.BOLD, 16));

						final Point canvasCenterPoint = new Point(textPoint.getX(), textPoint.getY());
						final Point canvasCenterPointShadow =
							new Point(textPoint.getX() + 1, textPoint.getY() + 1);

						OverlayUtil
							.renderTextLocation(graphics, canvasCenterPointShadow, icon.getText(), Color.BLACK);
						OverlayUtil
							.renderTextLocation(graphics, canvasCenterPoint, icon.getText(), Color.WHITE);
					}

					currentHeight += (icon.getImage().getHeight() * 2);
				}

				for (SpecialIcon icon : removeIcons)
				{
					icons.remove(icon);
				}

				if (icons.isEmpty())
				{
					removePlayers.add(playerName);
				}
			}
			else
			{
				removePlayers.add(playerName);
			}
		}

		for (String playerName : removePlayers)
		{
			drawings.remove(playerName);
		}

		return null;
	}
}
