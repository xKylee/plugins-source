package net.runelite.client.plugins.fightcavespawnrotation;

import java.awt.Color;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;
import net.runelite.client.config.Units;

@ConfigGroup("fightcavespawnrotation")
public interface FightCavesSpawnRotationConfig extends Config
{
	@ConfigItem(
		name = "Outside Caves Rotation Info",
		keyName = "outsideCavesInfoOverlay",
		description = "Displays information about the current/upcoming rotation outside of the fight caves",
		position = 0
	)
	default boolean outsideCavesInfoOverlay()
	{
		return false;
	}

	@ConfigItem(
		name = "Spawn Display Mode",
		keyName = "displayMode",
		description = "",
		position = 1
	)
	default FightCavesSpawnRotationConfig.DisplayMode displayMode()
	{
		return FightCavesSpawnRotationConfig.DisplayMode.OFF;
	}

	@Range(
		max = 3,
		min = 1
	)
	@Units("px")
	@ConfigItem(
		name = "Overlay Stroke Size",
		keyName = "overlayStrokeSize",
		description = "",
		position = 2
	)
	default int overlayStrokeSize()
	{
		return 2;
	}

	@ConfigItem(
		name = "Color Spawn Overlays",
		keyName = "colorSpawnOverlays",
		description = "Color the spawn overlays inside the Fight Caves.",
		position = 3
	)
	default boolean colorSpawnOverlays()
	{
		return false;
	}

	@ConfigItem(
		name = "Current Wave Color",
		keyName = "currentWaveColor",
		description = "",
		position = 4,
		hidden = true,
		unhide = "colorSpawnOverlays"
	)
	@Alpha
	default Color currentWaveColor()
	{
		return Color.WHITE;
	}

	@ConfigItem(
		name = "Next Wave Color",
		keyName = "nextWaveColor",
		description = "",
		position = 5,
		hidden = true,
		unhide = "colorSpawnOverlays"
	)
	@Alpha
	default Color nextWaveColor()
	{
		return Color.GREEN;
	}

	@Getter
	@AllArgsConstructor
	enum DisplayMode
	{
		OFF("Off"),
		CURRENT_WAVE("Current"),
		NEXT_WAVE("Next"),
		BOTH("Both");

		private final String name;

		@Override
		public String toString()
		{
			return getName();
		}
	}
}