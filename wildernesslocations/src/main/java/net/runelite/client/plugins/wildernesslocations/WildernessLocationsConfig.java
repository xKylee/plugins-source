/*
 * Copyright (c) 2019, St0newall
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

package net.runelite.client.plugins.wildernesslocations;

import java.awt.Color;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Keybind;

@ConfigGroup("wildernesslocations")
public interface WildernessLocationsConfig extends Config
{

	@ConfigItem(
		name = "Mirror Mode Compatibility?",
		keyName = "mirrorMode",
		description = "Should we show the overlay on Mirror Mode?",
		position = 0
	)
	default boolean mirrorMode()
	{
		return false;
	}

	@ConfigItem(
		keyName = "drawOverlay",
		name = "Draw Overlay",
		description = "Configure drawing wilderness locations overlay",
		position = 1
	)
	default boolean drawOverlay()
	{
		return true;
	}

	@ConfigItem(
		keyName = "pvpWorld",
		name = "PvP World",
		description = "When enabled, shows location when anywhere in a PvP World",
		position = 2
	)
	default boolean pvpWorld()
	{
		return true;
	}

	@ConfigItem(
		keyName = "keybind",
		name = "Send to CC",
		description = "Configure button to send current location to CC",
		position = 3
	)
	default Keybind keybind()
	{
		return Keybind.NOT_SET;
	}

	@ConfigItem(
		keyName = "worldMapNames",
		name = "Show Loc Names World Map",
		description = "Toggles whether or not Wilderness Location names are shown on the World Map",
		position = 4
	)
	default boolean worldMapOverlay()
	{
		return true;
	}

	@ConfigItem(
		keyName = "outlineLocations",
		name = "Outline Locations",
		description = "Draws an outline for the area of each location in the wilderness",
		position = 5
	)
	default boolean outlineLocations()
	{
		return false;
	}

	@ConfigItem(
		keyName = "mapOverlayColor",
		name = "World Map Color",
		description = "The color for the names and outlines of the locations on the World Map",
		position = 6
	)
	default Color mapOverlayColor()
	{
		return Color.cyan;
	}


}
