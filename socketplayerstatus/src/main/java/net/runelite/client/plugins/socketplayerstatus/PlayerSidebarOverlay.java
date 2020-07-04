/*
 * Copyright (c) 2019, Tomas Slusny <slusnucky@gmail.com>
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

package net.runelite.client.plugins.socketplayerstatus;

import com.google.inject.Inject;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Map;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.components.ComponentConstants;
import net.runelite.client.ui.overlay.components.PanelComponent;
import net.runelite.client.ui.overlay.components.ProgressBarComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

public class PlayerSidebarOverlay extends OverlayPanel
{

	private static final Color HP_FG = new Color(0, 146, 54, 230);
	private static final Color HP_BG = new Color(102, 15, 16, 230);

	private static final Color PRAY_FG = new Color(0, 149, 151);
	private static final Color PRAY_BG = Color.black;

	private static final Color RUN_FG = new Color(200, 90, 0);
	private static final Color RUN_BG = Color.black;

	private static final Color SPEC_FG = new Color(200, 180, 0);
	private static final Color SPEC_BG = Color.black;

	private final PlayerStatusPlugin plugin;
	private final PlayerStatusConfig config;

	@Inject
	private PlayerSidebarOverlay(final PlayerStatusPlugin plugin, final PlayerStatusConfig config)
	{
		super(plugin);
		
		this.plugin = plugin;
		this.config = config;

		panelComponent.setBorder(new Rectangle());
		panelComponent.setGap(new Point(0, ComponentConstants.STANDARD_BORDER / 2));
	}


	@Override
	public Dimension render(Graphics2D graphics)
	{
		final Map<String, PlayerStatus> partyStatus = plugin.getPartyStatus();
		if (partyStatus.size() <= 1)
		{
			return null;
		}

		if (!config.showPlayerHealth() && !config.showPlayerPrayer() &&
			!config.showPlayerSpecial() && !config.showPlayerRunEnergy())
		{
			return null; // No options are turned on. Nothing to display.
		}

		panelComponent.setBackgroundColor(null);

		synchronized (partyStatus)
		{
			partyStatus.forEach((targetName, targetStatus) -> {
				final PanelComponent panel = targetStatus.getPanel();
				panel.getChildren().clear();

				final TitleComponent name = TitleComponent.builder()
					.text(targetName)
					.color(Color.WHITE)
					.build();
				panel.getChildren().add(name);

				if (config.showPlayerHealth())
				{
					final ProgressBarComponent hpBar = new ProgressBarComponent();
					hpBar.setBackgroundColor(HP_BG);
					hpBar.setForegroundColor(HP_FG);
					hpBar.setMaximum(targetStatus.getMaxHealth());
					hpBar.setValue(targetStatus.getHealth());
					hpBar.setLabelDisplayMode(ProgressBarComponent.LabelDisplayMode.FULL);
					panel.getChildren().add(hpBar);
				}

				if (config.showPlayerPrayer())
				{
					final ProgressBarComponent prayBar = new ProgressBarComponent();
					prayBar.setBackgroundColor(PRAY_BG);
					prayBar.setForegroundColor(PRAY_FG);
					prayBar.setMaximum(targetStatus.getMaxPrayer());
					prayBar.setValue(targetStatus.getPrayer());
					prayBar.setLabelDisplayMode(ProgressBarComponent.LabelDisplayMode.FULL);
					panel.getChildren().add(prayBar);
				}

				if (config.showPlayerRunEnergy())
				{
					final ProgressBarComponent runBar = new ProgressBarComponent();
					runBar.setBackgroundColor(RUN_BG);
					runBar.setForegroundColor(RUN_FG);
					runBar.setMaximum(100);
					runBar.setValue(targetStatus.getRun());
					runBar.setLabelDisplayMode(ProgressBarComponent.LabelDisplayMode.PERCENTAGE);
					panel.getChildren().add(runBar);
				}

				if (config.showPlayerSpecial())
				{
					final ProgressBarComponent specBar = new ProgressBarComponent();
					specBar.setBackgroundColor(SPEC_BG);
					specBar.setForegroundColor(SPEC_FG);
					specBar.setMaximum(100);
					specBar.setValue(targetStatus.getSpecial());
					specBar.setLabelDisplayMode(ProgressBarComponent.LabelDisplayMode.PERCENTAGE);
					panel.getChildren().add(specBar);
				}

				panelComponent.getChildren().add(panel);
			});
		}

		return super.render(graphics);
	}
}
