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
import java.util.Arrays;
import java.util.Collections;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.ChatMessageType;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.plugins.gauntlet.GauntletConfig;
import net.runelite.client.plugins.gauntlet.GauntletPlugin;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.components.PanelComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;
import net.runelite.client.ui.overlay.components.table.TableAlignment;
import net.runelite.client.ui.overlay.components.table.TableComponent;
import net.runelite.client.ui.overlay.components.table.TableElement;
import net.runelite.client.ui.overlay.components.table.TableRow;

@Singleton
public class OverlayTimer extends Overlay
{
	private final GauntletPlugin plugin;
	private final GauntletConfig config;
	private final ChatMessageManager chatMessageManager;

	private final PanelComponent panelComponent;
	private final TableComponent tableComponent;

	private final TableRow rowRestartMessage;
	private final TableRow rowPrepTime;
	private final TableRow rowTotalTime;

	private State state;

	private long timeGauntletStart;
	private long timeHunllefStart;

	@Inject
	public OverlayTimer(final GauntletPlugin plugin, final GauntletConfig config, final ChatMessageManager chatMessageManager)
	{
		super(plugin);

		this.plugin = plugin;
		this.config = config;

		this.chatMessageManager = chatMessageManager;

		this.panelComponent = new PanelComponent();
		this.tableComponent = new TableComponent();

		panelComponent.getChildren().add(TitleComponent.builder().text("Gauntlet Timer").build());
		panelComponent.getChildren().add(tableComponent);

		tableComponent.setColumnAlignments(TableAlignment.LEFT, TableAlignment.RIGHT);

		this.rowRestartMessage = TableRow.builder()
			.elements(Collections.singletonList(
				TableElement.builder()
					.content("Restart the gauntlet!")
					.build()))
			.rowColor(Color.RED)
			.build();

		this.rowPrepTime = TableRow.builder()
			.elements(Arrays.asList(
				TableElement.builder()
					.content("Prep Time:")
					.build(),
				TableElement.builder()
					.content("")
					.build()))
			.build();

		this.rowTotalTime = TableRow.builder()
			.elements(Arrays.asList(
				TableElement.builder()
					.content("Total Time:")
					.build(),
				TableElement.builder()
					.content("")
					.build()))
			.build();

		this.state = State.UNKNOWN;

		this.timeGauntletStart = -1L;
		this.timeHunllefStart = -1L;

		setPosition(OverlayPosition.ABOVE_CHATBOX_RIGHT);
		setPriority(OverlayPriority.HIGH);
		determineLayer();
	}

	@Override
	public Dimension render(final Graphics2D graphics2D)
	{
		if (!plugin.isInGauntlet() || !config.timerOverlay())
		{
			return null;
		}

		if (state != State.UNKNOWN)
		{
			final TableRow tableRow = state == State.IN_HUNLLEF_ROOM ? rowTotalTime : rowPrepTime;

			tableRow.getElements()
				.get(1)
				.setContent(calculateElapsedTime(Instant.now().getEpochSecond(), timeGauntletStart));
		}

		return panelComponent.render(graphics2D);
	}

	public void determineLayer()
	{
		setLayer(config.mirrorMode() ? OverlayLayer.AFTER_MIRROR : OverlayLayer.UNDER_WIDGETS);
	}

	public void reset()
	{
		timeGauntletStart = -1L;
		timeHunllefStart = -1L;

		tableComponent.setRows(rowRestartMessage);

		state = State.UNKNOWN;
	}

	public void onPlayerDeath()
	{
		if (!config.timerChatMessage() || state != State.IN_HUNLLEF_ROOM)
		{
			return;
		}

		printTime();
		state = State.UNKNOWN;
	}

	public void setGauntletStart()
	{
		if (!config.timerOverlay() && !config.timerChatMessage())
		{
			return;
		}

		timeGauntletStart = Instant.now().getEpochSecond();
		rowPrepTime.setRowColor(Color.WHITE);
		tableComponent.setRows(rowPrepTime);
		state = State.IN_GAUNTLET;
	}

	public void setHunllefStart()
	{
		if (state != State.IN_GAUNTLET || (!config.timerOverlay() && !config.timerChatMessage()))
		{
			return;
		}

		timeHunllefStart = Instant.now().getEpochSecond();
		rowPrepTime.setRowColor(Color.LIGHT_GRAY);
		tableComponent.setRows(rowPrepTime);
		tableComponent.addRows(rowTotalTime);
		state = State.IN_HUNLLEF_ROOM;
	}

	enum State
	{
		UNKNOWN, IN_GAUNTLET, IN_HUNLLEF_ROOM
	}

	private void printTime()
	{
		final long current = Instant.now().getEpochSecond();

		final String elapsedPrepTime = calculateElapsedTime(timeHunllefStart, timeGauntletStart);
		final String elapsedBossTime = calculateElapsedTime(current, timeHunllefStart);
		final String elapsedTotalTime = calculateElapsedTime(current, timeGauntletStart);

		final ChatMessageBuilder chatMessageBuilder = new ChatMessageBuilder()
			.append(ChatColorType.HIGHLIGHT)
			.append("FAILED")
			.append(ChatColorType.NORMAL)
			.append(" - Prep time: ")
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
