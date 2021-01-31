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
			keyName = "hideNylo",
			name = "Hide Nylo",
			description = "Hides nylo when killed"
	)
	default boolean hideNylo()
	{
		return true;
	}

	@ConfigItem(
			position = 2,
			keyName = "maidenMarkers",
			name = "Maiden Markers",
			description = "Maiden Outline"
	)
	default boolean maidenMarkers()
	{
		return false;
	}


}
