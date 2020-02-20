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

package net.runelite.client.plugins.cerberus.Util;

import com.google.common.base.Strings;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import javax.annotation.Nullable;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.Setter;
import net.runelite.client.ui.overlay.components.BackgroundComponent;
import net.runelite.client.ui.overlay.components.LayoutableRenderableEntity;
import net.runelite.client.ui.overlay.components.TextComponent;

@Singleton
@Setter
public class ImagePanelComponent implements LayoutableRenderableEntity
{
	private static final int TOP_BORDER = 3;
	private static final int SIDE_BORDER = 6;
	private static final int BOTTOM_BORDER = 6;
	private static final int SEPARATOR = 4;
	@Getter
	private final Rectangle bounds = new Rectangle();
	@Nullable
	private String title;
	private Color titleColor = Color.WHITE;
	private Color backgroundColor = new Color(70, 61, 50, 156);
	private BufferedImage image;
	private Point preferredLocation = new Point();
	private int healthRemaining;
	private String nextPhase;
	private Dimension preferredSize;

	@Override
	public Dimension render(Graphics2D graphics)
	{
		final Dimension dimension = new Dimension();
		final FontMetrics metrics = graphics.getFontMetrics();
		int height = TOP_BORDER + (Strings.isNullOrEmpty(title) ? 0 : metrics.getHeight())
			+ SEPARATOR + image.getHeight() + BOTTOM_BORDER;
		int width = Math.max(Strings.isNullOrEmpty(title) ? 0 : metrics.stringWidth(title), image.getWidth()) + SIDE_BORDER * 2;
		dimension.setSize(85, 70);

		if (dimension.height == 0)
		{
			return null;
		}


		// Calculate panel dimensions
		int y = preferredLocation.y + TOP_BORDER + metrics.getHeight();

		// Render background
		final BackgroundComponent backgroundComponent = new BackgroundComponent();
		backgroundComponent.setBackgroundColor(backgroundColor);
		backgroundComponent.setRectangle(new Rectangle(preferredLocation.x, preferredLocation.y, dimension.width, dimension.height));
		backgroundComponent.render(graphics);

		// Render title
		if (!Strings.isNullOrEmpty(title))
		{
			final TextComponent titleComponent = new TextComponent();
			titleComponent.setText(title);
			titleComponent.setColor(titleColor);
			titleComponent.setPosition(new Point(preferredLocation.x + (dimension.width - metrics.stringWidth(title)) / 2, y));
			titleComponent.render(graphics);
			y += SEPARATOR;
		}


		// Render image
		graphics.drawImage(image, preferredLocation.x + (dimension.width - image.getWidth()) / 2, y, null);
		y += image.getHeight() + metrics.getHeight();

		// Render remaining hp
		if (healthRemaining > 0)
		{
			final String hpString = String.format("%s: +%d hp", nextPhase, healthRemaining);

			final TextComponent titleComponent = new TextComponent();
			titleComponent.setText(hpString);
			titleComponent.setColor(Color.GREEN);
			titleComponent.setPosition(new Point(preferredLocation.x + (dimension.width - metrics.stringWidth(hpString)) / 2, y));
			titleComponent.render(graphics);
		}

		bounds.setLocation(preferredLocation);
		bounds.setSize(dimension);

		return dimension;
	}


}