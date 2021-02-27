package net.runelite.client.plugins.socket.packet;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Event triggered by Socket, notifying plugins that a player has joined the party.
 * This event is not triggered on the client thread.
 */
@AllArgsConstructor
public class SocketPlayerJoin
{

	@Getter(AccessLevel.PUBLIC)
	private String playerName;

}
