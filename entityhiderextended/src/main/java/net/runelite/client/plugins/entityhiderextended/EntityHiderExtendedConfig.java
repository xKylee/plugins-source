/*
 * Copyright (c) 2018, Lotto <https://github.com/devLotto>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.entityhiderextended;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("entityhiderextended")
public interface EntityHiderExtendedConfig extends Config
{	@ConfigItem(
		position = 0,
		keyName = "hideDeadNPCs",
		name = "Hide Dead NPCs",
		description = "Configures whether or not NPCs that just died are hidden"
	)
	default boolean hideDeadNPCs()
	{
		return false;
	}

	@ConfigItem(
		position = 1,
		keyName = "hideNPCsOnDeath",
		name = "Hide NPCs On Death",
		description = "Configures which NPCs to hide when they die"
	)
	default String hideNPCsOnDeath()
	{
		return "";
	}

	@ConfigItem(
		position = 2,
		keyName = "blacklistDeadNpcs",
		name = "Hide on Death Blacklist",
		description = "Configures which NPCs NOT to hide when they die",
		hidden = true,
		unhide = "hideDeadNPCs"
	)
	default String blacklistDeadNpcs()
	{
		return "";
	}

	@ConfigItem(
		position = 3,
		keyName = "hideSpecificPlayers",
		name = "Hide Specific Players",
		description = "Hides players you never wish to see."
	)
	default String hideSpecificPlayers()
	{
		return "";
	}
}