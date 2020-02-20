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

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Prayer;
import net.runelite.client.plugins.cerberus.CerberusPlugin;
import net.runelite.client.plugins.cerberus.domain.CerberusPhase;
import net.runelite.client.util.ImageUtil;

@Slf4j
public class CerberusImageManager
{
	private static final BufferedImage[] bufferedImages = new BufferedImage[6];
	//private static final BufferedImage[] largeBufferedImages = new BufferedImage[6];
	private final static int width = 26;
	private final static int height = 26;
	//private final static int largeWidth = 34;
	//private final static int largeHeight = 34;

	public static BufferedImage getCerberusPhaseBufferedImage(CerberusPhase phase, Prayer p)
	{
		switch (phase)
		{
			case TRIPLE:
				if (bufferedImages[0] == null)
				{
					bufferedImages[0] = getBufferedImage("cerberus_triple.png", width, height);
				}
				return bufferedImages[0];
			case AUTO:
				switch (p)
				{
					case PROTECT_FROM_MISSILES:
						if (bufferedImages[1] == null)
						{
							bufferedImages[1] = getBufferedImage("cerberus_range.png", width, height);
						}
						return bufferedImages[1];
					case PROTECT_FROM_MELEE:
						if (bufferedImages[2] == null)
						{
							bufferedImages[2] = getBufferedImage("cerberus_melee.png", width, height);
						}
						return bufferedImages[2];
					default:
						if (bufferedImages[3] == null)
						{
							bufferedImages[3] = getBufferedImage("cerberus_magic.png", width, height);
						}
						return bufferedImages[3];
				}
			case GHOSTS:
				if (bufferedImages[4] == null)
				{
					bufferedImages[4] = getBufferedImage("cerberus_ghosts.png", width, height);
				}
				return bufferedImages[4];
			case LAVA:
				if (bufferedImages[5] == null)
				{
					bufferedImages[5] = getBufferedImage("cerberus_lava.png", width, height);
				}
				return bufferedImages[5];
			default:
				return null;
		}
	}

	public static BufferedImage getCerberusPrayerBufferedImage(Prayer p)
	{
		switch (p)
		{
			case PROTECT_FROM_MISSILES:
				if (bufferedImages[1] == null)
				{
					bufferedImages[1] = getBufferedImage("cerberus_range.png", width, height);
				}
				return bufferedImages[1];
			case PROTECT_FROM_MELEE:
				if (bufferedImages[2] == null)
				{
					bufferedImages[2] = getBufferedImage("cerberus_melee.png", width, height);
				}
				return bufferedImages[2];
			default:
				if (bufferedImages[3] == null)
				{
					bufferedImages[3] = getBufferedImage("cerberus_magic.png", width, height);
				}
				return bufferedImages[3];
		}
	}

	/*public static BufferedImage getCerberusPhaseLargeBufferedImage(CerberusAttackPhase phase, Prayer p)
	{
		switch (phase)
		{
			case TRIPLE:
				if (largeBufferedImages[0] == null)
				{
					largeBufferedImages[0] = getBufferedImage("cerberus_triple.png", largeWidth, largeHeight);
				}
				return largeBufferedImages[0];
			case AUTO:
				switch (p)
				{
					case PROTECT_FROM_MISSILES:
						if (largeBufferedImages[1] == null)
						{
							largeBufferedImages[1] = getBufferedImage("cerberus_range.png", largeWidth, largeHeight);
						}
						return largeBufferedImages[1];
					case PROTECT_FROM_MELEE:
						if (largeBufferedImages[2] == null)
						{
							largeBufferedImages[2] = getBufferedImage("cerberus_melee.png", largeWidth, largeHeight);
						}
						return largeBufferedImages[2];
					default:
						if (largeBufferedImages[3] == null)
						{
							largeBufferedImages[3] = getBufferedImage("cerberus_magic.png", largeWidth, largeHeight);
						}
						return largeBufferedImages[3];
				}
			case GHOSTS:
				if (largeBufferedImages[4] == null)
				{
					largeBufferedImages[4] = getBufferedImage("cerberus_ghosts.png", largeWidth, largeHeight);
				}
				return largeBufferedImages[4];
			case LAVA:
				if (largeBufferedImages[5] == null)
				{
					largeBufferedImages[5] = getBufferedImage("cerberus_lava.png", largeWidth, largeHeight);
				}
				return largeBufferedImages[5];
			default:
				return null;
		}
	}*/

	private static BufferedImage getBufferedImage(String path, int width, int height)
	{
		final BufferedImage img = ImageUtil.getResourceStreamFromClass(CerberusPlugin.class, path);
		final Image tmp = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
		final BufferedImage dimg = new BufferedImage(width, height, img.getType());

		Graphics2D g2d = dimg.createGraphics();
		g2d.drawImage(tmp, 0, 0, null);
		g2d.dispose();

		return dimg;
	}
}
