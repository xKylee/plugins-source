/*
 * Copyright (c) 2020 Dutta64 <https://github.com/Dutta64>
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
package net.runelite.client.plugins.hallowedsepulchre;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class HallowedSepulchreIDs
{
	static final int ARROW_9672 = 9672;
	static final int ARROW_9673 = 9673;
	static final int ARROW_9674 = 9674;

	static final int SWORD_9669 = 9669;
	static final int SWORD_9670 = 9670;
	static final int SWORD_9671 = 9671;

	static final int CROSSBOW_STATUE_38444 = 38444;
	static final int CROSSBOW_STATUE_38445 = 38445;
	static final int CROSSBOW_STATUE_38446 = 38446;

	static final int CROSSBOW_STATUE_ANIM_DEFAULT = 8681;

	static final int WIZARD_STATUE_38409 = 38409;
	static final int WIZARD_STATUE_38410 = 38410;
	static final int WIZARD_STATUE_38411 = 38411;
	static final int WIZARD_STATUE_38412 = 38412;
	static final int WIZARD_STATUE_ANIM_SPEED_4 = 4;

	static final int WIZARD_STATUE_38416 = 38416;
	static final int WIZARD_STATUE_38417 = 38417;
	static final int WIZARD_STATUE_38418 = 38418;
	static final int WIZARD_STATUE_38419 = 38419;
	static final int WIZARD_STATUE_38420 = 38420;
	static final int WIZARD_STATUE_ANIM_SPEED_3 = 3;

	static final int WIZARD_STATUE_38421 = 38421;
	static final int WIZARD_STATUE_38422 = 38422;
	static final int WIZARD_STATUE_38423 = 38423;
	static final int WIZARD_STATUE_38424 = 38424;
	static final int WIZARD_STATUE_38425 = 38425;
	static final int WIZARD_STATUE_ANIM_SPEED_2 = 2;

	static final int WIZARD_STATUE_ANIM_FIRE = 8658;

	static final Set<Integer> REGION_IDS = Stream.of(8794,
		8795,
		8796,
		8797,
		8798,
		9050,
		9051,
		9052,
		9053,
		9054,
		9306,
		9307,
		9308,
		9309,
		9310,
		9562,
		9563,
		9564,
//		9565, Lobby
		9566,
		9818,
		9819,
		9820,
		9821,
		9822,
		10074,
		10075,
		10076,
		10077,
		10078,
		10330,
		10331,
		10332,
		10333,
		10334)
		.collect(Collectors.toCollection(HashSet::new));
}
