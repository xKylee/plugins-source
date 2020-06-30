/*
 * Copyright (c) 2020, Charles Xu <github.com/kthisiscvpv>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.socket.plugins.playerstatus;

import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.plugins.socket.plugins.playerstatus.gametimer.GameIndicator;
import net.runelite.client.plugins.socket.plugins.playerstatus.gametimer.GameTimer;
import net.runelite.client.plugins.socket.plugins.playerstatus.marker.AbstractMarker;
import net.runelite.client.plugins.socket.plugins.playerstatus.marker.IndicatorMarker;
import net.runelite.client.plugins.socket.plugins.playerstatus.marker.TimerMarker;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;

import javax.inject.Inject;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PlayerStatusOverlay extends Overlay {

    private final Client client;
    private final PlayerStatusPlugin plugin;
    private final PlayerStatusConfig config;

    private final ItemManager itemManager;
    private final SpriteManager spriteManager;

    @Inject
    public PlayerStatusOverlay(Client client, PlayerStatusPlugin plugin, PlayerStatusConfig config, ItemManager itemManager, SpriteManager spriteManager) {
        this.client = client;
        this.plugin = plugin;
        this.config = config;

        this.itemManager = itemManager;
        this.spriteManager = spriteManager;

        this.setPosition(OverlayPosition.DYNAMIC);
        this.setPriority(OverlayPriority.HIGH);
        this.setLayer(OverlayLayer.ALWAYS_ON_TOP);
    }

    private boolean ignoreMarker(AbstractMarker marker) {
        if (marker == null)
            return true;

        if (marker instanceof IndicatorMarker) {
            GameIndicator indicator = ((IndicatorMarker) marker).getIndicator();
            switch (indicator) {
                case VENGEANCE_ACTIVE:
                    return !this.config.showVengeanceActive();
                default:
                    return true;
            }
        } else if (marker instanceof TimerMarker) {
            GameTimer timer = ((TimerMarker) marker).getTimer();
            switch (timer) {
                case VENGEANCE:
                    return !this.config.showVengeanceCooldown();
                case IMBUED_HEART:
                    return !this.config.showImbuedHeart();
                case OVERLOAD:
                case OVERLOAD_RAID:
                    return !this.config.showOverload();
                case PRAYER_ENHANCE:
                    return !this.config.showPrayerEnhance();
                case STAMINA:
                    return !this.config.showStamina();
                default:
                    return true;
            }
        }

        return true;
    }

    private List<AbstractMarker> renderPlayer(Graphics graphics, Player p, List<AbstractMarker> markers) {
        List<AbstractMarker> toRemove = new ArrayList<AbstractMarker>();

        int size = this.config.getIndicatorSize();
        int margin = this.config.getIndicatorPadding();
        graphics.setFont(new Font("SansSerif", Font.BOLD, (int) (0.75d * size)));

        Point base = Perspective.localToCanvas(this.client, p.getLocalLocation(), this.client.getPlane(), p.getLogicalHeight());
        int zOffset = 0;
        int xOffset = this.config.getIndicatorXOffset() - (size / 2);

        for (AbstractMarker marker : markers) {
            if (this.ignoreMarker(marker))
                continue;

            if (marker instanceof TimerMarker) {
                TimerMarker timer = (TimerMarker) marker;
                long elapsedTime = System.currentTimeMillis() - timer.getStartTime();
                double timeRemaining = timer.getTimer().getDuration().toMillis() - elapsedTime;
                if (timeRemaining < 0)
                    toRemove.add(marker);
                else {
                    BufferedImage icon = timer.getImage(size);
                    graphics.drawImage(icon, base.getX() + xOffset, base.getY() + zOffset, null);
                    zOffset += size;

                    int xDelta = icon.getWidth() + margin; // +5 for padding
                    String text;
                    if (timeRemaining > (100 * 1000))
                        text = String.format("%d", (long) (timeRemaining / 1000));
                    else
                        text = String.format("%.1f", timeRemaining / 1000);

                    graphics.setColor(Color.BLACK);
                    graphics.drawString(text, base.getX() + xOffset + xDelta + 1, base.getY() + zOffset);

                    graphics.setColor(Color.WHITE);
                    graphics.drawString(text, base.getX() + xOffset + xDelta, base.getY() + zOffset);
                    zOffset += margin;
                }
            } else if (marker instanceof IndicatorMarker) {
                IndicatorMarker timer = (IndicatorMarker) marker;
                BufferedImage icon = timer.getImage(size);
                graphics.drawImage(icon, base.getX() + xOffset, base.getY() + zOffset, null);
                zOffset += (size + margin);
            }
        }

        return toRemove;
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        Map<String, List<AbstractMarker>> effects = this.plugin.getStatusEffects();
        Player p = this.client.getLocalPlayer();

        List<AbstractMarker> localMarkers = effects.get(null);
        if (localMarkers != null) {
            List<AbstractMarker> toRemove = this.renderPlayer(graphics, p, localMarkers);

            if (!toRemove.isEmpty()) {
                synchronized (effects) {
                    for (AbstractMarker marker : toRemove)
                        localMarkers.remove(marker);

                    if (localMarkers.isEmpty())
                        effects.remove(null);
                }
            }
        }

        for (Player t : this.client.getPlayers()) {
            if (p == t)
                continue;

            List<AbstractMarker> markers = effects.get(t.getName());
            if (markers != null) {
                List<AbstractMarker> toRemove = this.renderPlayer(graphics, t, markers);

                if (!toRemove.isEmpty()) {
                    synchronized (markers) {
                        for (AbstractMarker marker : toRemove)
                            markers.remove(marker);

                        if (markers.isEmpty())
                            effects.remove(t.getName());
                    }
                }
            }
        }

        return null;
    }
}
