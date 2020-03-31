/*
 * Copyright (c) 2019 Im2be <https://github.com/Im2be>
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

package net.runelite.client.plugins.cerberus.domain;

import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.NPC;
import net.runelite.api.Prayer;
import net.runelite.client.plugins.cerberus.Util.CerberusUtil;

@Slf4j
public class CerberusNPC
{

	@Getter(AccessLevel.PUBLIC)
	private final List<CerberusPhase> attacksDone;
	private NPC cerberus;
	@Getter(AccessLevel.PUBLIC)
	private int phaseCount = 0;
	@Getter(AccessLevel.PUBLIC)
	private int lastAttackTick;

	@Getter(AccessLevel.PUBLIC)
	private CerberusPhase lastAttackPhase = CerberusPhase.SPAWNING;

	@Getter(AccessLevel.PUBLIC)
	private Attack lastAttack;

	@Getter(AccessLevel.PUBLIC)
	@Setter(AccessLevel.PUBLIC)
	private int lastGhostYellTick;

	@Getter(AccessLevel.PUBLIC)
	@Setter(AccessLevel.PUBLIC)
	private long lastGhostYellTime;

	@Getter(AccessLevel.PUBLIC)
	@Setter(AccessLevel.PUBLIC)
	private Attack lastTripleAttack;

	private int hp = 600;

	public CerberusNPC(NPC cerberus)
	{
		this.cerberus = cerberus;
		this.attacksDone = new ArrayList<>();
	}

	public void nextPhase(CerberusPhase lastAttackPhase)
	{
		phaseCount++;
		this.lastAttackPhase = lastAttackPhase;
	}

	public void doProjectileOrAnimation(int gameTick, Attack attack)
	{
		lastAttackTick = gameTick;
		lastAttack = attack;
	}

	public NPC getNpc()
	{
		return cerberus;
	}

	public int getHealth()
	{
		var calcualtedHp = CerberusUtil.getExactHp(cerberus.getHealthRatio(), cerberus.getHealth(), 600);
		if (calcualtedHp != -1)
		{
			hp = calcualtedHp;
		}
		return hp;
	}

	//https://pastebin.com/hWCvantS
	public CerberusPhase getNextAttackPhase(int i, int hp)
	{
		var nextAttack = this.phaseCount + i;
		if (nextAttack == 0)
		{
			return CerberusPhase.SPAWNING;
		}

		if ((nextAttack - 1) % 10 == 0)
		{
			return CerberusPhase.TRIPLE;
		}

		if (nextAttack % 7 == 0 && hp <= 400)
		{
			return CerberusPhase.GHOSTS;
		}

		if (nextAttack % 5 == 0 && hp <= 200)
		{
			return CerberusPhase.LAVA;
		}

		return CerberusPhase.AUTO;
	}

	@Getter(AccessLevel.PUBLIC)
	@AllArgsConstructor
	public
	enum Attack
	{
		SPAWN(null, 0),
		AUTO(null, 1),
		MELEE(Prayer.PROTECT_FROM_MELEE, 1),
		RANGED(Prayer.PROTECT_FROM_MISSILES, 1),
		MAGIC(Prayer.PROTECT_FROM_MAGIC, 1),
		LAVA(null, 0),
		GHOSTS(null, 0),
		GHOST_MELEE(Prayer.PROTECT_FROM_MELEE, 2),
		GHOST_RANGED(Prayer.PROTECT_FROM_MISSILES, 2),
		GHOST_MAGIC(Prayer.PROTECT_FROM_MAGIC, 2);

		@Getter(AccessLevel.PUBLIC)
		private Prayer prayer;

		@Getter(AccessLevel.PUBLIC)
		private int priority;
	}
}
