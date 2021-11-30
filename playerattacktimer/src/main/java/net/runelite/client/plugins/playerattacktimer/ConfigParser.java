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
import java.util.Map;

public final class ConfigParser
{
	private ConfigParser()
	{
	}

	public static boolean parse(final String value)
	{
		if (value.isEmpty() || value.isBlank())
		{
			return true;
		}

		try
		{
			final Splitter NEWLINE_SPLITTER = Splitter
				.on("\n")
				.omitEmptyStrings()
				.trimResults();

			final Map<String, String> tmp = NEWLINE_SPLITTER.withKeyValueSeparator(':').split(value);

			for (final Map.Entry<String, String> entry : tmp.entrySet())
			{
				Integer.parseInt(entry.getKey());
				Integer.parseInt(entry.getValue());
			}

			return true;
		}
		catch (final Exception ex)
		{
			return false;
		}
	}
}
