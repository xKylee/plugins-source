package net.runelite.client.plugins.fightcavespawnrotation.util;

public class FightCavesNpcSpawn
{
	private final FightCavesNpc fightCavesNpc;
	private final int spawnLocation;

	public String toString()
	{
		return String.format("%s -> %d", fightCavesNpc.getName(), spawnLocation);
	}

	public FightCavesNpcSpawn(FightCavesNpc fightCavesNpc, int spawnLocation)
	{
		this.fightCavesNpc = fightCavesNpc;
		this.spawnLocation = spawnLocation;
	}

	public FightCavesNpc getFightCavesNpc()
	{
		return fightCavesNpc;
	}

	public int getSpawnLocation()
	{
		return spawnLocation;
	}
}