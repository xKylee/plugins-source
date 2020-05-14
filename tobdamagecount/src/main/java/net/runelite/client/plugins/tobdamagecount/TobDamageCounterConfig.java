package net.runelite.client.plugins.tobdamagecount;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("tobdamagecounter")
public interface TobDamageCounterConfig extends Config
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
		keyName = "healCount",
		name = "Show Heal Count",
		description = "Show the total amount healed to each boss on the overlay."
	)
	default boolean showHealCount()
	{
		return true;
	}

	@ConfigItem(
		keyName = "damageSummary",
		name = "Print Raid Summary",
		description = "Print the damage of all the rooms when the raid ends."
	)
	default boolean showDamageSummary()
	{
		return true;
	}

	@ConfigItem(
		keyName = "leechMessages",
		name = "Leech Messages",
		description = "Print messages when a player leeches"
	)
	default boolean showLeechMessages()
	{
		return true;
	}

	@ConfigItem(
		keyName = "leechOverlay",
		name = "Leech Overlay",
		description = "Show leech counts on the overlay"
	)
	default boolean showLeechOverlay()
	{
		return true;
	}
}
