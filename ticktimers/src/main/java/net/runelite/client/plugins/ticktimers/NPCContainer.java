/*
 * Copyright (c) 2019, Ganom <https://github.com/Ganom>
 * Copyright (c) 2019, Lucas <https://github.com/lucwousin>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.ticktimers;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.awt.Color;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.runelite.api.Actor;
import net.runelite.api.NPC;
import net.runelite.api.NPCComposition;
import net.runelite.api.NpcID;
import net.runelite.api.Prayer;
import static net.runelite.client.plugins.ticktimers.TickTimersPlugin.BALFRUG_AUTO;
import static net.runelite.client.plugins.ticktimers.TickTimersPlugin.BREE_AUTO;
import static net.runelite.client.plugins.ticktimers.TickTimersPlugin.GEERIN_AUTO;
import static net.runelite.client.plugins.ticktimers.TickTimersPlugin.GEERIN_FLINCH;
import static net.runelite.client.plugins.ticktimers.TickTimersPlugin.GENERAL_AUTO1;
import static net.runelite.client.plugins.ticktimers.TickTimersPlugin.GENERAL_AUTO2;
import static net.runelite.client.plugins.ticktimers.TickTimersPlugin.GENERAL_AUTO3;
import static net.runelite.client.plugins.ticktimers.TickTimersPlugin.GROWLER_AUTO;
import static net.runelite.client.plugins.ticktimers.TickTimersPlugin.KILISA_AUTO;
import static net.runelite.client.plugins.ticktimers.TickTimersPlugin.KREE_RANGED;
import static net.runelite.client.plugins.ticktimers.TickTimersPlugin.KRIL_AUTO;
import static net.runelite.client.plugins.ticktimers.TickTimersPlugin.KRIL_SPEC;
import static net.runelite.client.plugins.ticktimers.TickTimersPlugin.MINION_AUTO1;
import static net.runelite.client.plugins.ticktimers.TickTimersPlugin.MINION_AUTO2;
import static net.runelite.client.plugins.ticktimers.TickTimersPlugin.MINION_AUTO3;
import static net.runelite.client.plugins.ticktimers.TickTimersPlugin.MINION_AUTO4;
import static net.runelite.client.plugins.ticktimers.TickTimersPlugin.SKREE_AUTO;
import static net.runelite.client.plugins.ticktimers.TickTimersPlugin.STARLIGHT_AUTO;
import static net.runelite.client.plugins.ticktimers.TickTimersPlugin.ZAKL_AUTO;
import static net.runelite.client.plugins.ticktimers.TickTimersPlugin.ZAMMY_GENERIC_AUTO;
import static net.runelite.client.plugins.ticktimers.TickTimersPlugin.ZILYANA_AUTO;
import static net.runelite.client.plugins.ticktimers.TickTimersPlugin.ZILYANA_MELEE_AUTO;
import static net.runelite.client.plugins.ticktimers.TickTimersPlugin.ZILYANA_SPEC;

@Getter(AccessLevel.PACKAGE)
class NPCContainer
{
	@Getter(AccessLevel.PACKAGE)
	private final NPC npc;
	private final int npcIndex;
	private final String npcName;
	private final ImmutableSet<Integer> animations;
	private final int attackSpeed;
	@Getter(AccessLevel.PACKAGE)
	private final BossMonsters monsterType;
	private int npcSize;
	@Setter(AccessLevel.PACKAGE)
	private int ticksUntilAttack;
	@Setter(AccessLevel.PACKAGE)
	private Actor npcInteracting;
	@Setter(AccessLevel.PACKAGE)
	private AttackStyle attackStyle;

	NPCContainer(final NPC npc)
	{
		this.npc = npc;
		this.npcName = npc.getName();
		this.npcIndex = npc.getIndex();
		this.npcInteracting = npc.getInteracting();
		this.attackStyle = AttackStyle.UNKNOWN;
		this.ticksUntilAttack = -1;
		final NPCComposition composition = npc.getTransformedComposition();

		BossMonsters monster = BossMonsters.of(npc.getId());

		if (monster == null)
		{
			throw new IllegalStateException();
		}

		this.monsterType = monster;
		this.animations = monster.animations;
		this.attackStyle = monster.attackStyle;
		this.attackSpeed = monster.attackSpeed;

		if (composition != null)
		{
			this.npcSize = composition.getSize();
		}
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(npc);
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
		NPCContainer that = (NPCContainer) o;
		return Objects.equals(npc, that.npc);
	}

	@RequiredArgsConstructor
	enum BossMonsters
	{
		SERGEANT_STRONGSTACK(NpcID.SERGEANT_STRONGSTACK, AttackStyle.MELEE, ImmutableSet.of(MINION_AUTO1, MINION_AUTO2, MINION_AUTO3), 5),
		SERGEANT_STEELWILL(NpcID.SERGEANT_STEELWILL, AttackStyle.MAGE, ImmutableSet.of(MINION_AUTO1, MINION_AUTO2, MINION_AUTO3), 5),
		SERGEANT_GRIMSPIKE(NpcID.SERGEANT_GRIMSPIKE, AttackStyle.RANGE, ImmutableSet.of(MINION_AUTO1, MINION_AUTO2, MINION_AUTO4), 5),
		GENERAL_GRAARDOR(NpcID.GENERAL_GRAARDOR, AttackStyle.MELEE, ImmutableSet.of(GENERAL_AUTO1, GENERAL_AUTO2, GENERAL_AUTO3), 6),

		TSTANON_KARLAK(NpcID.TSTANON_KARLAK, AttackStyle.MELEE, ImmutableSet.of(ZAMMY_GENERIC_AUTO), 5),
		BALFRUG_KREEYATH(NpcID.BALFRUG_KREEYATH, AttackStyle.MAGE, ImmutableSet.of(ZAMMY_GENERIC_AUTO, BALFRUG_AUTO), 5),
		ZAKLN_GRITCH(NpcID.ZAKLN_GRITCH, AttackStyle.RANGE, ImmutableSet.of(ZAMMY_GENERIC_AUTO, ZAKL_AUTO), 5),
		KRIL_TSUTSAROTH(NpcID.KRIL_TSUTSAROTH, AttackStyle.UNKNOWN, ImmutableSet.of(KRIL_SPEC, KRIL_AUTO), 6),

		STARLIGHT(NpcID.STARLIGHT, AttackStyle.MELEE, ImmutableSet.of(STARLIGHT_AUTO), 5),
		GROWLER(NpcID.GROWLER, AttackStyle.MAGE, ImmutableSet.of(GROWLER_AUTO), 5),
		BREE(NpcID.BREE, AttackStyle.RANGE, ImmutableSet.of(BREE_AUTO), 5),
		COMMANDER_ZILYANA(NpcID.COMMANDER_ZILYANA, AttackStyle.UNKNOWN, ImmutableSet.of(ZILYANA_AUTO, ZILYANA_MELEE_AUTO, ZILYANA_SPEC), 2),

		FLIGHT_KILISA(NpcID.FLIGHT_KILISA, AttackStyle.MELEE, ImmutableSet.of(KILISA_AUTO), 5),
		FLOCKLEADER_GEERIN(NpcID.FLOCKLEADER_GEERIN, AttackStyle.RANGE, ImmutableSet.of(GEERIN_AUTO, GEERIN_FLINCH), 5),
		WINGMAN_SKREE(NpcID.WINGMAN_SKREE, AttackStyle.MAGE, ImmutableSet.of(SKREE_AUTO), 5),
		KREEARRA(NpcID.KREEARRA, AttackStyle.RANGE, ImmutableSet.of(KREE_RANGED), 3);

		private static final ImmutableMap<Integer, BossMonsters> idMap;

		static
		{
			ImmutableMap.Builder<Integer, BossMonsters> builder = ImmutableMap.builder();

			for (BossMonsters monster : values())
			{
				builder.put(monster.npcID, monster);
			}

			idMap = builder.build();
		}

		private final int npcID;
		private final AttackStyle attackStyle;
		private final ImmutableSet<Integer> animations;
		private final int attackSpeed;

		static BossMonsters of(int npcID)
		{
			return idMap.get(npcID);
		}
	}

	@AllArgsConstructor
	@Getter
	public enum AttackStyle
	{
		MAGE("Mage", Color.CYAN, Prayer.PROTECT_FROM_MAGIC),
		RANGE("Range", Color.GREEN, Prayer.PROTECT_FROM_MISSILES),
		MELEE("Melee", Color.RED, Prayer.PROTECT_FROM_MELEE),
		UNKNOWN("Unknown", Color.WHITE, null);

		private String name;
		private Color color;
		private Prayer prayer;
	}
}
