package net.runelite.client.plugins.socketdeathindicator;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("socketdeathindicators")
public interface SocketDeathIndicatorsConfig extends Config
{

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

	@ConfigItem(
			position = 1,
			keyName = "showOutline",
			name = "Show outline",
			description = "Shows outline when killed"
	)
	default boolean showOutline()
	{
		return false;
	}

	@ConfigItem(
			position = 2,
			keyName = "hideNylo",
			name = "Hide Nylo",
			description = "Hides nylo when killed"
	)
	default boolean hideNylo()
	{
		return true;
	}

	@ConfigItem(
			position = 3,
			keyName = "hideNyloMenuEntries",
			name = "Hide Nylo Menu Entries",
			description = "Hides attack option on Nylos when they die"
	)
	default boolean hideNyloMenuEntries()
	{
		return false;
	}

	@ConfigItem(
			position = 4,
			keyName = "maidenMarkers",
			name = "Maiden Markers",
			description = "Maiden Outline"
	)
	default boolean maidenMarkers()
	{
		return false;
	}


}
