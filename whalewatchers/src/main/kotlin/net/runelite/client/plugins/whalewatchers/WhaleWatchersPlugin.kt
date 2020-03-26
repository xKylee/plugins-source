package net.runelite.client.plugins.whalewatchers

import com.google.inject.Provides
import net.runelite.api.*
import net.runelite.api.events.*
import net.runelite.api.events.player.headicon.PlayerSkullChanged
import net.runelite.api.kit.KitType
import net.runelite.client.config.ConfigManager
import net.runelite.client.eventbus.Subscribe
import net.runelite.client.events.ConfigChanged
import net.runelite.client.events.OverlayMenuClicked
import net.runelite.client.game.Sound
import net.runelite.client.game.SoundManager
import net.runelite.client.plugins.Plugin
import net.runelite.client.plugins.PluginDescriptor
import net.runelite.client.plugins.PluginType
import net.runelite.client.ui.overlay.OverlayManager
import net.runelite.client.util.PvPUtil
import org.apache.commons.lang3.ObjectUtils
import org.pf4j.Extension
import javax.inject.Inject
import kotlin.math.ceil

@Extension
@PluginDescriptor(
        name = "Whale Watchers",
        enabledByDefault = false,
        description = "A Plugin to save help whales in the wild",
        tags = ["whale watchers", "whale", "protect item", "warning", "pklite", "pneck"],
        type = PluginType.PVP
)
class WhaleWatchersPlugin : Plugin() {
    var protectItemOverlay = false
    var damageDone = 0
    var damageTaken = 0
    var inCombat = false

    @Inject
    lateinit var client: Client

    @Inject
    lateinit var config: WhaleWatchersConfig

    @Inject
    lateinit var overlay: WhaleWatchersOverlay

    @Inject
    lateinit var whaleWatchersProtOverlay: WhaleWatchersProtOverlay

    @Inject
    lateinit var smiteableOverlay: WhaleWatchersSmiteableOverlay

    @Inject
    lateinit var whaleWatchersGloryOverlay: WhaleWatchersGloryOverlay

    @Inject
    lateinit var overlayManager: OverlayManager

    @Inject
    lateinit var soundManager: SoundManager

    private var tickCountdown = 0

    var displaySmiteOverlay = false

    var displayGloryOverlay = false

    @Provides
    fun getConfig(configManager: ConfigManager): WhaleWatchersConfig {
        return configManager.getConfig(WhaleWatchersConfig::class.java)
    }

    @Subscribe
    private fun onOverlayMenuClicked(event: OverlayMenuClicked) {
        if (event.overlay == overlay && event.entry.option == "Reset") {
            resetDamageCounter()
        }
    }

    override fun startUp() {
        overlayManager.add(overlay)
        overlayManager.add(whaleWatchersProtOverlay)
        overlayManager.add(smiteableOverlay)
        overlayManager.add(whaleWatchersGloryOverlay)
    }

    override fun shutDown() {
        overlayManager.remove(overlay)
        overlayManager.remove(whaleWatchersProtOverlay)
        overlayManager.remove(smiteableOverlay)
        overlayManager.remove(whaleWatchersGloryOverlay)
        resetDamageCounter()
    }

    @Subscribe
    private fun onGameStateChanged(event: GameStateChanged) {
        if (event.gameState != GameState.LOGGED_IN && !config.protectItemWarning()) return
        if (shouldProtItem() && PvPUtil.getWildernessLevelFrom(client.localPlayer?.worldLocation) > 0) {
            protectItemOverlay = true
        }
    }

    @Subscribe
    private fun onConfigChanged(event: ConfigChanged) {
        if (event.group != "WhaleWatchers") {
            return
        }
        if (!config.protectItemWarning()) {
            protectItemOverlay = false
        }
        if (!config.gloryWarning()) {
            displayGloryOverlay = false
        }
        if (!config.smiteableWarning()) {
            displaySmiteOverlay = false
        }
    }

    @Subscribe
    private fun onChatMessage(event: ChatMessage) {
        if (config.pneckBreak() && event.type == ChatMessageType.GAMEMESSAGE && event.message == BROKEN_PNECK_MESSAGE) {
            soundManager.playSound(Sound.BREAK)
        }
    }

    @Subscribe
    private fun onHitsplatApplied(event: HitsplatApplied) {
        if (config.showDamageCounter()) {
            if (!(event.actor === client.localPlayer ||
                            event.actor === client.localPlayer!!.interacting)) {
                return
            }
            if (isAttackingPlayer || inCombat) {
                inCombat = true
                if (event.actor === client.localPlayer) {
                    damageTaken += event.hitsplat.amount
                }
                if (event.actor === client.localPlayer!!.interacting) {
                    damageDone += event.hitsplat.amount
                }
            }
        }
    }

    @Subscribe
    private fun onItemContainerChanged(event: ItemContainerChanged) {
        displayGloryOverlay = if (config.gloryWarning() && event.itemContainer
                === client.getItemContainer(InventoryID.EQUIPMENT)) {
            val amuletID = ObjectUtils.defaultIfNull(client.localPlayer
                    ?.playerAppearance!!.getEquipmentId(KitType.AMULET), 0)
            amuletID == ItemID.AMULET_OF_GLORY
        } else {
            false
        }
    }

    @Subscribe
    private fun onMenuOptionClicked(event: MenuOptionClicked) {
        if (config.showDamageCounter() && event.menuOpcode == MenuOpcode.SPELL_CAST_ON_PLAYER) {
            inCombat = true
        }
    }

    @Subscribe
    private fun onPlayerSkullChanged(event: PlayerSkullChanged) {
        if (event.player != client.localPlayer) return
        if (config.protectItemWarning()) {
            protectItemOverlay = true
        }
    }

    @Subscribe
    private fun onVarbitChanged(event: VarbitChanged) {
        if (config.showDamageCounter() && client.getVar(VarPlayer.ATTACKING_PLAYER) == -1 && inCombat) {
            tickCountdown = 10
        }
        if (config.protectItemWarning()) {
            val worldTypes = client.worldType
            if (!isSkulled() || WorldType.isHighRiskWorld(worldTypes)) {
                protectItemOverlay = false
                return
            }
            val skullIcon = client.localPlayer!!.skullIcon
            if (skullIcon == SkullIcon.SKULL) {
                protectItemOverlay = if (WorldType.isPvpWorld(worldTypes) || WorldType.isDeadmanWorld(worldTypes) ||
                        client.getVar(Varbits.IN_WILDERNESS) == 1) {
                    shouldProtItem()
                } else {
                    false
                }
            }
        }
    }

    private fun shouldProtItem() = client.getRealSkillLevel(Skill.PRAYER) > 25 &&
            isSkulled()

    private fun isSkulled(): Boolean {
        return client.localPlayer?.skullIcon != null
    }

    @Subscribe
    private fun onGameTick(event: GameTick) {
        if (config.showDamageCounter() && tickCountdown > 0 && tickCountdown < 11) {
            tickCountdown--
            if (tickCountdown == 1) {
                inCombat = false
                resetDamageCounter()
                return
            }
        }
        displaySmiteOverlay = if (config.smiteableWarning() && (client.getVar(Varbits.IN_WILDERNESS) == 1
                        || WorldType.isPvpWorld(client.worldType))) {
            if (client.localPlayer!!.skullIcon != null && client.localPlayer!!.skullIcon == SkullIcon.SKULL) {
                val currentHealth = client.localPlayer!!.health
                val currentPrayer = client.getBoostedSkillLevel(Skill.PRAYER)
                currentPrayer <= ceil(currentHealth / 4.toDouble())
            } else {
                false
            }
        } else {
            false
        }
    }

    /**
     * Checks to see if client is attacking another player
     *
     * @return returns true if they are, false otherwise
     */
    private val isAttackingPlayer: Boolean
        get() {
            if (client.getVar(Varbits.IN_WILDERNESS) == 1 && client.localPlayer!!.interacting != null) {
                return true
            }
            val varp = client.getVar(VarPlayer.ATTACKING_PLAYER)
            return varp != -1
        }

    private fun resetDamageCounter() {
        damageTaken = 0
        damageDone = 0
    }

    companion object {
        private const val BROKEN_PNECK_MESSAGE =
                "<col=ef1020>Your phoenix necklace heals you, but is destroyed in the process.</col>"
    }
}