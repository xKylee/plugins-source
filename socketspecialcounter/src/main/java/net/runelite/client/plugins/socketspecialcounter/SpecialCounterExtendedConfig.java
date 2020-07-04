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

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("Socket Special Attack Counter")
public interface SpecialCounterExtendedConfig extends Config
{

	@ConfigItem(
		position = 0,
		keyName = "mirrorMode",
		name = "Mirror Mode Compatibility?",
		description = "Should we show the overlay on Mirror Mode?"
	)
	default boolean mirrorMode()
	{
		return false;
	}

	@ConfigItem(
		position = 1,
		keyName = "showHitOverlay",
		name = "Hit Overlay",
		description = "Show the special attack overlay."
	)
	default boolean showHitOverlay()
	{
		return true;
	}

	@ConfigItem(
		position = 2,
		keyName = "getFadeDelay",
		name = "Fade Delay",
		description = "Delay, in milliseconds, until the icon disappears."
	)
	default int getFadeDelay()
	{
		return 5000;
	}

	@ConfigItem(
		position = 3,
		keyName = "getMaxHeight",
		name = "Travel Height",
		description = "Maximum height, in pixels, for the icon to travel."
	)
	default int getMaxHeight()
	{
		return 200;
	}

	@ConfigItem(
		position = 4,
		keyName = "guessDawnbringer",
		name = "Guess Dawnbringer Hit",
		description = "Guess Dawnbringer based on XP drop. Provides faster results."
	)
	default boolean guessDawnbringer()
	{
		return true;
	}
}
