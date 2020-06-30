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
package net.runelite.client.plugins.socket.plugins.sotetseg;

import com.google.inject.Provides;
import lombok.AccessLevel;
import lombok.Getter;
import net.runelite.api.Client;
import net.runelite.api.GroundObject;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.api.Tile;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.GroundObjectSpawned;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginType;
import net.runelite.client.plugins.socket.org.json.JSONArray;
import net.runelite.client.plugins.socket.org.json.JSONObject;
import net.runelite.client.plugins.socket.packet.SocketBroadcastPacket;
import net.runelite.client.plugins.socket.packet.SocketReceivePacket;
import net.runelite.client.ui.overlay.OverlayManager;
import org.pf4j.Extension;

import javax.inject.Inject;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

@Extension
@PluginDescriptor(
        name = "Socket - Sotetseg",
        description = "Extended plugin handler for Sotetseg in the Theatre of Blood.",
        tags = {"socket", "server", "discord", "connection", "broadcast", "sotetseg", "theatre", "tob"},
        enabledByDefault = false,
        type = PluginType.PVM
)
public class SotetsegPlugin extends Plugin {

    @Inject
    private Client client;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private EventBus eventBus;

    @Inject
    private SotetsegConfig config;

    @Provides
    SotetsegConfig getConfig(ConfigManager configManager) {
        return configManager.getConfig(SotetsegConfig.class);
    }

    @Inject
    private SotetsegOverlay overlay;

    // This boolean states whether or not the room is currently active.
    @Getter(AccessLevel.PUBLIC)
    private boolean sotetsegActive;

    // This NPC represents the boss.
    private NPC sotetsegNPC;

    // This represents the bad tiles.
    private LinkedHashSet<Point> redTiles;

    @Getter(AccessLevel.PUBLIC)
    private Set<WorldPoint> mazePings;

    // This represents the amount of times to send data.
    private int dispatchCount;

    // This represents the state of the raid.
    private boolean wasInUnderworld;
    private int overworldRegionID;

    @Override
    protected void startUp() {
        this.sotetsegActive = false;
        this.sotetsegNPC = null;

        this.redTiles = new LinkedHashSet<>();
        this.mazePings = Collections.synchronizedSet(new HashSet<>());

        this.dispatchCount = 5;
        this.wasInUnderworld = false;
        this.overworldRegionID = -1;

        this.overlayManager.add(this.overlay);
    }

    @Override
    protected void shutDown() {
        this.overlayManager.remove(this.overlay);
    }

    @Subscribe // Boss has entered the scene. Played has entered the room.
    public void onNpcSpawned(NpcSpawned event) {
        final NPC npc = event.getNpc();
        switch (npc.getId()) {
            case 8387:
            case 8388:
                this.sotetsegActive = true;
                this.sotetsegNPC = npc;
                break;
            default:
                break;
        }
    }

    @Subscribe // Boss has left the scene. Player left, died, or the boss was killed.
    public void onNpcDespawned(NpcDespawned event) {
        final NPC npc = event.getNpc();
        switch (npc.getId()) {
            case 8387:
            case 8388:
                if (this.client.getPlane() != 3) {
                    this.sotetsegActive = false;
                    this.sotetsegNPC = null;
                }

                break;
            default:
                break;
        }
    }

    @Subscribe
    public void onGameTick(GameTick event) {
        if (this.sotetsegActive) {
            final Player player = this.client.getLocalPlayer();

            // This check resets all the data if sotetseg is attackable.
            if (this.sotetsegNPC != null && this.sotetsegNPC.getId() == 8388) {
                this.redTiles.clear();
                this.mazePings.clear();
                this.dispatchCount = 5;

                if (this.isInOverWorld()) { // Set the overworld flags.
                    this.wasInUnderworld = false;
                    if (player != null && player.getWorldLocation() != null) {
                        WorldPoint wp = player.getWorldLocation();
                        this.overworldRegionID = wp.getRegionID();
                    }
                }
            }

            if (!this.redTiles.isEmpty() && this.wasInUnderworld) {
                if (this.dispatchCount > 0) { // Ensure we only send the data a couple times.
                    this.dispatchCount--;

                    JSONArray data = new JSONArray();

                    for (final Point p : this.redTiles) {
                        WorldPoint wp = this.translateMazePoint(p);

                        JSONObject jsonwp = new JSONObject();
                        jsonwp.put("x", wp.getX());
                        jsonwp.put("y", wp.getY());
                        jsonwp.put("plane", wp.getPlane());

                        data.put(jsonwp);
                    }

                    JSONObject payload = new JSONObject();
                    payload.put("sotetseg-extended", data);

                    eventBus.post(SocketBroadcastPacket.class, new SocketBroadcastPacket(payload));
                }
            }
        }
    }

    @Subscribe
    public void onSocketReceivePacket(SocketReceivePacket event) {
        try {
            JSONObject payload = event.getPayload();
            if (!payload.has("sotetseg-extended"))
                return;

            this.mazePings.clear();

            JSONArray data = payload.getJSONArray("sotetseg-extended");
            for (int i = 0; i < data.length(); i++) {
                JSONObject jsonwp = data.getJSONObject(i);
                int x = jsonwp.getInt("x");
                int y = jsonwp.getInt("y");
                int plane = jsonwp.getInt("plane");

                WorldPoint wp = new WorldPoint(x, y, plane);
                this.mazePings.add(wp);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Subscribe
    public void onGroundObjectSpawned(GroundObjectSpawned event) {
        if (this.sotetsegActive) {
            GroundObject o = event.getGroundObject();
            if (o.getId() == 33035) {
                final Tile t = event.getTile();
                final WorldPoint p = WorldPoint.fromLocal(this.client, t.getLocalLocation());
                final Point point = new Point(p.getRegionX(), p.getRegionY());

                if (this.isInOverWorld()) {  // (9, 22) are magical numbers that represent the overworld maze offset.
                    this.redTiles.add(new Point(point.getX() - 9, point.getY() - 22));
                }

                if (this.isInUnderWorld()) {   // (42, 31) are magical numbers that represent the underworld maze offset.
                    this.redTiles.add(new Point(point.getX() - 42, point.getY() - 31));
                    this.wasInUnderworld = true;
                }
            }
        }
    }

    /**
     * Returns whether or not the current player is in the overworld.
     *
     * @return In the overworld?
     */
    private boolean isInOverWorld() {
        return this.client.getMapRegions().length > 0 && this.client.getMapRegions()[0] == 13123;
    }

    /**
     * Returns whether or not the current player is in the underworld.
     *
     * @return In the underworld?
     */
    private boolean isInUnderWorld() {
        return this.client.getMapRegions().length > 0 && this.client.getMapRegions()[0] == 13379;
    }

    /**
     * Translates a maze point to a WorldPoint.
     *
     * @param mazePoint Point on the maze.
     * @return WorldPoint
     */
    private WorldPoint translateMazePoint(final Point mazePoint) {
        Player p = this.client.getLocalPlayer();

        // (9, 22) are magical numbers that represent the overworld maze offset.
        if (this.overworldRegionID == -1 && p != null) {
            WorldPoint wp = p.getWorldLocation();
            return WorldPoint.fromRegion(wp.getRegionID(), mazePoint.getX() + 9, mazePoint.getY() + 22, 0);
        }

        return WorldPoint.fromRegion(this.overworldRegionID, mazePoint.getX() + 9, mazePoint.getY() + 22, 0);
    }
}
