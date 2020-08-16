/*
 * Copyright (c) 2019, Lucas <https://github.com/lucwousin>
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
package net.runelite.client.plugins.alchemicalhydra;

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

@ConfigGroup("alchemicalhydra")
public interface AlchemicalHydraConfig extends Config
{
	// Sections

	@ConfigSection(
		keyName = "settings",
		name = "Settings",
		description = "",
		position = 0
	)
	default boolean settings()
	{
		return false;
	}

	@ConfigSection(
		keyName = "misc",
		name = "Misc",
		description = "",
		position = 1
	)
	default boolean misc()
	{
		return false;
	}

	// Settings

	@ConfigItem(
		keyName = "hydraImmunityOutline",
		name = "Hydra immunity outline",
		description = "Overlay the hydra with a colored outline while it has immunity/not weakened.",
		position = 0,
		section = "settings"
	)
	default boolean hydraImmunityOutline()
	{
		return false;
	}

	@ConfigItem(
		keyName = "fountainOutline",
		name = "Fountain occupancy outline",
		description = "Overlay fountains with a colored outline indicating if the hydra is standing on it.",
		position = 1,
		section = "settings"
	)
	default boolean fountainOutline()
	{
		return false;
	}

	@ConfigItem(
		keyName = "hidePrayerOnSpecial",
		name = "Hide prayer on special attack",
		description = "Hide prayer overlay during special attacks."
			+ "<br>This can help indicate when to save prayer points.",
		position = 2,
		section = "settings"
	)
	default boolean hidePrayerOnSpecial()
	{
		return false;
	}

	@ConfigItem(
		keyName = "showHpUntilPhaseChange",
		name = "Show HP until phase change",
		description = "Overlay hydra with hp remaining until next phase change.",
		position = 3,
		section = "settings"
	)
	default boolean showHpUntilPhaseChange()
	{
		return false;
	}

	@ConfigItem(
		name = "Font style",
		description = "Font style can be bold, plain, or italicized.",
		position = 4,
		keyName = "fontStyle",
		section = "settings",
		enumClass = FontStyle.class,
		hidden = true,
		unhide = "showHpUntilPhaseChange"
	)
	default FontStyle fontStyle()
	{
		return FontStyle.BOLD;
	}

	@ConfigItem(
		name = "Font shadow",
		description = "Toggle font shadow.",
		position = 5,
		keyName = "fontShadow",
		section = "settings",
		hidden = true,
		unhide = "showHpUntilPhaseChange"
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
		position = 6,
		keyName = "fontSize",
		section = "settings",
		hidden = true,
		unhide = "showHpUntilPhaseChange"
	)
	@Units(Units.POINTS)
	default int fontSize()
	{
		return 16;
	}

	@Alpha
	@ConfigItem(
		name = "Font color",
		description = "Adjust font color.",
		position = 7,
		keyName = "fontColor",
		section = "settings",
		hidden = true,
		unhide = "showHpUntilPhaseChange"
	)
	default Color fontColor()
	{
		return new Color(255, 255, 255, 255);
	}

	@Range(
		min = -100,
		max = 100
	)
	@ConfigItem(
		name = "Font zOffset",
		description = "Adjust the Z coordinate offset.",
		position = 8,
		keyName = "fontZOffset",
		section = "settings",
		hidden = true,
		unhide = "showHpUntilPhaseChange"
	)
	@Units(Units.POINTS)
	default int fontZOffset()
	{
		return 0;
	}

	// Misc

	@Alpha
	@ConfigItem(
		keyName = "safeColor",
		name = "Safe color",
		description = "Color indicating there are at least two hydra attacks pending.",
		position = 0,
		section = "misc"
	)
	default Color safeColor()
	{
		return new Color(0, 150, 0, 150);
	}

	@Alpha
	@ConfigItem(
		keyName = "warningColor",
		name = "Warning color",
		description = "Color indicating there is one hydra attack pending.",
		position = 1,
		section = "misc"
	)
	default Color warningColor()
	{
		return new Color(200, 150, 0, 150);
	}

	@Alpha
	@ConfigItem(
		keyName = "dangerColor",
		name = "Danger color",
		description = "Color indiciating the hydra will change attacks.",
		position = 2,
		section = "misc"
	)
	default Color dangerColor()
	{
		return new Color(150, 0, 0, 150);
	}

	@Alpha
	@ConfigItem(
		keyName = "poisonOutlineColor",
		name = "Poison outline color",
		description = "Outline color of poison area tiles.",
		position = 3,
		section = "misc"
	)
	default Color poisonOutlineColor()
	{
		return new Color(255, 0, 0, 100);
	}

	@Alpha
	@ConfigItem(
		keyName = "poisonFillColor",
		name = "Poison fill color",
		description = "Fill color of poison area tiles.",
		position = 4,
		section = "misc"
	)
	default Color poisonFillColor()
	{
		return new Color(255, 0, 0, 50);
	}

	// Mirror mode

	@ConfigItem(
		name = "Mirror mode compatibility",
		keyName = "mirrorMode",
		description = "Render overlays while mirror mode enabled.",
		position = 99
	)
	default boolean mirrorMode()
	{
		return false;
	}

	// Constants

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
