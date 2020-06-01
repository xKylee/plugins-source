/*
 * Copyright (c) 2019, kThisIsCvpv <https://github.com/kThisIsCvpv>
 * Copyright (c) 2019, ganom <https://github.com/Ganom>
 * Copyright (c) 2019, kyle <https://github.com/Kyleeld>
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

package net.runelite.client.plugins.gauntlet;

import java.awt.Color;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigTitleSection;
import net.runelite.client.config.Range;
import net.runelite.client.config.Title;
import net.runelite.client.config.Units;

@ConfigGroup("Gauntlet")

public interface GauntletConfig extends Config
{
	@Getter
	@AllArgsConstructor
	enum CounterDisplay
	{
		ONBOSS("On Boss"),
		BOX("Box"),
		BOTH("Both"),
		NONE("None");

		private String name;

		@Override
		public String toString()
		{
			return getName();
		}
	}

	@Getter
	@AllArgsConstructor
	enum PrayerHighlight
	{
		PRAYERWIDGET("On Widget"),
		BOX("Box"),
		BOTH("Both"),
		NONE("None");

		private String name;

		@Override
		public String toString()
		{
			return getName();
		}
	}

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

	@ConfigTitleSection(
		keyName = "resources",
		position = 0,
		name = "Resources",
		description = ""
	)
	default Title resources()
	{
		return new Title();
	}

	@ConfigItem(
		position = 1,
		keyName = "highlightResources",
		name = "Highlight resources (outline)",
		description = "Overlay resources with a colored outline.",
		titleSection = "resources"
	)
	default boolean highlightResources()
	{
		return false;
	}

	@ConfigItem(
		position = 2,
		keyName = "highlightResourcesColor",
		name = "Outline color",
		description = "Change the color of the resource outline.",
		titleSection = "resources",
		hidden = true,
		unhide = "highlightResources"
	)
	default Color highlightResourcesColor()
	{
		return Color.YELLOW;
	}

	@ConfigItem(
		position = 3,
		keyName = "highlightResourcesIcons",
		name = "Highlight resources (icon)",
		description = "Overlay resources with their respective icons.",
		titleSection = "resources"
	)
	default boolean highlightResourcesIcons()
	{
		return false;
	}

	@Range(
		min = 1,
		max = 50
	)
	@ConfigItem(
		position = 4,
		keyName = "resourceIconSize",
		name = "Icon size",
		description = "Change the size of the resource icons.",
		hidden = true,
		unhide = "highlightResourcesIcons",
		titleSection = "resources"
	)
	default int resourceIconSize()
	{
		return 16;
	}

	@ConfigItem(
		position = 5,
		keyName = "displayResources",
		name = "Resource counter (infobox)",
		description = "Display the count of gathered resources in infoboxes.",
		titleSection = "resources"
	)
	default boolean displayGatheredResources()
	{
		return false;
	}

	@ConfigTitleSection(
		keyName = "boss",
		position = 1,
		name = "Hunllef Boss",
		description = ""
	)
	default Title boss()
	{
		return new Title();
	}

	@ConfigItem(
		position = 0,
		keyName = "countAttacks",
		name = "Atk/prayer counter",
		description = "Display a counter for when the Hunllef will switch attacks or prayer.",
		titleSection = "boss"
	)
	default CounterDisplay countAttacks()
	{
		return CounterDisplay.NONE;
	}

	@ConfigItem(
		position = 1,
		keyName = "highlightPrayer",
		name = "Highlight prayer",
		description = "Highlight the correct prayer to use against the Hunllef's attacks.",
		titleSection = "boss"
	)
	default PrayerHighlight highlightPrayer()
	{
		return PrayerHighlight.NONE;
	}

	@ConfigItem(
		position = 2,
		keyName = "attackVisualOutline",
		name = "Attack projectiles (outline)",
		description = "Attack projectiles will have a colored outline overlay.",
		titleSection = "boss"
	)
	default boolean attackVisualOutline()
	{
		return false;
	}

	@ConfigItem(
		position = 3,
		keyName = "uniqueAttackVisual",
		name = "Attack projectiles (icon)",
		description = "Attack projectiles will have an icon overlay.",
		titleSection = "boss"
	)
	default boolean uniqueAttackVisual()
	{
		return false;
	}

	@ConfigItem(
		position = 4,
		keyName = "overlayBossPrayer",
		name = "Attack style (icon)",
		description = "Overlay the Hunllef with an icon denoting its current attack style.",
		titleSection = "boss"
	)
	default boolean overlayBossPrayer()
	{
		return false;
	}

	@ConfigItem(
		position = 5,
		keyName = "uniquePrayerAudio",
		name = "Prayer attack (audio)",
		description = "Play a sound when the Hunllef is about to use its prayer attack.",
		titleSection = "boss"
	)
	default boolean uniquePrayerAudio()
	{
		return false;
	}

	@ConfigItem(
		position = 6,
		keyName = "overlayBoss",
		name = "Wrong prayer (outline)",
		description = "While on the wrong prayer, outline the Hunllef with a color matching its attack style.",
		titleSection = "boss"
	)
	default boolean overlayBoss()
	{
		return false;
	}

	@ConfigItem(
		position = 7,
		keyName = "flashOnWrongAttack",
		name = "Wrong attack (flash)",
		description = "While on the wrong attack style, flash the screen.",
		titleSection = "boss"
	)
	default boolean flashOnWrongAttack()
	{
		return false;
	}

	@ConfigItem(
		position = 8,
		keyName = "overlayTornadoes",
		name = "Tornado decay counter",
		description = "Overlay tornadoes with the amount of ticks left until decay.",
		titleSection = "boss"
	)
	default boolean overlayTornadoes()
	{
		return false;
	}

	@Range(
		min = 1,
		max = 50
	)
	@ConfigItem(
		position = 9,
		keyName = "projectileIconSize",
		name = "Proj. icon size",
		description = "Change the size of projectile icons.",
		titleSection = "boss"
	)
	@Units(Units.PIXELS)
	default int projectileIconSize()
	{
		return 20;
	}

	@ConfigTitleSection(
		keyName = "timer",
		position = 2,
		name = "Timer",
		description = ""
	)
	default Title timer()
	{
		return new Title();
	}

	@ConfigItem(
		position = 0,
		keyName = "displayTimerWidget",
		name = "Gauntlet timer (overlay)",
		description = "Display a timer overlay that tracks your gauntlet progress.",
		titleSection = "timer"
	)
	default boolean displayTimerWidget()
	{
		return false;
	}

	@ConfigItem(
		position = 1,
		keyName = "displayTimerChat",
		name = "Gauntlet timer (chat)",
		description = "Display a chat message that tracks your gauntlet progress.",
		titleSection = "timer"
	)
	default boolean displayTimerChat()
	{
		return false;
	}
}
