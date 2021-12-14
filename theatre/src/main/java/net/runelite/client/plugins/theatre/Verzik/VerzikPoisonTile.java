package net.runelite.client.plugins.theatre.Verzik;

import java.util.Objects;
import java.util.Set;
import lombok.Getter;
import net.runelite.api.coords.WorldPoint;

public class VerzikPoisonTile
{
	private static final int VERZIK_P2_POISON_TICKS = 14;

	@Getter
	WorldPoint tile;

	@Getter
	int ticksRemaining;

	public VerzikPoisonTile(WorldPoint tile)
	{
		this.tile = tile;
		this.ticksRemaining = VERZIK_P2_POISON_TICKS;
	}

	public void decrement()
	{
		if (ticksRemaining > 0)
		{
			ticksRemaining -= 1;
		}
	}

	public boolean isDead()
	{
		return ticksRemaining == 0;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (o == null || getClass() != o.getClass())
		{
			return false;
		}
		VerzikPoisonTile that = (VerzikPoisonTile) o;
		return tile.equals(that.tile);
	}

	public boolean shouldHighlight()
	{
		return ticksRemaining < 4;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(tile);
	}

	static void updateTiles(Set<VerzikPoisonTile> tileSet)
	{
		tileSet.forEach(VerzikPoisonTile::decrement);
		tileSet.removeIf(VerzikPoisonTile::isDead);
	}
}
