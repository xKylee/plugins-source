package net.runelite.client.plugins.socket.packet;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Event triggered by Socket, notifying plugins that a player has left the party.
 * This event is not triggered on the client thread.
 */
@AllArgsConstructor
public class SocketPlayerLeave
{

	@Getter(AccessLevel.PUBLIC)
	private String playerName;

}
