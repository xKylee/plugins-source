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
import net.runelite.api.Player;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginType;
import static net.runelite.client.plugins.playerattacktimer.AttackTimerMap.ATTACK_TIMER_MAP;
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
	private final Map<Integer, Integer> customAnimationTickMap = new HashMap<>();

	private static final Splitter NEWLINE_SPLITTER = Splitter
		.on("\n")
		.omitEmptyStrings()
		.trimResults();

	@Inject
	private Client client;

	@Inject
	PlayerAttackTimerConfig config;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private PlayerOverlay playerOverlay;

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
		overlayManager.add(playerOverlay);

		parseCustomAnimationConfig(config.customAnimations());
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(playerOverlay);

		customAnimationTickMap.clear();
	}

	@Subscribe
	private void onConfigChanged(final ConfigChanged event)
	{
		if (!event.getGroup().equals("playerattacktimer"))
		{
			return;
		}

		switch (event.getKey())
		{
			case "customAnimations":
				parseCustomAnimationConfig(config.customAnimations());
				break;
			case "mirrorMode":
				playerOverlay.determineLayer();
				break;
			default:
				break;
		}
	}

	@Subscribe
	private void onGameTick(final GameTick event)
	{
		if (ticksUntilNextAnimation > 0)
		{
			--ticksUntilNextAnimation;
		}
	}

	@Subscribe
	private void onAnimationChanged(final AnimationChanged event)
	{
		final Player player = client.getLocalPlayer();

		if (ticksUntilNextAnimation > 1 || player == null || event.getActor() != player)
		{
			return;
		}

		final int animationId = player.getAnimation();

		final Integer delay = customAnimationTickMap.getOrDefault(animationId, ATTACK_TIMER_MAP.get(animationId));

		if (delay != null)
		{
			ticksUntilNextAnimation = delay + 1; // add 1 because GameTick event is posted after this
		}
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
