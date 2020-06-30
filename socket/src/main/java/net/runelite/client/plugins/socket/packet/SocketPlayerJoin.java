package net.runelite.client.plugins.socket.packet;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.runelite.api.events.Event;
import net.runelite.client.plugins.socket.org.json.JSONObject;

/**
 * Event triggered by Socket, notifying plugins that a player has joined the party.
 * This event is not triggered on the client thread.
 */
@AllArgsConstructor
public class SocketPlayerJoin implements Event {

    @Getter(AccessLevel.PUBLIC)
    private String playerName;

}
