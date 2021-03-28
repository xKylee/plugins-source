package net.runelite.client.plugins.fightcavespawnrotation.util;

import lombok.Getter;

public enum FightCavesSpawnLocations
{
	NW(10, 50),
	C(30, 30),
	SE(50, 25),
	S(35, 15),
	SW(10, 15);

	@Getter
	private final int regionX;
	@Getter
	private final int regionY;

	FightCavesSpawnLocations(int regionX, int regionY)
	{
		this.regionX = regionX;
		this.regionY = regionY;
	}

	public static FightCavesSpawnLocations lookup(int spawnValue)
	{
		switch (spawnValue)
		{
			case 0:
			case 5:
			case 9:
				return SE;
			case 1:
			case 4:
			case 10:
				return SW;
			case 2:
			case 8:
			case 13:
				return C;
			case 3:
			case 7:
			case 12:
				return NW;
			case 6:
			case 11:
			case 14:
				return S;
			default:
				return null;
		}
	}
}