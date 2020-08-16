package net.runelite.client.plugins.playerattacktimer;

import java.util.HashMap;
import java.util.Map;

final class AttackTimerMap
{
	private AttackTimerMap()
	{
	}

	static final Map<Integer, Integer> ATTACK_TIMER_MAP = new HashMap<>();

	static
	{
		ATTACK_TIMER_MAP.put(1658, 4); // Abyssal whip
	}
}
