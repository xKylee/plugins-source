/*
 * Copyright (c) 2019, Owain van Brakel <https://github.com/Owain94>
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

package net.runelite.client.plugins.playerattacktimer;

import com.google.common.base.Splitter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;


public final class ConfigParser
{
	private ConfigParser()
	{

	}

	public static boolean parse_config(final String value)
	{
		return parse(value).isPresent();
	}

	public static Optional<HashMap<Integer, AnimationTickMapEntry>> parse(final String value)
	{
		if (value.isEmpty() || value.isBlank())
		{
			return Optional.empty();
		}

		final Splitter NEWLINE_SPLITTER = Splitter
			.on("\n")
			.omitEmptyStrings()
			.trimResults();

		HashMap<Integer, AnimationTickMapEntry> tickMap = new HashMap<>();

		for (String line : NEWLINE_SPLITTER.split(value))
		{
			String[] segments = line.split(":");

			try
			{
				int animation = Integer.parseInt(segments[0]);
				int delay = Integer.parseInt(segments[1]);

				AttackPrayer prayer_choice = null;
				if (segments.length > 2)
				{
					if (Arrays.stream(AttackPrayer.prayerLetters).noneMatch(prayer -> prayer.equals(segments[2].toLowerCase())))
					{
						return Optional.empty();
					}

					switch (segments[2].toLowerCase())
					{
						case "r":
							prayer_choice = AttackPrayer.Rigour;
							break;
						case "a":
							prayer_choice = AttackPrayer.Augury;
							break;
						case "p":
							prayer_choice = AttackPrayer.Piety;
							break;
						case "e":
							prayer_choice = AttackPrayer.EagleEye;
							break;
						case "m":
							prayer_choice = AttackPrayer.MysticMight;
							break;
						case "c":
							prayer_choice = AttackPrayer.Chivalry;
							break;
						case "h":
							prayer_choice = AttackPrayer.HawkEye;
							break;
					}
				}

				tickMap.put(animation, new AnimationTickMapEntry(delay, prayer_choice));


			}
			catch (Exception e)
			{
				return Optional.empty();
			}
		}

		return Optional.of(tickMap);
	}
}
