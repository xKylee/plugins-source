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

package net.runelite.client.plugins.armadyl;

import java.awt.Font;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;
import net.runelite.client.config.Units;

@ConfigGroup("armadyl")
public interface ArmadylConfig extends Config
{
	// Sections

	@ConfigSection(
		name = "Tick counters",
		description = "Tick counter settings.",
		position = 0,
		keyName = "tickCounterSection"
	)
	default boolean tickCounterSection()
	{
		return false;
	}

	@ConfigSection(
		name = "Tiles",
		description = "Tile settings.",
		position = 1,
		keyName = "tileSection"
	)
	default boolean tileSection()
	{
		return false;
	}

	@ConfigSection(
		name = "Menus",
		description = "Left/right click menu settings.",
		position = 2,
		keyName = "menuSection"
	)
	default boolean menuSection()
	{
		return false;
	}

//	@ConfigSection(
//		name = "Miscellaneous",
//		description = "Miscellaneous settings.",
//		position = 3,
//		keyName = "miscSection"
//	)
//	default boolean miscSection()
//	{
//		return false;
//	}

	// Tick Counter Section

	@ConfigItem(
		name = "Show prayer tick counter",
		description = "Show tick counters on prayer widgets.",
		position = 0,
		keyName = "prayerTickCounter",
		section = "tickCounterSection"
	)
	default boolean prayerTickCounter()
	{
		return false;
	}

	@ConfigItem(
		name = "Show NPC tick counter",
		description = "Show tick counters on NPCs.",
		position = 1,
		keyName = "npcTickCounter",
		section = "tickCounterSection"
	)
	default boolean npcTickCounter()
	{
		return false;
	}

	@Range(
		min = 12,
		max = 64
	)
	@ConfigItem(
		name = "Font size",
		description = "Adjust the font size of npc tick counters.",
		position = 2,
		keyName = "npcTickCounterFontSize",
		section = "tickCounterSection",
		hidden = true,
		unhide = "npcTickCounter"
	)
	@Units(Units.POINTS)
	default int npcTickCounterFontSize()
	{
		return 16;
	}

	@ConfigItem(
		name = "Font style",
		description = "Bold/Italics/Plain",
		position = 3,
		keyName = "npcTickCounterFontStyle",
		section = "tickCounterSection",
		hidden = true,
		unhide = "npcTickCounter",
		enumClass = FontStyle.class
	)
	default FontStyle npcTickCounterFontStyle()
	{
		return FontStyle.BOLD;
	}

	@ConfigItem(
		name = "Show guitar hero mode",
		description = "Show descending boxes on prayer widgets.",
		position = 4,
		keyName = "guitarHeroMode",
		section = "tickCounterSection"
	)
	default boolean guitarHeroMode()
	{
		return false;
	}

	@ConfigItem(
		name = "Ignore non-targetting NPCs",
		description = "Hide tick counters for NPCs not targetting you.",
		position = 5,
		keyName = "ignoreNonTargettingNpcs",
		section = "tickCounterSection"
	)
	default boolean ignoreNonTargettingNpcs()
	{
		return false;
	}

	@ConfigItem(
		name = "Hide counters during Kree'arra",
		description = "Hide tick counters while Kree'arra is alive.",
		position = 6,
		keyName = "selectiveTickCount",
		section = "tickCounterSection"
	)
	default boolean selectiveTickCount()
	{
		return false;
	}

	// Tile Section

	@ConfigItem(
		name = "Show NPC tile outlines",
		description = "Show tile outlines for NPCs.",
		position = 0,
		keyName = "npcTileOutline",
		section = "tileSection"
	)
	default boolean npcTileOutline()
	{
		return false;
	}

	@Range(
		min = 1,
		max = 4
	)
	@ConfigItem(
		name = "Tile outline width",
		description = "Change the width of npc tile outlines.",
		position = 1,
		keyName = "npcTileOutlineWidth",
		section = "tileSection",
		hidden = true,
		unhide = "npcTileOutline"
	)
	@Units(Units.POINTS)
	default int npcTileOutlineWidth()
	{
		return 1;
	}

	// Menu Section

	@ConfigItem(
		name = "Color NPC menu entries",
		description = "Color NPC menu entries with their respective colors.",
		position = 0,
		keyName = "colorNpcMenuEntries",
		section = "menuSection"
	)
	default boolean colorNpcMenuEntries()
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
