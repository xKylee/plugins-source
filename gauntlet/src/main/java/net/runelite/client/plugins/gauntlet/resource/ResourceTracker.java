/*
 * Copyright (c) 2020, dutta64 <https://github.com/dutta64>
 * Copyright (c) 2020, Anthony Alves
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

package net.runelite.client.plugins.gauntlet.resource;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import net.runelite.api.Client;
import net.runelite.api.ItemID;
import net.runelite.api.Player;
import net.runelite.api.util.Text;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.gauntlet.GauntletConfig;
import net.runelite.client.plugins.gauntlet.GauntletPlugin;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;

@Singleton
public class ResourceTracker
{
	private static final int CORRUPTED_GAUNTLET_REGION_ID = 7768;

	private static final String MESSAGE_UNTRADEABLE_DROP = "Untradeable drop: ";
	private static final Pattern PATTERN_RESOURCE_DROP = Pattern.compile("((?<quantity>\\d+) x )?(?<name>.+)");

	private final GauntletPlugin plugin;
	private final GauntletConfig config;
	private final ItemManager itemManager;
	private final InfoBoxManager infoBoxManager;

	@Getter
	private final Map<Integer, Integer> resourceCounts;

	private String namedDropMessage;

	@Inject
	ResourceTracker(final GauntletPlugin plugin, final GauntletConfig config, final ItemManager itemManager, final InfoBoxManager infoBoxManager)
	{
		this.plugin = plugin;
		this.config = config;
		this.itemManager = itemManager;
		this.infoBoxManager = infoBoxManager;

		this.resourceCounts = new HashMap<>();
		this.namedDropMessage = null;
	}

	public void reset()
	{
		namedDropMessage = null;
		resourceCounts.clear();
		infoBoxManager.removeIf(c -> c instanceof InfoboxResourceCounter);
	}

	public void parseChatMessage(String chatMessage)
	{
		if (!config.resourceTracker() || namedDropMessage == null)
		{
			return;
		}

		final String messagePrefix = plugin.isLootDropNotificationsEnabled() ? MESSAGE_UNTRADEABLE_DROP : namedDropMessage;

		final boolean corrupted = plugin.getInstanceRegionId() == CORRUPTED_GAUNTLET_REGION_ID;

		chatMessage = Text.removeTags(chatMessage);

		if (chatMessage.startsWith(messagePrefix))
		{
			chatMessage = chatMessage.replace(messagePrefix, "");

			processMobItemDrop(chatMessage, corrupted);
		}
		else
		{
			processSkillItemDrop(chatMessage, corrupted);
		}
	}

	public void setNamedDropMessage(final Client client)
	{
		final Player player = client.getLocalPlayer();

		if (player == null)
		{
			namedDropMessage = null;
			return;
		}

		namedDropMessage = player.getName() + " received a drop: ";
	}

	private void processMobItemDrop(final String chatMessage, final boolean corrupted)
	{
		final Matcher matcher = PATTERN_RESOURCE_DROP.matcher(chatMessage);

		if (!matcher.matches())
		{
			return;
		}

		final String itemName = matcher.group("name");

		if (itemName == null)
		{
			return;
		}

		final GauntletResource resource = GauntletResource.fromName(itemName, corrupted);

		if (resource == null)
		{
			return;
		}

		if (config.resourceTrackerFilter() == GauntletConfig.ResourceFilter.DEFAULT)
		{
			switch (resource)
			{
				case TELEPORT_CRYSTAL:
				case CORRUPTED_TELEPORT_CRYSTAL:
				case WEAPON_FRAME:
				case CORRUPTED_WEAPON_FRAME:
				case CRYSTALLINE_BOWSTRING:
				case CORRUPTED_BOWSTRING:
				case CRYSTAL_SPIKE:
				case CORRUPTED_SPIKE:
				case CRYSTAL_ORB:
				case CORRUPTED_ORB:
					return;
				default:
					break;
			}
		}

		final int itemId = resource.itemId;

		final String quantity = matcher.group("quantity");

		if (quantity != null)
		{
			incrementItem(itemId, Integer.parseInt(quantity));
		}
		else
		{
			incrementItem(itemId, 1);
		}
	}


	private void processSkillItemDrop(final String chatMessage, final boolean corrupted)
	{
		for (final GauntletResource resource : GauntletResource.values())
		{
			if (resource.pattern == null ||
				(corrupted != resource.corrupted && resource != GauntletResource.RAW_PADDLEFISH))
			{
				continue;
			}

			final Matcher matcher = resource.pattern.matcher(chatMessage);

			if (!matcher.matches())
			{
				continue;
			}

			final int itemId = resource.itemId;

			if (matcher.groupCount() == 1)
			{
				// crystal shards
				incrementItem(itemId, Integer.parseInt(matcher.group(1)));
			}
			else
			{
				incrementItem(itemId, 1);
			}

			break;
		}
	}

	private void incrementItem(final int itemId, final int itemCount)
	{
		final int count = resourceCounts.computeIfAbsent(itemId, id -> 0);

		if (count == 0)
		{
			infoBoxManager.addInfoBox(new InfoboxResourceCounter(itemManager.getImage(itemId),
				plugin,
				config,
				resourceCounts,
				itemId,
				itemCount
			));
		}

		resourceCounts.put(itemId, count + itemCount);
	}

	private enum GauntletResource
	{
		TELEPORT_CRYSTAL("Teleport crystal", ItemID.TELEPORT_CRYSTAL, null, false),
		CORRUPTED_TELEPORT_CRYSTAL("Corrupted teleport crystal", ItemID.CORRUPTED_TELEPORT_CRYSTAL, null, true),

		WEAPON_FRAME("Weapon frame", ItemID.WEAPON_FRAME_23871, null, false),
		CORRUPTED_WEAPON_FRAME("Weapon frame", ItemID.WEAPON_FRAME, null, true),

		CRYSTALLINE_BOWSTRING("Crystalline bowstring", ItemID.CRYSTALLINE_BOWSTRING, null, false),
		CORRUPTED_BOWSTRING("Corrupted bowstring", ItemID.CORRUPTED_BOWSTRING, null, true),

		CRYSTAL_SPIKE("Crystal spike", ItemID.CRYSTAL_SPIKE, null, false),
		CORRUPTED_SPIKE("Corrupted spike", ItemID.CORRUPTED_SPIKE, null, true),

		CRYSTAL_ORB("Crystal orb", ItemID.CRYSTAL_ORB, null, false),
		CORRUPTED_ORB("Corrupted orb", ItemID.CORRUPTED_ORB, null, true),

		RAW_PADDLEFISH("Raw paddlefish", ItemID.RAW_PADDLEFISH, "You manage to catch a fish\\.", false),

		CRYSTAL_SHARDS("Crystal shards", ItemID.CRYSTAL_SHARDS, "You find (\\d+) crystal shards\\.", false),
		CORRUPTED_SHARDS("Corrupted shards", ItemID.CORRUPTED_SHARDS, "You find (\\d+) corrupted shards\\.", true),

		CRYSTAL_ORE("Crystal ore", ItemID.CRYSTAL_ORE, "You manage to mine some ore\\.", false),
		CORRUPTED_ORE("Corrupted ore", ItemID.CORRUPTED_ORE, "You manage to mine some ore\\.", true),

		PHREN_BARK("Phren bark", ItemID.PHREN_BARK_23878, "You get some bark\\.", false),
		CORRUPTED_PHREN_BARK("Phren bark", ItemID.PHREN_BARK, "You get some bark\\.", true),

		LINUM_TIRINUM("Linum tirinum", ItemID.LINUM_TIRINUM_23876, "You pick some fibre from the plant\\.", false),
		CORRUPTED_LINUM_TIRINUM("Linum tirinum", ItemID.LINUM_TIRINUM, "You pick some fibre from the plant\\.", true),

		GRYM_LEAF("Grym leaf", ItemID.GRYM_LEAF_23875, "You pick a herb from the roots\\.", false),
		CORRUPTED_GRYM_LEAF("Grym leaf", ItemID.GRYM_LEAF, "You pick a herb from the roots\\.", true),
		;

		private final String name;

		private final Pattern pattern;

		private final int itemId;

		private final boolean corrupted;

		GauntletResource(final String name, final int itemId, final String pattern, final boolean corrupted)
		{
			this.name = name;
			this.itemId = itemId;
			this.corrupted = corrupted;
			this.pattern = pattern != null ? Pattern.compile(pattern) : null;
		}

		static GauntletResource fromName(final String name, final boolean corrupted)
		{
			for (final GauntletResource resource : values())
			{
				if ((corrupted == resource.corrupted || resource == RAW_PADDLEFISH) && resource.name.equals(name))
				{
					return resource;
				}
			}

			return null;
		}
	}
}
