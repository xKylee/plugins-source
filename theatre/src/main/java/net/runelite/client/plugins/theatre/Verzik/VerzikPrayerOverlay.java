package net.runelite.client.plugins.theatre.Verzik;

import java.util.Queue;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.client.plugins.theatre.TheatreConfig;
import net.runelite.client.plugins.theatre.prayer.TheatrePrayerOverlay;
import net.runelite.client.plugins.theatre.prayer.TheatreUpcomingAttack;

public class VerzikPrayerOverlay extends TheatrePrayerOverlay
{

	private final Verzik plugin;

	@Inject
	protected VerzikPrayerOverlay(Client client, TheatreConfig config, Verzik plugin)
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
		return getConfig().verzikPrayerHelper();
	}
}
