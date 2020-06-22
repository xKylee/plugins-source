/*
 * Copyright (c) 2020, dutta64 <https://github.com/dutta64>
 * Copyright (c) 2020, Anthony Alves
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

package net.runelite.client.plugins.gauntlet.resource;

import java.awt.image.BufferedImage;
import java.util.Map;
import net.runelite.client.plugins.gauntlet.GauntletConfig;
import net.runelite.client.plugins.gauntlet.GauntletPlugin;
import net.runelite.client.ui.overlay.infobox.Counter;

public class InfoboxResourceCounter extends Counter
{
	private final GauntletPlugin plugin;
	private final GauntletConfig config;

	private final Map<Integer, Integer> resourceCounts;

	private final int itemId;

	InfoboxResourceCounter(final BufferedImage bufferedImage, final GauntletPlugin plugin, final GauntletConfig config, final Map<Integer, Integer> resourceCounts, final int itemId, final int itemCount)
	{
		super(bufferedImage, plugin, itemCount);
		this.plugin = plugin;
		this.config = config;
		this.resourceCounts = resourceCounts;
		this.itemId = itemId;
	}

	@Override
	public int getCount()
	{
		return resourceCounts.getOrDefault(itemId, 0);
	}

	@Override
	public boolean render()
	{
		return plugin.isInGauntlet() && config.resourceTracker() && !plugin.isInHunllefRoom();
	}
}
