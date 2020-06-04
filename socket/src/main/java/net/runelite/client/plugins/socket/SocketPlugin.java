package net.runelite.client.plugins.socket;

import com.google.inject.Provides;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameTick;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginType;
import net.runelite.client.plugins.socket.org.json.JSONException;
import net.runelite.client.plugins.socket.org.json.JSONObject;
import net.runelite.client.plugins.socket.packet.SocketBroadcastPacket;
import net.runelite.client.plugins.socket.packet.SocketReceivePacket;
import org.pf4j.Extension;

@Extension
@PluginDescriptor(
	name = "Socket",
	description = "Socket connection for broadcasting messages across clients.",
	tags = {"socket", "server", "discord", "connection", "broadcast"},
	enabledByDefault = false,
	type = PluginType.MISCELLANEOUS
)

@Slf4j
public class SocketPlugin extends Plugin implements Runnable
{

	@Inject
	private Client client;

	@Inject
	private EventBus eventBus;

	@Inject
	private ClientThread clientThread;

	@Inject
	private SocketConfig config;

	// State variables.
	private boolean isConnecting;
	private boolean requireReset;
	private long nextConnection;

	// Socket connection variables.
	private Socket socket;
	private BufferedReader socketInput;
	private PrintWriter socketOutput;

	private Thread socketThread;
	private boolean isStopping = false;

	/**
	 * Resets the variables in this class back to default.
	 */
	private void resetSettings()
	{
		this.isConnecting = false;
		this.requireReset = false;
		this.nextConnection = System.currentTimeMillis();

		this.socket = null;
		this.socketInput = null;
		this.socketOutput = null;
		this.socketThread = null;
	}

	@Provides
	SocketConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(SocketConfig.class);
	}

	@Override
	protected void startUp()
	{
		this.isStopping = false;
		this.resetSettings();
	}

	@Override
	protected void shutDown()
	{
		this.isStopping = true;
		this.clientThread.invoke(() -> this.client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "<col=ff0000>socket shutdown", null));
		try
		{
			if (this.socketThread != null)
			{
				this.socketThread.interrupt();
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}

		eventBus.unregister(SocketReceivePacket.class);
		eventBus.unregister(SocketBroadcastPacket.class);


		this.closeConnection(false);
		this.resetSettings();
	}

	public void closeConnection(boolean verbose)
	{
		try
		{ // Close the socket output stream.
			if (this.socketOutput != null)
			{
				this.socketOutput.close();
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		finally
		{
			this.socketOutput = null;
		}

		try
		{ // Close the socket input stream.
			if (this.socketInput != null)
			{
				this.socketInput.close();
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		finally
		{
			this.socketInput = null;
		}

		try
		{ // Close the actual socket.
			if (this.socket != null)
			{
				this.socket.close();
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		finally
		{
			this.socket = null;
		}

		if (verbose)
		{
			this.clientThread.invoke(() -> client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "<col=008000>Any active socket server connections was closed.", null));
		}
	}

	@Override
	public void run()
	{
		try
		{
			this.isConnecting = true;
			InetSocketAddress address = new InetSocketAddress(config.getServerAddressForReal(), config.getServerPort());

			this.socket = new Socket();
			socket.connect(address, 10000); // Timeout after 10 seconds.

			this.socketInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			this.socketOutput = new PrintWriter(socket.getOutputStream(), true);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();

			this.isConnecting = false;
			this.closeConnection(false);
			this.socketThread = null;
			this.nextConnection = System.currentTimeMillis() + (30 * 1000L);

			this.clientThread.invoke(() -> client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "<col=ff0000>Connection failed.", null));
			this.clientThread.invoke(() -> client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "<col=ff0000>" + ex.getClass().getName() + ": " + ex.getMessage(), null));

			if (!this.isStopping)
			{
				this.clientThread.invoke(() -> this.client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "<col=ff0000>Reconnecting in 30 seconds...", null));
			}

			return;
		}

		// Join the specific room.
		JSONObject joinCommand = new JSONObject();
		joinCommand.put("command", "JOIN_ROOM");
		joinCommand.put("room", this.config.getRoom());

		log.info("[SocketPlugin] [Send] [HERE] " + System.currentTimeMillis());
		log.info(joinCommand.toString(4));
		this.socketOutput.println(joinCommand.toString());

		this.clientThread.invoke(() -> client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "<col=008000>Connection was successful.", null));

		this.isConnecting = false;

		try
		{
			String ln;

			while (!this.isStopping // Plugin is not stopped.
				&& !this.requireReset // Connection does not require a reset.
				&& this.socket.isConnected() // Socket is connected.
				&& !this.socket.isClosed() // Socket is not closed.
				&& (ln = this.socketInput.readLine()) != null)
			{ // Plugin has received new input.
				try
				{

					if (ln.contains("Server heartbeat:"))
					{
						continue;
						//ignore heartbeat.........
					}
					log.info("[SocketPlugin] [Recieve] [ln] " + ln);
					String secret = SocketAESEncryption.decrypt(this.config.getSalt(), ln);
					log.info("[SocketPlugin] [Recieve] [secret] " + secret);
					if (secret == null)
					{ // We ignore bad packets.
						continue;
					}
					JSONObject payload = new JSONObject(secret); // We assume everything is JSON. If it's not, the packet wasn't meant for us.
					log.info("[SocketPlugin] [Receive] " + System.currentTimeMillis());
					log.info(payload.toString(4));
					this.clientThread.invoke(() -> eventBus.post(SocketReceivePacket.class, new SocketReceivePacket(payload))); // Dispatch the payload via an event handler.
				}
				catch (JSONException ignore)
				{
				}
			}
		}
		catch (Exception e)
		{
			if (!this.isStopping)
			{ // We don't print the error if the plugin is stopping -> It's not a bug!
				e.printStackTrace();
				this.clientThread.invoke(() -> {
					client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "<col=ff0000>An error has occurred with the socket server connection.", null);
					client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "<col=ff0000>" + e.getClass().getName() + ": " + e.getMessage(), null);
				});
			}
		}

		this.closeConnection(true);
		this.resetSettings();

		this.nextConnection = System.currentTimeMillis() + (30 * 1000L);
		if (!this.isStopping)
		{
			this.clientThread.invoke(() -> this.client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "<col=ff0000>Reconnecting in 30 seconds...", null));
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (client.getGameState() == GameState.LOGGED_IN)
		{
			if (this.socket == null && !this.isConnecting && System.currentTimeMillis() >= this.nextConnection)
			{ // Attempt a new connection.
				this.isConnecting = true;

				this.client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "<col=008000>Attempting to connect...", null);

				this.socketThread = new Thread(this);
				this.socketThread.start();
			}
		}

	}

	@Subscribe
	public void onSocketReceivePacket(SocketReceivePacket packet)
	{
		//System.out.println("TEST - IDK");
	}

	@Subscribe
	public void onSocketBroadcastPacket(SocketBroadcastPacket packet)
	{
		try
		{
			if (this.socket != null && this.socket.isConnected() && !socket.isClosed())
			{
				JSONObject payload = new JSONObject();
				payload.put("command", "BROADCAST");
				payload.put("payload", packet.getPayload()); // Payload is current a JSONObject


				log.info("[SocketPlugin] [Send] " + System.currentTimeMillis());
				log.info(payload.toString(4));

				payload.put("payload", SocketAESEncryption.encrypt(this.config.getSalt(), packet.getPayload().toString())); // Payload is now an encrypted string.

				this.socketOutput.println(payload.toString());
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		// I'm lazy.
		if (event.getGroup().equals("SocketPlugin"))
		{
			this.clientThread.invoke(() -> this.client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "<col=ff0000>Configuration changed. Please restart the plugin to see updates.", null));
		}

		// This works, but honestly, it's slow.

//        if (event.getGroup().equals("SocketPlugin")) {
//            System.out.println("[SocketPlugin] [ConfigChanged] " + event.getGroup() + " | " + event.getKey() + " -> " + event.getNewValue());
//            this.requireReset = true;
//
//            if (this.socket != null && !this.isConnecting && this.socketThread != null) {
//                this.closeConnection(false);
//            }
//        }
	}
}
