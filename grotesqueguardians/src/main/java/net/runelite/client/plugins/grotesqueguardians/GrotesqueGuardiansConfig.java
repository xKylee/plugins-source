package net.runelite.client.plugins.grotesqueguardians;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("grotesqueguardians")
public interface GrotesqueGuardiansConfig extends Config
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
