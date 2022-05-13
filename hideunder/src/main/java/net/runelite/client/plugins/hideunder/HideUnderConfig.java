package net.runelite.client.plugins.hideunder;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("hideunder")
public interface HideUnderConfig extends Config
{

	@ConfigItem(
		position = 0,
		keyName = "renderMethod",
		name = "Render Method",
		description = "choose between Render Self and Entity Hider."
	)
	default hideUnderEnum renderMethod()
	{
		return hideUnderEnum.RENDER_SELF;
	}

	@Getter
	@AllArgsConstructor
	enum hideUnderEnum
	{
		RENDER_SELF("Render Self"),
		ENTITY_HIDER("Entity Hider");

		private final String name;

		@Override
		public String toString()
		{
			return getName();
		}
	}
}
