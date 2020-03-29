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
import net.runelite.client.util.ImageUtil
import java.awt.*
import javax.inject.Inject
import javax.inject.Singleton

val font: Font by lazy {
    FontManager.getRunescapeBoldFont().deriveFont(Font.BOLD, 72f)
}

@Singleton
class WhaleWatchersOverlay @Inject constructor(
        private val client: Client,
        private val plugin: WhaleWatchersPlugin,
        private val config: WhaleWatchersConfig
) : Overlay(plugin) {

    private val panelComponent: PanelComponent by lazy { PanelComponent() }
    private var lastOpponent: String = "-"
    private fun damageDealt() = "Damage Dealt: ${plugin.damageDone}"

    private val overlayMenuEntry: OverlayMenuEntry by lazy {
        OverlayMenuEntry(MenuOpcode.RUNELITE_OVERLAY,
                "Reset", "Damage Counter")
    }
    private val damageTitle: TitleComponent by lazy { titleComponent { "" } }

    private val title: TitleComponent by lazy { damageTitle }

    private val damageDealt: TitleComponent by lazy {
        return@lazy titleComponent {
            text(damageDealt())
        }
    }

    private fun TitleComponent.updateText(text: String): TitleComponent {
        setText(text)
        return this
    }

    override fun render(graphics: Graphics2D): Dimension? {
        panelComponent.children.clear()
        if (!plugin.inCombat || !config.showDamageCounter()) {
            return null
        } else {
            panelComponent.setOrientation(ComponentOrientation.HORIZONTAL)
            panelComponent.setWrapping(5)
            val target = ((client.localPlayer ?: return null).interacting ?: return null).name
            with(panelComponent.children) {
                add(title.updateText(target ?: lastOpponent))
                add(damageDealt.updateText(damageDealt()))
                add(damageDealt.updateText("Damage Taken: ${plugin.damageTaken}"))
            }
        }
        return panelComponent.render(graphics)
    }

    init {
        menuEntries.add(overlayMenuEntry)
        layer = OverlayLayer.ABOVE_WIDGETS
        priority = OverlayPriority.HIGHEST
        position = OverlayPosition.TOP_LEFT
        preferredPosition = OverlayPosition.TOP_LEFT
    }
}

private val gloryTitleComponent by lazy {
    titleComponent {
        text("Uncharged Glory")
        color(Color.BLACK)
    }
}

@Singleton
class WhaleWatchersGloryOverlay @Inject constructor(private val plugin: WhaleWatchersPlugin) : Overlay() {

    private val panelComponent: PanelComponent by lazy(::PanelComponent)

    @Inject
    private lateinit var itemManager: ItemManager

    private val gloryImage: Image by lazy {
        val image = itemManager.getImage(ItemID.AMULET_OF_GLORY)
        image.getScaledInstance((image.height * 1.5).toInt(), (image.width * 1.5).toInt(), Image.SCALE_DEFAULT)
    }

    override fun render(graphics: Graphics2D): Dimension? {
        panelComponent.children.clear()
        if (!plugin.displayGloryOverlay) return null
        with(panelComponent.children) {
            clear()
            add(gloryTitleComponent)
            add(ImageComponent(ImageUtil.bufferedImageFromImage(gloryImage)))
        }
        return panelComponent.render(graphics)
    }

    init {
        layer = OverlayLayer.ABOVE_WIDGETS
        priority = OverlayPriority.HIGH
        position = OverlayPosition.DETACHED
    }
}

@Singleton
class WhaleWatchersProtOverlay @Inject constructor(
        private val client: Client,
        private val plugin: WhaleWatchersPlugin,
        private val config: WhaleWatchersConfig
) : Overlay() {

    private val rectangle: Rectangle by lazy {
        val rectangle = Rectangle()
        rectangle.bounds = client.canvas.bounds
        rectangle.location = client.canvas.location
        return@lazy rectangle
    }

    override fun render(graphics: Graphics2D): Dimension? {
        if (!plugin.protectItemOverlay || !config.protectItemWarning()) return null
        val oldStroke = graphics.stroke
        if (config.lessObnoxiousProtWarning()) {
            drawLessObnoxiousWarning(graphics)
        } else {
            drawProtItemWarning(graphics)
        }
        graphics.stroke = oldStroke
        return null
    }

    private fun drawProtItemWarning(graphics: Graphics2D) {
        if (config.lessObnoxiousProtWarning()) {
            graphics.stroke === smallStroke
        } else {
            graphics.stroke = biggerStroke
        }
        OverlayUtil.renderTextLocation(graphics, Point(rectangle.centerX.toInt() - 50, font.size),
                "Protect item prayer disabled!!!", Color.red)
        graphics.color = Color.RED
        graphics.draw(rectangle)
    }

    private fun drawLessObnoxiousWarning(graphics: Graphics2D) {
        graphics.font = font
        OverlayUtil.renderTextLocation(graphics, Point(rectangle.centerX.toInt() - 50, font.size),
                "Protect item prayer disabled!!!", Color.red)
    }

    init {
        layer = OverlayLayer.ABOVE_WIDGETS
        priority = OverlayPriority.HIGH
        position = OverlayPosition.DYNAMIC
    }
}

@Singleton
class WhaleWatchersSmiteableOverlay @Inject constructor(private val plugin: WhaleWatchersPlugin) : Overlay() {

    private val smitePanelComponent: PanelComponent by lazy { PanelComponent() }

    override fun render(graphics: Graphics2D): Dimension? {
        smitePanelComponent.children.clear()
        if (plugin.displaySmiteOverlay) {
            smitePanelComponent.run {
                setBackgroundColor(Color.WHITE)
                setPreferredSize(Dimension(
                        graphics.fontMetrics.stringWidth(subText)
                                + 20,
                        0
                ))
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
    }
}

fun titleComponent(titleComponent: TitleComponent.TitleComponentBuilder.() -> Unit): TitleComponent {
    val builder = TitleComponent.builder().apply {
        this.titleComponent()
    }
    return builder.build()
}

private val mainTitle = titleComponent {
    text("LOW PRAYER WARNING")
    color(Color.BLACK)
}

private val subtextComponent: TitleComponent by lazy {
    titleComponent {
        text(subText)
        color(Color.BLACK)
    }
}
const val subText = "You could be smited in 1 tick"
val smallStroke = BasicStroke(3F)
val biggerStroke = BasicStroke(10F)