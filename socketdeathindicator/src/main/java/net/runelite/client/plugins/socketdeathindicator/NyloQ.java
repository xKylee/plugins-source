package net.runelite.client.plugins.socketdeathindicator;

import net.runelite.api.NPC;

public class NyloQ
{
	public NPC npc;
	public int queuedDamage;
	public int hp;
	public int hiddenTicks;
	public boolean hidden;
	public int maxHP;
	public int phase;

	public NyloQ(NPC npc, int queuedDamage, int hp)
	{
		this.npc = npc;
		this.queuedDamage = queuedDamage;
		this.hp = hp;
		this.maxHP = hp;
		this.hiddenTicks = 0;
		this.hidden = false;
		this.phase = 0;
	}
}
