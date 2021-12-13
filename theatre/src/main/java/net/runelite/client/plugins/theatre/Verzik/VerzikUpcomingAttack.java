package net.runelite.client.plugins.theatre.Verzik;

import lombok.AccessLevel;
import lombok.Getter;
import net.runelite.api.Prayer;

public class VerzikUpcomingAttack implements Comparable<VerzikUpcomingAttack>
{
	@Getter(AccessLevel.PACKAGE)
	private int ticksUntil;

	@Getter(AccessLevel.PACKAGE)
	private final Prayer prayer;

	public VerzikUpcomingAttack(int ticksUntil, Prayer prayer)
	{
		this.ticksUntil = ticksUntil;
		this.prayer = prayer;
	}

	void decrementTicks()
	{
		if (ticksUntil > 0)
		{
			ticksUntil -= 1;
		}
	}

	boolean shouldRemove()
	{
		return ticksUntil == 0;
	}

	@Override
	public int compareTo(VerzikUpcomingAttack verzikUpcomingAttack)
	{
		return Integer.compare(ticksUntil, verzikUpcomingAttack.ticksUntil);
	}
}
