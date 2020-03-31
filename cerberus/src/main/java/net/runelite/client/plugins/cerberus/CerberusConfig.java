/*
 * Copyright (c) 2017, Aria <aria@ar1as.space>
 * Copyright (c) 2017, Adam <Adam@sigterm.info>
 * Copyright (c) 2017, Devin French <https://github.com/devinfrench>
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

package net.runelite.client.plugins.cerberus;

import net.runelite.client.config.Button;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigTitleSection;
import net.runelite.client.plugins.cerberus.Util.CerberusReadMeButton;

@ConfigGroup("cerberus")

public interface CerberusConfig extends Config
{
	@ConfigTitleSection(
		name = "General",
		description = "Configure general settings.",
		position = 0,
		keyName = "generalSection"
	)
	default boolean generalSection()
	{
		return true;
	}

	@ConfigTitleSection(
		name = "Upcoming attacks",
		description = "Configure how the upcoming attacks are rendered. The upcoming attacks usually follow the same pattern (see http://pastebin.com/hWCvantS).",
		position = 1,
		keyName = "upcomingAttacksSection"
	)
	default boolean upcomingAttacksSection()
	{
		return true;
	}

	@ConfigTitleSection(
		name = "'Guitar Hero'-mode",
		description = "Configure the 'Guitar-hero'-mode.",
		position = 2,
		keyName = "guitarHeroSection"
	)
	default boolean guitarHeroSection()
	{
		return true;
	}


	@ConfigItem(
		keyName = "drawGhostTiles",
		name = "Show ghost tiles",
		description = "Check this to mark the tiles beneath the ghosts with a timer that counts down until its attack.",
		position = 0,
		titleSection = "generalSection"
	)
	default boolean drawGhostTiles()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showPrayerTimer",
		name = "Show prayer timer",
		description = "Check this to show a timer on the prayer overlay until the attack hits.",
		position = 1,
		titleSection = "generalSection"
	)
	default boolean showPrayerTimer()
	{
		return true;
	}

	@ConfigItem(
		keyName = "useSmallBoxes",
		name = "Use small boxes",
		description = "Display the upcoming phases using a smaller display.",
		position = 2,
		titleSection = "generalSection"
	)
	default boolean useSmallBoxes()
	{
		return false;
	}


	@ConfigItem(
		keyName = "amountOfAttacksShown",
		name = "Amount of attacks",
		description = "Indicates how many upcoming attacks you would like to draw on the overlay. (0 - 10)",
		position = 0,
		titleSection = "upcomingAttacksSection"
	)
	default int amountOfAttacksShown()
	{
		return 4;
	}

	@ConfigItem(
		keyName = "reverseUpcomingAttacks",
		name = "Reverse order",
		description = "Check this to reverse the order of the upcoming attacks shown on the overlay.",
		position = 1,
		titleSection = "upcomingAttacksSection"
	)
	default boolean reverseUpcomingAttacks()
	{
		return false;
	}

	@ConfigItem(
		keyName = "horizontalUpcomingAttacks",
		name = "Horizontal mode",
		description = "Check this to show the upcoming attacks horizontally.",
		position = 2,
		titleSection = "upcomingAttacksSection"
	)
	default boolean horizontalUpcomingAttacks()
	{
		return false;
	}

	@ConfigItem(
		keyName = "showAttackNumber",
		name = "Show attack number",
		description = "Check this to show the attack number on each upcoming attack. This can be useful if you understand Cerberus' attack patterns (see http://pastebin.com/hWCvantS).",
		position = 2,
		titleSection = "upcomingAttacksSection"
	)
	default boolean showAttackNumber()
	{
		return true;
	}


	@ConfigItem(
		keyName = "drawDescendingBoxes",
		name = "Enable 'Guitar Hero'-mode",
		description = "Check this to show descending boxes that show you what to pray against every upcoming attack.",
		position = 0,
		titleSection = "guitarHeroSection"
	)
	default boolean drawDescendingBoxes()
	{
		return true;
	}

	@ConfigItem(
		keyName = "amountOfDescendingBoxes",
		name = "Amount of ticks",
		description = "Indicates how many upcoming ticks you would like to draw on the overlay. (0 - 10)",
		position = 1,
		titleSection = "guitarHeroSection"
	)
	default int amountOfDescendingBoxes()
	{
		return 4;
	}


	@ConfigItem(
		keyName = "readMeButton",
		name = "Cerberus info",
		description = "Read this if you don't know how Cerberus or the plugin works!",
		position = 0,
		clazz = CerberusReadMeButton.class
	)
	default Button readMeButton()
	{
		return new Button();
	}
}
