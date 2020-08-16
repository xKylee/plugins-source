package net.runelite.client.plugins.entityfocuspile;

import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.VarPlayer;
import net.runelite.api.events.GameTick;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginType;
import net.runelite.rs.api.RSClient;
import org.pf4j.Extension;

@Extension
@PluginDescriptor(
	name = "Entity FocusPile",
	enabledByDefault = false,
	description = "Bring player you are interacting with to the top of the pile.",
	tags = {"entity", "pile", "stack", "pvp", "focus", "dd"},
	type = PluginType.MISCELLANEOUS
)
public class EntityFocusPilePlugin extends Plugin
{
	@Inject
	private Client client;

	@Subscribe
	private void onGameTick(GameTick event)
	{
		client.getPlayers().forEach((player) ->
		{
			if (player.getAnimation() == -1
				|| player.getRSInteracting() != (client.getLocalPlayerIndex() + 32768))
			{
				return;
			}

			if (client.getVar(VarPlayer.ATTACKING_PLAYER) == -1)
			{
				((RSClient) client).setLocalInteractingIndex(player.getPlayerId() & 2047);
			}
		});
	}
}