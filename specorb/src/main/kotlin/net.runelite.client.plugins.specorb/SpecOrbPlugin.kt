package net.runelite.client.plugins.specorb

import net.runelite.api.Client
import net.runelite.api.MenuOpcode
import net.runelite.api.events.ClientTick
import net.runelite.api.events.MenuEntryAdded
import net.runelite.api.widgets.WidgetInfo
import net.runelite.client.eventbus.Subscribe
import net.runelite.client.plugins.Plugin
import net.runelite.client.plugins.PluginDescriptor
import net.runelite.client.plugins.PluginType
import org.pf4j.Extension
import java.awt.Point
import javax.inject.Inject

@Extension
@PluginDescriptor(
		name = "Spec Orb",
		enabledByDefault = false,
		description = "Make the special attack orb work everywhere with all weapons",
		type = PluginType.MISCELLANEOUS
)
class SpecOrbPlugin : Plugin() {

	@Inject
	private lateinit var client: Client

	@Subscribe
	private fun onMenuEntryAdded(event: MenuEntryAdded) {
		if (event.menuOpcode == MenuOpcode.CC_OP && event.param1 == WidgetInfo.MINIMAP_SPEC_CLICKBOX.id) {
			client.menuOptionCount = client.menuOptionCount - 1
		}
	}

	@Subscribe
	private fun onClientTick(event: ClientTick) {
		if (client.getWidget(WidgetInfo.MINIMAP_SPEC_CLICKBOX)?.bounds?.contains(Point(client.mouseCanvasPosition.x, client.mouseCanvasPosition.y)) == true)
			client.insertMenuItem("Use <col=00ff00>Special Attack</col>", "", MenuOpcode.CC_OP.id, 1, -1, WidgetInfo
					.COMBAT_SPECIAL_ATTACK_CLICKBOX.id, false)
	}
}