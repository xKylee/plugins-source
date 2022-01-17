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
package net.runelite.client.plugins.nex;

import java.awt.Color;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;
import net.runelite.client.config.Units;

@ConfigGroup(NexConfig.GROUP)
public interface NexConfig extends Config
{
	String GROUP = "nextended";

	@ConfigSection(
		name = "General",
		description = "Configure general settings.",
		position = 100,
		keyName = "generalSection"
	)
	String generalSection = "General";

	@ConfigItem(
		keyName = "flash",
		name = "Flash on null attack",
		description = "Flash screen when attacking and invulnerable",
		position = 101,
		section = generalSection
	)
	default boolean flash()
	{
		return false;
	}

	@ConfigItem(
		keyName = "prayerHelper",
		name = "Prayer helper",
		description = "Displays the correct prayer to use at various points in the fight.",
		position = 102,
		section = generalSection,
		enumClass = PrayerDisplay.class
	)
	default PrayerDisplay prayerHelper()
	{
		return PrayerDisplay.BOTH;
	}

	@ConfigItem(
		position = 103,
		keyName = "alwaysShowPrayerHelper",
		name = "Always Show Prayer Helper",
		description = "Render prayer helper at all time, even when other inventory tabs are open.",
		section = generalSection
	)
	default boolean alwaysShowPrayerHelper()
	{
		return true;
	}

	@ConfigItem(
		position = 104,
		keyName = "indicatePrayerIsCorrect",
		name = "Indicate when Correct Prayer",
		description = "Show a green box around prayer when its correct",
		section = generalSection
	)
	default boolean indicatePrayerIsCorrect()
	{
		return false;
	}

	@ConfigItem(
		position = 105,
		keyName = "indicateNexVulnerability",
		name = "Indicate Nex",
		description = "Indicate when you should or should not be attacking Nex",
		section = generalSection,
		enumClass = VulnerabilityDisplay.class

	)
	default VulnerabilityDisplay indicateNexVulnerability()
	{
		return VulnerabilityDisplay.BOTH;
	}

	@ConfigItem(
		position = 106,
		keyName = "indicateMinionVulnerability",
		name = "Indicate Minion",
		description = "Indicate when you should attack a minion",
		section = generalSection,
		enumClass = VulnerabilityDisplay.class
	)
	default VulnerabilityDisplay indicateMinionVulnerability()
	{
		return VulnerabilityDisplay.BOTH;
	}

	@Alpha
	@ConfigItem(
		position = 107,
		keyName = "invulnerableColor",
		name = "Invulnerable Color",
		description = "Color for when something is invulnerable",
		section = generalSection
	)
	default Color invulnerableColor()
	{
		return new Color(241, 48, 48, 224);
	}

	@Range(
		min = 1,
		max = 10
	)
	@ConfigItem(
		position = 108,
		keyName = "invulnerableWidth",
		name = "Invulnerable Width",
		description = "Size for outline",
		section = generalSection
	)
	default int invulnerableWidth()
	{
		return 3;
	}

	@Alpha
	@ConfigItem(
		position = 109,
		keyName = "vulnerableColor",
		name = "Vulnerable Color",
		description = "Color for vulnerable things",
		section = generalSection
	)
	default Color vulnerableColor()
	{
		return new Color(61, 241, 48, 224);
	}

	@Range(
		min = 1,
		max = 10
	)
	@ConfigItem(
		position = 110,
		keyName = "vulnerableWidth",
		name = "Vulnerable Width",
		description = "Size for outline",
		section = generalSection
	)
	default int vulnerableWidth()
	{
		return 3;
	}

	@ConfigItem(
		position = 111,
		keyName = "outlineFeather",
		name = "Outline feather",
		description = "Specify between 0-4 how much of the model outline should be faded",
		section = generalSection
	)
	@Range(
		max = 4
	)
	default int outlineFeather()
	{
		return 4;
	}

	@ConfigItem(
		position = 112,
		keyName = "indicateInvulnerableNexTicks",
		name = "Invulnerable Nex Ticks",
		description = "if we know how many ticks before shes clickable",
		section = generalSection
	)
	default boolean indicateInvulnerableNexTicks()
	{
		return true;
	}

	@Range(
		min = 6,
		max = 72
	)
	@ConfigItem(
		position = 113,
		keyName = "indicateInvulnerableNexTicksFontSize",
		name = "Ticks Font Size",
		description = "size of le font",
		hidden = true,
		unhide = "indicateInvulnerableNexTicks",
		section = generalSection
	)
	default int indicateInvulnerableNexTicksFontSize()
	{
		return 18;
	}

	@ConfigItem(
		position = 114,
		keyName = "drawNexHp",
		name = "Nex HP %",
		description = "Draw nex HP per phase",
		section = generalSection
	)
	default boolean drawNexHp()
	{
		return true;
	}

	@ConfigItem(
		position = 115,
		keyName = "drawMinionHP",
		name = "Minion HP %",
		description = "Draw minion HP",
		section = generalSection
	)
	default boolean drawMinionHP()
	{
		return true;
	}

	@Range(
		min = 6,
		max = 72
	)
	@ConfigItem(
		position = 116,
		keyName = "drawNexHpFontSize",
		name = "HP Font Size",
		description = "size of next hp",
		hidden = true,
		unhide = "drawNexHp",
		section = generalSection
	)
	default int drawNexHpFontSize()
	{
		return 12;
	}

	@ConfigItem(
		position = 117,
		keyName = "indicateTank",
		name = "Indicate Tank",
		description = "Highlight the tile of the current tank",
		section = generalSection
	)
	default boolean indicateTank()
	{
		return true;
	}

	@Alpha
	@ConfigItem(
		position = 118,
		keyName = "tankOtherColor",
		name = "Tank Color",
		description = "Color for tank tile when it is not you",
		hidden = true,
		unhide = "indicateTank",
		section = generalSection
	)
	default Color tankOtherColor()
	{
		return new Color(192, 192, 192, 200);
	}

	@Alpha
	@ConfigItem(
		position = 119,
		keyName = "tankOtherColorMe",
		name = "Tank Color Personal",
		description = "Color for tank tile when it is you",
		hidden = true,
		unhide = "indicateTank",
		section = generalSection
	)
	default Color tankOtherColorMe()
	{
		return new Color(48, 164, 255, 200);
	}

	@ConfigItem(
		position = 120,
		keyName = "indicateNexRange",
		name = "Indicate Nex Range",
		description = "Highlight the tiles where standing on or inside will allow nex to deal damage to you.",
		section = generalSection
	)
	default boolean indicateNexRange()
	{
		return false;
	}

	@Alpha
	@ConfigItem(
		position = 121,
		keyName = "indicateNexRangeColor",
		name = "Range Color",
		description = "Color for the tiles",
		hidden = true,
		unhide = "indicateNexRange",
		section = generalSection
	)
	default Color indicateNexRangeColor()
	{
		return new Color(0, 0, 0, 50);
	}

	@ConfigItem(
		keyName = "drawDashLane",
		name = "Dash Lane Highlight",
		description = "Draw dash lane aoe",
		position = 122,
		section = generalSection
	)
	default boolean drawDashLane()
	{
		return true;
	}

	@ConfigItem(
		keyName = "drawTicksOnDash",
		name = "Dash Ticks",
		description = "Draw ticks on ice trap",
		position = 123,
		hidden = true,
		unhide = "drawDashLane",
		section = generalSection
	)
	default boolean drawTicksOnDash()
	{
		return true;
	}

	@Alpha
	@ConfigItem(
		keyName = "drawDashLaneColor",
		name = "Dash Color",
		description = "Color ya idiot",
		position = 124,
		hidden = true,
		unhide = "drawDashLane",
		section = generalSection
	)
	default Color drawDashLaneColor()
	{
		return new Color(255, 0, 98, 100);
	}

	@ConfigSection(
		name = "Smoke Settings",
		description = "Configure settings for the virus",
		position = 200,
		keyName = "shadowSection"
	)
	String smokeSection = "Smoke Phase";

	@ConfigItem(
		keyName = "hideHealthyPlayers",
		name = "Hide healthy players",
		description = "Hides all players that are not coughing",
		position = 201,
		section = smokeSection
	)
	default boolean hideHealthyPlayers()
	{
		return false;
	}

	@Range(
		max = 60,
		min = 1
	)
	@ConfigItem(
		keyName = "hideHealthyAbove",
		name = "Hide above #",
		description = "Hides all players when more than set number are in room",
		position = 202,
		section = smokeSection,
		hidden = true,
		unhide = "hideHealthyPlayers"
	)
	default int hideAboveNumber()
	{
		return 12;
	}

	@ConfigItem(
		keyName = "coughTileIndicator",
		name = "Cough tiles highlight",
		description = "Shows the tiles that will make you sick",
		position = 203,
		section = smokeSection
	)
	default boolean coughTileIndicator()
	{
		return true;
	}

	@Range(
		max = 20,
		min = 1
	)
	@ConfigItem(
		keyName = "coughTileRenderDistance",
		name = "Render Distance",
		description = "Render cough distance in tiles from your player",
		position = 204,
		section = smokeSection
	)
	@Units("tiles")
	default int coughTileRenderDistance()
	{
		return 8;
	}

	@Alpha
	@ConfigItem(
		keyName = "coughColorBase",
		name = "Cough Color",
		description = "Color for sickness highlight",
		position = 205,
		section = smokeSection
	)
	default Color coughColorBase()
	{
		return new Color(255, 0, 98, 100);
	}

	@ConfigItem(
		keyName = "healthyTileIndicator",
		name = "Healthy tiles highlight",
		description = "Shows the tiles that will spread the sick when you have it",
		position = 206,
		section = smokeSection
	)
	default boolean healthyTileIndicator()
	{
		return true;
	}

	@Range(
		max = 20,
		min = 1
	)
	@ConfigItem(
		keyName = "healthyTileRenderDistance",
		name = "Render Distance",
		description = "Render healthy distance in tiles from your player",
		position = 207,
		section = smokeSection
	)
	@Units("tiles")
	default int healthyTileRenderDistance()
	{
		return 8;
	}

	@Alpha
	@ConfigItem(
		keyName = "healthColorBase",
		name = "Health Color",
		description = "Color for healthy highlight",
		position = 208,
		section = smokeSection
	)
	default Color healthColorBase()
	{
		return new Color(0, 255, 255, 100);
	}

	@ConfigSection(
		name = "Shadow Phase",
		description = "Configure Shadow Phase settings.",
		position = 300,
		keyName = "shadowSection"
	)
	String shadowSection = "Shadow Phase";

	@ConfigItem(
		keyName = "shadowsIndicator",
		name = "Shadows highlight",
		description = "Shows the ticks until portals will damage you",
		position = 301,
		section = shadowSection
	)
	default boolean shadowsIndicator()
	{
		return true;
	}

	@Range(
		max = 20,
		min = 1
	)
	@ConfigItem(
		keyName = "shadowsRenderDistance",
		name = "Render Distance",
		description = "Render shadows distance in tiles from your player",
		position = 302,
		section = shadowSection
	)
	@Units("tiles")
	default int shadowsRenderDistance()
	{
		return 6;
	}

	@ConfigItem(
		keyName = "shadowsTickCounter",
		name = "Shadows Tick Counter",
		description = "Displays the number of ticks until shadows do damage",
		position = 303,
		section = shadowSection
	)
	default boolean shadowsTickCounter()
	{
		return true;
	}

	@Alpha
	@ConfigItem(
		keyName = "shadowsColorBase",
		name = "Shadows Color",
		description = "Color the area highlighted by shadows",
		position = 304,
		section = shadowSection
	)
	default Color shadowsColorBase()
	{
		return new Color(0, 255, 255, 100);
	}

	@ConfigItem(
		keyName = "shadowStandingFlash",
		name = "Flash if danger",
		description = "will flash with shadow color if they spawn and you are in danger",
		position = 305,
		section = shadowSection
	)
	default boolean shadowStandingFlash()
	{
		return false;
	}

	@ConfigSection(
		name = "Blood Phase",
		description = "Configure Blood Phase settings.",
		position = 400,
		keyName = "iceSection"
	)
	String bloodSection = "Blood Phase";

	@ConfigItem(
		position = 401,
		keyName = "indicateSacrificeAOE",
		name = "Sacrifice Safe Line",
		description = "Highlight the tiles where you wont heal nex. please report back any bugs",
		section = bloodSection
	)
	default boolean indicateSacrificeAOE()
	{
		return true;
	}

	@Alpha
	@ConfigItem(
		position = 402,
		keyName = "indicateSacrificeAOEColor",
		name = "Safe Color",
		description = "Area where sacrafice will heal nex",
		hidden = true,
		unhide = "indicateSacrificeAOE",
		section = bloodSection
	)
	default Color indicateSacrificeAOEColor()
	{
		return new Color(0, 255, 119, 100);
	}

	@ConfigSection(
		name = "Ice Phase",
		description = "Configure Ice Phase settings.",
		position = 500,
		keyName = "iceSection"
	)
	String iceSection = "Ice Phase";

	@ConfigItem(
		keyName = "drawIceTraps",
		name = "Ice Trap Highlight",
		description = "Draw ice trap",
		position = 501,
		section = iceSection
	)
	default boolean drawIceTraps()
	{
		return true;
	}

	@ConfigItem(
		keyName = "drawTicksOnIceTrap",
		name = "Ice Trap Ticks",
		description = "Draw ticks on ice trap",
		position = 502,
		section = iceSection
	)
	default boolean drawTicksOnIceTrap()
	{
		return true;
	}

	@Alpha
	@ConfigItem(
		keyName = "iceColorBase",
		name = "Ice Color",
		description = "Color the area highlighted by ice",
		position = 503,
		section = iceSection
	)
	default Color iceColorBase()
	{
		return new Color(255, 0, 98, 100);
	}

	@ConfigItem(
		position = 504,
		keyName = "indicateContainAOE",
		name = "Indicate Contain This!",
		description = "Highlight the tiles where you gunna get smacked",
		section = iceSection
	)
	default boolean indicateContainAOE()
	{
		return true;
	}

	@ConfigItem(
		keyName = "drawTicksOnContain",
		name = "Contain Trap Ticks",
		description = "Draw ticks on ice trap",
		position = 505,
		section = iceSection
	)
	default boolean drawTicksOnContain()
	{
		return true;
	}

	@Alpha
	@ConfigItem(
		position = 506,
		keyName = "indicateContainAOEColor",
		name = "Contain AOE Color",
		description = "Stop reading these",
		hidden = true,
		unhide = "indicateContainAOE",
		section = iceSection
	)
	default Color indicateContainAOEColor()
	{
		return new Color(255, 0, 98, 100);
	}


	@ConfigSection(
		name = "Zaros Phase",
		description = "Configure Zaros Phase settings.",
		position = 600,
		keyName = "zarosSection"
	)
	String zarosSection = "Zaros Phase";

	@ConfigItem(
		position = 601,
		keyName = "indicateDeathAOE",
		name = "Indicate Death AOE",
		description = "Highlight the tiles where you gunna get smacked",
		section = zarosSection
	)
	default boolean indicateDeathAOE()
	{
		return true;
	}

	@Alpha
	@ConfigItem(
		position = 602,
		keyName = "indicateDeathAOEColor",
		name = "Death AOE Color",
		description = "Stop reading these",
		hidden = true,
		unhide = "indicateDeathAOE",
		section = zarosSection
	)
	default Color indicateDeathAOEColor()
	{
		return new Color(255, 0, 98, 100);
	}

	@ConfigItem(
		position = 603,
		keyName = "indicateTankSwitchTicks",
		name = "Ticks: Tank Switch",
		description = "countdown until tank switches",
		section = zarosSection
	)
	default boolean indicateTankSwitchTicks()
	{
		return true;
	}

	/* disabled until we know how it works

	@ConfigItem(
		position = 604,
		keyName = "indicatePraySwitchTicks",
		name = "Ticks: Prayer Switch",
		description = "countdown until nex's prayer switches",
		section = zarosSection
	)
	default boolean indicatePraySwitchTicks()
	{
		return true;
	}

	 */

	@Range(
		min = 4,
		max = 100
	)
	@ConfigItem(
		position = 605,
		keyName = "counterZOffset",
		name = "Z offset",
		description = "how far down to render below hp (if hp is present)",
		section = zarosSection
	)
	default int counterZOffset()
	{
		return 16;
	}

	enum PrayerDisplay
	{
		PRAYER_TAB,
		BOTTOM_RIGHT,
		BOTH;

		public boolean showInfoBox()
		{
			switch (this)
			{
				case BOTTOM_RIGHT:
				case BOTH:
					return true;
				default:
					return false;
			}
		}

		public boolean showWidgetHelper()
		{
			switch (this)
			{
				case PRAYER_TAB:
				case BOTH:
					return true;
				default:
					return false;
			}
		}
	}

	enum VulnerabilityDisplay
	{
		VULNERABLE,
		INVULNERABLE,
		BOTH,
		NONE;

		public boolean showVulnerable()
		{
			switch (this)
			{
				case BOTH:
				case VULNERABLE:
					return true;
				default:
					return false;
			}
		}

		public boolean showInvulnerable()
		{
			switch (this)
			{
				case BOTH:
				case INVULNERABLE:
					return true;
				default:
					return false;
			}
		}
	}
}
