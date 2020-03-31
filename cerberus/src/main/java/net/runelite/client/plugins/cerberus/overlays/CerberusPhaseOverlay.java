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
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.client.plugins.cerberus.CerberusPlugin;
import net.runelite.client.plugins.cerberus.Util.CerberusImageManager;
import net.runelite.client.plugins.cerberus.Util.CerberusInfoBoxComponent;
import net.runelite.client.plugins.cerberus.Util.ImagePanelComponent;
import net.runelite.client.plugins.cerberus.domain.CerberusPhase;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.components.ComponentOrientation;
import net.runelite.client.ui.overlay.components.PanelComponent;

@Singleton
public class CerberusPhaseOverlay extends Overlay
{
	final PanelComponent panel = new PanelComponent();
	private final Color autoColor = new Color(66, 37, 14, 50);
	private final Color ghostColor = new Color(255, 255, 255, 150);
	private final Color tripleColor = new Color(0, 15, 255, 160);
	private final Color lavaColor = new Color(82, 0, 0, 255);
	@Inject
	private CerberusPlugin plugin;

	@Inject
	public CerberusPhaseOverlay()
	{
		setPosition(OverlayPosition.BOTTOM_RIGHT);
		setPriority(OverlayPriority.HIGH);

		panel.setBackgroundColor(null);
		panel.setBorder(new Rectangle(0, 0, 0, 0));
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

		final int amountOfAttacks = Math.max(Math.min(plugin.getConfig().amountOfAttacksShown(), 10), 0);
		if (amountOfAttacks == 0)
		{
			return null;
		}
		final Point gap = new Point(plugin.getConfig().horizontalUpcomingAttacks() ? 5 : 0, plugin.getConfig().horizontalUpcomingAttacks() ? 0 : 5);
		panel.setGap(gap);
		panel.getChildren().clear();

		for (int i = 0; i < amountOfAttacks; ++i)
		{
			final int attack;
			if (plugin.getConfig().reverseUpcomingAttacks() ^ !plugin.getConfig().horizontalUpcomingAttacks())
			{
				attack = amountOfAttacks - i;
			}
			else
			{
				attack = i + 1;
			}
			panel.setOrientation(plugin.getConfig().horizontalUpcomingAttacks() ? ComponentOrientation.HORIZONTAL : ComponentOrientation.VERTICAL);

			final int cerbHp = plugin.getCerberus().getHealth();
			final CerberusPhase phase = plugin.getCerberus().getNextAttackPhase(attack, cerbHp);
			final CerberusPhase nextThresholdPhase = plugin.getCerberus().getNextAttackPhase(attack, cerbHp - 200);

			final StringBuilder sbTitle = new StringBuilder();
			final Color backgroundColor;
			final BufferedImage image;

			if (attack == 1)
			{
				int x = -1;
				int y = -1;

				final int dx;
				final int dy;
				if (plugin.getConfig().useSmallBoxes())
				{
					dx = 50;
					dy = 50;
				}
				else
				{
					dx = 85;
					dy = 70;
				}


				if (plugin.getConfig().horizontalUpcomingAttacks() && plugin.getConfig().reverseUpcomingAttacks())
				{
					x += (dx + gap.getX()) * (amountOfAttacks - 1);
				}
				else if (!plugin.getConfig().horizontalUpcomingAttacks() && !plugin.getConfig().reverseUpcomingAttacks())
				{
					y += (dy + gap.getY()) * (amountOfAttacks - 1);
				}

				Rectangle outsideStroke = new Rectangle();
				outsideStroke.setLocation(x, y);
				outsideStroke.setSize(dx + 1, dy + 1);
				graphics.setColor(Color.WHITE);
				graphics.draw(outsideStroke);
			}

			image = CerberusImageManager.getCerberusPhaseBufferedImage(phase, plugin.getPrayer());
			if (image == null)
			{
				continue;
			}


			switch (phase)
			{
				case AUTO:
					sbTitle.append("AUTO");
					backgroundColor = null;
					break;
				case TRIPLE:
					sbTitle.append("TRIPLE");
					backgroundColor = tripleColor;
					break;
				case LAVA:
					sbTitle.append("LAVA");
					backgroundColor = lavaColor;
					break;
				case GHOSTS:
					sbTitle.append("GHOSTS");
					backgroundColor = ghostColor;
					break;
				default:
					continue;
			}

			if (plugin.getConfig().showAttackNumber())
			{
				sbTitle.append(" (");
				sbTitle.append((plugin.getCerberus().getPhaseCount() + attack));
				sbTitle.append(")");
			}


			if (plugin.getConfig().useSmallBoxes())
			{
				final CerberusInfoBoxComponent infoBoxComponent = new CerberusInfoBoxComponent();
				infoBoxComponent.setImage(image);
				if (!nextThresholdPhase.equals(phase))
				{
					final StringBuilder sbInfoBoxText = new StringBuilder(nextThresholdPhase.name().substring(0, Math.min(1, nextThresholdPhase.name().length())));
					sbInfoBoxText.append(" +").append(cerbHp % 200);
					infoBoxComponent.setText(sbInfoBoxText.toString()); // Insert text and pad left up to 9 characters
					infoBoxComponent.setColor(Color.GREEN);
				}
				if (backgroundColor != null)
				{
					infoBoxComponent.setBackgroundColor(backgroundColor);
				}
				infoBoxComponent.setPreferredSize(new Dimension(50, 50));
				panel.setPreferredSize(new Dimension(50, 0));
				panel.getChildren().add(infoBoxComponent);
			}
			else
			{
				final ImagePanelComponent imagePanelComponent = new ImagePanelComponent();
				imagePanelComponent.setTitle(sbTitle.toString());
				if (!nextThresholdPhase.equals(phase))
				{
					imagePanelComponent.setHealthRemaining(cerbHp % 200);
					imagePanelComponent.setNextPhase(nextThresholdPhase.name().substring(0, Math.min(3, nextThresholdPhase.name().length())));
				}
				if (backgroundColor != null)
				{
					imagePanelComponent.setBackgroundColor(backgroundColor);
				}
				imagePanelComponent.setImage(image);

				panel.setPreferredSize(new Dimension(50, 0));
				panel.getChildren().add(imagePanelComponent);
			}

		}
		return panel.render(graphics);
	}
}
