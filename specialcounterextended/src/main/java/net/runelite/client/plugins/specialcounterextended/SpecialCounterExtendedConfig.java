package net.runelite.client.plugins.specialcounterextended;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

import java.awt.*;

@ConfigGroup("SpecialCounterExtendedConfig")
public interface SpecialCounterExtendedConfig extends Config {

    @ConfigItem(
            position = 0,
            keyName = "showHitOverlay",
            name = "Hit Overlay",
            description = "Show the special attack overlay."
    )
    default boolean showHitOverlay() {
        return true;
    }

    @ConfigItem(
            position = 1,
            keyName = "getFadeDelay",
            name = "Fade Delay",
            description = "Delay, in milliseconds, until the icon disappears."
    )
    default int getFadeDelay() {
        return 5000;
    }

    @ConfigItem(
            position = 2,
            keyName = "getMaxHeight",
            name = "Travel Height",
            description = "Maximum height, in pixels, for the icon to travel."
    )
    default int getMaxHeight() {
        return 200;
    }
}
