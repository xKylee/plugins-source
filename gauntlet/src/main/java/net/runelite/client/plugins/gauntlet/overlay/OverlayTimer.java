/*
 * Copyright (c) 2020, dutta64 <https://github.com/dutta64>
 * Copyright (c) 2018, Seth <http://github.com/sethtroll>
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

package net.runelite.client.plugins.gauntlet.overlay;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.time.Instant;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.ChatMessageType;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.plugins.gauntlet.GauntletConfig;
import net.runelite.client.plugins.gauntlet.GauntletPlugin;
import static net.runelite.client.plugins.gauntlet.overlay.OverlayTimer.State.IN_GAUNTLET;
import static net.runelite.client.plugins.gauntlet.overlay.OverlayTimer.State.IN_HUNLLEF_ROOM;
import static net.runelite.client.plugins.gauntlet.overlay.OverlayTimer.State.OUTSIDE_GAUNTLET;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.components.PanelComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;
import net.runelite.client.ui.overlay.components.table.TableAlignment;
import net.runelite.client.ui.overlay.components.table.TableComponent;

@Singleton
public class OverlayTimer extends Overlay
{
	private final GauntletPlugin plugin;
	private final GauntletConfig config;
	private final ChatMessageManager chatMessageManager;

	private final PanelComponent panelComponent;

	private State currentState;

	private long timeRaidStart;
	private long timeBossEnter;

	@Inject
	public OverlayTimer(final GauntletPlugin plugin, final GauntletConfig config, final ChatMessageManager chatMessageManager)
	{
		super(plugin);

		this.plugin = plugin;
		this.config = config;

		this.chatMessageManager = chatMessageManager;

		this.panelComponent = new PanelComponent();

		this.currentState = OUTSIDE_GAUNTLET;

		this.timeRaidStart = -1L;
		this.timeBossEnter = -1L;

		setPosition(OverlayPosition.ABOVE_CHATBOX_RIGHT);
		setPriority(OverlayPriority.HIGH);
		determineLayer();
	}

	@Override
	public Dimension render(final Graphics2D graphics)
	{
		if (!plugin.isInGauntlet() || !config.timerOverlay())
		{
			return null;
		}

		panelComponent.getChildren().clear();

		panelComponent.getChildren().add(TitleComponent.builder().text("Gauntlet Timer").color(Color.WHITE).build());

		final TableComponent tableComponent = new TableComponent();

		tableComponent.setColumnAlignments(TableAlignment.LEFT, TableAlignment.RIGHT);

		if (timeRaidStart == -1L)
		{
			tableComponent.addRow("Restart the gauntlet =)", "");
		}
		else
		{
			final long current = Instant.now().getEpochSecond();

			final String elapsedPrepTime;
			final String elapsedBossTime;
			final String elapsedTotalTime;

			switch (currentState)
			{
				case IN_GAUNTLET:
					elapsedPrepTime = calculateElapsedTime(current, timeRaidStart);
					elapsedBossTime = "00:00";
					elapsedTotalTime = elapsedPrepTime;
					break;
				case IN_HUNLLEF_ROOM:
					elapsedPrepTime = calculateElapsedTime(timeBossEnter, timeRaidStart);
					elapsedBossTime = calculateElapsedTime(current, timeBossEnter);
					elapsedTotalTime = calculateElapsedTime(current, timeRaidStart);
					break;
				default:
					throw new IllegalStateException("Unexpected timer state: " + currentState);
			}

			tableComponent.addRow("Prep Time:", elapsedPrepTime);
			tableComponent.addRow("Boss Time:", elapsedBossTime);
			tableComponent.addRow("Total Time:", elapsedTotalTime);
		}

		panelComponent.getChildren().add(tableComponent);

		return panelComponent.render(graphics);
	}

	public void determineLayer()
	{
		setLayer(config.mirrorMode() ? OverlayLayer.AFTER_MIRROR : OverlayLayer.UNDER_WIDGETS);
	}

	public void initialize()
	{
		timeRaidStart = -1L;
		timeBossEnter = -1L;

		currentState = plugin.isInHunllefRoom() ? IN_HUNLLEF_ROOM : IN_GAUNTLET;
	}

	public void reset()
	{
		timeRaidStart = -1L;
		timeBossEnter = -1L;

		currentState = OUTSIDE_GAUNTLET;
	}

	public void onPlayerDeath()
	{
		if (!config.timerChatMessage() || currentState != IN_HUNLLEF_ROOM)
		{
			return;
		}

		printTime();
	}

	public void updateState()
	{
		switch (currentState)
		{
			case OUTSIDE_GAUNTLET:
				if (plugin.isInHunllefRoom())
				{
					currentState = IN_HUNLLEF_ROOM;
					timeRaidStart = timeBossEnter = Instant.now().getEpochSecond();
				}
				else
				{
					currentState = IN_GAUNTLET;
					timeRaidStart = Instant.now().getEpochSecond();
				}
				break;
			case IN_GAUNTLET:
				if (plugin.isInHunllefRoom())
				{
					currentState = IN_HUNLLEF_ROOM;
					timeBossEnter = Instant.now().getEpochSecond();
				}
				break;
			default:
				break;
		}
	}

	enum State
	{
		OUTSIDE_GAUNTLET, IN_GAUNTLET, IN_HUNLLEF_ROOM
	}

	private void printTime()
	{
		if (timeRaidStart == -1L || timeBossEnter == -1L)
		{
			return;
		}

		final long current = Instant.now().getEpochSecond();

		final String elapsedPrepTime = calculateElapsedTime(timeBossEnter, timeRaidStart);
		final String elapsedBossTime = calculateElapsedTime(current, timeBossEnter);
		final String elapsedTotalTime = calculateElapsedTime(current, timeRaidStart);

		final ChatMessageBuilder chatMessageBuilder = new ChatMessageBuilder()
			.append(ChatColorType.NORMAL)
			.append("Prep time: ")
			.append(ChatColorType.HIGHLIGHT)
			.append(elapsedPrepTime)
			.append(ChatColorType.NORMAL)
			.append(" Boss time: ")
			.append(ChatColorType.HIGHLIGHT)
			.append(elapsedBossTime)
			.append(ChatColorType.NORMAL)
			.append(" Total time: ")
			.append(ChatColorType.HIGHLIGHT)
			.append(elapsedTotalTime);

		chatMessageManager.queue(QueuedMessage.builder()
			.type(ChatMessageType.CONSOLE)
			.runeLiteFormattedMessage(chatMessageBuilder.build())
			.build());
	}

	private static String calculateElapsedTime(final long end, final long start)
	{
		final long elapsed = end - start;

		final long minutes = elapsed % 3600 / 60;
		final long seconds = elapsed % 60;

		return String.format("%01d:%02d", minutes, seconds);
	}
}
