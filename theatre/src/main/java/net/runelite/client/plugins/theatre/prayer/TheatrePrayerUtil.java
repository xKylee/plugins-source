package net.runelite.client.plugins.theatre.prayer;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

public class TheatrePrayerUtil
{
	public static void updateNextPrayerQueue(Queue<TheatreUpcomingAttack> queue)
	{
		queue.forEach(TheatreUpcomingAttack::decrementTicks);
		queue.removeIf(TheatreUpcomingAttack::shouldRemove);
	}

	// Map ticks until to prayer
	public static Map<Integer, TheatreUpcomingAttack> getTickPriorityMap(Queue<TheatreUpcomingAttack> queue)
	{
		Map<Integer, TheatreUpcomingAttack> map = new HashMap<>();

		queue.forEach(attack -> {
			if (!map.containsKey(attack.getTicksUntil()))
			{
				map.put(attack.getTicksUntil(), attack);
			}

			if (attack.getPriority() < map.get(attack.getTicksUntil()).getPriority())
			{
				map.put(attack.getTicksUntil(), attack);
			}
		});

		return map;
	}
}
