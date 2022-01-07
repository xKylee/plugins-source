package net.runelite.client.plugins.nex;

import java.util.Map;

public enum NexSpecial
{
	CHOKE,
	EMBRACE_DARKNESS,
	BLOOD_SIPHON,
	BLOOD_SACRIFICE,
	CONTAIN,
	ICE_PRISON;

	private static final Map<String, NexSpecial> specialMap = Map.of(
		NexText.NEX_SPECIAL_CONTAIN, CONTAIN,
		NexText.NEX_SPECIAL_BLOOD_SACRIFICE, BLOOD_SACRIFICE,
		NexText.NEX_SPECIAL_CHOKE, CHOKE,
		NexText.NEX_SPECIAL_BLOOD_SIPHON, BLOOD_SIPHON,
		NexText.NEX_SPECIAL_ICE_PRISON, ICE_PRISON,
		NexText.NEX_SPECIAL_EMBRACE_DARKNESS, EMBRACE_DARKNESS
	);


	public static NexSpecial mapSpecial(String message)
	{
		return specialMap.getOrDefault(message, null);
	}
}
