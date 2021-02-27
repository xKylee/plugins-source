/*
 * Copyright (c) 2018, Tomas Slusny <slusnucky@gmail.com>
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
package net.runelite.client.plugins.playerindicatorsextended;

import java.awt.Color;
import java.util.EnumSet;
import net.runelite.api.FriendsChatRank;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.ConfigTitle;

@ConfigGroup("playerindicators")
public interface PlayerIndicatorsExtendedConfig extends Config
{
	EnumSet<PlayerIndicatorsExtendedPlugin.PlayerIndicationLocation> defaultPlayerIndicatorMode = EnumSet.complementOf(EnumSet.of(PlayerIndicatorsExtendedPlugin.PlayerIndicationLocation.HULL));

	@ConfigSection(
		name = "Yourself",
		description = "",
		position = 0,
		keyName = "yourselfSection"
	)
	String yourselfSection = "Yourself";

	@ConfigSection(
		name = "Friends",
		description = "",
		position = 1,
		keyName = "friendsSection"
	)
	String friendsSection = "Friends";

	@ConfigSection(
		name = "Clan",
		description = "",
		position = 2,
		keyName = "friendsChatSection"
	)
	String friendsChatSection = "Clan";

	@ConfigSection(
		name = "Team",
		description = "",
		position = 3,
		keyName = "teamSection"
	)
	String teamSection = "Team";

	@ConfigSection(
		name = "Target",
		description = "",
		position = 4,
		keyName = "targetSection"
	)
	String targetSection = "Target";

	@ConfigSection(
		name = "Other",
		description = "",
		position = 5,
		keyName = "otherSection"
	)
	String otherSection = "Other";

	@ConfigSection(
		name = "Callers",
		description = "",
		position = 6,
		keyName = "callersSection"
	)
	String callersSection = "Callers";

	@ConfigSection(
		name = "Miscellaneous",
		description = "",
		position = 7,
		keyName = "miscellaneousSection"
	)
	String miscellaneousSection = "Miscellaneous";

	@ConfigItem(
		position = 0,
		keyName = "drawOwnName",
		name = "Highlight own player",
		description = "Configures whether or not your own player should be highlighted",
		section = yourselfSection
	)
	default boolean highlightOwnPlayer()
	{
		return false;
	}

	@ConfigItem(
		position = 1,
		keyName = "ownNameColor",
		name = "Own player color",
		description = "Color of your own player",
		section = yourselfSection
	)
	default Color getOwnPlayerColor()
	{
		return new Color(0, 184, 212);
	}

	@ConfigItem(
		position = 2,
		keyName = "selfIndicatorModes",
		name = "Indicator Mode",
		description = "Location(s) of the overlay",
		section = yourselfSection,
		enumClass = PlayerIndicatorsExtendedPlugin.PlayerIndicationLocation.class
	)
	default EnumSet<PlayerIndicatorsExtendedPlugin.PlayerIndicationLocation> selfIndicatorModes()
	{
		return defaultPlayerIndicatorMode;
	}

	@ConfigItem(
		position = 0,
		keyName = "drawFriendNames",
		name = "Highlight friends",
		description = "Configures whether or not friends should be highlighted",
		section = friendsSection
	)
	default boolean highlightFriends()
	{
		return false;
	}

	@ConfigItem(
		position = 1,
		keyName = "friendNameColor",
		name = "Friend color",
		description = "Color of friend names",
		section = friendsSection
	)
	default Color getFriendColor()
	{
		return new Color(0, 200, 83);
	}

	@ConfigItem(
		position = 2,
		keyName = "friendIndicatorMode",
		name = "Indicator Mode",
		description = "Location(s) of the overlay",
		section = friendsSection,
		enumClass = PlayerIndicatorsExtendedPlugin.PlayerIndicationLocation.class

	)
	default EnumSet<PlayerIndicatorsExtendedPlugin.PlayerIndicationLocation> friendIndicatorMode()
	{
		return defaultPlayerIndicatorMode;
	}

	@ConfigItem(
		position = 0,
		keyName = "highlightClan",
		name = "Highlight friends chat members",
		description = "Configures whether or friends chat members should be highlighted",
		section = friendsChatSection
	)
	default boolean highlightClan()
	{
		return true;
	}

	@ConfigItem(
		position = 1,
		keyName = "clanMemberColor",
		name = "Friends chat member color",
		description = "Color of friends chat members",
		section = friendsChatSection
	)
	default Color getFriendsChatColor()
	{
		return new Color(170, 0, 255);
	}

	@ConfigItem(
		position = 2,
		keyName = "clanIndicatorModes",
		name = "Indicator Mode",
		description = "Location(s) of the overlay",
		section = friendsChatSection,
		enumClass = PlayerIndicatorsExtendedPlugin.PlayerIndicationLocation.class

	)
	default EnumSet<PlayerIndicatorsExtendedPlugin.PlayerIndicationLocation> friendsChatIndicatorModes()
	{
		return defaultPlayerIndicatorMode;
	}

	@ConfigItem(
		position = 3,
		keyName = "clanMenuIcons",
		name = "Show friends chat ranks",
		description = "Add friends chat rank to right click menu and next to player names",
		section = friendsChatSection
	)
	default boolean showFriendsChatRanks()
	{
		return false;
	}

	@ConfigItem(
		position = 0,
		keyName = "drawTeamMemberNames",
		name = "Highlight team members",
		description = "Configures whether or not team members should be highlighted",
		section = teamSection
	)
	default boolean highlightTeamMembers()
	{
		return false;
	}

	@ConfigItem(
		position = 1,
		keyName = "teamMemberColor",
		name = "Team member color",
		description = "Color of team members",
		section = teamSection
	)
	default Color getTeamcolor()
	{
		return new Color(19, 110, 247);
	}

	@ConfigItem(
		position = 2,
		keyName = "teamIndicatorModes",
		name = "Indicator Mode",
		description = "Location(s) of the overlay",
		section = teamSection,
		enumClass = PlayerIndicatorsExtendedPlugin.PlayerIndicationLocation.class

	)
	default EnumSet<PlayerIndicatorsExtendedPlugin.PlayerIndicationLocation> teamIndicatorModes()
	{
		return defaultPlayerIndicatorMode;
	}

	@ConfigItem(
		position = 0,
		keyName = "drawTargetsNames",
		name = "Highlight attackable targets",
		description = "Configures whether or not attackable targets should be highlighted",
		section = targetSection
	)
	default boolean highlightTargets()
	{
		return false;
	}

	@ConfigItem(
		position = 1,
		keyName = "targetColor",
		name = "Target member color",
		description = "Color of attackable targets",
		section = targetSection
	)
	default Color getTargetsColor()
	{
		return new Color(19, 110, 247);
	}

	@ConfigItem(
		position = 2,
		keyName = "targetsIndicatorModes",
		name = "Indicator Mode",
		description = "Location(s) of the overlay",
		section = targetSection,
		enumClass = PlayerIndicatorsExtendedPlugin.PlayerIndicationLocation.class

	)
	default EnumSet<PlayerIndicatorsExtendedPlugin.PlayerIndicationLocation> targetsIndicatorModes()
	{
		return defaultPlayerIndicatorMode;
	}

	@ConfigItem(
		position = 3,
		keyName = "showAgility",
		name = "Show Agility Levels",
		description = "Show the agility level of attackable players next to their name while in the wilderness.",
		section = targetSection
	)
	default boolean showAgilityLevel()
	{
		return false;
	}

	@ConfigItem(
		position = 4,
		keyName = "agilityFormat",
		name = "Format",
		description = "Whether to show the agility level as text, or as icons (1 skull >= 1st threshold, 2 skulls >= 2nd threshold).",
		section = targetSection
	)
	default PlayerIndicatorsExtendedPlugin.AgilityFormats agilityFormat()
	{
		return PlayerIndicatorsExtendedPlugin.AgilityFormats.TEXT;
	}

	@ConfigItem(
		position = 5,
		keyName = "agilityFirstThreshold",
		name = "First Threshold",
		description = "When showing agility as icons, show one icon for agility >= this level.",
		section = targetSection
	)
	default int agilityFirstThreshold()
	{
		return 70;
	}

	@ConfigItem(
		position = 6,
		keyName = "agilitySecondThreshold",
		name = "Second Threshold",
		description = "When showing agility as icons, show two icons for agility >= this level.",
		section = targetSection
	)
	default int agilitySecondThreshold()
	{
		return 84;
	}

	@ConfigItem(
		position = 7,
		keyName = "playerSkull",
		name = "Show Skull Information",
		description = "shows",
		section = targetSection
	)
	default boolean playerSkull()
	{
		return false;
	}

	@ConfigItem(
		position = 8,
		keyName = "minimapSkullLocation",
		name = "Skull Icon Location",
		description = "The location of the skull icon for skulled players",
		section = targetSection
	)
	default PlayerIndicatorsExtendedPlugin.MinimapSkullLocations skullLocation()
	{
		return PlayerIndicatorsExtendedPlugin.MinimapSkullLocations.AFTER_NAME;
	}

	@ConfigItem(
		position = 9,
		keyName = "targetRisk",
		name = "Indicate Target Risk",
		description = "Indicates the risk (in K GP) of the target",
		section = targetSection
	)
	default boolean targetRisk()
	{
		return false;
	}

	@ConfigItem(
		position = 10,
		keyName = "showCombat",
		name = "Show Combat Levels",
		description = "Show the combat level of attackable players next to their name.",
		section = targetSection
	)
	default boolean showCombatLevel()
	{
		return false;
	}

	@ConfigItem(
		position = 0,
		keyName = "drawOtherPlayerNames",
		name = "Highlight other players",
		description = "Configures whether or not other players should be highlighted",
		section = otherSection
	)
	default boolean highlightOtherPlayers()
	{
		return false;
	}

	@ConfigItem(
		position = 1,
		keyName = "otherPlayerColor",
		name = "Other player color",
		description = "Color of other players' names",
		section = otherSection
	)
	default Color getOtherColor()
	{
		return Color.RED;
	}

	@ConfigItem(
		position = 2,
		keyName = "otherIndicatorModes",
		name = "Indicator Mode",
		description = "Location(s) of the overlay",
		section = otherSection,
		enumClass = PlayerIndicatorsExtendedPlugin.PlayerIndicationLocation.class

	)
	default EnumSet<PlayerIndicatorsExtendedPlugin.PlayerIndicationLocation> otherIndicatorModes()
	{
		return defaultPlayerIndicatorMode;
	}

	@ConfigTitle(
		keyName = "callerConfiguration",
		position = 0,
		name = "Caller Configuration",
		description = "",
		section = callersSection
	)
	String callerConfiguration = "Caller Configuration";

	@ConfigItem(
		position = 1,
		keyName = "highlightCallers",
		name = "Highlight Callers",
		description = "Highlights Callers Onscreen",
		section = callersSection,
		title = callerConfiguration
	)
	default boolean highlightCallers()
	{
		return false;
	}

	@ConfigItem(
		position = 2,
		keyName = "useClanchatRanks",
		name = "Use Ranks as Callers",
		description = "Uses friends chat ranks as the list of callers",
		section = callersSection,
		title = callerConfiguration
	)
	default boolean useClanchatRanks()
	{
		return false;
	}

	@ConfigItem(
		position = 3,
		keyName = "callerRank",
		name = "Minimum rank for friends chat Caller",
		description = "Chooses the minimum rank to use as friends chat callers.",
		section = callersSection,
		title = callerConfiguration
	)
	default FriendsChatRank callerRank()
	{
		return FriendsChatRank.CAPTAIN;
	}

	@ConfigItem(
		position = 4,
		keyName = "callers",
		name = "List of callers to highlight",
		description = "Highlights callers, only highlights one at a time. Separate each entry with a comma and enter" +
			" in the order you want them highlighted.",
		section = callersSection,
		title = callerConfiguration
	)
	default String callers()
	{
		return " ";
	}

	@ConfigTitle(
		keyName = "callerIndicators",
		position = 5,
		name = "Caller Indicators",
		description = "",
		section = callersSection
	)
	String callerIndicators = "Caller Indicators";

	@ConfigItem(
		position = 6,
		keyName = "callerColor",
		name = "Caller Color",
		description = "Color of Indicated Callers",
		section = callersSection,
		title = callerIndicators
	)
	default Color callerColor()
	{
		return Color.WHITE;
	}

	@ConfigItem(
		position = 7,
		keyName = "callerHighlightOptions",
		name = "Caller indication methods",
		description = "Location(s) of the overlay",
		section = callersSection,
		title = callerIndicators,
		enumClass = PlayerIndicatorsExtendedPlugin.PlayerIndicationLocation.class
	)
	default EnumSet<PlayerIndicatorsExtendedPlugin.PlayerIndicationLocation> callerHighlightOptions()
	{
		return defaultPlayerIndicatorMode;
	}

	@ConfigTitle(
		keyName = "callerTargetIndicators",
		position = 8,
		name = "Caller Target Indicators",
		description = "",
		section = callersSection
	)
	String callerTargetIndicators = "Caller Target Indicators";

	@ConfigItem(
		position = 9,
		keyName = "callersTargets",
		name = "Calllers' targets",
		description = "Highlights the targets of callers",
		section = callersSection,
		title = callerTargetIndicators
	)
	default boolean callersTargets()
	{
		return true;
	}

	@ConfigItem(
		position = 10,
		keyName = "callerTargetColor",
		name = "Callers' targets color",
		description = "Color of the the targets of callers",
		section = callersSection,
		title = callerTargetIndicators
	)
	default Color callerTargetColor()
	{
		return Color.WHITE.darker();
	}

	@ConfigItem(
		position = 11,
		keyName = "callerTargetHighlightOptions",
		name = "Pile indication methods",
		description = "How to highlight the callers' target",
		section = callersSection,
		title = callerTargetIndicators,
		enumClass = PlayerIndicatorsExtendedPlugin.PlayerIndicationLocation.class
	)
	default EnumSet<PlayerIndicatorsExtendedPlugin.PlayerIndicationLocation> callerTargetHighlightOptions()
	{
		return defaultPlayerIndicatorMode;
	}

	@ConfigItem(
		position = 0,
		keyName = "unchargedGlory",
		name = "Uncharged Glory Indication",
		description = "Indicates if players have an uncharged glory (this only works if the above head indicator is selected)",
		section = miscellaneousSection
	)
	default boolean unchargedGlory()
	{
		return false;
	}

}
