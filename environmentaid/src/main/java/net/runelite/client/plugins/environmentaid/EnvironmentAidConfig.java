/*
 * Copyright (c) 2020, <github.com/xKylee> xKylee
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
package net.runelite.client.plugins.environmentaid;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("environmentAid")
public interface EnvironmentAidConfig extends Config
{
	@ConfigItem(
		keyName = "showBarrowsMinimap",
		name = "Show Barrows Minimap",
		description = "Configures whether or not the minimap is displayed",
		position = 1
	)
	default boolean showBarrowsMinimap()
	{
		return true;
	}

	@ConfigItem(
		keyName = "zamorakDarkness",
		name = "Remove darkness",
		description = "Removes the on-screen darkening effect in the Zamorak chamber of GWD",
		position = 2
	)
	default boolean zamorakEffect()
	{
		return false;
	}

	@ConfigItem(
		keyName = "snowTrollheim",
		name = "Remove snow",
		description = "Removes the on-screen snow effect north of Trollheim & God Wars entrance",
		position = 3
	)
	default boolean snowEffect()
	{
		return false;
	}

	@ConfigItem(
		position = 4,
		keyName = "waterEffect",
		name = "Remove Underwater",
		description = "Removes the on-screen water effect in when diving underwater"
	)
	default boolean waterEffect()
	{
		return false;
	}

	@ConfigItem(
		position = 5,
		keyName = "scryPool",
		name = "Remove Scry Pool",
		description = "Removes the on-screen Scry Pool effect"
	)
	default boolean scryPool()
	{
		return false;
	}
}
