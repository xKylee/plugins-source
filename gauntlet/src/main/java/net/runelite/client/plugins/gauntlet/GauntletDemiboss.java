/*
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

package net.runelite.client.plugins.gauntlet;

import java.awt.Color;
import java.util.Set;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.runelite.api.NPC;
import net.runelite.api.NpcID;

@Getter(AccessLevel.PACKAGE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
class GauntletDemiboss
{
	@EqualsAndHashCode.Include
	final NPC npc;

	final Demiboss demiboss;

	GauntletDemiboss(final NPC npc)
	{
		this.npc = npc;
		this.demiboss = Demiboss.fromId(npc.getId());
	}

	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	enum Demiboss
	{
		BEAR(Set.of(NpcID.CRYSTALLINE_BEAR, NpcID.CORRUPTED_BEAR), Color.RED),
		DARK_BEAST(Set.of(NpcID.CRYSTALLINE_DARK_BEAST, NpcID.CORRUPTED_DARK_BEAST), Color.GREEN),
		DRAGON(Set.of(NpcID.CRYSTALLINE_DRAGON, NpcID.CORRUPTED_DRAGON), Color.BLUE);

		private final Set<Integer> ids;

		@Getter(AccessLevel.PACKAGE)
		private final Color outlineColor;

		static Demiboss fromId(final int id)
		{
			for (final Demiboss demiboss : Demiboss.values())
			{
				if (demiboss.ids.contains(id))
				{
					return demiboss;
				}
			}

			return null;
		}
	}
}
