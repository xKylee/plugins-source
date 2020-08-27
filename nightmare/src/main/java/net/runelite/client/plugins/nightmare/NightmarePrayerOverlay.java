package net.runelite.client.plugins.nightmare;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.OverlayUtil;

@Singleton
@Slf4j
class NightmarePrayerOverlay extends Overlay
{
	private final Client client;
	private final NightmarePlugin plugin;
	private final NightmareConfig config;

	@Inject
	private NightmarePrayerOverlay(final Client client, final NightmarePlugin plugin, final NightmareConfig config)
	{
		this.client = client;
		this.plugin = plugin;
		this.config = config;
		setPosition(OverlayPosition.DYNAMIC);
		determineLayer();
		setPriority(OverlayPriority.LOW);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!plugin.isInFight() || plugin.getNm() == null)
		{
			return null;
		}

		NightmareAttack attack = plugin.getPendingNightmareAttack();

		if (attack == null)
		{
			return null;
		}

		if (!config.prayerHelper().showWidgetHelper())
		{
			return null;
		}

		Color color = client.isPrayerActive(attack.getPrayer()) ? Color.GREEN : Color.RED;
		OverlayUtil.renderPrayerOverlay(graphics, client, attack.getPrayer(), color);

		return null;
	}

	public void determineLayer()
	{
		if (config.mirrorMode())
		{
			setLayer(OverlayLayer.AFTER_MIRROR);
		}
		if (!config.mirrorMode())
		{
			setLayer(OverlayLayer.ABOVE_WIDGETS);
		}
	}
}