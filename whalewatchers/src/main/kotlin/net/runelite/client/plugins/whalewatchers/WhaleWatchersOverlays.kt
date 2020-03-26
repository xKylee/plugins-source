package net.runelite.client.plugins.whalewatchers

import net.runelite.api.Client
import net.runelite.api.ItemID
import net.runelite.api.MenuOpcode
import net.runelite.api.Point
import net.runelite.client.game.ItemManager
import net.runelite.client.ui.FontManager
import net.runelite.client.ui.overlay.*
import net.runelite.client.ui.overlay.components.ComponentOrientation
import net.runelite.client.ui.overlay.components.ImageComponent
import net.runelite.client.ui.overlay.components.PanelComponent
import net.runelite.client.ui.overlay.components.TitleComponent
import net.runelite.client.util.AsyncBufferedImage
import java.awt.*
import javax.inject.Inject
import javax.inject.Singleton

private fun getOverlayMenuEntry(): OverlayMenuEntry {
    return OverlayMenuEntry(MenuOpcode.RUNELITE_OVERLAY,
            "Reset", "Damage Counter")
}


/**
 * The overlay for the Damage Counter
 */
@Singleton
class WhaleWatchersOverlay @Inject constructor(
        private val client: Client,
        private val plugin: WhaleWatchersPlugin,
        private val config: WhaleWatchersConfig
) : Overlay(plugin) {

    private val panelComponent: PanelComponent
    private var lastOpponent: String? = "-"
    private fun damageDealt() = "Damage Dealt: ${plugin.damageDone}"

    override fun render(graphics: Graphics2D): Dimension? {
        panelComponent.children.clear()
        if (plugin.inCombat && config.showDamageCounter()) {
            panelComponent.setOrientation(ComponentOrientation.HORIZONTAL)
            panelComponent.setWrapping(5)
            val target = ((client.localPlayer ?: return null).interacting ?: return null).name
            val damageTaken = "Damage Taken: ${plugin.damageTaken}"

            panelComponent.children.apply {
                add(titleComponent {
                    text(target ?: lastOpponent)
                })
                add(titleComponent {
                    text(damageDealt())
                })
                add(titleComponent {
                    text(damageTaken)
                })
            }
        } else {
            panelComponent.children.clear()
        }
        return panelComponent.render(graphics)
    }

    init {
        menuEntries.add(getOverlayMenuEntry())
        layer = OverlayLayer.ABOVE_WIDGETS
        priority = OverlayPriority.HIGHEST
        position = OverlayPosition.TOP_LEFT
        preferredPosition = OverlayPosition.TOP_LEFT
        panelComponent = PanelComponent()
    }
}

private val gloryTitleComponent = titleComponent {
    text("Uncharged Glory")
    color(Color.BLACK)
}

@Singleton
class WhaleWatchersGloryOverlay @Inject constructor(private val plugin: WhaleWatchersPlugin) : Overlay() {

    private val panelComponent: PanelComponent

    @Inject
    private lateinit var itemManager: ItemManager

    private val gloryImage: AsyncBufferedImage? by lazy {
        itemManager.getImage(ItemID.AMULET_OF_GLORY)
    }

    override fun render(graphics: Graphics2D): Dimension? {
        panelComponent.children.clear()
        if (plugin.displayGloryOverlay) {
            panelComponent.setBackgroundColor(Color.lightGray)
            panelComponent.children += gloryTitleComponent
            panelComponent.children.add(ImageComponent(gloryImage))
        }
        return panelComponent.render(graphics)
    }

    init {
        layer = OverlayLayer.ABOVE_WIDGETS
        priority = OverlayPriority.HIGH
        position = OverlayPosition.DETACHED
        panelComponent = PanelComponent()
    }
}

private val font: Font by lazy {
    FontManager.getRunescapeBoldFont().deriveFont(Font.BOLD, 72f)
}

@Singleton
class WhaleWatchersProtOverlay @Inject constructor(
        private val client: Client,
        private val plugin: WhaleWatchersPlugin,
        private val config: WhaleWatchersConfig
) : Overlay() {

    override fun render(graphics: Graphics2D): Dimension? {
        if (plugin.protectItemOverlay && config.protectItemWarning()) {
            val rectangle = Rectangle()
            rectangle.bounds = client.canvas.bounds
            rectangle.location = client.canvas.location
            val oldStroke = graphics.stroke
            if (config.lessObnoxiousProtWarning()) {
                graphics.stroke = smallStroke
            } else {
                graphics.stroke = biggerStroke
            }
            graphics.color = Color.RED
            graphics.draw(rectangle)
            if (!config.lessObnoxiousProtWarning()) {
                val font = font
                graphics.font = font
                OverlayUtil.renderTextLocation(graphics, Point(rectangle.centerX.toInt() - 50, font.size),
                        "Protect item prayer disabled!!!", Color.red)
            }
            graphics.stroke = oldStroke
        }
        return null
    }

    init {
        layer = OverlayLayer.ABOVE_WIDGETS
        priority = OverlayPriority.HIGH
        position = OverlayPosition.DYNAMIC
    }

}

@Singleton
class WhaleWatchersSmiteableOverlay @Inject constructor(private val plugin: WhaleWatchersPlugin) : Overlay() {

    private val smitePanelComponent: PanelComponent

    override fun render(graphics: Graphics2D): Dimension? {
        smitePanelComponent.children.clear()
        if (plugin.displaySmiteOverlay) {
            smitePanelComponent.run {
                setBackgroundColor(Color.WHITE)
                setPreferredSize(Dimension(graphics.fontMetrics.stringWidth(subText)
                        + 20, 0))
                children += mainTitle
                children += subtextComponent

            }
        }
        return smitePanelComponent.render(graphics)
    }

    init {
        layer = OverlayLayer.ABOVE_WIDGETS
        priority = OverlayPriority.HIGH
        position = OverlayPosition.BOTTOM_RIGHT
        this.smitePanelComponent = PanelComponent()
    }

}

fun titleComponent(titleComponent: TitleComponent.TitleComponentBuilder.() -> Unit): TitleComponent {
    val builder = TitleComponent.builder().apply {
        this.titleComponent()
    }
    return builder.build()
}

val mainTitle = titleComponent {
    text("LOW PRAYER WARNING")
    color(Color.BLACK)
}
val subtextComponent = titleComponent {
    text(subText)
    color(Color.BLACK)
}
const val subText = "You could be smited in 1 tick"
private val smallStroke = BasicStroke(3F)
private val biggerStroke = BasicStroke(10F)