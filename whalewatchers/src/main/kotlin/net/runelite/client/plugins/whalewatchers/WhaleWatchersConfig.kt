package net.runelite.client.plugins.whalewatchers

import net.runelite.client.config.Config
import net.runelite.client.config.ConfigGroup
import net.runelite.client.config.ConfigItem

@ConfigGroup("WhaleWatchers")
interface WhaleWatchersConfig : Config {
    @ConfigItem(
            position = 1,
            keyName = "protectItemWarning",
            name = "Protect Item Warning",
            description = "Warns you when you are skulled and don't have protect item turned on."
    )
    fun protectItemWarning(): Boolean {
        return false
    }

    @ConfigItem(
            position = 2,
            keyName = "lessObnoxiousProtWarning",
            name = "Less Obnoxious Protect Item Warning",
            description = "Replaces the overlay with thick border and text with a less obtrusive overlay with a thin border and no text"
    )
    fun lessObnoxiousProtWarning(): Boolean {
        return false
    }

    @ConfigItem(
            position = 3,
            keyName = "smiteableWarning",
            name = "Smite Warning",
            description = "Displays a warning overlay when your prayer is at a smiteable level"
    )
    fun smiteableWarning(): Boolean {
        return true
    }

    @ConfigItem(
            position = 4,
            keyName = "gloryWarning",
            name = "Glory Warning",
            description = "Displays a warning box while you are wearing an uncharged glory"
    )
    fun gloryWarning(): Boolean {
        return true
    }
}