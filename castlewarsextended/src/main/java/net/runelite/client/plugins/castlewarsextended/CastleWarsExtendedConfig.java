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

import java.awt.Color;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("castlewarsextended")
public interface CastleWarsExtendedConfig extends Config
{
	@ConfigItem(
		name = "Mirror Mode Compatibility?",
		keyName = "mirrorMode",
		description = "Should we show the overlay on Mirror Mode?",
		position = 0
	)
	default boolean mirrorMode()
	{
		return false;
	}

	@ConfigItem(
		keyName = "hidePlayerOptions",
		name = "Quick use",
		description = "Hide player options when using explosive potion or tinderbox for quick explosion/tinder.",
		position = 1
	)
	default boolean hidePlayerOptions()
	{
		return true;
	}

	@ConfigItem(
		keyName = "hideNpcOptions",
		name = "Quick flag grab",
		description = "Hide npc options when flag is on same location as barricade for quick grab.",
		position = 2
	)
	default boolean hideNpcOptions()
	{
		return true;
	}

	@ConfigItem(
		keyName = "displayOpenTunnels",
		name = "Display open tunnels",
		description = "Display which tunnel underground is open.",
		position = 3
	)
	default boolean displayOpenTunnels()
	{
		return true;
	}

	@ConfigItem(
		keyName = "barricadeHighlight",
		name = "Barricade highlight",
		description = "Set highlight on barricades for each team color.",
		position = 4
	)
	default boolean barricadeHighlight()
	{
		return true;
	}

	@ConfigItem(
		keyName = "hexColorSaradominBarricade",
		name = "Saradomin barricade highlight color",
		description = "Color of saradomin barricade highlight.",
		position = 5
	)
	default Color getSaradominHighlightColor()
	{
		return Color.BLUE;
	}

	@ConfigItem(
		keyName = "hexColorZamorakBarricade",
		name = "Zamorak barricade highlight color",
		description = "Color of zamorak barricade highlight.",
		position = 6
	)
	default Color getZamorakHighlightColor()
	{
		return Color.RED;
	}

	@ConfigItem(
		keyName = "rocksHighlight",
		name = "Underground rocks highlight",
		description = "Highlight underground rocks.",
		position = 7
	)
	default boolean rocksHighlight()
	{
		return true;
	}

	@ConfigItem(
		keyName = "hexColorRocks",
		name = "Rocks highlight color",
		description = "Color of underground rocks highlight.",
		position = 8
	)
	default Color getRocksHighlightColor()
	{
		return Color.GRAY;
	}

	@ConfigItem(
		keyName = "useTindTimer",
		name = "Tind timer",
		description = "Use timer for lit barricades",
		position = 9
	)
	default boolean useTindTimer()
	{
		return true;
	}

	@ConfigItem(
		keyName = "hexColorLit",
		name = "Tind timer color",
		description = "Color of tind timer.",
		position = 10
	)
	default Color getLitColor()
	{
		return Color.YELLOW;
	}

	@ConfigItem(
		keyName = "hexColorEmpty",
		name = "Tind timer nearly finished color",
		description = "Color of timer when almost finished.",
		position = 11
	)
	default Color getEmptyColor()
	{
		return Color.RED;
	}
}