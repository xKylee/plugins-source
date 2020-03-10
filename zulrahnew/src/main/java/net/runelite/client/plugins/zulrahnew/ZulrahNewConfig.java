package net.runelite.client.plugins.zulrahnew;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("zulrahnew")
public interface ZulrahNewConfig extends Config
{
	@ConfigItem(
		position = 1,
		keyName = "zulrahprayenable",
		name = "Show Prayer Helper",
		description = "Configures whether or not to show when to pray at Zulrah."
	)
	default boolean EnableZulrahPrayerHelper()
	{
		return true;
	}

	@ConfigItem(
		position = 2,
		keyName = "jadphasehelper",
		name = "Jad Phase Helper",
		description = "Tells you what to pray against Zulrah jad phase"
	)
	default boolean ZulrahJadHelper()
	{
		return true;
	}
}