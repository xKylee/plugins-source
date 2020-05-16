/*
 * Copyright (c) 2020, Trevor <https://github.com/Trevor159>
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
package net.runelite.client.plugins.tobdamagecount;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.Map;
import javax.inject.Inject;
import net.runelite.api.Player;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;
import net.runelite.client.util.QuantityFormatter;

class TobDamageOverlay extends OverlayPanel
{
	private final TobDamageCounterPlugin plugin;
	private final TobDamageCounterConfig config;

	@Inject
	TobDamageOverlay(TobDamageCounterPlugin plugin, TobDamageCounterConfig config)
	{
		super(plugin);
		this.plugin = plugin;
		this.config = config;
		determineLayer();
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!plugin.isInTob())
		{
			return null;
		}

		// no damage yet
		if (plugin.getCurrentRoom() == null && plugin.getRaidDamage() == null)
		{
			return null;
		}

		final String title = plugin.getCurrentRoom() == null ? "Tob Damage" : plugin.getCurrentRoom().toString() + " Damage";
		panelComponent.getChildren().add(
			TitleComponent.builder()
				.text(title)
				.build());

		TobDamageCounterPlugin.Damage damage;

		if (plugin.getCurrentRoom() != null)
		{
			damage = plugin.getDamageMap().get(plugin.getCurrentRoom());
		}
		else
		{
			damage = plugin.getRaidDamage();
		}


		String left = "Personal Damage";
		String right = QuantityFormatter.formatNumber(damage.getPersonalDamage());
		panelComponent.getChildren().add(
			LineComponent.builder()
				.left(left)
				.right(right)
				.build());

		left = "Total Damage";
		right = QuantityFormatter.formatNumber(damage.getTotalDamage());
		panelComponent.getChildren().add(
			LineComponent.builder()
				.left(left)
				.right(right)
				.build());

		if (config.showHealCount() && damage.getTotalHealing() != 0)
		{
			panelComponent.getChildren().add(
				LineComponent.builder()
					.left("Total Healing")
					.right(QuantityFormatter.formatNumber(damage.getTotalHealing()))
					.build()
			);
		}

		if (config.showLeechOverlay() && damage.getLeechCounts().size() != 0)
		{
			panelComponent.getChildren().add(LineComponent.builder().build());

			panelComponent.getChildren().add(
				TitleComponent.builder()
					.text("Leech Count")
					.build());

			Map<Player, Integer> leeches = damage.getLeechCounts();
			for (Player p : leeches.keySet())
			{
				panelComponent.getChildren().add(
					LineComponent.builder()
						.left(p.getName())
						.right(leeches.get(p) + "")
						.build());
			}
		}
		return super.render(graphics);
	}

	public void determineLayer()
	{
		if (config.mirrorMode())
		{
			setLayer(OverlayLayer.AFTER_MIRROR);
		}
	}
}
