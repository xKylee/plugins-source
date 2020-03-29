/*
 * Copyright (c) 2017, Aria <aria@ar1as.space>
 * Copyright (c) 2017, Adam <Adam@sigterm.info>
 * Copyright (c) 2017, Devin French <https://github.com/devinfrench>
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
package net.runelite.client.plugins.cerberus.overlays;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Prayer;
import net.runelite.client.plugins.cerberus.CerberusPlugin;
import net.runelite.client.plugins.cerberus.Util.CerberusImageManager;
import net.runelite.client.plugins.cerberus.Util.CerberusInfoBoxComponent;
import net.runelite.client.plugins.cerberus.Util.ImagePanelComponent;
import net.runelite.client.plugins.cerberus.domain.CerberusNPC;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;

@Singleton
@Slf4j
public class CerberusPrayerOverlay extends Overlay
{
	private final Client client;
	private final CerberusPlugin plugin;

	@Inject
	CerberusPrayerOverlay(final @Nullable Client client, final CerberusPlugin plugin)
	{
		setPosition(OverlayPosition.BOTTOM_RIGHT);
		setPriority(OverlayPriority.MED);
		this.client = client;
		this.plugin = plugin;
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (plugin.getCerberus() == null)
		{
			return null;
		}
		if (!plugin.inCerberusArena())
		{
			return null;
		}

		if (plugin.getUpcomingAttacks().size() > 0 && plugin.getUpcomingAttacks().get(0).getTick() <= plugin.getGameTick() + 6)
		{

			var attack = plugin.getUpcomingAttacks().get(0);
			final Prayer prayer;
			if (attack.getAttack() == CerberusNPC.Attack.AUTO)
			{
				prayer = plugin.getPrayer();
			}
			else
			{
				prayer = attack.getAttack().getPrayer();
			}

			BufferedImage prayerImage = CerberusImageManager.getCerberusPrayerBufferedImage(prayer);


			if (plugin.getConfig().useSmallBoxes())
			{

				final CerberusInfoBoxComponent infoBoxComponent = new CerberusInfoBoxComponent();
				if (plugin.getConfig().showPrayerTimer())
				{
					final StringBuilder sbTitle = new StringBuilder();
					sbTitle.append("+");
					var timeUntilAttack = Math.max((double) ((attack.getTick() - plugin.getGameTick()) * 600 - (System.currentTimeMillis() - plugin.getLastTick())) / 1000, 0);
					sbTitle.append(String.format("%.1f", timeUntilAttack));
					sbTitle.append("s");
					infoBoxComponent.setText(sbTitle.toString());
					infoBoxComponent.setColor(Color.white);
				}
				infoBoxComponent.setImage(prayerImage);
				if (!client.isPrayerActive(prayer))
				{
					infoBoxComponent.setBackgroundColor(new Color(150, 0, 0, 128));
				}
				infoBoxComponent.setPreferredSize(new Dimension(50, 50));

				return infoBoxComponent.render(graphics);
			}
			else
			{
				final StringBuilder sbTitle = new StringBuilder();
				if (!client.isPrayerActive(prayer))
				{
					sbTitle.append("Switch!");
				}
				else
				{
					sbTitle.append("Prayer");
				}

				if (plugin.getConfig().showPrayerTimer())
				{
					sbTitle.append(" (");
					var timeUntilAttack = Math.max((double) ((attack.getTick() - plugin.getGameTick()) * 600 - (System.currentTimeMillis() - plugin.getLastTick())) / 1000, 0);
					sbTitle.append(String.format("%.1f", timeUntilAttack));
					sbTitle.append(")");
				}

				final ImagePanelComponent imagePanelComponent = new ImagePanelComponent();
				imagePanelComponent.setTitle(sbTitle.toString());
				imagePanelComponent.setImage(prayerImage);
				if (!client.isPrayerActive(prayer))
				{
					imagePanelComponent.setBackgroundColor(new Color(150, 0, 0, 128));
				}

				return imagePanelComponent.render(graphics);
			}
		}

		return null;


	}
}
