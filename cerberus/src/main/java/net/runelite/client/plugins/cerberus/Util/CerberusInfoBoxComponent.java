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
import java.awt.image.ImageObserver;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.components.BackgroundComponent;
import net.runelite.client.ui.overlay.components.ComponentConstants;
import net.runelite.client.ui.overlay.components.LayoutableRenderableEntity;
import net.runelite.client.ui.overlay.components.TextComponent;

public class CerberusInfoBoxComponent implements LayoutableRenderableEntity
{
	private static final int SEPARATOR = 3;
	private static final int DEFAULT_SIZE = 32;
	private final Rectangle bounds = new Rectangle();
	private String tooltip;
	private Point preferredLocation = new Point();
	private Dimension preferredSize = new Dimension(32, 32);
	private String text;
	private Color color;
	private Color backgroundColor;
	private BufferedImage image;

	public CerberusInfoBoxComponent()
	{
		this.color = Color.WHITE;
		this.backgroundColor = ComponentConstants.STANDARD_BACKGROUND_COLOR;
	}

	public Dimension render(Graphics2D graphics)
	{
		if (this.image == null)
		{
			return new Dimension();
		}
		else
		{
			graphics.setFont(this.getSize() < 32 ? FontManager.getRunescapeSmallFont() : FontManager.getRunescapeFont());
			int baseX = this.preferredLocation.x;
			int baseY = this.preferredLocation.y;
			FontMetrics metrics = graphics.getFontMetrics();
			int size = this.getSize();
			Rectangle bounds = new Rectangle(baseX, baseY, size, size);
			BackgroundComponent backgroundComponent = new BackgroundComponent();
			backgroundComponent.setBackgroundColor(this.backgroundColor);
			backgroundComponent.setRectangle(bounds);
			backgroundComponent.render(graphics);
			if (!Strings.isNullOrEmpty(this.text))
			{
				graphics.drawImage(this.image, baseX + (size - this.image.getWidth((ImageObserver) null)) / 2, baseY + metrics.getHeight() / 2, (ImageObserver) null);
			}
			else
			{
				graphics.drawImage(this.image, baseX + (size - this.image.getWidth((ImageObserver) null)) / 2, baseY + (size - this.image.getHeight((ImageObserver) null)) / 2, (ImageObserver) null);
			}

			if (!Strings.isNullOrEmpty(this.text))
			{
				TextComponent textComponent = new TextComponent();
				textComponent.setColor(this.color);
				textComponent.setText(this.text);
				textComponent.setPosition(new Point(baseX + (size - metrics.stringWidth(this.text)) / 2, baseY + size - 3));
				textComponent.render(graphics);
			}

			this.bounds.setBounds(bounds);
			return bounds.getSize();
		}
	}

	private int getSize()
	{
		return Math.max(this.preferredSize.width, this.preferredSize.height);
	}

	public void setText(String text)
	{
		this.text = text;
	}

	public void setColor(Color color)
	{
		this.color = color;
	}

	public void setBackgroundColor(Color backgroundColor)
	{
		this.backgroundColor = backgroundColor;
	}

	public void setImage(BufferedImage image)
	{
		this.image = image;
	}

	public String getTooltip()
	{
		return this.tooltip;
	}

	public void setTooltip(String tooltip)
	{
		this.tooltip = tooltip;
	}

	public Rectangle getBounds()
	{
		return this.bounds;
	}

	public void setPreferredLocation(Point preferredLocation)
	{
		this.preferredLocation = preferredLocation;
	}

	public void setPreferredSize(Dimension preferredSize)
	{
		this.preferredSize = preferredSize;
	}
}

