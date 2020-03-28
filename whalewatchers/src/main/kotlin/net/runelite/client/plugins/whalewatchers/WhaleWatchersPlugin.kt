package net.runelite.client.plugins.whalewatchers

import com.google.inject.Provides
import net.runelite.api.*
import net.runelite.api.events.*
import net.runelite.api.events.player.headicon.PlayerSkullChanged
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
    private lateinit var client: Client

    @Inject
    private lateinit var config: WhaleWatchersConfig

    @Inject
    private lateinit var overlay: WhaleWatchersOverlay

    @Inject
    private lateinit var whaleWatchersProtOverlay: WhaleWatchersProtOverlay

    @Inject
    private lateinit var smiteableOverlay: WhaleWatchersSmiteableOverlay

    @Inject
    private lateinit var whaleWatchersGloryOverlay: WhaleWatchersGloryOverlay

    @Inject
    private lateinit var overlayManager: OverlayManager

    @Inject
    private lateinit var soundManager: SoundManager

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
        if (event.gameState != GameState.LOGGED_IN && !config.protectItemWarning()) {
            return
        }
        if (canProtectItem() && PvPUtil.getWildernessLevelFrom(client.localPlayer?.worldLocation) > 0) {
            protectItemOverlay = true
        }
    }

    @Subscribe
    private fun onConfigChanged(event: ConfigChanged) {
        if (event.group != "WhaleWatchers") return
        when {
            !config.protectItemWarning() -> protectItemOverlay = false
            !config.gloryWarning() -> displayGloryOverlay = false
            !config.smiteableWarning() -> displaySmiteOverlay = false
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
        if (!config.showDamageCounter()) return
        if (!shouldCountHitsplat(event) || !(isAttackingPlayer() || inCombat)) return
        when {
            event.actor === client.localPlayer -> damageTaken += event.hitsplat.amount
            event.actor === client.localPlayer!!.interacting -> damageDone += event.hitsplat.amount
        }
        inCombat = true
    }

    @Subscribe
    private fun onItemContainerChanged(event: ItemContainerChanged) {
        if (!config.gloryWarning() || event.itemContainer !== client.getItemContainer(InventoryID.EQUIPMENT)) return
        displayGloryOverlay = client.getItemContainer(InventoryID.EQUIPMENT)!!
                .items
                .map { it.id }.contains(ItemID.AMULET_OF_GLORY)
    }

    @Subscribe
    private fun onMenuOptionClicked(event: MenuOptionClicked) {
        if (!config.showDamageCounter() || event.menuOpcode != MenuOpcode.SPELL_CAST_ON_PLAYER) return
        inCombat = true
    }

    @Subscribe
    private fun onPlayerSkullChanged(event: PlayerSkullChanged) {
        if (event.player != client.localPlayer || !config.protectItemWarning()) return
        protectItemOverlay = true
    }

    @Subscribe
    private fun onVarbitChanged(event: VarbitChanged) {
        if (config.showDamageCounter() && client.getVar(VarPlayer.ATTACKING_PLAYER) == -1 && inCombat) {
            tickCountdown = 10
        }
        if (!config.protectItemWarning() || !isSkulled()) {
            protectItemOverlay = false
            return
        }
        if (WorldType.isHighRiskWorld(client.worldType)) {
            protectItemOverlay = false
            return
        }
        protectItemOverlay = if (shouldBeProtecting()) {
            canProtectItem()
        } else {
            false
        }
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
                val currentPrayer = client.getBoostedSkillLevel(Skill.PRAYER)
                currentPrayer <= ceil(client.getBoostedSkillLevel(Skill.PRAYER) / 4.toDouble())
            } else {
                false
            }
        } else {
            false
        }
    }

    private fun shouldCountHitsplat(event: HitsplatApplied) = event.actor === client.localPlayer ||
            event.actor === client.localPlayer!!.interacting

    private fun shouldBeProtecting() =
            (WorldType.isPvpWorld(client.worldType) ||
                    WorldType.isDeadmanWorld(client.worldType) || client.getVar(Varbits.IN_WILDERNESS) == 1) &&
                    isSkulled() && canProtectItem() && !client.isPrayerActive(Prayer.PROTECT_ITEM)

    private fun canProtectItem() = client.getRealSkillLevel(Skill.PRAYER) > 25

    private fun isSkulled(): Boolean = client.localPlayer?.skullIcon != null && client.localPlayer!!.skullIcon == SkullIcon.SKULL

    private fun isAttackingPlayer(): Boolean = (client.getVar(Varbits.IN_WILDERNESS) == 1 &&
            client.localPlayer!!.interacting != null) && client.getVar(VarPlayer.ATTACKING_PLAYER) != -1

    private fun resetDamageCounter() {
        damageTaken = 0
        damageDone = 0
    }

    companion object {
        private const val BROKEN_PNECK_MESSAGE =
                "<col=ef1020>Your phoenix necklace heals you, but is destroyed in the process.</col>"
    }
}