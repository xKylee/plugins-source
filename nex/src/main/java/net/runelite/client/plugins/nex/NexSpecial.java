package net.runelite.client.plugins.nex;

import java.util.Map;

public enum NexSpecial
{
	CHOKE,
	DASH,
	EMBRACE_DARKNESS,
	BLOOD_SIPHON,
	BLOOD_SACRIFICE,
	BLOOD_SACRIFICE_PERSONAL,
	CONTAIN,
	ICE_PRISON;

	private static final Map<String, NexSpecial> specialMap = Map.of(
		NexText.NEX_SPECIAL_CONTAIN, CONTAIN,
		NexText.NEX_SPECIAL_DASH, DASH,
		NexText.NEX_SPECIAL_BLOOD_SACRIFICE, BLOOD_SACRIFICE,
		NexText.NEX_SPECIAL_CHOKE, CHOKE,
		NexText.NEX_SPECIAL_BLOOD_SIPHON, BLOOD_SIPHON,
		NexText.NEX_SPECIAL_ICE_PRISON, ICE_PRISON,
		NexText.NEX_SPECIAL_EMBRACE_DARKNESS, EMBRACE_DARKNESS,
		NexText.NEX_SPECIAL_BLOOD_SACRIFICE_PERSONAL, BLOOD_SACRIFICE_PERSONAL
	);

	public static NexSpecial mapSpecial(String message)
	{
		return specialMap.getOrDefault(message, null);
	}
}
