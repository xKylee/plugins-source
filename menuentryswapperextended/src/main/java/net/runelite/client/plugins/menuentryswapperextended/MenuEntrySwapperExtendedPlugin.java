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

import static com.google.common.base.Predicates.alwaysTrue;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.inject.Provides;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.inject.Inject;
import lombok.Getter;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
import net.runelite.api.KeyCode;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.VarClientInt;
import net.runelite.api.Varbits;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOpened;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.PostItemComposition;
import net.runelite.api.events.WidgetMenuOptionClicked;
import net.runelite.api.util.Text;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemVariationMapping;
import net.runelite.client.input.KeyManager;
import net.runelite.client.menus.MenuManager;
import net.runelite.client.menus.WidgetMenuOption;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginManager;
import net.runelite.client.plugins.menuentryswapper.Swap;
import net.runelite.client.plugins.menuentryswapperextended.util.AbstractComparableEntry;
import net.runelite.client.plugins.menuentryswapperextended.util.BurningAmuletMode;
import net.runelite.client.plugins.menuentryswapperextended.util.CombatBraceletMode;
import net.runelite.client.plugins.menuentryswapperextended.util.ConstructionCapeMode;
import net.runelite.client.plugins.menuentryswapperextended.util.CraftingCapeMode;
import net.runelite.client.plugins.menuentryswapperextended.util.CustomSwaps;
import net.runelite.client.plugins.menuentryswapperextended.util.DiaryCapeMode;
import net.runelite.client.plugins.menuentryswapperextended.util.DigsitePendantMode;
import net.runelite.client.plugins.menuentryswapperextended.util.DuelingRingMode;
import net.runelite.client.plugins.menuentryswapperextended.util.FarmCapeMode;
import net.runelite.client.plugins.menuentryswapperextended.util.GamesNecklaceMode;
import net.runelite.client.plugins.menuentryswapperextended.util.GloryMode;
import net.runelite.client.plugins.menuentryswapperextended.util.MagicCapeMode;
import net.runelite.client.plugins.menuentryswapperextended.util.MaxCapeEquippedMode;
import net.runelite.client.plugins.menuentryswapperextended.util.NecklaceOfPassageMode;
import net.runelite.client.plugins.menuentryswapperextended.util.QuestCapeMode;
import net.runelite.client.plugins.menuentryswapperextended.util.RingOfWealthMode;
import net.runelite.client.plugins.menuentryswapperextended.util.SkillsNecklaceMode;
import net.runelite.client.plugins.menuentryswapperextended.util.XericsTalismanMode;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.pf4j.Extension;

@Extension
@PluginDescriptor(
	name = "Menu Entry Swapper Extended",
	enabledByDefault = false,
	description = "Change the default option that is displayed when hovering over objects",
	tags = {"pickpocket", "equipped items", "inventory", "items", "equip", "construction"}
)
public class MenuEntrySwapperExtendedPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private MenuEntrySwapperExtendedConfig config;

	@Inject
	private PluginManager pluginManager;

	@Inject
	private ConfigManager configManager;

	@Inject
	private ItemManager itemManager;

	@Inject
	private MenuManager menuManager;

	@Inject
	private KeyManager keyManager;

	@Inject
	public EventBus eventBus;

	@Inject
	private CustomSwaps customswaps;

	@Getter
	private boolean configuringShiftClick = false;

	private final Map<AbstractComparableEntry, Integer> customSwaps = new HashMap<>();
	private final List<Pair<AbstractComparableEntry, AbstractComparableEntry>> prioSwaps = new ArrayList<>();

	private static <T extends Comparable<? super T>> void sortedInsert(List<T> list,
			T value) // NOPMD: UnusedPrivateMethod: false positive
	{
		int idx = Collections.binarySearch(list, value);
		list.add(idx < 0 ? -idx - 1 : idx, value);
	}

	private MenuEntry[] menuEntries;
	List<String> targetList;
	List<String> optionsList;
	private boolean inTobRaid = false;
	private boolean inCoxRaid = false;
	private static final String CONFIGURE = "Configure";
	private static final String SAVE = "Save";
	private static final String RESET = "Reset";
	private static final String MENU_TARGET = "Shift-click";

	private static final String SHIFTCLICK_CONFIG_GROUP = "shiftclick";
	private static final String ITEM_KEY_PREFIX = "item_";

	private static final WidgetMenuOption FIXED_INVENTORY_TAB_CONFIGURE = new WidgetMenuOption(
			CONFIGURE,
			MENU_TARGET, WidgetInfo.FIXED_VIEWPORT_INVENTORY_TAB);

	private static final WidgetMenuOption FIXED_INVENTORY_TAB_SAVE = new WidgetMenuOption(SAVE,
			MENU_TARGET, WidgetInfo.FIXED_VIEWPORT_INVENTORY_TAB);

	private static final WidgetMenuOption RESIZABLE_INVENTORY_TAB_CONFIGURE = new WidgetMenuOption(
			CONFIGURE,
			MENU_TARGET, WidgetInfo.RESIZABLE_VIEWPORT_INVENTORY_TAB);

	private static final WidgetMenuOption RESIZABLE_INVENTORY_TAB_SAVE = new WidgetMenuOption(SAVE,
			MENU_TARGET, WidgetInfo.RESIZABLE_VIEWPORT_INVENTORY_TAB);

	private static final WidgetMenuOption RESIZABLE_BOTTOM_LINE_INVENTORY_TAB_CONFIGURE = new WidgetMenuOption(
			CONFIGURE,
			MENU_TARGET, WidgetInfo.RESIZABLE_VIEWPORT_BOTTOM_LINE_INVENTORY_TAB);

	private static final WidgetMenuOption RESIZABLE_BOTTOM_LINE_INVENTORY_TAB_SAVE = new WidgetMenuOption(
			SAVE,
			MENU_TARGET, WidgetInfo.RESIZABLE_VIEWPORT_BOTTOM_LINE_INVENTORY_TAB);

	private static final Set<MenuAction> ITEM_MENU_TYPES = ImmutableSet.of(
			MenuAction.ITEM_FIRST_OPTION,
			MenuAction.ITEM_SECOND_OPTION,
			MenuAction.ITEM_THIRD_OPTION,
			MenuAction.ITEM_FOURTH_OPTION,
			MenuAction.ITEM_FIFTH_OPTION,
			MenuAction.EXAMINE_ITEM,
			MenuAction.ITEM_USE
	);

	private static final Set<MenuAction> NPC_MENU_TYPES = ImmutableSet.of(
			MenuAction.NPC_FIRST_OPTION,
			MenuAction.NPC_SECOND_OPTION,
			MenuAction.NPC_THIRD_OPTION,
			MenuAction.NPC_FOURTH_OPTION,
			MenuAction.NPC_FIFTH_OPTION,
			MenuAction.EXAMINE_NPC);

	private final Multimap<String, Swap> swaps = Multimaps
			.synchronizedSetMultimap(LinkedHashMultimap.create());
	private final ArrayListMultimap<String, Integer> optionIndexes = ArrayListMultimap.create();

	private List<String> bankItemNames = new ArrayList<>();
	private List<String> hideAttackIgnoredNPCs = new ArrayList<>();
	private List<String> hideCastIgnoredSpells = new ArrayList<>();

	@Provides
	MenuEntrySwapperExtendedConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(MenuEntrySwapperExtendedConfig.class);
	}

	@Override
	public void startUp()
	{
		parseOldFormatConfig();
		eventBus.register(customswaps);
		keyManager.registerKeyListener(customswaps);

		if (client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}
		loadSwaps();
	}

	@Subscribe
	private void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}
		loadSwaps();
	}

	@Override
	public void shutDown()
	{
		eventBus.unregister(customSwaps);
		swaps.clear();
	}

	public Swap swap(String option, Predicate<String> targetPredicate, String swappedOption,
			Supplier<Boolean> enabled)
	{
		Swap swap = new Swap(alwaysTrue(), targetPredicate, swappedOption, enabled, true);
		swaps.put(option, swap);
		return swap;
	}

	public Swap swapContains(String option, Predicate<String> targetPredicate, String swappedOption,
			Supplier<Boolean> enabled)
	{
		Swap swap = new Swap(alwaysTrue(), targetPredicate, swappedOption, enabled, false);
		swaps.put(option, swap);
		return swap;
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (event.getGroup().equals("menuentryswapperextended") && event.getKey()
				.equals("shiftClickCustomization"))
		{
			if (event.getGroup().equals(SHIFTCLICK_CONFIG_GROUP) && event.getKey()
					.startsWith(ITEM_KEY_PREFIX))
			{
				clientThread.invoke(this::resetItemCompositionCache);
			}
			if (!"menuentryswapper".equals(event.getGroup()))
			{
				return;
			}
		}

		loadSwaps();
	}


	private void resetItemCompositionCache()
	{
		client.getItemCompositionCache().reset();
	}

	private Integer getSwapConfig(int itemId)
	{
		itemId = ItemVariationMapping.map(itemId);
		String config = configManager
				.getConfiguration(SHIFTCLICK_CONFIG_GROUP, ITEM_KEY_PREFIX + itemId);
		if (config == null || config.isEmpty())
		{
			return null;
		}

		return Integer.parseInt(config);
	}

	private void setSwapConfig(int itemId, int index)
	{
		itemId = ItemVariationMapping.map(itemId);
		configManager.setConfiguration(SHIFTCLICK_CONFIG_GROUP, ITEM_KEY_PREFIX + itemId, index);
	}

	private void unsetSwapConfig(int itemId)
	{
		itemId = ItemVariationMapping.map(itemId);
		configManager.unsetConfiguration(SHIFTCLICK_CONFIG_GROUP, ITEM_KEY_PREFIX + itemId);
	}

	@Subscribe
	public void onWidgetMenuOptionClicked(WidgetMenuOptionClicked event)
	{
		if (event.getWidget() == WidgetInfo.FIXED_VIEWPORT_INVENTORY_TAB
				|| event.getWidget() == WidgetInfo.RESIZABLE_VIEWPORT_INVENTORY_TAB
				|| event.getWidget() == WidgetInfo.RESIZABLE_VIEWPORT_BOTTOM_LINE_INVENTORY_TAB)
		{
			configuringShiftClick =
					event.getMenuOption().equals(CONFIGURE) && Text.removeTags(event.getMenuTarget())
							.equals(MENU_TARGET);
			refreshShiftClickCustomizationMenus();
		}
	}

	@Subscribe
	public void onMenuOpened(MenuOpened event)
	{
		if (!configuringShiftClick)
		{
			return;
		}

		MenuEntry firstEntry = event.getFirstEntry();
		if (firstEntry == null)
		{
			return;
		}

		int widgetId = firstEntry.getParam1();
		if (widgetId != WidgetInfo.INVENTORY.getPackedId())
		{
			return;
		}

		int itemId = firstEntry.getIdentifier();
		if (itemId == -1)
		{
			return;
		}

		ItemComposition itemComposition = itemManager.getItemComposition(itemId);
		MenuAction shiftClickAction = MenuAction.ITEM_USE;
		final int shiftClickActionIndex = itemComposition.getShiftClickActionIndex();

		if (shiftClickActionIndex >= 0)
		{
			shiftClickAction = MenuAction
					.of(MenuAction.ITEM_FIRST_OPTION.getId() + shiftClickActionIndex);
		}

		MenuEntry[] entries = event.getMenuEntries();

		for (MenuEntry entry : entries)
		{
			final MenuAction menuAction = MenuAction.of(entry.getType());

			if (ITEM_MENU_TYPES.contains(menuAction) && entry.getIdentifier() == itemId)
			{
				entry.setType(MenuAction.RUNELITE.getId());

				if (shiftClickAction == menuAction)
				{
					entry.setOption("* " + entry.getOption());
				}
			}
		}

		final MenuEntry resetShiftClickEntry = new MenuEntry();
		resetShiftClickEntry.setOption(RESET);
		resetShiftClickEntry.setTarget(MENU_TARGET);
		resetShiftClickEntry.setIdentifier(itemId);
		resetShiftClickEntry.setParam1(widgetId);
		resetShiftClickEntry.setType(MenuAction.RUNELITE.getId());
		client.setMenuEntries(ArrayUtils.addAll(entries, resetShiftClickEntry));

		Player localPlayer = client.getLocalPlayer();

		if (localPlayer == null)
		{
			return;
		}

		event.setMenuEntries(updateMenuEntries(client.getMenuEntries()));
		event.setModified();
	}

	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded menuEntryAdded)
	{

		menuEntries = client.getMenuEntries();
		if ((client.getVarbitValue(2176) != 1)
		&& menuEntryAdded.getOpcode() != MenuAction.GAME_OBJECT_FIFTH_OPTION.getId())
		{
			return;
		}
		swapConstructionMenu(menuEntries);

		if (!config.getEasyConstruction())
		{
			return;
		}

	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		if (event.getMenuAction() != MenuAction.RUNELITE || event.getWidgetId() != WidgetInfo.INVENTORY
				.getPackedId())
		{
			return;
		}

		int itemId = event.getId();

		if (itemId == -1)
		{
			return;
		}

		String option = event.getMenuOption();
		String target = event.getMenuTarget();
		ItemComposition itemComposition = itemManager.getItemComposition(itemId);

		if (option.equals(RESET) && target.equals(MENU_TARGET))
		{
			unsetSwapConfig(itemId);
			return;
		}

		if (!itemComposition.getName().equals(Text.removeTags(target)))
		{
			return;
		}

		if (option.equals("Use")) //because "Use" is not in inventoryActions
		{
			setSwapConfig(itemId, -1);
		}
		else
		{
			String[] inventoryActions = itemComposition.getInventoryActions();

			for (int index = 0; index < inventoryActions.length; index++)
			{
				if (option.equals(inventoryActions[index]))
				{
					setSwapConfig(itemId, index);
					break;
				}
			}
		}
	}

	private void swapMenuEntry(int index, MenuEntry menuEntry)
	{
		final int eventId = menuEntry.getIdentifier();
		final MenuAction menuAction = MenuAction.of(menuEntry.getType());
		final String option = Text.removeTags(menuEntry.getOption()).toLowerCase();
		final String target = Text.removeTags(menuEntry.getTarget()).toLowerCase();
		final NPC hintArrowNpc = client.getHintArrowNpc();

		if (hintArrowNpc != null
				&& hintArrowNpc.getIndex() == eventId
				&& NPC_MENU_TYPES.contains(menuAction))
		{
			return;
		}

		if (shiftModifier() && (menuAction == MenuAction.ITEM_FIRST_OPTION
				|| menuAction == MenuAction.ITEM_SECOND_OPTION
				|| menuAction == MenuAction.ITEM_THIRD_OPTION
				|| menuAction == MenuAction.ITEM_FOURTH_OPTION
				|| menuAction == MenuAction.ITEM_FIFTH_OPTION
				|| menuAction == MenuAction.ITEM_USE))
		{
			return;
		}

		Collection<Swap> swaps = this.swaps.get(option);
		for (Swap swap : swaps)
		{
			if (swap.getTargetPredicate().test(target) && swap.getEnabled().get())
			{
				if (swap(swap.getSwappedOption(), target, index, swap.isStrict()))
				{
					break;
				}
			}
		}
	}

	@Subscribe
	public void onClientTick(ClientTick clientTick)
	{
		// The menu is not rebuilt when it is open, so don't swap or else it will
		// repeatedly swap entries
		if (client.getGameState() == GameState.LOGGED_IN && !client.isMenuOpen())
		{
			MenuEntry[] menuEntries = client.getMenuEntries();

			// Build option map for quick lookup in findIndex
			int idx = 0;
			optionIndexes.clear();
			for (MenuEntry entry : menuEntries)
			{
				String option = Text.removeTags(entry.getOption()).toLowerCase();
				optionIndexes.put(option, idx++);
			}

			// Perform swaps
			idx = 0;
			for (MenuEntry entry : menuEntries)
			{
				swapMenuEntry(idx++, entry);
			}
		}
		client.setMenuEntries(updateMenuEntries(client.getMenuEntries()));
	}

	@Subscribe
	public void onPostItemComposition(PostItemComposition event)
	{
		ItemComposition itemComposition = event.getItemComposition();
		Integer option = getSwapConfig(itemComposition.getId());

		if (option != null)
		{
			itemComposition.setShiftClickActionIndex(option);
		}
	}

	private boolean swap(String option, String target, int index, boolean strict)
	{
		MenuEntry[] menuEntries = client.getMenuEntries();

		// find option to swap with
		int optionIdx = findIndex(menuEntries, index, option, target, strict);

		if (optionIdx >= 0)
		{
			swap(optionIndexes, menuEntries, optionIdx, index);
			return true;
		}

		return false;
	}

	private int findIndex(MenuEntry[] entries, int limit, String option, String target,
			boolean strict)
	{
		if (strict)
		{
			List<Integer> indexes = optionIndexes.get(option);

			// We want the last index which matches the target, as that is what is top-most
			// on the menu
			for (int i = indexes.size() - 1; i >= 0; --i)
			{
				int idx = indexes.get(i);
				MenuEntry entry = entries[idx];
				String entryTarget = Text.removeTags(entry.getTarget()).toLowerCase();

				// Limit to the last index which is prior to the current entry
				if (idx < limit && entryTarget.equals(target))
				{
					return idx;
				}
			}
		}
		else
		{
			// Without strict matching we have to iterate all entries up to the current limit...
			for (int i = limit - 1; i >= 0; i--)
			{
				MenuEntry entry = entries[i];
				String entryOption = Text.removeTags(entry.getOption()).toLowerCase();
				String entryTarget = Text.removeTags(entry.getTarget()).toLowerCase();

				if (entryOption.contains(option.toLowerCase()) && entryTarget.equals(target))
				{
					return i;
				}
			}

		}

		return -1;
	}

	private void swap(ArrayListMultimap<String, Integer> optionIndexes, MenuEntry[] entries, int index1, int index2)
	{
		Widget inv = client.getWidget(WidgetInfo.INVENTORY);
		Widget eq = client.getWidget(WidgetInfo.EQUIPMENT);
		if (client.getVar(VarClientInt.INVENTORY_TAB) == 3 && inv != null)
		{
			MenuEntry[] clonedEntries = new MenuEntry[entries.length];
			System.arraycopy(entries, 0, clonedEntries, 0, entries.length);

			MenuEntry entry1 = entries[index1];
			MenuEntry entry2 = entries[index2];

			clonedEntries[index1] = entry2;
			clonedEntries[index2] = entry1;

			client.setMenuEntries(clonedEntries);

			String option1 = Text.removeTags(entry1.getOption()).toLowerCase(),
					option2 = Text.removeTags(entry2.getOption()).toLowerCase();

			List<Integer> list1 = optionIndexes.get(option1),
					list2 = optionIndexes.get(option2);

			list1.remove((Integer) index1);
			list2.remove((Integer) index2);

			sortedInsert(list1, index2);
			sortedInsert(list2, index1);
		}
		else if (client.getVar(VarClientInt.INVENTORY_TAB) == 4 && eq != null)
		{
			MenuEntry[] clonedEntries = new MenuEntry[entries.length];
			System.arraycopy(entries, 0, clonedEntries, 0, entries.length);

			MenuEntry entry1 = entries[index1];
			MenuEntry entry2 = entries[index2];

			int temp = entry1.getType();
			entry1.setType(entry2.getType());
			entry2.setType(temp);

			clonedEntries[index1] = entry2;
			clonedEntries[index2] = entry1;

			client.setMenuEntries(clonedEntries);

			String option1 = Text.removeTags(entry1.getOption()).toLowerCase(),
					option2 = Text.removeTags(entry2.getOption()).toLowerCase();

			List<Integer> list1 = optionIndexes.get(option1),
					list2 = optionIndexes.get(option2);

			list1.remove((Integer) index1);
			list2.remove((Integer) index2);

			sortedInsert(list1, index2);
			sortedInsert(list2, index1);
		}
	}

	private void removeShiftClickCustomizationMenus()
	{
		menuManager.removeManagedCustomMenu(FIXED_INVENTORY_TAB_CONFIGURE);
		menuManager.removeManagedCustomMenu(FIXED_INVENTORY_TAB_SAVE);
		menuManager.removeManagedCustomMenu(RESIZABLE_BOTTOM_LINE_INVENTORY_TAB_CONFIGURE);
		menuManager.removeManagedCustomMenu(RESIZABLE_BOTTOM_LINE_INVENTORY_TAB_SAVE);
		menuManager.removeManagedCustomMenu(RESIZABLE_INVENTORY_TAB_CONFIGURE);
		menuManager.removeManagedCustomMenu(RESIZABLE_INVENTORY_TAB_SAVE);
	}

	private void refreshShiftClickCustomizationMenus()
	{
		removeShiftClickCustomizationMenus();
		if (configuringShiftClick)
		{
			menuManager.addManagedCustomMenu(FIXED_INVENTORY_TAB_SAVE);
			menuManager.addManagedCustomMenu(RESIZABLE_BOTTOM_LINE_INVENTORY_TAB_SAVE);
			menuManager.addManagedCustomMenu(RESIZABLE_INVENTORY_TAB_SAVE);
		}
		else
		{
			menuManager.addManagedCustomMenu(FIXED_INVENTORY_TAB_CONFIGURE);
			menuManager.addManagedCustomMenu(RESIZABLE_BOTTOM_LINE_INVENTORY_TAB_CONFIGURE);
			menuManager.addManagedCustomMenu(RESIZABLE_INVENTORY_TAB_CONFIGURE);
		}
	}

	private boolean shiftModifier()
	{
		return client.isKeyPressed(KeyCode.KC_SHIFT);
	}

	private Predicate<String> targetSwap(String string)
	{
		return (in) -> in.toLowerCase().contains(string);
	}

	private void addSwaps()
	{

		for (String option : new String[]{"attack", "talk-to"})
		{
			swapContains(option, (s) -> true, "pickpocket", config::swapPickpocket);
		}
		swap("remove", targetSwap("burning amulet"), "chaos temple", () ->
				config.getBurningAmulet() && config.getBurningAmuletMode() == BurningAmuletMode.CHAOS_TEMPLE);
		swap("remove", targetSwap("burning amulet"), "bandit camp", () ->
				config.getBurningAmulet() && config.getBurningAmuletMode() == BurningAmuletMode.BANDIT_CAMP);
		swap("remove", targetSwap("burning amulet"), "lava maze", () ->
				config.getBurningAmulet() && config.getBurningAmuletMode() == BurningAmuletMode.LAVA_MAZE);

		swap("remove", targetSwap("combat bracelet"), "warriors' guild", () ->
				config.getCombatBracelet() && config.getCombatBraceletMode() == CombatBraceletMode.WARRIORS_GUILD);
		swap("remove", targetSwap("combat bracelet"), "champions' guild", () ->
				config.getCombatBracelet() && config.getCombatBraceletMode() == CombatBraceletMode.CHAMPIONS_GUILD);
		swap("remove", targetSwap("combat bracelet"), "edgeville monastery", () ->
				config.getCombatBracelet() && config.getCombatBraceletMode() == CombatBraceletMode.EDGEVILLE_MONASTERY);
		swap("remove", targetSwap("combat bracelet"), "ranging guild", () ->
				config.getCombatBracelet() && config.getCombatBraceletMode() == CombatBraceletMode.RANGING_GUILD);

		swap("remove", targetSwap("games necklace"), "burthorpe", () ->
				config.getGamesNecklace() && config.getGamesNecklaceMode() == GamesNecklaceMode.BURTHORPE);
		swap("remove", targetSwap("games necklace"), "barbarian outpost", () ->
				config.getGamesNecklace() && config.getGamesNecklaceMode() == GamesNecklaceMode.BARBARIAN_OUTPOST);
		swap("remove", targetSwap("games necklace"), "corporeal beast", () ->
				config.getGamesNecklace() && config.getGamesNecklaceMode() == GamesNecklaceMode.CORPOREAL_BEAST);
		swap("remove", targetSwap("games necklace"), "tears of guthix", () ->
				config.getGamesNecklace() && config.getGamesNecklaceMode() == GamesNecklaceMode.TEARS_OF_GUTHIX);
		swap("remove", targetSwap("games necklace"), "wintertodt camp", () ->
				config.getGamesNecklace() && config.getGamesNecklaceMode() == GamesNecklaceMode.WINTER);

		swap("remove", targetSwap("ring of dueling"), "duel arena", () ->
				config.getDuelingRing() && config.getDuelingRingMode() == DuelingRingMode.DUEL_ARENA);
		swap("remove", targetSwap("ring of dueling"), "castle wars", () ->
				config.getDuelingRing() && config.getDuelingRingMode() == DuelingRingMode.CASTLE_WARS);
		swap("remove", targetSwap("ring of dueling"), "ferox enclave", () ->
				config.getDuelingRing() && config.getDuelingRingMode() == DuelingRingMode.FEROX_ENCLAVE);

		swap("remove", targetSwap("amulet of glory"), "edgeville", () ->
				config.getGlory() && config.getGloryMode() == GloryMode.EDGEVILLE);
		swap("remove", targetSwap("amulet of glory"), "karamja", () ->
				config.getGlory() && config.getGloryMode() == GloryMode.KARAMJA);
		swap("remove", targetSwap("amulet of glory"), "al kharid", () ->
				config.getGlory() && config.getGloryMode() == GloryMode.AL_KHARID);
		swap("remove", targetSwap("amulet of glory"), "draynor village", () ->
				config.getGlory() && config.getGloryMode() == GloryMode.DRAYNOR_VILLAGE);
		swap("remove", targetSwap("amulet of eternal glory"), "edgeville", () ->
				config.getGlory() && config.getGloryMode() == GloryMode.EDGEVILLE);
		swap("remove", targetSwap("amulet of eternal glory"), "karamja", () ->
				config.getGlory() && config.getGloryMode() == GloryMode.KARAMJA);
		swap("remove", targetSwap("amulet of eternal glory"), "al kharid", () ->
				config.getGlory() && config.getGloryMode() == GloryMode.AL_KHARID);
		swap("remove", targetSwap("amulet of eternal glory"), "draynor village", () ->
				config.getGlory() && config.getGloryMode() == GloryMode.DRAYNOR_VILLAGE);

		swap("remove", targetSwap("skills necklace"), "fishing guild", () ->
				config.getSkillsNecklace() && config.getSkillsNecklaceMode() == SkillsNecklaceMode.FISHING_GUILD);
		swap("remove", targetSwap("skills necklace"), "mining guild", () ->
				config.getSkillsNecklace() && config.getSkillsNecklaceMode() == SkillsNecklaceMode.MINING_GUILD);
		swap("remove", targetSwap("skills necklace"), "farming guild", () ->
				config.getSkillsNecklace() && config.getSkillsNecklaceMode() == SkillsNecklaceMode.FARMING_GUILD);
		swap("remove", targetSwap("skills necklace"), "cooking guild", () ->
				config.getSkillsNecklace() && config.getSkillsNecklaceMode() == SkillsNecklaceMode.COOKING_GUILD);
		swap("remove", targetSwap("skills necklace"), "woodcutting guild", () ->
				config.getSkillsNecklace() & config.getSkillsNecklaceMode() == SkillsNecklaceMode.WOODCUTTING_GUILD);
		swap("remove", targetSwap("skills necklace"), "crafting guild", () ->
				config.getSkillsNecklace() && config.getSkillsNecklaceMode() == SkillsNecklaceMode.CRAFTING_GUILD);

		swap("remove", targetSwap("necklace of passage"), "wizards' tower", () ->
				config.getNecklaceofPassage() && config.getNecklaceofPassageMode() == NecklaceOfPassageMode.WIZARDS_TOWER);
		swap("remove", targetSwap("necklace of passage"), "the outpost", () ->
				config.getNecklaceofPassage() && config.getNecklaceofPassageMode() == NecklaceOfPassageMode.THE_OUTPOST);
		swap("remove", targetSwap("necklace of passage"), "eagles' eyrie", () ->
				config.getNecklaceofPassage() && config.getNecklaceofPassageMode() == NecklaceOfPassageMode.EAGLES_EYRIE);

		swap("remove", targetSwap("digsite pendant"), "digsite", () ->
				config.getDigsitePendant() && config.getDigsitePendantMode() == DigsitePendantMode.DIGSITE);
		swap("remove", targetSwap("digsite pendant"), "fossil island", () ->
				config.getDigsitePendant() && config.getDigsitePendantMode() == DigsitePendantMode.FOSSIL_ISLAND);
		swap("remove", targetSwap("digsite pendant"), "lithkren dungeon", () ->
				config.getDigsitePendant() && config.getDigsitePendantMode() == DigsitePendantMode.LITHKREN);

		swap("remove", targetSwap("ring of wealth"), "miscellania", () ->
				config.getRingofWealth() && config.getRingofWealthMode() == RingOfWealthMode.MISCELLANIA);
		swap("remove", targetSwap("ring of wealth"), "grand exchange", () ->
				config.getRingofWealth() && config.getRingofWealthMode() == RingOfWealthMode.GRAND_EXCHANGE);
		swap("remove", targetSwap("ring of wealth"), "falador", () ->
				config.getRingofWealth() && config.getRingofWealthMode() == RingOfWealthMode.FALADOR);
		swap("remove", targetSwap("ring of wealth"), "dondakan", () ->
				config.getRingofWealth() && config.getRingofWealthMode() == RingOfWealthMode.DONDAKAN);

		swap("remove", targetSwap("talisman"), "xeric's glade", () ->
				config.getXericsTalisman() && config.getXericsTalismanMode() == XericsTalismanMode.XERICS_GLADE);
		swap("remove", targetSwap("talisman"), "xeric's lookout", () ->
				config.getXericsTalisman() && config.getXericsTalismanMode() == XericsTalismanMode.XERICS_LOOKOUT);
		swap("remove", targetSwap("talisman"), "xeric's inferno", () ->
				config.getXericsTalisman() && config.getXericsTalismanMode() == XericsTalismanMode.XERICS_INFERNO);
		swap("remove", targetSwap("talisman"), "xeric's heart", () ->
				config.getXericsTalisman() && config.getXericsTalismanMode() == XericsTalismanMode.XERICS_HEART);
		swap("remove", targetSwap("talisman"), "xeric's honour", () ->
				config.getXericsTalisman() && config.getXericsTalismanMode() == XericsTalismanMode.XERICS_HONOUR);

		swap("wear", targetSwap("crafting cape"), "teleport", () ->
				config.getCraftingCapeMode() == CraftingCapeMode.INVENTORY || config.getCraftingCapeMode() == CraftingCapeMode.ALWAYS);
		swap("remove", targetSwap("crafting cape"), "teleport", () ->
				config.getCraftingCapeMode() == CraftingCapeMode.EQUIPPED || config.getCraftingCapeMode() == CraftingCapeMode.ALWAYS);

		swap("wear", targetSwap("construct."), "tele to poh", () ->
				config.getConstructionCapeMode() == ConstructionCapeMode.INVENTORY || config.getConstructionCapeMode() == ConstructionCapeMode.ALWAYS);
		swap("remove", targetSwap("construct."), "tele to poh", () ->
				config.getConstructionCapeMode() == ConstructionCapeMode.EQUIPPED || config.getConstructionCapeMode() == ConstructionCapeMode.ALWAYS);

		swap("wear", targetSwap("quest point cape"), "teleport", () ->
				config.getQuestCapeMode() == QuestCapeMode.INVENTORY || config.getQuestCapeMode() == QuestCapeMode.ALWAYS);
		swap("remove", targetSwap("quest point cape"), "teleport", () ->
				config.getQuestCapeMode() == QuestCapeMode.EQUIPPED || config.getQuestCapeMode() == QuestCapeMode.ALWAYS);

		swap("wear", targetSwap("achievement diary cape"), "teleport", () ->
				config.getDiaryCapeMode() == DiaryCapeMode.INVENTORY || config.getDiaryCapeMode() == DiaryCapeMode.ALWAYS);
		swap("remove", targetSwap("achievement diary cape"), "teleport", () ->
				config.getDiaryCapeMode() == DiaryCapeMode.EQUIPPED || config.getDiaryCapeMode() == DiaryCapeMode.ALWAYS);

		swap("wear", targetSwap("farming cape"), "teleport", () ->
				config.getFarmingCapeMode() == FarmCapeMode.INVENTORY || config.getFarmingCapeMode() == FarmCapeMode.ALWAYS);
		swap("remove", targetSwap("farming cape"), "teleport", () ->
				config.getFarmingCapeMode() == FarmCapeMode.EQUIPPED || config.getFarmingCapeMode() == FarmCapeMode.ALWAYS);
				
		swap("wear", targetSwap("magic cape"), "spellbook", () ->
				config.getMagicCapeMode() == MagicCapeMode.INVENTORY || config.getMagicCapeMode() == MagicCapeMode.ALWAYS);
		swap("remove", targetSwap("magic cape"), "spellbook", () ->
				config.getMagicCapeMode() == MagicCapeMode.EQUIPPED || config.getMagicCapeMode() == MagicCapeMode.ALWAYS);

		swap("remove", targetSwap("max cape"), "tele to poh", () ->
				config.getMaxCapeEquippedMode() == MaxCapeEquippedMode.TELE_TO_POH);
		swap("remove", targetSwap("max cape"), "crafting guild", () ->
				config.getMaxCapeEquippedMode() == MaxCapeEquippedMode.CRAFTING_GUILD);
		swap("remove", targetSwap("max cape"), "warriors' guild", () ->
				config.getMaxCapeEquippedMode() == MaxCapeEquippedMode.WARRIORS_GUILD);
		swap("remove", targetSwap("max cape"), "fishing teleports", () ->
				config.getMaxCapeEquippedMode() == MaxCapeEquippedMode.FISHING_TELEPORTS);
		swap("remove", targetSwap("max cape"), "poh portals", () ->
				config.getMaxCapeEquippedMode() == MaxCapeEquippedMode.POH_PORTRALS);
		swap("remove", targetSwap("max cape"), "other teleports", () ->
				config.getMaxCapeEquippedMode() == MaxCapeEquippedMode.OTHER_TELEPORTS);
		swap("remove", targetSwap("max cape"), "spellbook", () ->
				config.getMaxCapeEquippedMode() == MaxCapeEquippedMode.SPELLBOOK);
		swap("remove", targetSwap("max cape"), "features", () ->
				config.getMaxCapeEquippedMode() == MaxCapeEquippedMode.FEATURES);
	}

	private void loadSwaps()
	{
		addSwaps();
		loadConstructionItems();
		hideAttackIgnoredNPCs = Text.fromCSV(config.hideAttackIgnoredNPCs().toLowerCase().trim());
		hideCastIgnoredSpells = Text.fromCSV(config.hideCastIgnoredSpells().toLowerCase().trim());
	}

	private void loadConstructionItems()
	{
		targetList = config.getConstructionMode().getTargetList();
		optionsList = config.getConstructionMode().getOptionsList();
	}

	private void swapConstructionMenu(MenuEntry[] menuEntries)
	{
		for (MenuEntry menuEntry : menuEntries)
		{
			if (validConstructionSwap(menuEntry))
			{
				createConstructionMenu(menuEntry);
			}
		}
	}

	public boolean validConstructionSwap(MenuEntry menuEntry)
	{
		return (matchesConstructionOption(menuEntry) && matchesConstructionTarget(menuEntry));
	}

	public boolean matchesConstructionOption(MenuEntry menuEntry)
	{
		return config.getConstructionMode().getOptionsList().stream()
				.anyMatch(Text.standardize(menuEntry.getOption())::contains);
	}

	public boolean matchesConstructionTarget(MenuEntry menuEntry)
	{
		return config.getConstructionMode().getTargetList().stream()
				.anyMatch(Text.standardize(menuEntry.getTarget())::contains);
	}

	private void createConstructionMenu(MenuEntry menuEntry)
	{
		MenuEntry[] newEntries = new MenuEntry[1];

		newEntries[0] = menuEntry;

		client.setMenuEntries(newEntries);
	}

	private final Predicate<MenuEntry> filterMenuEntries = entry ->
	{
		String option = Text.removeTags(entry.getOption()).toLowerCase();
		String target = Text.removeTags(entry.getTarget()).toLowerCase();

		if (config.hideTradeWith() && option.contains("trade with"))
		{
			return false;
		}

		if (config.hideEmpty() && option.contains("empty"))
		{
			return !entry.getTarget().contains("potion") && !entry.getTarget().contains("Antidote")
					&& !entry.getTarget().contains("venom") && !entry.getTarget().contains("antifire")
					&& !entry.getTarget().contains("Antipoison") && !entry.getTarget()
					.contains("Superantipoison")
					&& !entry.getTarget().contains("Saradomin brew") && !entry.getTarget()
					.contains("Super restore")
					&& !entry.getTarget().contains("Zamorak brew") && !entry.getTarget()
					.contains("Guthix rest");
		}

		if (config.hideDestroy() && option.contains("destroy") && target.contains("rune pouch"))
		{
			return false;
		}

		if (config.hideExamine() && option.contains("examine"))
		{
			return false;
		}

		if (config.hideLootImpJars() && target.contains("impling") && option.contains("loot"))
		{
			if (client.getItemContainer(InventoryID.BANK) != null)
			{
				bankItemNames = new ArrayList<>();
				for (Item i : Objects.requireNonNull(client.getItemContainer(InventoryID.BANK)).getItems())
				{
					bankItemNames.add(client.getItemDefinition((i.getId())).getName());
				}
			}
			List<String> invItemNames = new ArrayList<>();
			switch (target)
			{
				case "gourmet impling jar":
					if (client.getItemContainer(InventoryID.INVENTORY) != null)
					{
						for (Item i : Objects.requireNonNull(client.getItemContainer(InventoryID.INVENTORY)).getItems())
						{
							invItemNames.add(client.getItemDefinition((i.getId())).getName());
						}
						if ((invItemNames.contains("Clue scroll (easy)") || bankItemNames.contains("Clue scroll (easy)")))
						{
							return false;
						}
					}
					break;
				case "young impling jar":
					if (client.getItemContainer(InventoryID.INVENTORY) != null)
					{
						for (Item i : Objects.requireNonNull(client.getItemContainer(InventoryID.INVENTORY)).getItems())
						{
							invItemNames.add(client.getItemDefinition((i.getId())).getName());
						}
						if (invItemNames.contains("Clue scroll (beginner)") || bankItemNames.contains("Clue scroll (beginner)"))
						{
							return false;
						}
					}
					break;
				case "eclectic impling jar":
					if (client.getItemContainer(InventoryID.INVENTORY) != null)
					{
						for (Item i : Objects.requireNonNull(client.getItemContainer(InventoryID.INVENTORY)).getItems())
						{
							invItemNames.add(client.getItemDefinition((i.getId())).getName());
						}
						if ((invItemNames.contains("Clue scroll (medium)") || bankItemNames.contains("Clue scroll (medium)")))
						{
							return false;
						}
					}
					break;
				case "magpie impling jar":
				case "nature impling jar":
				case "ninja impling jar":
					if (client.getItemContainer(InventoryID.INVENTORY) != null)
					{
						for (Item i : Objects.requireNonNull(client.getItemContainer(InventoryID.INVENTORY)).getItems())
						{
							invItemNames.add(client.getItemDefinition((i.getId())).getName());
						}
						if ((invItemNames.contains("Clue scroll (hard)") || bankItemNames.contains("Clue scroll (hard)")))
						{
							return false;
						}
					}
					break;
				case "crystal impling jar":
				case "dragon impling jar":
					if (client.getItemContainer(InventoryID.INVENTORY) != null)
					{
						for (Item i : Objects.requireNonNull(client.getItemContainer(InventoryID.INVENTORY)).getItems())
						{
							invItemNames.add(client.getItemDefinition((i.getId())).getName());
						}
						if ((invItemNames.contains("Clue scroll (elite)") || bankItemNames.contains("Clue scroll (elite)")))
						{
							return false;
						}
					}
					break;
			}
		}

		if (config.hideAttack() && entry.getType() == MenuAction.NPC_SECOND_OPTION.getId())
		{
			NPC npc = client.getCachedNPCs()[entry.getIdentifier()];
			if (npc != null && npc.getName() != null && npc.getHealthRatio() == 0 && !hideAttackIgnoredNPCs.contains(Text.standardize(npc.getName())))
			{
				return false;
			}
		}

		if (config.hideCastRaids() && (client.getVar(Varbits.IN_RAID) == 1 || client.getVar(Varbits.THEATRE_OF_BLOOD) == 2))
		{
			if (client.getSpellSelected() && !hideCastIgnoredSpells.contains(Text.standardize(client.getSelectedSpellName())) && entry.getType() == MenuAction.SPELL_CAST_ON_PLAYER.getId())
			{
				return false;
			}
		}

		if (config.hideCastThralls() && target.contains("thrall") && entry.getType() == MenuAction.SPELL_CAST_ON_NPC.getId())
		{
			return false;
		}

		return true;
	};

	private MenuEntry[] updateMenuEntries(MenuEntry[] menuEntries)
	{
		return Arrays.stream(menuEntries)
				.filter(filterMenuEntries).sorted((o1, o2) ->
				{
					//Priority swaps
					var prioSwap = prioSwaps
							.stream()
							.filter(o -> o.getKey().matches(o1) && o.getValue().matches(o2))
							.findFirst();
					if (prioSwap.isPresent())
					{
						return 1;
					}

					prioSwap = prioSwaps
							.stream()
							.filter(o -> o.getKey().matches(o2) && o.getValue().matches(o1))
							.findFirst();
					if (prioSwap.isPresent())
					{
						return -1;
					}

					return 0;
				})
				.sorted(
						//Hotkey swaps
						Comparator.comparingInt(o -> customSwaps.entrySet()
								.stream()
								.filter(x -> x.getKey().matches(o))
								.map(x -> x.getValue())
								.sorted(Comparator.reverseOrder())
								.findFirst().orElse(Integer.MIN_VALUE)))
				.toArray(MenuEntry[]::new);
	}

	private void parseOldFormatConfig()
	{
		if (config.customSwapsString().trim().isEmpty())
		{
			return;
		}

		// LOAD OLD FORMAT CUSTOM SWAPS
		StringBuilder newFormatString = new StringBuilder();
		for (String oldFormat : config.customSwapsString().trim().split("\n"))
		{
			oldFormat = oldFormat.split(":")[0];
			newFormatString.append(oldFormat).append("\n");
		}

		if (newFormatString.length() > 0)
		{
			configManager.setConfiguration("menuentryswapperextended", "customSwaps", newFormatString);
		}
	}
}
