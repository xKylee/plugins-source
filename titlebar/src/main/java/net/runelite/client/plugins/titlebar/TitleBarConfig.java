package net.runelite.client.plugins.titlebar;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("titlebar")
public interface TitleBarConfig extends Config
{
	@ConfigItem(keyName = "RuneLite", name = "RuneLite", description = "RuneLite title bar", position = 1)
	default boolean runeliteTitleBar()
	{
		return true;
	}
}