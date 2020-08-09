/*
 * BSD 2-Clause License
 *
 * Copyright (c) 2020, dutta64 <https://github.com/dutta64>
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

package net.runelite.client.plugins.playerattacktimer;

import com.google.common.base.Splitter;
import com.google.inject.Provides;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginType;
import net.runelite.client.ui.overlay.OverlayManager;
import org.pf4j.Extension;

@Singleton
@Extension
@PluginDescriptor(
	name = "Player Attack Timer",
	enabledByDefault = false,
	description = "Tick delay timer for player attacks.",
	tags = {"player", "attack", "tick", "timer", "delay"},
	type = PluginType.UTILITY
)
public class PlayerAttackTimerPlugin extends Plugin
{
	private static final Map<Integer, Integer> animationTickMap = new HashMap<>();

	static
	{
		animationTickMap.put(1658, 4); // Abyssal whip
	}

	private final Map<Integer, Integer> customAnimationTickMap = new HashMap<>();

	private static final Splitter NEWLINE_SPLITTER = Splitter
		.on("\n")
		.omitEmptyStrings()
		.trimResults();

	@Inject
	private Client client;

	@Inject
	private EventBus eventBus;

	@Inject
	PlayerAttackTimerConfig config;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private PlayerOverlay playerOverlay;

	private boolean enabled;

	@Getter
	private int ticksUntilNextAnimation;

	@Provides
	PlayerAttackTimerConfig getConfig(final ConfigManager configManager)
	{
		return configManager.getConfig(PlayerAttackTimerConfig.class);
	}

	@Override
	protected void startUp()
	{
		if (client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}

		init();
	}

	@Override
	protected void shutDown()
	{
		eventBus.unregister(this);

		overlayManager.remove(playerOverlay);

		customAnimationTickMap.clear();

		enabled = false;
	}

	@Subscribe
	private void onConfigChanged(final ConfigChanged event)
	{
		if (!event.getGroup().equals("playerattacktimer"))
		{
			return;
		}

		if (event.getKey().equals("customAnimations"))
		{
			parseCustomAnimationConfig(config.customAnimations());
		}
	}

	@Subscribe
	private void onGameStateChanged(final GameStateChanged event)
	{
		final GameState gameState = event.getGameState();

		switch (gameState)
		{
			case LOGGED_IN:
				if (!enabled)
				{
					init();
				}
				break;
			case LOGIN_SCREEN:
			case HOPPING:
				if (enabled)
				{
					shutDown();
				}
				break;
		}
	}

	private void onGameTick(final GameTick event)
	{
		final Player player = client.getLocalPlayer();

		if (player == null)
		{
			return;
		}

		if (ticksUntilNextAnimation > 0)
		{
			--ticksUntilNextAnimation;
		}

		if (ticksUntilNextAnimation <= 0)
		{
			final int animationId = player.getAnimation();

			final Integer delay = animationTickMap.getOrDefault(animationId, customAnimationTickMap.get(animationId));

			if (delay != null)
			{
				ticksUntilNextAnimation = delay;
			}
		}
	}

	private void init()
	{
		enabled = true;

		parseCustomAnimationConfig(config.customAnimations());

		overlayManager.add(playerOverlay);

		eventBus.subscribe(GameTick.class, this, this::onGameTick);
	}

	private void parseCustomAnimationConfig(final String config)
	{
		if (config.isBlank() || config.isEmpty())
		{
			return;
		}

		customAnimationTickMap.clear();

		final StringBuilder sb = new StringBuilder();

		for (final String str : config.split("\n"))
		{
			if (!str.startsWith("//"))
			{
				sb.append(str).append("\n");
			}
		}

		final Map<String, String> split = NEWLINE_SPLITTER.withKeyValueSeparator(':').split(sb);

		for (final Map.Entry<String, String> entry : split.entrySet())
		{
			customAnimationTickMap.put(Integer.valueOf(entry.getKey()), Integer.valueOf(entry.getValue()));
		}
	}
}
