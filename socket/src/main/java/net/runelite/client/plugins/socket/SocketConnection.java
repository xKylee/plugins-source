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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.EventBus;
import static net.runelite.client.plugins.socket.SocketPlugin.PASSWORD_SALT;
import net.runelite.client.plugins.socket.hash.AES256;
import net.runelite.client.plugins.socket.hash.SHA256;
import net.runelite.client.plugins.socket.org.json.JSONArray;
import net.runelite.client.plugins.socket.org.json.JSONException;
import net.runelite.client.plugins.socket.org.json.JSONObject;
import net.runelite.client.plugins.socket.packet.SocketPlayerJoin;
import net.runelite.client.plugins.socket.packet.SocketPlayerLeave;
import net.runelite.client.plugins.socket.packet.SocketReceivePacket;

/**
 * Represents an instance of a socket connection to the server.
 */
@Slf4j
public class SocketConnection implements Runnable
{

	private SocketPlugin plugin;
	private SocketConfig config;

	private Client client;
	private ClientThread clientThread;

	private EventBus eventBus;

	private String playerName;

	// Variable that identifies the state of the socket connection.
	@Getter(AccessLevel.PUBLIC)
	private SocketState state;

	// Socket IO Variables
	@Getter(AccessLevel.PUBLIC)
	private Socket socket;

	@Getter(AccessLevel.PUBLIC)
	private BufferedReader inputStream;

	@Getter(AccessLevel.PUBLIC)
	private PrintWriter outputStream;

	// The last time a heartbeat was sent to the server.
	// We want to send a heartbeat periodically to validate that the connection is still valid.
	private long lastHeartbeat;

	public SocketConnection(SocketPlugin plugin, String playerName)
	{
		this.plugin = plugin;
		config = plugin.getConfig();

		client = plugin.getClient();
		clientThread = plugin.getClientThread();

		eventBus = plugin.getEventBus();

		this.playerName = playerName;
		lastHeartbeat = 0L;

		state = SocketState.DISCONNECTED;
	}

	@Override
	public void run()
	{

		// Socket can only be started once. If the state isn't originally disconnected, ignore everything.
		if (state != SocketState.DISCONNECTED)
		{
			throw new IllegalStateException(
				"Socket connection is already in state " + state.name() + ".");
		}

		// Let's start a new connection.
		state = SocketState.CONNECTING;
		log.info("Attempting to establish socket connection to {}:{}", config.getServerAddress(),
			config.getServerPort());

		// Apply the salt to the password.
		final String secret = new String(config.getPassword() + PASSWORD_SALT);

		try
		{

			// Attempt to establish a connection.
			InetSocketAddress address =
				new InetSocketAddress(config.getServerAddress(), config.getServerPort());

			socket = new Socket();
			socket.connect(address, 10000);

			inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			outputStream = new PrintWriter(socket.getOutputStream(), true);

			// Notify the server about your connection credentials. This establishes the necessary handshake.
			JSONObject joinPacket = new JSONObject();
			joinPacket.put("header", SocketPacket.JOIN);
			joinPacket.put("room", SHA256.encrypt(secret));
			joinPacket.put("name", AES256.encrypt(secret, playerName));
			outputStream.println(joinPacket.toString());

			// Start listening for input.
			while (true)
			{
				if (state == SocketState.DISCONNECTED || state == SocketState.TERMINATED)
				{
					break; // If object was terminated, stop loop.
				}

				if (!socket.isConnected() || socket.isClosed())
				{
					break; // Socket was disconnected, stop loop.
				}

				if (outputStream.checkError())
				{
					throw new IOException("Broken transmission stream");
				}

				if (!inputStream.ready())
				{ // If there is no data ready, ping (heartbeat) the server.
					long elapsedTime = System.currentTimeMillis() - lastHeartbeat;
					if (elapsedTime >= 30000)
					{ // Maintain a heartbeat with the server every 30 seconds.
						lastHeartbeat = System.currentTimeMillis();
						synchronized (outputStream)
						{
							outputStream.println();
						}
					}

					Thread.sleep(20L);
					continue;
				}

				// Read the input line.
				String packet = inputStream.readLine();
				if (packet == null || packet.isEmpty())
				{
					continue;
				}

				log.debug("Received packet from server: {}", packet);

				JSONObject data;
				try
				{
					data = new JSONObject(packet);
					log.debug("Decoded packet as JSON.");
				}
				catch (JSONException e)
				{
					log.error("Bad packet. Unable to decode: {}", packet);
					continue;
				}

				// The header will determine the packet type.
				if (!data.has("header"))
				{
					throw new NullPointerException("Packet missing header");
				}

				String header = data.getString("header");

				try
				{ // Read and decode the packet based on the header.
					if (header
						.equals(SocketPacket.BROADCAST))
					{ // Player is broadcasting a packet to all members.
						String message = AES256.decrypt(secret, data.getString("payload"));
						JSONObject payload = new JSONObject(message);
						clientThread.invoke(
							() -> eventBus.post(SocketReceivePacket.class, new SocketReceivePacket(payload)));

					}
					else if (header.equals(SocketPacket.JOIN))
					{ // Player has joined the party.
						String targetName = AES256.decrypt(secret, data.getString("player"));
						logMessage(SocketLog.INFO, targetName + " has joined the party.");

						if (targetName.equals(playerName))
						{ // You have joined the party.
							state = SocketState.CONNECTED;
							log.info("You have successfully joined the socket party.");
						}

						JSONArray membersArray = data.getJSONArray("party");
						logMessage(SocketLog.INFO, mergeMembers(membersArray, secret));

						try
						{
							eventBus.post(SocketPlayerJoin.class, new SocketPlayerJoin(targetName));
						}
						catch (Exception ignored)
						{
						}

					}
					else if (header.equals(SocketPacket.LEAVE))
					{ // Player has left the party.
						String targetName = AES256.decrypt(secret, data.getString("player"));
						logMessage(SocketLog.ERROR, targetName + " has left the party.");

						JSONArray membersArray = data.getJSONArray("party");
						logMessage(SocketLog.ERROR, mergeMembers(membersArray, secret));

						try
						{
							eventBus.post(SocketPlayerLeave.class, new SocketPlayerLeave(targetName));
						}
						catch (Exception ignored)
						{
						}

					}
					else if (header
						.equals(SocketPacket.MESSAGE))
					{ // Socket server wishes to send you a message.
						String message = data.getString("message");
						clientThread.invoke(
							() -> client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", message, null));

					}
				}
				catch (JSONException e)
				{
					log.warn("Bad packet contents. Unable to decode.");
					continue;
				}
			}
		}
		catch (Exception ex)
		{
			// Oh no, something went wrong! Terminate the connection and log.
			log.error("Unable to establish connection with the server.", ex);
			terminate(false);

			logMessage(SocketLog.ERROR,
				"Socket terminated. " + ex.getClass().getSimpleName() + ": " + ex.getMessage());

			// Try to reconnect in 30 seconds.
			plugin.setNextConnection(System.currentTimeMillis() + 30000L);
			logMessage(SocketLog.ERROR, "Reconnecting in 30 seconds...");
			return;
		}
	}

	/**
	 * Terminate the connection to the server.
	 *
	 * @param verbose Whether or not to log to the user's chatbox.
	 */
	public void terminate(boolean verbose)
	{
		if (state == SocketState.TERMINATED)
		{
			return;
		}

		state = SocketState.TERMINATED;

		try
		{ // Close the socket output stream.
			if (outputStream != null)
			{
				outputStream.close();
			}
		}
		catch (Exception ignored)
		{
		}

		try
		{ // Close the socket input stream.
			if (inputStream != null)
			{
				inputStream.close();
			}
		}
		catch (Exception ignored)
		{
		}

		try
		{ // Close the actual socket.
			if (socket != null)
			{
				socket.close();
				socket.shutdownOutput();
				socket.shutdownInput();
			}
		}
		catch (Exception ignored)
		{
		}

		log.info("Terminated connections with the socket server.");
		if (verbose)
		{
			logMessage(SocketLog.INFO, "Any active socket server connections were closed.");
		}
	}

	/**
	 * Given a JSONArray of a list of encrypted member names, decrypt the list using the symmetrical key.
	 *
	 * @param membersArray JSONArray of encrypted member names
	 * @param secret       AES symmetrical password
	 * @return String of member names, delimited by a comma.
	 */
	private String mergeMembers(JSONArray membersArray, String secret)
	{
		int count = membersArray.length();
		String members = String.format("Member%s (%d): ", count != 1 ? "s" : "", count);

		for (int i = 0; i < count; i++)
		{
			if (i > 0)
			{
				members += ", ";
			}
			members += AES256.decrypt(secret, membersArray.getString(i));
		}

		return members;
	}

	/**
	 * Logs a message inside the player's in-game chatbox.
	 *
	 * @param level   Log level, for color coding.
	 * @param message The message to log, as a string.
	 */
	private void logMessage(SocketLog level, String message)
	{
		clientThread.invoke(() -> client
			.addChatMessage(ChatMessageType.GAMEMESSAGE, "", level.getPrefix() + message, null));
	}
}
