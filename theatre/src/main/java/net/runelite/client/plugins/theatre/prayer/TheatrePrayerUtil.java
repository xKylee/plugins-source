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

	public static Map<Integer, Integer> getTickPriorityMap(Queue<TheatreUpcomingAttack> queue)
	{
		Map<Integer, Integer> map = new HashMap<>();

		queue.forEach(attack -> {
			map.putIfAbsent(attack.getTicksUntil(), attack.getPriority());
			if (map.get(attack.getTicksUntil()) < attack.getPriority()) {
				map.put(attack.getTicksUntil(), attack.getPriority());
			}
		});

		return map;
	}
}
