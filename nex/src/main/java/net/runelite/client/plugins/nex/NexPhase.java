package net.runelite.client.plugins.nex;

import java.util.Map;
import java.util.Optional;
import net.runelite.api.NPC;
import static net.runelite.api.NpcID.CRUOR;
import static net.runelite.api.NpcID.FUMUS;
import static net.runelite.api.NpcID.GLACIES;
import static net.runelite.api.NpcID.UMBRA;
import net.runelite.api.Player;
import net.runelite.api.Prayer;

public enum NexPhase
{
	NONE,
	STARTING,
	SMOKE,
	SHADOW,
	BLOOD,
	ICE,
	ZAROS;

	private static final Map<String, NexPhase> phaseMap = Map.of(
		NexText.NEX_FIGHT_ENDING, NONE,
		NexText.NEX_FIGHT_BEGINNING, STARTING,
		NexText.NEX_SMOKE_PHASE, SMOKE,
		NexText.NEX_SHADOW_PHASE, SHADOW,
		NexText.NEX_BLOOD_PHASE, BLOOD,
		NexText.NEX_ICE_PHASE, ICE,
		NexText.NEX_ZAROS_PHASE, ZAROS
	);

	private static final Map<NexPhase, Optional<Prayer>> prayerPhase = Map.of(
		NONE, Optional.empty(),
		STARTING, Optional.of(Prayer.PROTECT_FROM_MAGIC),
		SMOKE, Optional.of(Prayer.PROTECT_FROM_MAGIC),
		SHADOW, Optional.of(Prayer.PROTECT_FROM_MISSILES),
		BLOOD, Optional.of(Prayer.PROTECT_FROM_MAGIC),
		ICE, Optional.of(Prayer.PROTECT_FROM_MAGIC),
		ZAROS, Optional.of(Prayer.PROTECT_FROM_MAGIC)
	);


	public static NexPhase mapPhase(String message)
	{
		return phaseMap.getOrDefault(message, null);
	}

	public static Prayer phasePrayer(NexPhase phase, Player player, NPC nex, boolean trappedInIce)
	{
		// turns out you can be in ice during zaros
		if ((phase == ICE || phase == ZAROS) && trappedInIce)
		{
			return Prayer.PROTECT_FROM_MISSILES;
		}
		else if (phase == ZAROS && nex.getInteracting() == player && nex.getWorldArea().isInMeleeDistance(player.getWorldLocation()))
		{
			return Prayer.PROTECT_FROM_MELEE;
		}
		else
		{
			return prayerPhase.getOrDefault(phase, Optional.empty()).orElse(null);
		}
	}

	public static int getMinionId(NexPhase phase)
	{
		switch (phase)
		{
			case ICE:
				return GLACIES;
			case BLOOD:
				return CRUOR;
			case SMOKE:
				return FUMUS;
			case SHADOW:
				return UMBRA;
			default:
				return -1;
		}
	}

	public static double getHpPercentage(NexPhase phase, double current)
	{
		var start = 100;
		var end = 0;

		switch (phase)
		{
			case SMOKE:
				start = 100;
				end = 80;
				break;
			case SHADOW:
				start = 80;
				end = 60;
				break;
			case BLOOD:
				start = 60;
				end = 40;
				break;
			case ICE:
				start = 40;
				end = 20;
				break;
		}

		var percent = (current - end) / (start - end) * 100.0;
		return Math.max(0, percent);
	}
}



