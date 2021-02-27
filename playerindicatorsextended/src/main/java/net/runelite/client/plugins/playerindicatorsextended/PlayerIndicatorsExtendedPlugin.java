/*
 * Copyright (c) 2018, Tomas Slusny <slusnucky@gmail.com>
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
package net.runelite.client.plugins.playerindicatorsextended;

import com.google.inject.Provides;
import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.inject.Inject;
import lombok.AccessLevel;
import lombok.Getter;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.FriendsChatManager;
import net.runelite.api.FriendsChatMember;
import net.runelite.api.FriendsChatRank;
import static net.runelite.api.FriendsChatRank.UNRANKED;
import static net.runelite.api.MenuAction.*;
import net.runelite.api.MenuEntry;
import net.runelite.api.Player;
import net.runelite.api.Varbits;
import net.runelite.api.WorldType;
import net.runelite.api.events.FriendsChatMemberJoined;
import net.runelite.api.events.FriendsChatMemberLeft;
import net.runelite.api.events.InteractingChanged;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.PlayerSpawned;
import net.runelite.api.util.Text;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.FriendChatManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.friendtagging.FriendTaggingPlugin;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ColorUtil;
import net.runelite.client.util.PvPUtil;
import net.runelite.http.api.RuneLiteAPI;
import net.runelite.http.api.hiscore.HiscoreClient;
import net.runelite.http.api.hiscore.HiscoreResult;
import org.pf4j.Extension;

@Extension
@PluginDescriptor(
	name = "Player Indicators",
	description = "Highlight players on-screen and/or on the minimap",
	tags = {"highlight", "minimap", "overlay", "players", "pklite"}
)
@Getter(AccessLevel.PACKAGE)
@PluginDependency(FriendTaggingPlugin.class)
public class PlayerIndicatorsExtendedPlugin extends Plugin
{
	private static final HiscoreClient HISCORE_CLIENT = new HiscoreClient(RuneLiteAPI.CLIENT);
	private final List<String> callers = new ArrayList<>();
	private final Map<Player, PlayerRelation> colorizedMenus = new ConcurrentHashMap<>();
	private final Map<PlayerRelation, Color> relationColorHashMap = new ConcurrentHashMap<>();
	private final Map<PlayerRelation, Object[]> locationHashMap = new ConcurrentHashMap<>();
	private final Map<String, Actor> callerPiles = new ConcurrentHashMap<>();
	@Getter(AccessLevel.PACKAGE)
	private final Map<String, HiscoreResult> resultCache = new HashMap<>();
	private final ExecutorService executorService = Executors.newFixedThreadPool(100);

	@Inject
	@Getter(AccessLevel.NONE)
	private OverlayManager overlayManager;

	@Inject
	@Getter(AccessLevel.NONE)
	private PlayerIndicatorsExtendedConfig config;

	@Inject
	@Getter(AccessLevel.NONE)
	private PlayerIndicatorsExtendedOverlay playerIndicatorsExtendedOverlay;

	@Inject
	@Getter(AccessLevel.NONE)
	private PlayerIndicatorsExtendedMinimapOverlay playerIndicatorsExtendedMinimapOverlay;

	@Inject
	@Getter(AccessLevel.NONE)
	private Client client;

	@Inject
	@Getter(AccessLevel.NONE)
	private FriendChatManager friendChatManager;

	@Inject
	@Getter(AccessLevel.NONE)
	private EventBus eventBus;


	@Provides
	PlayerIndicatorsExtendedConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(PlayerIndicatorsExtendedConfig.class);
	}

	@Override
	protected void startUp()
	{
		updateConfig();
		resultCache.clear();
		overlayManager.add(playerIndicatorsExtendedOverlay);
		overlayManager.add(playerIndicatorsExtendedMinimapOverlay);
		getCallerList();
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(playerIndicatorsExtendedOverlay);
		overlayManager.remove(playerIndicatorsExtendedMinimapOverlay);
		resultCache.clear();
	}

	@Subscribe
	private void onInteractingChanged(InteractingChanged event)
	{
		if (!config.callersTargets() || event.getSource() == null || callers.isEmpty() || !isCaller(event.getSource()))
		{
			return;
		}

		final Actor caller = event.getSource();

		if (this.callerPiles.containsKey(caller.getName()))
		{
			if (event.getTarget() == null)
			{
				callerPiles.remove(caller.getName());
				return;
			}

			callerPiles.replace(caller.getName(), event.getTarget());
			return;
		}

		if (event.getTarget() == null)
		{
			return;
		}

		callerPiles.put(caller.getName(), event.getTarget());
	}

	@Subscribe
	private void onConfigChanged(ConfigChanged event)
	{
		if (!event.getGroup().equals("playerindicators"))
		{
			return;
		}

		updateConfig();
	}

	@Subscribe
	private void onFriendsChatMemberJoined(FriendsChatMemberJoined event)
	{
		getCallerList();
	}

	@Subscribe
	private void onFriendsChatMemberLeft(FriendsChatMemberLeft event)
	{
		getCallerList();
	}

	@Subscribe
	private void onPlayerSpawned(PlayerSpawned event)
	{
		final Player player = event.getPlayer();

		if (!config.showAgilityLevel() || resultCache.containsKey(player.getName())
			|| (client.getVar(Varbits.IN_WILDERNESS) == 0 && !WorldType.isAllPvpWorld(client.getWorldType())))
		{
			return;
		}

		executorService.submit(() ->
		{
			int timeout = 0;
			HiscoreResult result;
			do
			{
				if (timeout >= 10)
				{
					return;
				}
				try
				{
					result = HISCORE_CLIENT.lookup(player.getName());
				}
				catch (IOException ex)
				{
					timeout++;
					result = null;
					try
					{
						Thread.sleep(250);
					}
					catch (InterruptedException e)
					{
						e.printStackTrace();
					}
				}
			}
			while (result == null);

			resultCache.put(player.getName(), result);
		});
	}

	@Subscribe
	private void onMenuEntryAdded(MenuEntryAdded menuEntryAdded)
	{
		int type = menuEntryAdded.getOpcode();

		if (type >= 2000)
		{
			type -= 2000;
		}

		int identifier = menuEntryAdded.getIdentifier();
		if (type == FOLLOW.getId() || type == TRADE.getId()
			|| type == SPELL_CAST_ON_PLAYER.getId() || type == ITEM_USE_ON_PLAYER.getId()
			|| type == PLAYER_FIRST_OPTION.getId()
			|| type == PLAYER_SECOND_OPTION.getId()
			|| type == PLAYER_THIRD_OPTION.getId()
			|| type == PLAYER_FOURTH_OPTION.getId()
			|| type == PLAYER_FIFTH_OPTION.getId()
			|| type == PLAYER_SIXTH_OPTION.getId()
			|| type == PLAYER_SEVENTH_OPTION.getId()
			|| type == PLAYER_EIGTH_OPTION.getId()
			|| type == RUNELITE.getId())
		{
			final Player localPlayer = client.getLocalPlayer();
			final Player[] players = client.getCachedPlayers();
			Player player = null;

			if (identifier >= 0 && identifier < players.length)
			{
				player = players[identifier];
			}

			if (player == null)
			{
				return;
			}

			int image = -1;
			int image2 = -1;
			Color color = null;

			if (config.highlightCallers() && isCaller(player))
			{
				if (Arrays.asList(this.locationHashMap.get(PlayerRelation.CALLER)).contains(PlayerIndicationLocation.MENU))
				{
					color = relationColorHashMap.get(PlayerRelation.CALLER);
				}
			}
			else if (config.callersTargets() && isPile(player))
			{
				if (Arrays.asList(this.locationHashMap.get(PlayerRelation.CALLER_TARGET)).contains(PlayerIndicationLocation.MENU))
				{
					color = relationColorHashMap.get(PlayerRelation.CALLER_TARGET);
				}
			}
			else if (config.highlightFriends() && client.isFriended(player.getName(), false))
			{
				if (Arrays.asList(this.locationHashMap.get(PlayerRelation.FRIEND)).contains(PlayerIndicationLocation.MENU))
				{
					color = relationColorHashMap.get(PlayerRelation.FRIEND);
				}
			}
			else if (config.highlightClan() && player.isFriendsChatMember())
			{
				if (Arrays.asList(this.locationHashMap.get(PlayerRelation.CLAN)).contains(PlayerIndicationLocation.MENU))
				{
					color = relationColorHashMap.get(PlayerRelation.CLAN);
				}

				FriendsChatRank rank = friendChatManager.getRank(player.getName());
				if (rank != UNRANKED)
				{
					image = friendChatManager.getIconNumber(rank);
				}
			}
			else if (config.highlightTeamMembers() && player.getTeam() > 0 && (localPlayer != null ? localPlayer.getTeam() : -1) == player.getTeam())
			{
				if (Arrays.asList(this.locationHashMap.get(PlayerRelation.TEAM)).contains(PlayerIndicationLocation.MENU))
				{
					color = relationColorHashMap.get(PlayerRelation.TEAM);
				}
			}
			else if (config.highlightOtherPlayers() && !player.isFriendsChatMember() && !player.isFriend() && !PvPUtil.isAttackable(client, player))
			{
				if (Arrays.asList(this.locationHashMap.get(PlayerRelation.OTHER)).contains(PlayerIndicationLocation.MENU))
				{
					color = relationColorHashMap.get(PlayerRelation.OTHER);
				}
			}
			else if (config.highlightTargets() && !player.isFriendsChatMember() && !client.isFriended(player.getName(),
				false) && PvPUtil.isAttackable(client, player))
			{
				if (Arrays.asList(this.locationHashMap.get(PlayerRelation.TARGET)).contains(PlayerIndicationLocation.MENU))
				{
					color = relationColorHashMap.get(PlayerRelation.TARGET);
				}
			}


			if (config.playerSkull() && !player.isFriendsChatMember() && player.getSkullIcon() != null)
			{
				image2 = 35;
			}

			if (image != -1 || color != null)
			{
				final MenuEntry[] menuEntries = client.getMenuEntries();
				final MenuEntry lastEntry = menuEntries[menuEntries.length - 1];


				if (color != null)
				{
					// strip out existing <col...
					String target = lastEntry.getTarget();
					final int idx = target.indexOf('>');
					if (idx != -1)
					{
						target = target.substring(idx + 1);
					}

					lastEntry.setTarget(ColorUtil.prependColorTag(target, color));
				}
				if (image != -1)
				{
					lastEntry.setTarget("<img=" + image + ">" + lastEntry.getTarget());
				}

				if (image2 != -1 && config.playerSkull())
				{
					lastEntry.setTarget("<img=" + image2 + ">" + lastEntry.getTarget());
				}

				client.setMenuEntries(menuEntries);
			}
		}
	}


	private void getCallerList()
	{
		if (!config.highlightCallers())
		{
			return;
		}

		callers.clear();

		final FriendsChatManager clanMemberManager = client.getFriendsChatManager();
		if (config.useClanchatRanks() && clanMemberManager != null)
		{
			for (FriendsChatMember clanMember : clanMemberManager.getMembers())
			{
				if (clanMember.getRank().getValue() >= config.callerRank().getValue())
				{
					callers.add(Text.standardize(clanMember.getName()));
				}
			}
		}

		if (config.callers().contains(","))
		{
			callers.addAll(Arrays.asList(config.callers().split(",")));
			return;
		}

		if (!config.callers().equals(""))
		{
			callers.add(config.callers());
		}
	}

	/**
	 * Checks if a player is a caller
	 *
	 * @param player The player to check
	 * @return true if they are, false otherwise
	 */
	boolean isCaller(Actor player)
	{
		if (player == null || player.getName() == null)
		{
			return false;
		}

		if (callers.size() > 0)
		{
			for (String name : callers)
			{
				String finalName = Text.standardize(name.trim());
				if (Text.standardize(player.getName()).equals(finalName))
				{
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Checks if a player is currently a target of any of the current callers
	 *
	 * @param actor The player to check
	 * @return true if they are a target, false otherwise
	 */
	public boolean isPile(Actor actor)
	{
		/**
		 if (!(actor instanceof Player))
		 {
		 return false;
		 }
		 **/
		if (actor == null)
		{
			return false;
		}

		return callerPiles.containsValue(actor);
	}


	private void updateConfig()
	{
		locationHashMap.clear();
		relationColorHashMap.clear();

		if (config.highlightOwnPlayer())
		{
			relationColorHashMap.put(PlayerRelation.SELF, config.getOwnPlayerColor());
			if (config.selfIndicatorModes() != null)
			{
				locationHashMap.put(PlayerRelation.SELF, EnumSet.copyOf(config.selfIndicatorModes()).toArray());
			}
		}

		if (config.highlightFriends())
		{
			relationColorHashMap.put(PlayerRelation.FRIEND, config.getFriendColor());
			if (config.friendIndicatorMode() != null)
			{
				locationHashMap.put(PlayerRelation.FRIEND, config.friendIndicatorMode().toArray());
			}
		}

		if (config.highlightClan())
		{
			relationColorHashMap.put(PlayerRelation.CLAN, config.getFriendsChatColor());
			if (config.friendsChatIndicatorModes() != null)
			{
				locationHashMap.put(PlayerRelation.CLAN, config.friendsChatIndicatorModes().toArray());
			}
		}

		if (config.highlightTeamMembers())
		{
			relationColorHashMap.put(PlayerRelation.TEAM, config.getTeamcolor());
			if (config.teamIndicatorModes() != null)
			{
				locationHashMap.put(PlayerRelation.TEAM, config.teamIndicatorModes().toArray());
			}
		}

		if (config.highlightOtherPlayers())
		{
			relationColorHashMap.put(PlayerRelation.OTHER, config.getOtherColor());
			if (config.otherIndicatorModes() != null)
			{
				locationHashMap.put(PlayerRelation.OTHER, EnumSet.copyOf(config.otherIndicatorModes()).toArray());
			}
		}

		if (config.highlightTargets())
		{
			relationColorHashMap.put(PlayerRelation.TARGET, config.getTargetsColor());
			if (config.targetsIndicatorModes() != null)
			{
				locationHashMap.put(PlayerRelation.TARGET, config.targetsIndicatorModes().toArray());
			}
		}

		if (config.highlightCallers())
		{
			relationColorHashMap.put(PlayerRelation.CALLER, config.callerColor());
			if (config.callerHighlightOptions() != null)
			{
				locationHashMap.put(PlayerRelation.CALLER, config.callerHighlightOptions().toArray());
			}
			getCallerList();
		}

		if (config.callersTargets())
		{
			relationColorHashMap.put(PlayerRelation.CALLER_TARGET, config.callerTargetColor());
			if (config.callerTargetHighlightOptions() != null)
			{
				locationHashMap.put(PlayerRelation.CALLER_TARGET, config.callerTargetHighlightOptions().toArray());
			}
		}
	}

	public enum MinimapSkullLocations
	{
		BEFORE_NAME,
		AFTER_NAME
	}

	public enum AgilityFormats
	{
		TEXT,
		ICONS
	}

	public enum PlayerIndicationLocation
	{
		/**
		 * Indicates the player by rendering their username above their head
		 */
		ABOVE_HEAD,
		/**
		 * Indicates the player by outlining the player model's hull.
		 * NOTE: this may cause FPS lag if enabled for lots of players
		 */
		HULL,
		/**
		 * Indicates the player by rendering their username on the minimap
		 */
		MINIMAP,
		/**
		 * Indicates the player by colorizing their right click menu
		 */
		MENU,
		/**
		 * Indicates the player by rendering a tile marker underneath them
		 */
		TILE
	}

	public enum PlayerRelation
	{
		SELF,
		FRIEND,
		CLAN,
		TEAM,
		TARGET,
		OTHER,
		CALLER,
		CALLER_TARGET
	}
}
