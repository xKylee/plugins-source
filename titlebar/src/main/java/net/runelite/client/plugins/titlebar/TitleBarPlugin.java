/*
 * Copyright (c) 2022, Xperiosa <https://github.com/Xperiosa>
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
package net.runelite.client.plugins.titlebar;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.events.BeforeRender;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.config.RuneLiteConfig;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientUI;
import net.runelite.client.util.ImageUtil;
import org.pf4j.Extension;
import javax.inject.Inject;
import java.awt.image.BufferedImage;

@Singleton
@Extension
@PluginDescriptor(
		name = "Title bar",
		description = "Change title bar to runelite",
		enabledByDefault = true,
		tags =
				{
						"title", "bar", "runelite", "openosrs"
				}
)
@Slf4j
public class TitleBarPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private TitleBarConfig titleBarConfig;

	@Inject
	private RuneLiteConfig runeLiteConfig;

	private javax.swing.JFrame clientFrame;
	private BufferedImage runeliteImage;
	private BufferedImage openosrsImage;

	@Provides
	TitleBarConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(TitleBarConfig.class);
	}

	@Override
	protected void startUp()
	{
		this.clientFrame = ClientUI.getFrame();
		this.runeliteImage = ImageUtil.loadImageResource(ClientUI.class, "/runelite.png");
		this.openosrsImage = ImageUtil.loadImageResource(ClientUI.class, "/openosrs.png");

		if (titleBarConfig.runeliteTitleBar())
		{
			setRuneLiteTitleBar();
		}
	}

	@Override
	protected void shutDown()
	{
		setOpenOSRSTitleBar();
	}

	@Subscribe
	private void onConfigChanged(ConfigChanged event)
	{
		if (!event.getGroup().startsWith("titlebar"))
		{
			return;
		}

		if (titleBarConfig.runeliteTitleBar())
		{
			setRuneLiteTitleBar();
		}
		else
		{
			setOpenOSRSTitleBar();
		}
	}

	@Subscribe
	private void onBeforeRender(BeforeRender event)
	{
		if (titleBarConfig.runeliteTitleBar() && !clientFrame.getTitle().startsWith("RuneLite"))
		{
			setRuneLiteTitleBar();
		}
	}

	private void setRuneLiteTitleBar()
	{
		Player player = client.getLocalPlayer();
		if (player != null && runeLiteConfig.usernameInTitle())
		{
			clientFrame.setTitle("RuneLite - " + player.getName());
		}
		else
		{
			clientFrame.setTitle("RuneLite");
		}
		clientFrame.setIconImage(runeliteImage);
	}

	private void setOpenOSRSTitleBar()
	{
		Player player = client.getLocalPlayer();
		if (player != null && runeLiteConfig.usernameInTitle())
		{
			clientFrame.setTitle("OpenOSRS - " + player.getName());
		}
		else
		{
			clientFrame.setTitle("OpenOSRS");
		}
		clientFrame.setIconImage(openosrsImage);
	}
}