/*
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
package net.runelite.client.plugins.alchemicalhydra;

import java.awt.Color;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigTitleSection;

@ConfigGroup("betterHydra")
public interface AlchemicalHydraConfig extends Config
{
	@ConfigTitleSection(
		keyName = "features",
		name = "Features",
		description = "Feathers. Jk, features",
		position = 0
	)
	default boolean features()
	{
		return true;
	}

	@ConfigItem(
		keyName = "counting",
		name = "Prayer helper",
		description = "Basically everything this plugin is known for. Also has attacks between specs and poison overlay. Shouldn't NOT use this tbh",
		position = 1,
		titleSection = "features"
	)
	default boolean counting()
	{
		return true;
	}

	@ConfigItem(
		keyName = "fountain",
		name = "Fountain helper",
		description = "Indicates if hydra is on a fountain",
		position = 2,
		titleSection = "features"
	)
	default boolean fountain()
	{
		return true;
	}

	@ConfigItem(
		keyName = "stun",
		name = "Stun timer",
		description = "Shows when you can walk in fire phase",
		position = 3,
		titleSection = "features"
	)
	default boolean stun()
	{
		return false;
	}

	@ConfigTitleSection(
		keyName = "colors",
		name = "Colors",
		description = "colors...",
		position = 2
	)
	default boolean colors()
	{
		return false;
	}

	@Alpha
	@ConfigItem(
		keyName = "safeCol",
		name = "Safe color",
		description = "Color overlay will be when there's >2 attacks left",
		position = 1,
		titleSection = "colors"
	)
	default Color safeCol()
	{
		return new Color(0, 156, 0, 156);
	}

	@Alpha
	@ConfigItem(
		keyName = "medCol",
		name = "Medium color",
		description = "Color overlay will be when a input is coming up",
		position = 2,
		titleSection = "colors"
	)
	default Color medCol()
	{
		return new Color(200, 156, 0, 156);
	}

	@Alpha
	@ConfigItem(
		keyName = "badCol",
		name = "Bad color",
		description = "Color overlay will be when you have to do something NOW",
		position = 3,
		titleSection = "colors"
	)
	default Color badCol()
	{
		return new Color(156, 0, 0, 156);
	}

	@Alpha
	@ConfigItem(
		keyName = "poisonBorderCol",
		name = "Poison border color",
		description = "Color the edges of the area highlighted by poison special will be",
		position = 4,
		titleSection = "colors"
	)
	default Color poisonBorderCol()
	{
		return new Color(255, 0, 0, 100);
	}

	@Alpha
	@ConfigItem(
		keyName = "poisonCol",
		name = "Poison color",
		description = "Color the fill of the area highlighted by poison special will be",
		position = 5,
		titleSection = "colors"
	)
	default Color poisonCol()
	{
		return new Color(255, 0, 0, 50);
	}

	@Alpha
	@ConfigItem(
		keyName = "fountainColA",
		name = "Fountain color (not on top)",
		description = "Fountain color (not on top)",
		position = 6,
		titleSection = "colors"
	)
	default Color fountainColA()
	{
		return new Color(255, 0, 0, 100);
	}

	@Alpha
	@ConfigItem(
		keyName = "fountainColB",
		name = "Fountain color (on top)",
		description = "Fountain color (on top)",
		position = 7,
		titleSection = "colors"
	)
	default Color fountainColB()
	{
		return new Color(0, 255, 0, 100);
	}
}
