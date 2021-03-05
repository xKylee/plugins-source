package net.runelite.client.plugins.spawntimer;

import java.awt.Color;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("spawntimer")
public interface SpawnTimerConfig extends Config
{
	@ConfigSection(
		keyName = "npcsTitle",
		position = 1,
		name = "NPCs",
		description = ""
	)
	String npcsTitle = "NPCs";

	@ConfigItem(
		position = 2,
		keyName = "npcToHighlight",
		name = "NPCs to show timer for",
		description = "List of NPC names to show timer for",
		section = npcsTitle
	)
	default String getNpcToHighlight()
	{
		return "";
	}

	@ConfigSection(
		keyName = "colorsTitle",
		position = 3,
		name = "Colors",
		description = ""
	)
	String colorsTitle = "Colors";

	@ConfigItem(
		position = 4,
		keyName = "npcColor",
		name = "Text Color",
		description = "Color of the NPC timer",
		section = colorsTitle
	)
	default Color getHighlightColor()
	{
		return Color.RED;
	}
}