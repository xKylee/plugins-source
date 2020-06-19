/*
 * Copyright (c) 2020, dutta64 <https://github.com/dutta64>
 * Copyright (c) 2019, ganom <https://github.com/Ganom>
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

package net.runelite.client.plugins.gauntlet.entity;

import java.awt.Color;
import java.awt.image.BufferedImage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.runelite.api.NPC;
import net.runelite.api.Prayer;
import net.runelite.api.Skill;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.util.ImageUtil;

public class Hunllef
{
	private static final int MAX_PLAYER_ATTACKS = 6;
	private static final int MAX_BOSS_ATTACKS = 4;

	@Getter
	private final NPC npc;

	private final BufferedImage originalMagicIcon;
	private final BufferedImage originalRangeIcon;

	private BufferedImage magicIcon;
	private BufferedImage rangeIcon;

	@Getter
	private BossAttackPhase currentPhase;

	@Getter
	private int bossAttacks;
	@Getter
	private int playerAttacks;
	@Getter
	private int ticksUntilAttack;

	private int attackStyleIconSize;

	public Hunllef(final NPC npc, final int attackStyleIconSize, final SkillIconManager skillIconManager)
	{
		this.npc = npc;
		this.bossAttacks = MAX_BOSS_ATTACKS;
		this.playerAttacks = MAX_PLAYER_ATTACKS;
		this.ticksUntilAttack = 0;
		this.attackStyleIconSize = attackStyleIconSize;
		this.originalMagicIcon = skillIconManager.getSkillImage(Skill.MAGIC);
		this.originalRangeIcon = skillIconManager.getSkillImage(Skill.RANGED);
		this.currentPhase = BossAttackPhase.RANGE;
	}

	public void onGameTick()
	{
		if (ticksUntilAttack > 0)
		{
			ticksUntilAttack--;
		}
	}

	public void updatePlayerAttack()
	{
		playerAttacks--;

		if (playerAttacks <= 0)
		{
			playerAttacks = MAX_PLAYER_ATTACKS;
		}
	}

	public void updateAttack(final BossAttack style)
	{
		ticksUntilAttack = MAX_PLAYER_ATTACKS;

		switch (style)
		{
			case PRAYER:
			case MAGIC:
				if (currentPhase != BossAttackPhase.MAGIC)
				{
					currentPhase = BossAttackPhase.MAGIC;
					bossAttacks = MAX_BOSS_ATTACKS - 1;
				}
				else
				{
					bossAttacks--;
				}
				break;
			case RANGE:
				if (currentPhase != BossAttackPhase.RANGE)
				{
					currentPhase = BossAttackPhase.RANGE;
					bossAttacks = MAX_BOSS_ATTACKS - 1;
				}
				else
				{
					bossAttacks--;
				}
				break;
			case LIGHTNING:
				bossAttacks--;
				break;
		}

		if (bossAttacks <= 0)
		{
			final BossAttackPhase nextPhase;

			switch (currentPhase)
			{
				case MAGIC:
				default:
					bossAttacks = MAX_BOSS_ATTACKS;
					nextPhase = BossAttackPhase.RANGE;
					break;
				case RANGE:
					bossAttacks = MAX_BOSS_ATTACKS;
					nextPhase = BossAttackPhase.MAGIC;
					break;
			}

			currentPhase = nextPhase;
		}
	}

	public void setAttackStyleIconSize(final int attackStyleIconSize)
	{
		this.attackStyleIconSize = attackStyleIconSize;
		magicIcon = ImageUtil.resizeImage(originalMagicIcon, attackStyleIconSize, attackStyleIconSize);
		rangeIcon = ImageUtil.resizeImage(originalRangeIcon, attackStyleIconSize, attackStyleIconSize);
	}

	public BufferedImage getAttackStyleIcon()
	{
		final BufferedImage icon;

		switch (currentPhase)
		{
			case MAGIC:
				if (magicIcon == null)
				{
					magicIcon = ImageUtil.resizeImage(originalMagicIcon, attackStyleIconSize, attackStyleIconSize);
				}

				icon = magicIcon;
				break;
			case RANGE:
				if (rangeIcon == null)
				{
					rangeIcon = ImageUtil.resizeImage(originalRangeIcon, attackStyleIconSize, attackStyleIconSize);
				}

				icon = rangeIcon;
				break;
			default:
				throw new IllegalStateException("Unexpected boss attack phase: " + currentPhase);
		}

		return icon;
	}

	@AllArgsConstructor
	@Getter
	public enum BossAttackPhase
	{
		MAGIC(Color.CYAN, Prayer.PROTECT_FROM_MAGIC),
		RANGE(Color.GREEN, Prayer.PROTECT_FROM_MISSILES);

		private final Color color;
		private final Prayer prayer;
	}

	public enum BossAttack
	{
		MAGIC,
		RANGE,
		PRAYER,
		LIGHTNING
	}
}
