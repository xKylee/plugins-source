/*
 * Copyright (c) 2020, Kyle <https://github.com/xKylee>
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
package net.runelite.client.plugins.menuentryswapperextended;

import com.google.common.base.Splitter;
import com.google.common.collect.Sets;
import com.google.inject.Provides;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import lombok.AccessLevel;
import lombok.Setter;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import static net.runelite.api.ItemID.FIRE_TIARA;
import static net.runelite.api.ItemID.MAX_CAPE;
import static net.runelite.api.ItemID.RUNECRAFT_CAPE;
import static net.runelite.api.ItemID.RUNECRAFT_CAPET;
import net.runelite.api.MenuEntry;
import net.runelite.api.Player;
import net.runelite.api.Varbits;
import static net.runelite.api.Varbits.BUILDING_MODE;
import static net.runelite.api.Varbits.IN_WILDERNESS;
import net.runelite.api.WorldType;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.FocusChanged;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.MenuOpened;
import net.runelite.api.events.PlayerAppearanceChanged;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.kit.KitType;
import net.runelite.api.util.Text;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.input.KeyManager;
import net.runelite.client.menus.AbstractComparableEntry;
import net.runelite.client.menus.EquipmentComparableEntry;
import static net.runelite.client.menus.ComparableEntries.newBaseComparableEntry;
import net.runelite.client.menus.MenuManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginManager;
import net.runelite.client.plugins.PluginType;
import net.runelite.client.plugins.menuentryswapperextended.util.MESAbstractComparables;
import net.runelite.client.plugins.pvptools.PvpToolsConfig;
import net.runelite.client.plugins.pvptools.PvpToolsPlugin;
import net.runelite.client.util.HotkeyListener;
import org.pf4j.Extension;

@Extension
@PluginDescriptor(
	name = "Menu Entry Swapper Extended",
	enabledByDefault = false,
	description = "Change the default option that is displayed when hovering over objects",
	tags = {"pickpocket", "equipped items", "inventory", "items", "equip", "construction"},
	type = PluginType.UTILITY
)
@PluginDependency(PvpToolsPlugin.class)
public class MenuEntrySwapperExtendedPlugin extends Plugin
{
	private static final Object HOTKEY = new Object();
	private static final Object HOTKEY_CHECK = new Object();

	private final Map<AbstractComparableEntry, AbstractComparableEntry> dePrioSwaps = new HashMap<>();

	private static final Splitter NEWLINE_SPLITTER = Splitter
		.on("\n")
		.omitEmptyStrings()
		.trimResults();

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private MenuEntrySwapperExtendedConfig config;

	@Inject
	private PluginManager pluginManager;

	@Inject
	private MenuManager menuManager;

	@Inject
	private KeyManager keyManager;

	@Inject
	private EventBus eventBus;

	@Inject
	private PvpToolsPlugin pvpTools;

	@Inject
	private PvpToolsConfig pvpToolsConfig;

	private boolean buildingMode;
	private boolean inTobRaid = false;
	private boolean inCoxRaid = false;
	@Setter(AccessLevel.PRIVATE)
	private boolean hotkeyActive;

	private static final int FIRE_ALTAR = 10315;
	private static final int CWARS = 9776;
	private static final int CRAFT_GUILD = 11571;

	private final HotkeyListener hotkey = new HotkeyListener(() -> config.hotkeyMod())
	{
		@Override
		public void hotkeyPressed()
		{
			startHotkey();
			setHotkeyActive(true);
		}

		@Override
		public void hotkeyReleased()
		{
			stopHotkey();
			setHotkeyActive(false);
		}
	};

	@Provides
	MenuEntrySwapperExtendedConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(MenuEntrySwapperExtendedConfig.class);
	}

	@Override
	public void startUp()
	{
		if (client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}
		keyManager.registerKeyListener(hotkey);
		setCastOptions(true);
		loadSwaps();
	}

	@Override
	public void shutDown()
	{
		removeSwaps();
		keyManager.unregisterKeyListener(hotkey);
		if (client.getGameState() == GameState.LOGGED_IN)
		{
			resetCastOptions();
		}
	}

	@Subscribe
	private void onFocusChanged(FocusChanged event)
	{
		if (!event.isFocused())
		{
			stopHotkey();
		}
	}

	@Subscribe
	private void onConfigChanged(ConfigChanged event)
	{
		if (!"menuentryswapperextended".equals(event.getGroup()))
		{
			return;
		}

		removeSwaps();
		loadSwaps();

		switch (event.getKey())
		{
			case "hideCastToB":
			case "hideCastIgnoredToB":
				if (config.hideCastToB())
				{
					setCastOptions(true);
				}
				else
				{
					resetCastOptions();
				}
				return;
			case "hideCastCoX":
			case "hideCastIgnoredCoX":
				if (config.hideCastCoX())
				{
					setCastOptions(true);
				}
				else
				{
					resetCastOptions();
				}
		}
	}

	@Subscribe
	private void onGameStateChanged(GameStateChanged event)
	{
		final GameState gameState = event.getGameState();

		switch (gameState)
		{
			case LOADING:
			case CONNECTION_LOST:
			case HOPPING:
			case LOGIN_SCREEN:
				keyManager.unregisterKeyListener(hotkey);
				break;
			case LOGGED_IN:
				removeSwaps();
				loadSwaps();
				keyManager.registerKeyListener(hotkey);
				break;
		}
	}

	@Subscribe
	private void onVarbitChanged(VarbitChanged event)
	{
		buildingMode = client.getVar(BUILDING_MODE) == 1;
		setCastOptions(false);
		leftClickTrade();
	}

	@Subscribe
	private void onMenuOpened(MenuOpened event)
	{
		Player localPlayer = client.getLocalPlayer();

		if (localPlayer == null)
		{
			return;
		}

		List<MenuEntry> menu_entries = new ArrayList<>();

		for (MenuEntry entry : event.getMenuEntries())
		{
			String option = Text.removeTags(entry.getOption()).toLowerCase();

			if (option.contains("trade with") && config.hideTradeWith())
			{
				continue;
			}

			if (option.contains("empty") && config.hideEmpty())
			{
				if (entry.getTarget().contains("potion") || entry.getTarget().contains("Antidote")
					|| entry.getTarget().contains("venom") || entry.getTarget().contains("antifire")
					|| entry.getTarget().contains("Antipoison") || entry.getTarget().contains("Superantipoison")
					|| entry.getTarget().contains("Saradomin brew") || entry.getTarget().contains("Super restore")
					|| entry.getTarget().contains("Zamorak brew") || entry.getTarget().contains("Guthix rest"))
				{
					continue;
				}
			}

			menu_entries.add(entry);
		}
		event.setMenuEntries(menu_entries.toArray(new MenuEntry[0]));
		event.setModified();
	}


	@Subscribe
	private void onPlayerAppearanceChanged(PlayerAppearanceChanged event)
	{
		if (!event.getPlayer().equals(client.getLocalPlayer()))
		{
			return;
		}
		rcSwaps();

	}

	private void loadSwaps()
	{
		addSwaps();
		leftClickTrade();
		rcSwaps();
		loadConstructionItems();
	}

	private void addSwaps()
	{
		final List<String> tmp = NEWLINE_SPLITTER.splitToList(config.prioEntry());

		for (String str : tmp)
		{
			String[] strings = str.split(",");

			if (strings.length <= 1)
			{
				continue;
			}

			final AbstractComparableEntry a = newBaseComparableEntry("", strings[1], -1, -1, false, true);
			final AbstractComparableEntry b = newBaseComparableEntry(strings[0], "", -1, -1, false, false);
			dePrioSwaps.put(a, b);
			menuManager.addSwap(a, b);
		}

		if (config.swapPickpocket())
		{
			menuManager.addPriorityEntry("Pickpocket").setPriority(1);
		}

		if (config.getBurningAmulet())
		{
			menuManager.addPriorityEntry(new EquipmentComparableEntry(config.getBurningAmuletMode().toString(), "burning amulet"));
		}

		if (config.getCombatBracelet())
		{
			menuManager.addPriorityEntry(new EquipmentComparableEntry(config.getCombatBraceletMode().toString(), "combat bracelet"));
		}

		if (config.getGamesNecklace())
		{
			menuManager.addPriorityEntry(new EquipmentComparableEntry(config.getGamesNecklaceMode().toString(), "games necklace"));
		}

		if (config.getDuelingRing())
		{
			menuManager.addPriorityEntry(new EquipmentComparableEntry(config.getDuelingRingMode().toString(), "ring of dueling"));
		}

		if (config.getGlory())
		{
			menuManager.addPriorityEntry(new EquipmentComparableEntry(config.getGloryMode().toString(), "glory"));
		}

		if (config.getSkillsNecklace())
		{
			menuManager.addPriorityEntry(new EquipmentComparableEntry(config.getSkillsNecklaceMode().toString(), "skills necklace"));
		}

		if (config.getNecklaceofPassage())
		{
			menuManager.addPriorityEntry(new EquipmentComparableEntry(config.getNecklaceofPassageMode().toString(), "necklace of passage"));
		}

		if (config.getDigsitePendant())
		{
			menuManager.addPriorityEntry(new EquipmentComparableEntry(config.getDigsitePendantMode().toString(), "digsite pendant"));
		}

		if (config.getSlayerRing())
		{
			menuManager.addPriorityEntry(new EquipmentComparableEntry(config.getSlayerRingMode().toString(), "slayer ring"));
		}

		if (config.getXericsTalisman())
		{
			menuManager.addPriorityEntry(new EquipmentComparableEntry(config.getXericsTalismanMode().toString(), "talisman"));
		}

		if (config.getRingofWealth())
		{
			menuManager.addPriorityEntry(new EquipmentComparableEntry(config.getRingofWealthMode().toString(), "ring of wealth"));
		}

	}

	private void leftClickTrade()
	{
		if (client.getVar(IN_WILDERNESS) == 1 || WorldType.isAllPvpWorld(client.getWorldType()))
		{
			return;
		}

		if (config.leftClickTrade())
		{
			menuManager.addPriorityEntry(MESAbstractComparables.TRADE);
		}

		if (config.leftClickFollow())
		{
			menuManager.addPriorityEntry(MESAbstractComparables.FOLLOW);
		}
	}

	private void rcSwaps()
	{
		if (client.getLocalPlayer() == null || !config.swapDuelRingLavas()
			|| client.getLocalPlayer().getWorldLocation().getRegionID() != CWARS
			|| client.getLocalPlayer().getWorldLocation().getRegionID() != CRAFT_GUILD
			|| client.getLocalPlayer().getWorldLocation().getRegionID() != FIRE_ALTAR)
		{
			menuManager.removePriorityEntry(new EquipmentComparableEntry("castle wars", "ring of dueling"));
			menuManager.removePriorityEntry(new EquipmentComparableEntry("duel arena", "ring of dueling"));
			menuManager.removePriorityEntry(new EquipmentComparableEntry("Teleport", "Crafting cape"));
			menuManager.removePriorityEntry(new EquipmentComparableEntry("Teleport", "Crafting cape(t)"));
			menuManager.removePriorityEntry(new EquipmentComparableEntry("Crafting Guild", "Max cape"));
		}

		if (checkFireAltarAccess())
		{
			if (client.getLocalPlayer().getWorldLocation().getRegionID() == FIRE_ALTAR)
			{
				menuManager.addPriorityEntry(new EquipmentComparableEntry("Teleport", "Crafting cape")).setPriority(100);
				menuManager.addPriorityEntry(new EquipmentComparableEntry("Teleport", "Crafting cape(t)")).setPriority(100);
				menuManager.addPriorityEntry(new EquipmentComparableEntry("Crafting Guild", "Max cape")).setPriority(100);
				menuManager.addPriorityEntry(new EquipmentComparableEntry("castle wars", "ring of dueling")).setPriority(100);
				menuManager.removePriorityEntry(new EquipmentComparableEntry("duel arena", "ring of dueling"));
			}
			else if (client.getLocalPlayer().getWorldLocation().getRegionID() == CWARS
				|| client.getLocalPlayer().getWorldLocation().getRegionID() == CRAFT_GUILD)
			{
				menuManager.addPriorityEntry(new EquipmentComparableEntry("duel arena", "ring of dueling")).setPriority(100);
				menuManager.removePriorityEntry(new EquipmentComparableEntry("castle wars", "ring of dueling"));
				menuManager.removePriorityEntry(new EquipmentComparableEntry("Teleport", "Crafting cape"));
				menuManager.removePriorityEntry(new EquipmentComparableEntry("Teleport", "Crafting cape(t)"));
				menuManager.removePriorityEntry(new EquipmentComparableEntry("Crafting Guild", "Max cape"));
			}
			else
			{
				menuManager.removePriorityEntry(new EquipmentComparableEntry("castle wars", "ring of dueling"));
				menuManager.removePriorityEntry(new EquipmentComparableEntry("duel arena", "ring of dueling"));
				menuManager.removePriorityEntry(new EquipmentComparableEntry("Teleport", "Crafting cape"));
				menuManager.removePriorityEntry(new EquipmentComparableEntry("Teleport", "Crafting cape(t)"));
				menuManager.removePriorityEntry(new EquipmentComparableEntry("Crafting Guild", "Max cape"));
			}
		}
	}

	private void removeSwaps()
	{
		final Iterator<Map.Entry<AbstractComparableEntry, AbstractComparableEntry>> dePrioIter = dePrioSwaps.entrySet().iterator();
		dePrioIter.forEachRemaining((e) ->
		{
			menuManager.removeSwap(e.getKey(), e.getValue());
			dePrioIter.remove();
		});
		menuManager.removePriorityEntry("Pickpocket");
		menuManager.removePriorityEntry(new EquipmentComparableEntry(config.getBurningAmuletMode().toString(), "burning amulet"));
		menuManager.removePriorityEntry(new EquipmentComparableEntry(config.getCombatBraceletMode().toString(), "combat bracelet"));
		menuManager.removePriorityEntry(new EquipmentComparableEntry(config.getDigsitePendantMode().toString(), "digsite pendant"));
		menuManager.removePriorityEntry(new EquipmentComparableEntry(config.getDuelingRingMode().toString(), "ring of dueling"));
		menuManager.removePriorityEntry(new EquipmentComparableEntry(config.getGamesNecklaceMode().toString(), "games necklace"));
		menuManager.removePriorityEntry(new EquipmentComparableEntry(config.getGloryMode().toString(), "glory"));
		menuManager.removePriorityEntry(new EquipmentComparableEntry(config.getNecklaceofPassageMode().toString(), "necklace of passage"));
		menuManager.removePriorityEntry(new EquipmentComparableEntry(config.getRingofWealthMode().toString(), "ring of wealth"));
		menuManager.removePriorityEntry(new EquipmentComparableEntry(config.getSkillsNecklaceMode().toString(), "skills necklace"));
		menuManager.removePriorityEntry(new EquipmentComparableEntry(config.getSlayerRingMode().toString(), "slayer ring"));
		menuManager.removePriorityEntry(new EquipmentComparableEntry(config.getXericsTalismanMode().toString(), "talisman"));
		menuManager.removePriorityEntry(config.getConstructionMode().getBuild());
		menuManager.removePriorityEntry(config.getConstructionMode().getRemove());
		menuManager.removePriorityEntry(new EquipmentComparableEntry("castle wars", "ring of dueling"));
		menuManager.removePriorityEntry(new EquipmentComparableEntry("duel arena", "ring of dueling"));
		menuManager.removePriorityEntry(new EquipmentComparableEntry("Teleport", "Crafting cape"));
		menuManager.removePriorityEntry(new EquipmentComparableEntry("Teleport", "Crafting cape(t)"));
		menuManager.removePriorityEntry(new EquipmentComparableEntry("Crafting Guild", "Max cape"));
		menuManager.removePriorityEntry(MESAbstractComparables.TRADE);
		menuManager.removePriorityEntry(MESAbstractComparables.FOLLOW);
	}

	private void loadConstructionItems()
	{
		if (client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}

		if (!buildingMode)
		{
			menuManager.removePriorityEntry(config.getConstructionMode().getBuild());
			menuManager.removePriorityEntry(config.getConstructionMode().getRemove());
			return;
		}

		if (config.getEasyConstruction())
		{
			menuManager.addPriorityEntry(config.getConstructionMode().getBuild()).setPriority(100);
			menuManager.addPriorityEntry(config.getConstructionMode().getRemove()).setPriority(100);
		}
	}

	private void startHotkey()
	{
		eventBus.subscribe(ClientTick.class, HOTKEY, this::addHotkey);
		eventBus.subscribe(ClientTick.class, HOTKEY_CHECK, this::hotkeyCheck);
	}

	private void addHotkey(ClientTick event)
	{
		if (config.hotKeyLoot())
		{
			menuManager.addPriorityEntry(MESAbstractComparables.TAKE);
		}
		if (config.hotKeyWalk())
		{
			menuManager.addPriorityEntry(MESAbstractComparables.WALK);
		}

		eventBus.unregister(HOTKEY);
	}

	private void stopHotkey()
	{
		eventBus.subscribe(ClientTick.class, HOTKEY, this::removeHotkey);
	}

	private void removeHotkey(ClientTick event)
	{
		menuManager.removePriorityEntry(MESAbstractComparables.TAKE);
		menuManager.removePriorityEntry(MESAbstractComparables.WALK);

		eventBus.unregister(HOTKEY);
	}

	private void hotkeyCheck(ClientTick event)
	{
		if (hotkeyActive)
		{
			int i = 0;
			for (boolean bol : client.getPressedKeys())
			{
				if (bol)
				{
					i++;
				}
			}
			if (i == 0)
			{
				stopHotkey();
				setHotkeyActive(false);
				eventBus.unregister(HOTKEY_CHECK);
			}
		}
	}

	private void setCastOptions(boolean force)
	{
		clientThread.invoke(() ->
		{
			boolean tmpInCoxRaid = client.getVar(Varbits.IN_RAID) == 1;
			if (tmpInCoxRaid != inCoxRaid || force)
			{
				if (tmpInCoxRaid && config.hideCastCoX())
				{
					client.setHideFriendCastOptions(true);
					client.setHideClanmateCastOptions(true);
					client.setUnhiddenCasts(Sets.newHashSet(Text.fromCSV(config.hideCastIgnoredCoX().toLowerCase())));
				}

				inCoxRaid = tmpInCoxRaid;
			}

			boolean tmpInTobRaid = client.getVar(Varbits.THEATRE_OF_BLOOD) == 2;
			if (tmpInTobRaid != inTobRaid || force)
			{
				if (tmpInTobRaid && config.hideCastToB())
				{
					client.setHideFriendCastOptions(true);
					client.setHideClanmateCastOptions(true);
					client.setUnhiddenCasts(Sets.newHashSet(Text.fromCSV(config.hideCastIgnoredToB().toLowerCase())));
				}

				inTobRaid = tmpInTobRaid;
			}

			if (!inCoxRaid && !inTobRaid)
			{
				resetCastOptions();
			}
		});
	}

	private void resetCastOptions()
	{
		clientThread.invoke(() ->
		{
			if (client.getVar(Varbits.IN_WILDERNESS) == 1 || WorldType.isAllPvpWorld(client.getWorldType()) && pluginManager.isPluginEnabled(pvpTools) && pvpToolsConfig.hideCast())
			{
				pvpTools.setCastOptions();
			}
			else
			{
				client.setHideFriendCastOptions(false);
				client.setHideClanmateCastOptions(false);
			}
		});
	}

	private boolean checkFireAltarAccess()
	{
		if (client.getLocalPlayer() == null || client.getLocalPlayer().getPlayerAppearance() == null)
		{
			return false;
		}
		return client.getLocalPlayer().getPlayerAppearance().getEquipmentId(KitType.HEAD) == FIRE_TIARA
			|| client.getLocalPlayer().getPlayerAppearance().getEquipmentId(KitType.CAPE) == RUNECRAFT_CAPE
			|| client.getLocalPlayer().getPlayerAppearance().getEquipmentId(KitType.CAPE) == RUNECRAFT_CAPET
			|| client.getLocalPlayer().getPlayerAppearance().getEquipmentId(KitType.CAPE) == MAX_CAPE;
	}
}
