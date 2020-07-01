/*
 * Copyright (c) 2017, Seth <Sethtroll3@gmail.com>
 * Copyright (c) 2018, Jordan Atwood <jordan.atwood423@gmail.com>
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

package net.runelite.client.plugins.socketplayerstatus.gametimer;

import java.util.regex.Pattern;

public class GameTimerConstant
{

	public static final String ANTIFIRE_DRINK_MESSAGE = "You drink some of your antifire potion.";
	public static final String ANTIFIRE_EXPIRED_MESSAGE =
		"<col=7f007f>Your antifire potion has expired.</col>";
	public static final String ANTIVENOM_DRINK_MESSAGE = "You drink some of your antivenom potion";
	public static final String CANNON_FURNACE_MESSAGE = "You add the furnace.";
	public static final String CANNON_PICKUP_MESSAGE = "You pick up the cannon. It's really heavy.";
	public static final String CANNON_REPAIR_MESSAGE =
		"You repair your cannon, restoring it to working order.";
	public static final String CHARGE_EXPIRED_MESSAGE =
		"<col=ef1020>Your magical charge fades away.</col>";
	public static final String CHARGE_MESSAGE =
		"<col=ef1020>You feel charged with magic power.</col>";
	public static final String EXTENDED_ANTIFIRE_DRINK_MESSAGE =
		"You drink some of your extended antifire potion.";
	public static final String EXTENDED_SUPER_ANTIFIRE_DRINK_MESSAGE =
		"You drink some of your extended super antifire potion.";
	public static final String FROZEN_MESSAGE = "<col=ef1020>You have been frozen!</col>";
	public static final String GOD_WARS_ALTAR_MESSAGE = "you recharge your prayer.";
	public static final String IMBUED_HEART_READY_MESSAGE =
		"<col=ef1020>Your imbued heart has regained its magical power.</col>";
	public static final String MAGIC_IMBUE_EXPIRED_MESSAGE = "Your Magic Imbue charge has ended.";
	public static final String MAGIC_IMBUE_MESSAGE = "You are charged to combine runes!";
	public static final String SANFEW_SERUM_DRINK_MESSAGE = "You drink some of your Sanfew Serum.";
	public static final String STAFF_OF_THE_DEAD_SPEC_EXPIRED_MESSAGE = "Your protection fades away";
	public static final String STAFF_OF_THE_DEAD_SPEC_MESSAGE =
		"Spirits of deceased evildoers offer you their protection";
	public static final String STAMINA_DRINK_MESSAGE = "You drink some of your stamina potion.";
	public static final String STAMINA_SHARED_DRINK_MESSAGE =
		"You have received a shared dose of stamina potion.";
	public static final String STAMINA_EXPIRED_MESSAGE =
		"<col=8f4808>Your stamina potion has expired.</col>";
	public static final String SUPER_ANTIFIRE_DRINK_MESSAGE =
		"You drink some of your super antifire potion";
	public static final String SUPER_ANTIFIRE_EXPIRED_MESSAGE =
		"<col=7f007f>Your super antifire potion has expired.</col>";
	public static final String SUPER_ANTIVENOM_DRINK_MESSAGE =
		"You drink some of your super antivenom potion";

	public static final Pattern DEADMAN_HALF_TELEBLOCK_PATTERN = Pattern.compile(
		"<col=4f006f>A Tele Block spell has been cast on you by (.+). It will expire in 1 minute, 15 seconds.</col>");
	public static final Pattern FULL_TELEBLOCK_PATTERN = Pattern.compile(
		"<col=4f006f>A Tele Block spell has been cast on you by (.+). It will expire in 5 minutes, 0 seconds.</col>");
	public static final Pattern HALF_TELEBLOCK_PATTERN = Pattern.compile(
		"<col=4f006f>A Tele Block spell has been cast on you by (.+). It will expire in 2 minutes, 30 seconds.</col>");

}
