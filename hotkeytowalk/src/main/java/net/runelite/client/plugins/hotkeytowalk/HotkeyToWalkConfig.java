package net.runelite.client.plugins.hotkeytowalk;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Keybind;

@ConfigGroup("hotkeyToWalk")
public interface HotkeyToWalkConfig extends Config
{
	@ConfigItem(
		keyName = "hotkeyToWalk",
		name = "Hotkey",
		description = "",
		position = 0
	)
	default Keybind hotkeyToWalk()
	{
		return Keybind.NOT_SET;
	}
}
