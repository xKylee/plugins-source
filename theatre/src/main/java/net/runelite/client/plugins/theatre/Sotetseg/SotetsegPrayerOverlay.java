package net.runelite.client.plugins.theatre.Sotetseg;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.Queue;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.plugins.theatre.TheatreConfig;
import net.runelite.client.plugins.theatre.Verzik.Verzik;
import net.runelite.client.plugins.theatre.prayer.TheatrePrayerOverlay;
import net.runelite.client.plugins.theatre.prayer.TheatreUpcomingAttack;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.OverlayUtil;

public class SotetsegPrayerOverlay extends TheatrePrayerOverlay
{
	private final Sotetseg plugin;

	@Inject
	protected SotetsegPrayerOverlay(Client client, TheatreConfig config, Sotetseg plugin)
	{
		super(client, config);
		this.plugin = plugin;
	}

	@Override
	protected Queue<TheatreUpcomingAttack> getAttackQueue()
	{
		return plugin.getUpcomingAttackQueue();
	}

	@Override
	protected long getLastTick()
	{
		return plugin.getLastTick();
	}

	@Override
	protected boolean isEnabled()
	{
		return getConfig().sotetsegPrayerHelper();
	}
}
