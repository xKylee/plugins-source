package net.runelite.client.plugins.nex;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.runelite.api.Player;

@AllArgsConstructor
@EqualsAndHashCode
@Getter
public class NexCoughingPlayer
{
	private String name;
	@EqualsAndHashCode.Exclude
	private int lastCoughCycle;
	@EqualsAndHashCode.Exclude
	private Player player;

	public boolean shouldRemove(int currentCycle)
	{
		return currentCycle - lastCoughCycle > 360; // 360 cycles == 12 game ticks
	}
}
