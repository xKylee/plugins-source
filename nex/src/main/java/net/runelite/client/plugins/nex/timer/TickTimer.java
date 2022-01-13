package net.runelite.client.plugins.nex.timer;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode
public class TickTimer
{
	@Getter
	private int ticks = 0;

	private final Action action;

	public TickTimer(Action action)
	{
		this.action = action;
	}

	public TickTimer()
	{
		this.action = null;
	}

	public void setTicks(int ticks)
	{
		this.ticks = Math.max(0, ticks);
	}

	public void setTicksIfExpired(int ticks)
	{
		if (isExpired())
		{
			setTicks(ticks);
		}
	}

	public void tick()
	{
		if (ticks > 0)
		{
			ticks -= 1;

			if (ticks == 0 && action != null)
			{
				action.method();
			}
		}
	}

	public void reset()
	{
		setTicks(0);
	}

	public boolean isActive()
	{
		return getTicks() > 0;
	}

	public boolean isExpired()
	{
		return !isActive();
	}

	@Override
	public String toString()
	{
		return String.valueOf(ticks);
	}
}
