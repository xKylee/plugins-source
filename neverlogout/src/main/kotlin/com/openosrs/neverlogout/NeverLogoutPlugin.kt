package com.openosrs.neverlogout

import net.runelite.api.Client
import net.runelite.api.events.GameTick
import net.runelite.client.eventbus.Subscribe
import net.runelite.client.plugins.Plugin
import net.runelite.client.plugins.PluginDescriptor
import net.runelite.client.plugins.PluginType
import org.pf4j.Extension
import javax.inject.Inject

@Extension
@PluginDescriptor(
        name = "Never Logout",
        enabledByDefault = false,
        description = "Overrides the 5 min afk logout timer",
        tags = ["never log", "idle", "logout", "log"],
        type = PluginType.MISCELLANEOUS)
class NeverLogoutPlugin : Plugin() {

    @Inject
    lateinit var client: Client

    @Subscribe
    fun onGameTick(event: GameTick) {
        if (!isLoggedIn()) return
        client.run {
            keyboardIdleTicks = 0
            mouseIdleTicks = 0
        }
    }
    private fun isLoggedIn() = client.localPlayer != null
}
