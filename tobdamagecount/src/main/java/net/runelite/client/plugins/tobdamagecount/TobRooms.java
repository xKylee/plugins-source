/*
 * Copyright (c) 2020, Trevor <https://github.com/Trevor159>
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
package net.runelite.client.plugins.tobdamagecount;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import net.runelite.api.NpcID;

public enum TobRooms
{
	MAIDEN(
		"Maiden",
		NpcID.THE_MAIDEN_OF_SUGADINTI,
		NpcID.THE_MAIDEN_OF_SUGADINTI_8361,
		NpcID.THE_MAIDEN_OF_SUGADINTI_8362,
		NpcID.THE_MAIDEN_OF_SUGADINTI_8363,
		NpcID.THE_MAIDEN_OF_SUGADINTI_8364,
		NpcID.NYLOCAS_MATOMENOS,
		NpcID.BLOOD_SPAWN
	),
	BLOAT(
		"Bloat",
		NpcID.PESTILENT_BLOAT
	),
	NYLOCAS_WAVES(
		"Nylocas Waves",
		NpcID.NYLOCAS_ISCHYROS_8342,    //8342
		NpcID.NYLOCAS_TOXOBOLOS_8343,    //8343
		NpcID.NYLOCAS_HAGIOS,            //8344
		NpcID.NYLOCAS_ISCHYROS_8345,    //8345
		NpcID.NYLOCAS_TOXOBOLOS_8346,    //8346
		NpcID.NYLOCAS_HAGIOS_8347,        //8347
		NpcID.NYLOCAS_ISCHYROS_8348,    //4348
		NpcID.NYLOCAS_TOXOBOLOS_8349,    //8349
		NpcID.NYLOCAS_HAGIOS_8350,        //8350
		NpcID.NYLOCAS_ISCHYROS_8351,    //8351
		NpcID.NYLOCAS_TOXOBOLOS_8352,    //8352
		NpcID.NYLOCAS_HAGIOS_8353        //8353
	),
	NYLOCAS_BOSS(
		"Nylocas Boss",
		NpcID.NYLOCAS_VASILIAS_8355,
		NpcID.NYLOCAS_VASILIAS_8356,
		NpcID.NYLOCAS_VASILIAS_8357
	),
	SOTETSEG(
		"Sotetseg",
		NpcID.SOTETSEG,
		NpcID.SOTETSEG_8388
	),
	XARPUS(
		"Xarpus",
		NpcID.XARPUS,
		NpcID.XARPUS_8339,
		NpcID.XARPUS_8340,
		NpcID.XARPUS_8341
	),
	VERZIK_P1("Verzik P1", NpcID.VERZIK_VITUR_8370),
	VERZIK_P2(
		"Verzik P2",
		NpcID.VERZIK_VITUR_8372,
		NpcID.VERZIK_VITUR_8373,
		NpcID.NYLOCAS_HAGIOS_8383,
		NpcID.NYLOCAS_ISCHYROS_8381,
		NpcID.NYLOCAS_TOXOBOLOS_8382,
		NpcID.NYLOCAS_ATHANATOS,
		NpcID.NYLOCAS_MATOMENOS_8385
	),
	VERZIK_P3(
		"Verzik P3",
		NpcID.VERZIK_VITUR_8374,
		NpcID.VERZIK_VITUR_8375,
		NpcID.NYLOCAS_HAGIOS_8383,
		NpcID.NYLOCAS_ISCHYROS_8381,
		NpcID.NYLOCAS_TOXOBOLOS_8382,
		NpcID.WEB);

	private String name;

	@Getter
	private Set<Integer> npcIds;

	TobRooms(String name, Integer... npcIDs)
	{
		this.name = name;
		this.npcIds = new HashSet<>(Arrays.asList(npcIDs));
	}

	@Override
	public String toString()
	{
		return this.name;
	}
}
