package net.runelite.client.plugins.zulrah.overlays;

import net.runelite.api.Client;
import net.runelite.api.Prayer;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.plugins.zulrah.ZulrahConfig;
import net.runelite.client.plugins.zulrah.ZulrahPlugin;
import net.runelite.client.plugins.zulrah.rotationutils.ZulrahData;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.components.InfoBoxComponent;

import javax.inject.Inject;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Iterator;

public class PrayerHelperOverlay extends OverlayPanel {
    private final Client client;
    private final ZulrahPlugin plugin;
    private final ZulrahConfig config;
    private final SpriteManager spriteManager;
    private final Color RED = new Color(255, 0, 0, 25);
    private final Color GREEN = new Color(0, 255, 0, 25);

    @Inject
    private PrayerHelperOverlay(Client client, ZulrahPlugin plugin, ZulrahConfig config, SpriteManager spriteManager) {
        this.client = client;
        this.plugin = plugin;
        this.config = config;
        this.spriteManager = spriteManager;
        this.setResizable(false);
        this.setPriority(OverlayPriority.HIGH);
        this.setPosition(OverlayPosition.ABOVE_CHATBOX_RIGHT);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        if (config.prayerHelper() && plugin.getZulrahNpc() != null && !plugin.getZulrahNpc().isDead()) {
            Prayer prayer = null;
            Iterator var3 = plugin.getZulrahData().iterator();

            while (var3.hasNext()) {
                ZulrahData data = (ZulrahData) var3.next();
                if (data.getCurrentPhasePrayer().isPresent()) {
                    prayer = (Prayer) data.getCurrentPhasePrayer().get();
                }
            }

            if (prayer == null) {
                return null;
            } else {
                InfoBoxComponent prayComponent = new InfoBoxComponent();
                prayComponent.setImage(spriteManager.getSprite(prayerToSpriteId(prayer), 0));
                prayComponent.setBackgroundColor(!client.isPrayerActive(prayer) ? RED : GREEN);
                prayComponent.setPreferredSize(new Dimension(40, 40));
                panelComponent.getChildren().add(prayComponent);
                panelComponent.setPreferredSize(new Dimension(40, 0));
                panelComponent.setBorder(new Rectangle(0, 0, 0, 0));
                return super.render(graphics);
            }
        } else {
            return null;
        }
    }

    private int prayerToSpriteId(Prayer prayer) {
        switch (prayer) {
            case PROTECT_FROM_MELEE:
                return 129;
            case PROTECT_FROM_MISSILES:
                return 128;
            case PROTECT_FROM_MAGIC:
                return 127;
            default:
                return -1;
        }
    }
}
