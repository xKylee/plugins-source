package net.runelite.client.plugins.socketdeathindicator;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("socketdeathindicators")
public interface SocketDeathIndicatorsConfig extends Config
{
	@ConfigItem(
			position = 0,
			keyName = "showOutline",
			name = "Show outline",
			description = "Shows outline when killed"
	)
	default boolean showOutline()
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
