package net.runelite.client.plugins.nex;

import java.util.Set;

public class NexMinion
{
	private static final Set<String> minionMap = Set.of(
		NexText.CAN_ATTACK_CRUOR,
		NexText.CAN_ATTACK_FUMUS,
		NexText.CAN_ATTACK_GLACIES,
		NexText.CAN_ATTACK_UMBRA
	);

	public static boolean minionActive(String message)
	{
		return minionMap.contains(message);
	}
}
