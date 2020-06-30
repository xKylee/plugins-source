package net.runelite.client.plugins.socket.packet;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.runelite.api.events.Event;
import net.runelite.client.plugins.socket.org.json.JSONObject;

/**
 * Event triggered by alternative plugins, broadcasting a packet to the server.
 */
@AllArgsConstructor
public class SocketBroadcastPacket implements Event
{

	@Getter(AccessLevel.PUBLIC)
	private JSONObject payload;

}
