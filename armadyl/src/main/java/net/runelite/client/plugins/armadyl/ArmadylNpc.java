/*
 * BSD 2-Clause License
 *
 * Copyright (c) 2020, dutta64 <https://github.com/dutta64>
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

package net.runelite.client.plugins.armadyl;

import java.awt.Color;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.Actor;
import net.runelite.api.AnimationID;
import net.runelite.api.NPC;
import net.runelite.api.NpcID;
import net.runelite.api.Prayer;

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
class ArmadylNpc
{
	@Getter
	@EqualsAndHashCode.Include
	private final NPC npc;
	@Getter
	private final int npcId;

	private final int animationTickSpeed;

	private final int animationId;

	@Getter
	private final Color color;

	@Getter
	private int ticksUntilNextAnimation;

	@Getter
	private final AttackStyle attackStyle;

	ArmadylNpc(final NPC npc)
	{
		this.npc = npc;
		this.npcId = npc.getId();
		this.ticksUntilNextAnimation = 0;

		final Boss boss = Boss.of(npcId);
		this.animationId = boss.animationId;
		this.animationTickSpeed = boss.attackSpeed;
		this.color = boss.color;
		this.attackStyle = boss.attackStyle;
	}

	void updateTicksUntilNextAnimation()
	{
		if (ticksUntilNextAnimation >= 0)
		{
			ticksUntilNextAnimation--;
		}

		if (npc.getAnimation() == animationId && ticksUntilNextAnimation < 1)
		{
			ticksUntilNextAnimation = animationTickSpeed;
		}
	}

	Actor getInteractingActor()
	{
		return npc.getInteracting();
	}

	@RequiredArgsConstructor
	enum Boss
	{
		KREEARRA(NpcID.KREEARRA, AnimationID.KREE_RANGED, 3, AttackStyle.RANGE, Color.ORANGE),
		FLIGHT_KILISA(NpcID.FLIGHT_KILISA, AnimationID.KILISA_AUTO, 5, AttackStyle.MELEE, Color.RED),
		FLOCKLEADER_GEERIN(NpcID.FLOCKLEADER_GEERIN, AnimationID.GEERIN_AUTO, 5, AttackStyle.RANGE, Color.GREEN),
		WINGMAN_SKREE(NpcID.WINGMAN_SKREE, AnimationID.SKREE_AUTO, 5, AttackStyle.MAGE, Color.CYAN);

		private final int npcId;
		private final int animationId;
		private final int attackSpeed;
		private final AttackStyle attackStyle;
		private final Color color;

		static Boss of(final int npcId)
		{
			for (final Boss boss : Boss.values())
			{
				if (boss.npcId == npcId)
				{
					return boss;
				}
			}

			throw new IllegalArgumentException("Unsupported Boss npcId");
		}
	}

	@RequiredArgsConstructor
	@Getter
	public enum AttackStyle
	{
		MAGE(Color.CYAN, Prayer.PROTECT_FROM_MAGIC),
		RANGE(Color.GREEN, Prayer.PROTECT_FROM_MISSILES),
		MELEE(Color.RED, Prayer.PROTECT_FROM_MELEE);

		private final Color color;
		private final Prayer prayer;
	}
}
