package net.runelite.client.plugins.socket;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import java.util.UUID;

@ConfigGroup("SocketPlugin")
public interface SocketConfig extends Config
{

	@ConfigItem(
		position = 1,
		keyName = "getServerAddressForReal",
		name = "Server Address",
		description = "The address of the server to connect to."
	)
	default String getServerAddressForReal()
	{
		return "127.0.0.1";
	}

	@ConfigItem(
		position = 2,
		keyName = "getServerPort",
		name = "Server port",
		description = "The port of the server to connect to."
	)
	default int getServerPort()
	{
		return 1;
	}

	@ConfigItem(
		position = 3,
		keyName = "getRoom",
		name = "Server Room",
		description = "The room on the server to get data from."
	)
	default int getRoom()
	{
		return 1;
	}

	@ConfigItem(
		position = 4,
		keyName = "getSalt",
		name = "Shared Password",
		description = "Used to encrypt and decrypt data sent to the server."
	)
	default String getSalt()
	{
		return UUID.randomUUID().toString().replaceAll("-", "");
	}

}
