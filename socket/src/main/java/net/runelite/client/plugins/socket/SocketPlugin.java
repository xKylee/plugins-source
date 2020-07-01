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

package net.runelite.client.plugins.socket;

import com.google.inject.Provides;
import java.io.PrintWriter;
import javax.inject.Inject;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginType;
import net.runelite.client.plugins.socket.hash.AES256;
import net.runelite.client.plugins.socket.org.json.JSONObject;
import net.runelite.client.plugins.socket.packet.SocketBroadcastPacket;
import net.runelite.client.plugins.socket.packet.SocketPlayerJoin;
import net.runelite.client.plugins.socket.packet.SocketPlayerLeave;
import net.runelite.client.plugins.socket.packet.SocketReceivePacket;
import org.pf4j.Extension;

@Slf4j
@Extension
@PluginDescriptor(
	name = "Socket",
	description = "Socket connection for broadcasting messages across clients.",
	tags = {"socket", "server", "discord", "connection", "broadcast"},
	enabledByDefault = false,
	type = PluginType.UTILITY
)
public class SocketPlugin extends Plugin
{

	// To help users who decide to use weak passwords.
	public static final String PASSWORD_SALT = "$P@_/gKR`y:mv)6K";

	@Inject
	@Getter(AccessLevel.PUBLIC)
	private Client client;

	@Inject
	@Getter(AccessLevel.PUBLIC)
	private EventBus eventBus;

	@Inject
	@Getter(AccessLevel.PUBLIC)
	private ClientThread clientThread;

	@Inject
	@Getter(AccessLevel.PUBLIC)
	private SocketConfig config;

	@Provides
	SocketConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(SocketConfig.class);
	}

	// This variables controls the next UNIX epoch time to establish the next connection.
	@Getter(AccessLevel.PUBLIC)
	@Setter(AccessLevel.PUBLIC)
	private long nextConnection;

	// This variables controls the current active connection.
	private SocketConnection connection = null;

	@Override
	protected void startUp()
	{
		nextConnection = 0L;
	}

	@Override
	protected void shutDown()
	{
		eventBus.unregister(SocketReceivePacket.class);
		eventBus.unregister(SocketBroadcastPacket.class);

		eventBus.unregister(SocketPlayerJoin.class);
		eventBus.unregister(SocketPlayerLeave.class);

		if (connection != null)
		{
			connection.terminate(true);
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		// Attempt connecting, or re-establishing connection to the socket server, only when the user is logged in.
		if (client.getGameState() == GameState.LOGGED_IN)
		{
			if (connection != null)
			{ // If an connection is already being established, ignore.
				SocketState state = connection.getState();
				if (state == SocketState.CONNECTING || state == SocketState.CONNECTED)
				{
					return;
				}
			}

			if (System.currentTimeMillis() >= nextConnection)
			{ // Create a new connection.
				nextConnection = System.currentTimeMillis() + 30000L;
				connection = new SocketConnection(this, client.getLocalPlayer().getName());
				new Thread(connection).start(); // Handler blocks, so run it on a separate thread.
			}
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		// Notify the user to restart the plugin when the config changes.
		if (event.getGroup().equals("Socket"))
		{
			clientThread.invoke(() -> client.addChatMessage(ChatMessageType.GAMEMESSAGE, "",
				"<col=b4281e>Configuration changed. Please restart the plugin to see updates.",
				null));
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		// Terminate all connections to the socket server when the user logs out.
		if (event.getGameState() == GameState.LOGIN_SCREEN)
		{
			if (connection != null)
			{
				connection.terminate(false);
			}
		}
	}

	@Subscribe
	public void onSocketBroadcastPacket(SocketBroadcastPacket packet)
	{
		try
		{
			// Handles the packets that alternative plugins broadcasts.
			if (connection == null || connection.getState() != SocketState.CONNECTED)
			{
				return;
			}

			String data = packet.getPayload().toString();
			log.debug("Deploying packet from client: {}", data);

			String secret = config.getPassword() + PASSWORD_SALT;

			JSONObject payload = new JSONObject();
			payload.put("header", SocketPacket.BROADCAST);
			payload.put("payload", AES256.encrypt(secret, data)); // Payload is now an encrypted string.

			PrintWriter outputStream = connection.getOutputStream();
			synchronized (outputStream)
			{
				outputStream.println(payload.toString());
			}
		}
		catch (Exception e)
		{ // Oh no, something went wrong!
			e.printStackTrace();
			log.error("An error has occurred while trying to broadcast a packet.", e);
		}
	}
}
