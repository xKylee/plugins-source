package net.runelite.client.plugins.theatre.rooms;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.NPC;
import net.runelite.api.coords.WorldPoint;

@Getter(AccessLevel.PACKAGE)
class MemorizedTornado
{
	@Getter(AccessLevel.PACKAGE)
	private NPC npc;

	@Getter(AccessLevel.PACKAGE)
	@Setter(AccessLevel.PACKAGE)
	private WorldPoint lastPosition;

	@Getter(AccessLevel.PACKAGE)
	@Setter(AccessLevel.PACKAGE)
	private WorldPoint currentPosition;

	MemorizedTornado(final NPC npc)
	{
		this.npc = npc;
		this.lastPosition = null;
		this.currentPosition = null;
	}

	public int getRelativeXDelta(WorldPoint pt)
	{
		return pt.getX() - this.currentPosition.getX() - (pt.getX() - this.lastPosition.getX());
	}

	public int getRelativeYDelta(WorldPoint pt)
	{
		return pt.getY() - this.currentPosition.getY() - (pt.getY() - this.lastPosition.getY());
	}

	public int getRelativeDelta(WorldPoint pt)
	{
		//if the tornado is newly spawned and doesn't have positions loaded, return -1
		if (this.lastPosition == null || this.currentPosition == null)
		{
			return -1;
		}

		//if the last position is equal to the current position, it didn't move yet. return -1
		if (this.lastPosition.distanceTo(this.currentPosition) == 0)
		{
			return -1;
		}

		return pt.distanceTo(this.currentPosition) - pt.distanceTo(this.lastPosition);
	}
}