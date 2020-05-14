package net.runelite.client.plugins.dropparty;

import java.awt.Color;
import java.awt.Font;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;
import net.runelite.client.config.Units;

@ConfigGroup("dropparty")
public interface DropPartyConfig extends Config
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
		keyName = "playerName",
		name = "Dropping player",
		description = "selects what players name to mark tiles",
		position = 0
	)
	default String playerName()
	{
		return "";
	}

	@Range(
		min = 1,
		max = 60
	)
	@ConfigItem(
		keyName = "showAmount",
		name = "Trail length",
		description = "Shows the length of the droppers trail",
		position = 1
	)
	default int showAmount()
	{
		return 10;
	}

	@ConfigItem(
		position = 2,
		keyName = "overlayColor",
		name = "Overlay Color",
		description = "Configures the color of the overlay"
	)
	default Color overlayColor()
	{
		return new Color(0, 150, 200);
	}

	@ConfigItem(
		position = 3,
		keyName = "fontStyle",
		name = "Font Style",
		description = "Bold/Italics/Plain"
	)
	default FontStyle fontStyle()
	{
		return FontStyle.BOLD;
	}

	@Range(
		min = 10,
		max = 40
	)
	@ConfigItem(
		position = 4,
		keyName = "textSize",
		name = "Text Size",
		description = "Text Size for Timers."
	)
	@Units(Units.POINTS)
	default int textSize()
	{
		return 18;
	}

	@Getter
	@AllArgsConstructor
	enum FontStyle
	{
		BOLD("Bold", Font.BOLD),
		ITALIC("Italic", Font.ITALIC),
		PLAIN("Plain", Font.PLAIN);

		private String name;
		private int font;

		@Override
		public String toString()
		{
			return getName();
		}
	}
}
