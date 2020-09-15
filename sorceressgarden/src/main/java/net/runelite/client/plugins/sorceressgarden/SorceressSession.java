package net.runelite.client.plugins.sorceressgarden;

import javax.inject.Singleton;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ItemID;

@Slf4j
@Singleton
public class SorceressSession
{
	@Getter(AccessLevel.PACKAGE)
	private int winterSqirk;

	@Getter(AccessLevel.PACKAGE)
	private int springSqirk;

	@Getter(AccessLevel.PACKAGE)
	private int autumnSqirk;

	@Getter(AccessLevel.PACKAGE)
	private int summerSqirk;

	void incrementSqirks(int sqirkID)
	{
		switch (sqirkID)
		{
			case ItemID.WINTER_SQIRK:
				winterSqirk++;
				log.info("Winter sqirk is now " + Integer.toString(winterSqirk));
				break;
			case ItemID.SPRING_SQIRK:
				springSqirk++;
				break;
			case ItemID.AUTUMN_SQIRK:
				autumnSqirk++;
				break;
			case ItemID.SUMMER_SQIRK:
				summerSqirk++;
				break;
			default:
				log.debug("Invalid sqirk specified. The sqirk count will not be updated.");
		}
	}
}
