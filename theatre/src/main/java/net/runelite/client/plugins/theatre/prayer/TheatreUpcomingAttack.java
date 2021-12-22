package net.runelite.client.plugins.theatre.prayer;

import java.util.Comparator;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import net.runelite.api.Prayer;

public class TheatreUpcomingAttack implements Comparable<TheatreUpcomingAttack>
{
	@Getter(AccessLevel.PUBLIC)
	private int ticksUntil;

	@Getter(AccessLevel.PUBLIC)
	private final Prayer prayer;

	@Getter(AccessLevel.PUBLIC)
	private final int priority;

	public TheatreUpcomingAttack(int ticksUntil, Prayer prayer, int priority)
	{
		this.ticksUntil = ticksUntil;
		this.prayer = prayer;
		this.priority = priority;
	}

	public TheatreUpcomingAttack(int ticksUntil, Prayer prayer)
	{
		this(ticksUntil, prayer, 0);
	}

	public void decrementTicks()
	{
		if (ticksUntil > 0)
		{
			ticksUntil -= 1;
		}
	}

	public boolean shouldRemove()
	{
		return ticksUntil == 0;
	}

	@Override
	public int compareTo(@NonNull TheatreUpcomingAttack o)
	{
		return Comparator.comparing(TheatreUpcomingAttack::getTicksUntil).thenComparing(TheatreUpcomingAttack::getPriority).compare(this, o);
	}
}
