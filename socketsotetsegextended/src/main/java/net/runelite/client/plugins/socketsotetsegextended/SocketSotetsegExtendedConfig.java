package net.runelite.client.plugins.socketsotetsegextended;

import java.awt.Color;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("SocketSotetsegExtended")
public interface SocketSotetsegExtendedConfig extends Config
{
	@ConfigItem(
			position = 0,
			keyName = "numbersOn",
			name = "Add numbers",
			description = "Adds number overlay to tiles you need to click"
	)
	default boolean numbersOn()
	{
		return true;
	}

	@ConfigItem(
			position = 1,
			keyName = "getFontSize",
			name = "Font Size",
			description = "Size of font for numbers"
	)
	default int getFontSize()
	{
		return 10;
	}

	@ConfigItem(
			position = 2,
			keyName = "highlightTiles",
			name = "Add outline",
			description = "Adds tile highlight to tiles you need to click"
	)
	default boolean highlightTiles()
	{
		return true;
	}

	@ConfigItem(
			position = 3,
			keyName = "getHighlightTileOutline",
			name = "Tile Outline Color",
			description = "The color of the outline of the highlighted tiles"
	)
	default Color getHighlightTileOutline()
	{
		return Color.GREEN;
	}

	@ConfigItem(
			position = 4,
			keyName = "showDPSSplits",
			name = "Show between maze splits",
			description = "Shows time between mazes"
	)
	default boolean showBetweenSplits()
	{
		return true;
	}

	@ConfigItem(
			position = 5,
			keyName = "showMazeSplits",
			name = "Show maze splits",
			description = "Shows maze splits"
	)
	default boolean showMazeSplits()
	{
		return true;
	}

	@ConfigItem(
			position = 6,
			keyName = "showDetailedSplits",
			name = "Show detailed splits",
			description = "Adds extra information to splits"
	)
	default boolean showDetailedSplits()
	{
		return true;
	}

	@ConfigItem(
			position = 7,
			keyName = "splitMessageColor",
			name = "Time splits message color",
			description = "Color of splits in chat box"
	)
	default Color getSplitsMessageColor()
	{
		return Color.decode("222222");
	}

	@ConfigItem(
			position = 8,
			name = "Mirror Mode Compatibility?",
			keyName = "mirrorMode",
			description = "Should we show the overlay on Mirror Mode?"
	)
	default boolean mirrorMode()
	{
		return false;
	}

}
