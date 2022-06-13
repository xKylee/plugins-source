package net.runelite.client.plugins.socketchat;

import net.runelite.client.config.Range;
import java.awt.Color;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Keybind;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.Config;

@ConfigGroup("Socket Chat")
public interface SocketChatConfig extends Config
	{
	@ConfigItem(
			keyName = "hotkey",
			name = "Chat Toggle",
			description = "When you press this key it will toggle typing to socket",
			position = 0
	)
	default Keybind hotkey()
	{
		return new Keybind(192, 0);
	}

	@ConfigItem(
			keyName = "singleText",
			name = "Single Message",
			description = "Only sends one socket message before returning to normal chat",
			position = 1
	)
	default boolean singleText()
	{
		return false;
	}

	@ConfigItem(
			keyName = "overrideTradeButton",
			name = "Override Trade Button",
			description = "Uses trade button for socket chat",
			position = 2
	)
	default boolean overrideTradeButton()
	{
		return false;
	}

	@ConfigItem(
			keyName = "overrideSlash",
			name = "/ Bypasses socket chat",
			description = "Typing / before message will send it to cc even if socket chat is toggled.",
			position = 3
	)
	default boolean overrideSlash()
	{
		return false;
	}

	@ConfigItem(
			position = 4,
			keyName = "userNameColour",
			name = "Name colour",
			description = "Colour of the username text"
	)
	default Color getNameColor()
	{
		return Color.CYAN;
	}

	@ConfigItem(
			position = 5,
			keyName = "messageColour",
			name = "Message colour",
			description = "Colour of the message text"
	)
	default Color messageColor()
	{
		return Color.WHITE;
	}

	@ConfigItem(
			position = 6,
			keyName = "dateTimeColour",
			name = "DateTime colour",
			description = "Colour of the DateTime text"
	)
	default Color getDateTimeColor()
	{
		return Color.GREEN;
	}

	@Range(max = 380)
	@ConfigItem(
			position = 7,
			keyName = "chatIcon",
			name = "Chat Icon",
			description = "Select an Icon to appear before your name in socket chat"
	)
	default int getIcon()
	{
		return 0;
	}

	@ConfigItem(
			position = 8,
			keyName = "showTimeStamp",
			name = "Show Time",
			description = "Show time of the message"
	)
	default boolean getTimeStamp()
	{
		return true;
	}

	@ConfigItem(
			position = 9,
			keyName = "showDateStamp",
			name = "Show Date",
			description = "Show date of the message"
	)
	default boolean getDateStamp()
	{
		return false;
	}

	@ConfigItem(
			position = 10,
			keyName = "freedomUnits",
			name = "American Andys",
			description = "Display the date incorrectly for Americans"
	)
	default boolean getFreedomUnits()
	{
		return false;
	}

	@ConfigItem(
			position = 11,
			keyName = "overheadText",
			name = "Show Socket Messages Overhead",
			description = "Display socket messages as overhead chat messages"
	)
	default boolean overheadText()
	{
		return false;
	}

	@ConfigItem(
			position = 12,
			keyName = "hideLocalPlayerOverhead",
			name = "Overhead Text Exemption",
			description = "Hide Overhead text on your own player."
	)
	default boolean hideLocalPlayerOverhead()
	{
		return false;
	}

	@ConfigItem(
			position = 13,
			keyName = "splitSocketChat",
			name = "Split Chat",
			description = "Combine Socket Chat with private chat"
	)
	default boolean splitSocketChat()
	{
		return false;
	}

	@ConfigItem(
			position = 14,
			keyName = "showCustomMsg",
			name = "Show Custom Message",
			description = "A Custom Message that goes after your name")
	default String showCustomMsg()
	{
		return "";
	}

	@ConfigItem(
			position = 15,
			keyName = "chatnameFake",
			name = "Use Fake Name",
			description = "Uses a fake name for socket messages"
	)
	default String getNameFake()
	{
		return "";
	}
	}
