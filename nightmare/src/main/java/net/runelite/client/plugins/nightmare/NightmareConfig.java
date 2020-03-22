/*
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
package net.runelite.client.plugins.nightmare;

import java.awt.Color;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigTitleSection;

@ConfigGroup("nightmareOfAshihama")
public interface NightmareConfig extends Config
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

	@ConfigItem(
		keyName = "prayerHelper",
		name = "Prayer helper",
		description = "Displays the correct prayer to use at various points in the fight.",
		position = 0,
		titleSection = "generalSection"
	)
	default boolean prayerHelper()
	{
		return true;
	}

	@ConfigItem(
		keyName = "tickCounter",
		name = "Show Ticks",
		description = "Displays the number of ticks until next attack",
		position = 1,
		titleSection = "generalSection"
	)
	default boolean ticksCounter()
	{
		return true;
	}

	@ConfigItem(
		keyName = "highlightTotems",
		name = "Highlight Totems",
		description = "Highlights Totems based on their status",
		position = 2,
		titleSection = "generalSection"
	)
	default boolean highlightTotems()
	{
		return true;
	}

	@ConfigItem(
		keyName = "highlightShadows",
		name = "Highlight Shadows",
		description = "Highlights the Shadow Attacks",
		position = 3,
		titleSection = "generalSection"
	)
	default boolean highlightShadows()
	{
		return true;
	}

	@ConfigItem(
		keyName = "highlightSpores",
		name = "Highlight Spores",
		description = "Highlights spores that will make you yawn",
		position = 4,
		titleSection = "generalSection"
	)
	default boolean highlightSpores()
	{
		return true;
	}

	@Alpha
	@ConfigItem(
		keyName = "poisonBorderCol",
		name = "Poison border colour",
		description = "Colour the edges of the area highlighted by poison special will be",
		position = 5,
		titleSection = "generalSection"
	)

	default Color poisonBorderCol()
	{
		return new Color(255, 0, 0, 100);
	}

	@Alpha
	@ConfigItem(
		keyName = "poisonCol",
		name = "Poison colour",
		description = "Colour the fill of the area highlighted by poison special will be",
		position = 6,
		titleSection = "generalSection"
	)
	default Color poisonCol()
	{
		return new Color(255, 0, 0, 50);
	}

	@ConfigItem(
		keyName = "showTicksUntilParasite",
		name = "Indicate Parasites",
		description = "Displays a red tick timer on you showing if/when a parasite will emerge",
		position = 7,
		titleSection = "generalSection"
	)
	default boolean showTicksUntilParasite()
	{
		return true;
	}
}
