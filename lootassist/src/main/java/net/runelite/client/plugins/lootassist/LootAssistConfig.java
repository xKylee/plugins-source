package net.runelite.client.plugins.lootassist;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("lootassist")
public interface LootAssistConfig extends Config
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
}
