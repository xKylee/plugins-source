/*
 * Copyright (c) 2018, Adam <Adam@sigterm.info>
 * Copyright (c) 2018, Kamiel
 * Copyright (c) 2019, alanbaumgartner <https://github.com/alanbaumgartner>
 * Copyright (c) 2019, Kyle <https://github.com/kyleeld>
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
import net.runelite.api.MenuEntry;
import net.runelite.api.MenuOpcode;
import net.runelite.api.Player;
import net.runelite.api.Varbits;
import static net.runelite.api.Varbits.BUILDING_MODE;
import net.runelite.api.WorldType;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.FocusChanged;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.MenuOpened;
import net.runelite.api.events.VarbitChanged;
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
import net.runelite.client.plugins.pvptools.PvpToolsConfig;
import net.runelite.client.plugins.pvptools.PvpToolsPlugin;
import net.runelite.client.util.HotkeyListener;
import org.pf4j.Extension;

@Extension
@PluginDescriptor(
	name = "Menu Entry Swapper Extended",
	enabledByDefault = false,
	description = "Change the default option that is displayed when hovering over objects",
	tags = {"pickpocket", "equipped items", "inventory", "items", "equip"},
	type = PluginType.UTILITY
)
@PluginDependency(PvpToolsPlugin.class)
public class MenuEntrySwapperExtendedPlugin extends Plugin
{
	private static final Object HOTKEY = new Object();
	private static final Object HOTKEY_CHECK = new Object();

	private static final EquipmentComparableEntry CASTLE_WARS = new EquipmentComparableEntry("castle wars", "ring of dueling");
	private static final EquipmentComparableEntry DUEL_ARENA = new EquipmentComparableEntry("duel arena", "ring of dueling");
	private final Map<AbstractComparableEntry, AbstractComparableEntry> dePrioSwaps = new HashMap<>();
	
	private static final Splitter NEWLINE_SPLITTER = Splitter
		.on("\n")
		.omitEmptyStrings()
		.trimResults();
		
	private static final AbstractComparableEntry WALK = new AbstractComparableEntry()
	{
		private final int hash = "WALK".hashCode() * 79 + getPriority();

		@Override
		public int hashCode()
		{
			return hash;
		}

		@Override
		public boolean equals(Object entry)
		{
			return entry.getClass() == this.getClass() && entry.hashCode() == this.hashCode();
		}

		@Override
		public int getPriority()
		{
			return 99;
		}

		@Override
		public boolean matches(MenuEntry entry)
		{
			return
				entry.getOpcode() == MenuOpcode.WALK.getId() ||
					entry.getOpcode() == MenuOpcode.WALK.getId() + MenuOpcode.MENU_ACTION_DEPRIORITIZE_OFFSET;
		}
	};

	private static final AbstractComparableEntry TAKE = new AbstractComparableEntry()
	{
		private final int hash = "TAKE".hashCode() * 79 + getPriority();

		@Override
		public int hashCode()
		{
			return hash;
		}

		@Override
		public boolean equals(Object entry)
		{
			return entry.getClass() == this.getClass() && entry.hashCode() == this.hashCode();
		}

		@Override
		public int getPriority()
		{
			return 100;
		}

		@Override
		public boolean matches(MenuEntry entry)
		{
			int opcode = entry.getOpcode();
			if (opcode > MenuOpcode.MENU_ACTION_DEPRIORITIZE_OFFSET)
			{
				opcode -= MenuOpcode.MENU_ACTION_DEPRIORITIZE_OFFSET;
			}

			return
				opcode >= MenuOpcode.GROUND_ITEM_FIRST_OPTION.getId() &&
					opcode <= MenuOpcode.GROUND_ITEM_FIFTH_OPTION.getId();
		}
	};

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
		addSwaps();
		rcSwaps();
		loadConstructionItems();

		if (client.getGameState() == GameState.LOGGED_IN)
		{
			keyManager.registerKeyListener(hotkey);
			setCastOptions(true);
		}
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
		addSwaps();
		rcSwaps();
		loadConstructionItems();

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
		if (event.getGameState() != GameState.LOGGED_IN)
		{
			keyManager.unregisterKeyListener(hotkey);
			return;
		}

		rcSwaps();
		loadConstructionItems();
		keyManager.registerKeyListener(hotkey);
	}

	@Subscribe
	private void onVarbitChanged(VarbitChanged event)
	{
		buildingMode = client.getVar(BUILDING_MODE) == 1;

		setCastOptions(false);
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

			if (option.contains("lookup") && config.hideLookup())
			{
				continue;
			}

			if (option.contains("report") && config.hideReport())
			{
				continue;
			}

			menu_entries.add(entry);
		}
		event.setMenuEntries(menu_entries.toArray(new MenuEntry[0]));
		event.setModified();
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

	private void rcSwaps()
	{
		if (config.swapDuelRingLavas())
		{
			if (client.getLocalPlayer().getWorldLocation().getRegionID() != FIRE_ALTAR)
			{
				menuManager.removePriorityEntry(CASTLE_WARS);
				menuManager.addPriorityEntry(DUEL_ARENA).setPriority(100);
			}
			else if (client.getLocalPlayer().getWorldLocation().getRegionID() == FIRE_ALTAR)
			{
				menuManager.removePriorityEntry(DUEL_ARENA);
				menuManager.addPriorityEntry(CASTLE_WARS).setPriority(100);
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
		menuManager.removePriorityEntry(CASTLE_WARS);
		menuManager.removePriorityEntry(DUEL_ARENA);
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
			menuManager.addPriorityEntry(TAKE);
		}
		if (config.hotKeyWalk())
		{
			menuManager.addPriorityEntry(WALK);
		}

		eventBus.unregister(HOTKEY);
	}

	private void stopHotkey()
	{
		eventBus.subscribe(ClientTick.class, HOTKEY, this::removeHotkey);
	}

	private void removeHotkey(ClientTick event)
	{
		menuManager.removePriorityEntry(TAKE);
		menuManager.removePriorityEntry(WALK);

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
}
