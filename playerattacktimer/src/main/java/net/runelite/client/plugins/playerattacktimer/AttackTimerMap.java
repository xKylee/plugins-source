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
		// sorted by animation ID
		ATTACK_TIMER_MAP.put(386, 4); // arclight stab
		ATTACK_TIMER_MAP.put(390, 4); // arclight slash
		ATTACK_TIMER_MAP.put(393, 4);
		ATTACK_TIMER_MAP.put(395, 6); // Battleaxe slash
		ATTACK_TIMER_MAP.put(400, 5); // Pickaxe crush
		ATTACK_TIMER_MAP.put(401, 5); // Pickaxe stab + battleaxe crush (5 tick and 6 tick respectively)
		ATTACK_TIMER_MAP.put(426, 5); // tbow rapid, 6 tick accurate/long range
		ATTACK_TIMER_MAP.put(428, 7); // Halberd Stab
		ATTACK_TIMER_MAP.put(440, 7); // Halberd Slash
		ATTACK_TIMER_MAP.put(1167, 4);
		ATTACK_TIMER_MAP.put(1203, 7); // Halberd special
		ATTACK_TIMER_MAP.put(1378, 6);
		ATTACK_TIMER_MAP.put(1658, 4); // Abyssal whip
		ATTACK_TIMER_MAP.put(1711, 4);
		ATTACK_TIMER_MAP.put(1978, 5); // Ice Blitz
		ATTACK_TIMER_MAP.put(1979, 5); // Ice Barrage
		ATTACK_TIMER_MAP.put(5061, 2); // Blowpipe - note we assume 2 ticks but is 3 ticks in PVP and on long range/accurate
		ATTACK_TIMER_MAP.put(7514, 4);
		ATTACK_TIMER_MAP.put(7554, 2); // Darts
		ATTACK_TIMER_MAP.put(8056, 5);
		ATTACK_TIMER_MAP.put(8145, 4);
		ATTACK_TIMER_MAP.put(8288, 4);
		ATTACK_TIMER_MAP.put(76181, 4);
	}
}
