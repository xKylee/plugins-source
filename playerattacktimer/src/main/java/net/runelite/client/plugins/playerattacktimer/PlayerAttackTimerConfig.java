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

package net.runelite.client.plugins.playerattacktimer;

import java.awt.Color;
import java.awt.Font;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;
import net.runelite.client.config.Units;

@ConfigGroup("playerattacktimer")
public interface PlayerAttackTimerConfig extends Config
{
	@ConfigSection(
		name = "Settings",
		description = "",
		position = 0,
		keyName = "settings"
	)
	String settings = "Settings";

	@ConfigSection(
		name = "Font Settings",
		description = "",
		position = 1,
		keyName = "fontSettings"
	)
	String fontSettings = "Font Settings";

	//------------------------------------------------------------//
	// Settings
	//------------------------------------------------------------//

	@ConfigItem(
		name = "Debug animation ids",
		description = "Show your player's current animation ID."
			+ "<br>Animation IDs can be viewed by wielding a weapon and attacking an NPC.",
		position = 0,
		keyName = "debugAnimationIds",
		section = settings,
		disabledBy = "playerAttackTimer"
	)
	default boolean debugAnimationIds()
	{
		return false;
	}

	@ConfigItem(
		name = "Custom animations (one per line)",
		description = "Syntax AnimationID:TickDelay"
			+ "<br>e.g. Abyssal whip would be 1658:4"
			+ "<br>Animation Ids can be obtained by enabling the above debug setting."
			+ "<br>Weapon tick delays can be found on the wiki.",
		position = 1,
		keyName = "customAnimations",
		section = settings,
		parse = true,
		clazz = ConfigParser.class,
		method = "parse"
	)
	default String customAnimations()
	{
		return "";
	}

	//------------------------------------------------------------//
	// Font Settings
	//------------------------------------------------------------//

	@ConfigItem(
		name = "Font style",
		description = "Font style can be bold, plain, or italicized.",
		position = 0,
		keyName = "fontStyle",
		section = fontSettings,
		enumClass = FontStyle.class
	)
	default FontStyle fontStyle()
	{
		return FontStyle.BOLD;
	}

	@ConfigItem(
		name = "Font shadow",
		description = "Toggle font shadow.",
		position = 1,
		keyName = "fontShadow",
		section = fontSettings
	)
	default boolean fontShadow()
	{
		return true;
	}

	@Range(
		min = 12,
		max = 64
	)
	@ConfigItem(
		name = "Font size",
		description = "Adjust font size.",
		position = 2,
		keyName = "fontSize",
		section = fontSettings
	)
	@Units(Units.POINTS)
	default int fontSize()
	{
		return 20;
	}

	@Alpha
	@ConfigItem(
		name = "Font color",
		description = "Adjust font color.",
		position = 3,
		keyName = "fontColor",
		section = fontSettings
	)
	default Color fontColor()
	{
		return new Color(255, 0, 0, 255);
	}

	@Range(
		min = -100,
		max = 100
	)
	@ConfigItem(
		name = "Font zOffset",
		description = "Adjust the Z coordinate offset.",
		position = 4,
		keyName = "fontZOffset",
		section = fontSettings
	)
	@Units(Units.POINTS)
	default int fontZOffset()
	{
		return 0;
	}

	//------------------------------------------------------------//
	// Constants
	//------------------------------------------------------------//

	@Getter
	@AllArgsConstructor
	enum FontStyle
	{
		BOLD("Bold", Font.BOLD),
		ITALIC("Italic", Font.ITALIC),
		PLAIN("Plain", Font.PLAIN);

		private final String name;
		private final int font;

		@Override
		public String toString()
		{
			return name;
		}
	}
}
