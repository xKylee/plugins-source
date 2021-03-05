/*
 * Copyright (c) 2019, ganom <https://github.com/Ganom>
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
package net.runelite.client.plugins.hideunder;

import com.google.inject.Provides;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import org.pf4j.Extension;

@Extension
@PluginDescriptor(
	name = "Hide Under",
	enabledByDefault = false,
	description = "Hide local player when under targeted players",
	tags = {"hide", "local", "player", "under"}
)
public class HideUnderPlugin extends Plugin
{
	@Inject
	private Client client;
	@Inject HideUnderConfig config;

	@Provides
	HideUnderConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(HideUnderConfig.class);
	}


	@Override
	protected void shutDown()
	{
		client.setLocalPlayerHidden(false);
		client.setRenderSelf(true);
	}

	@Subscribe
	private void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGGED_IN)
		{
			client.setIsHidingEntities(true);
		}
	}

	@Subscribe
	private void onGameTick(GameTick event)
	{
		if (client.getLocalPlayer() == null)
		{
			return;
		}

		final WorldPoint localPlayerWp = WorldPoint.fromLocalInstance(client, client.getLocalPlayer().getLocalLocation());
		final WorldPoint lp = client.getLocalPlayer().getWorldLocation();
		boolean hide = false;
		for (Player player : client.getPlayers())
		{
			if (player == client.getLocalPlayer())
			{
				continue;
			}
			if (client.getVarbitValue(5314) == 1)
			{
				final WorldPoint playerWp = WorldPoint.fromLocalInstance(client, player.getLocalLocation());
				if (localPlayerWp != null && localPlayerWp.distanceTo(playerWp) == 0)
				{
					hide = true;
				}
				continue;
			}

			if (lp != null && player.getWorldLocation().distanceTo(lp) == 0)
			{
				hide = true;
			}
		}
		if (config.useRenderself())
		{
			client.setRenderSelf(!hide);
		}
		else if (config.useEntityHider())
		{
			client.setLocalPlayerHidden(hide);
		}
	}
}
