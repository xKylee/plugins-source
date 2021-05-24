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
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.google.inject.Provides;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.Player;
import net.runelite.api.Varbits;
import net.runelite.api.WorldType;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOpened;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.util.Text;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginManager;
import net.runelite.client.plugins.menuentryswapper.MenuEntrySwapperPlugin;
import net.runelite.client.plugins.menuentryswapperextended.util.BurningAmuletMode;
import net.runelite.client.plugins.menuentryswapperextended.util.CombatBraceletMode;
import net.runelite.client.plugins.menuentryswapperextended.util.ConstructionCapeMode;
import net.runelite.client.plugins.menuentryswapperextended.util.CraftingCapeMode;
import net.runelite.client.plugins.menuentryswapperextended.util.DigsitePendantMode;
import net.runelite.client.plugins.menuentryswapperextended.util.DuelingRingMode;
import net.runelite.client.plugins.menuentryswapperextended.util.GamesNecklaceMode;
import net.runelite.client.plugins.menuentryswapperextended.util.GloryMode;
import net.runelite.client.plugins.menuentryswapperextended.util.MagicCapeMode;
import net.runelite.client.plugins.menuentryswapperextended.util.MaxCapeEquippedMode;
import net.runelite.client.plugins.menuentryswapperextended.util.NecklaceOfPassageMode;
import net.runelite.client.plugins.menuentryswapperextended.util.RingOfWealthMode;
import net.runelite.client.plugins.menuentryswapperextended.util.SkillsNecklaceMode;
import net.runelite.client.plugins.menuentryswapperextended.util.XericsTalismanMode;
import net.runelite.client.plugins.menuentryswapperextended.util.comparableentry.AbstractComparableEntry;
import net.runelite.client.plugins.pvptools.PvpToolsConfig;
import net.runelite.client.plugins.pvptools.PvpToolsPlugin;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.pf4j.Extension;
import static net.runelite.api.Varbits.IN_WILDERNESS;
import static net.runelite.client.plugins.menuentryswapperextended.util.comparableentry.ComparableEntries.newBaseComparableEntry;

@Extension
@PluginDescriptor(
	name = "Menu Entry Swapper Extended",
	enabledByDefault = false,
	description = "Change the default option that is displayed when hovering over objects",
	tags = {"pickpocket", "equipped items", "inventory", "items", "equip", "construction"}
)
@PluginDependency(PvpToolsPlugin.class)
@PluginDependency(MenuEntrySwapperPlugin.class)
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
	private PvpToolsPlugin pvpTools;

	@Inject
	private PvpToolsConfig pvpToolsConfig;

	@Inject
	private MenuEntrySwapperPlugin mesPlugin;

	private boolean inTobRaid = false;
	private boolean inCoxRaid = false;

	private MenuEntry[] menuEntries;
	List<String> targetList;
	List<String> optionsList;

	private static final Splitter NEWLINE_SPLITTER = Splitter
			.on("\n")
			.omitEmptyStrings()
			.trimResults();
	private final Map<AbstractComparableEntry, Integer> customSwaps = new HashMap<>();
	private final List<Pair<AbstractComparableEntry, AbstractComparableEntry>> prioSwaps = new ArrayList<>();

	@Provides
	MenuEntrySwapperExtendedConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(MenuEntrySwapperExtendedConfig.class);
	}

	@Override
	public void startUp()
	{
		//Clean up old custom swaps configuration in MenuEntrySwapper-plugin
		if (configManager != null && Strings.isNullOrEmpty(config.customSwaps()))
		{
			String oldCustomSwaps = configManager.getConfiguration("menuentryswapper", "customSwaps");
			if (!Strings.isNullOrEmpty(oldCustomSwaps))
			{
				configManager.setConfiguration("menuentryswapperextended", "customSwaps", oldCustomSwaps);
				configManager.unsetConfiguration("menuentryswapper", "customSwaps");
			}
		}

		loadCustomSwaps(config.customSwaps(), customSwaps);
		loadPrioSwaps(config.prioEntry(), prioSwaps);

		if (client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}
		setCastOptions(true);
		loadSwaps();
	}

	@Override
	public void shutDown()
	{
		loadCustomSwaps("", customSwaps); // Removes all custom swaps
		loadPrioSwaps("", prioSwaps); // Removes all priority swaps

		if (client.getGameState() == GameState.LOGGED_IN)
		{
			resetCastOptions();
		}
	}

	@Subscribe
	private void onMenuEntryAdded(MenuEntryAdded menuEntryAdded)
	{
		if (!config.getEasyConstruction())
		{
			return;
		}

		if ((client.getVarbitValue(2176) != 1) && menuEntryAdded.getOpcode() != MenuAction.GAME_OBJECT_FIFTH_OPTION.getId())
		{
			return;
		}

		menuEntries = client.getMenuEntries();
		swapConstructionMenu(menuEntries);
	}

	@Subscribe
	private void onConfigChanged(ConfigChanged event)
	{
		if (!"menuentryswapperextended".equals(event.getGroup()))
		{
			return;
		}

		loadSwaps();

		switch (event.getKey())
		{
			case "customSwaps":
				loadCustomSwaps(config.customSwaps(), customSwaps);
				return;
			case "prioEntry":
				loadPrioSwaps(config.prioEntry(), prioSwaps);
				return;
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
	private void onVarbitChanged(VarbitChanged event)
	{
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

		event.setMenuEntries(updateMenuEntries(event.getMenuEntries()));
		event.setModified();
	}

	private final Predicate<MenuEntry> filterMenuEntries = entry ->
	{
		String option = Text.removeTags(entry.getOption()).toLowerCase();

		if (option.contains("trade with") && config.hideTradeWith())
		{
			return false;
		}

		if (option.contains("empty") && config.hideEmpty())
		{
			if (entry.getTarget().contains("potion") || entry.getTarget().contains("Antidote")
					|| entry.getTarget().contains("venom") || entry.getTarget().contains("antifire")
					|| entry.getTarget().contains("Antipoison") || entry.getTarget().contains("Superantipoison")
					|| entry.getTarget().contains("Saradomin brew") || entry.getTarget().contains("Super restore")
					|| entry.getTarget().contains("Zamorak brew") || entry.getTarget().contains("Guthix rest"))
			{
				return false;
			}
		}

		return true;
	};

	@Subscribe
	public void onClientTick(ClientTick clientTick)
	{
		// The menu is not rebuilt when it is open, so don't swap or else it will
		// repeatedly swap entries
		if (client.getGameState() != GameState.LOGGED_IN || client.isMenuOpen())
		{
			return;
		}

		client.setMenuEntries(updateMenuEntries(client.getMenuEntries()));
	}

	private MenuEntry[] updateMenuEntries(MenuEntry[] menuEntries)
	{
		return Arrays.stream(menuEntries)
				.filter(filterMenuEntries)
				.sorted((o1, o2) ->
				{
					//Priority swaps
					var prioSwap = prioSwaps
							.stream()
							.filter(o -> o.getKey().matches(o1) && o.getValue().matches(o2))
							.findFirst();
					if (prioSwap.isPresent())
						return 1;

					prioSwap = prioSwaps
							.stream()
							.filter(o -> o.getKey().matches(o2) && o.getValue().matches(o1))
							.findFirst();
					if (prioSwap.isPresent())
						return -1;

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

	private void loadSwaps()
	{
		addSwaps();
		loadConstructionItems();
	}

	private void loadConstructionItems()
	{
		targetList = config.getConstructionMode().getTargetList();
		optionsList = config.getConstructionMode().getOptionsList();
	}


	private void loadPrioSwaps(String config, List<Pair<AbstractComparableEntry, AbstractComparableEntry>> map)
	{
		map.clear();
		if (Strings.isNullOrEmpty(config))
		{
			return;
		}

		StreamSupport
			.stream(NEWLINE_SPLITTER.split(config).spliterator(), false)
			.map(s -> Arrays.stream(s.split(",")).filter(o -> !Strings.isNullOrEmpty(o)).toArray(String[]::new))
			.filter(o -> o.length == 2)
			.forEach(o -> map.add(new ImmutablePair<>(
					newBaseComparableEntry(o[0], "", -1, -1, true, false),
					newBaseComparableEntry("", o[1], -1, -1, false, false))));
	}

	private void loadCustomSwaps(String config, Map<AbstractComparableEntry, Integer> map)
	{
		final Map<AbstractComparableEntry, Integer> tmp = new HashMap<>();

		if (!Strings.isNullOrEmpty(config))
		{
			final StringBuilder sb = new StringBuilder();

			for (String str : NEWLINE_SPLITTER.split(config))
			{
				if (!str.startsWith("//"))
				{
					sb.append(str).append("\n");
				}
			}

			final Map<String, String> split = NEWLINE_SPLITTER.withKeyValueSeparator(':').split(sb);

			for (Map.Entry<String, String> entry : split.entrySet())
			{
				final String prio = entry.getKey();
				int priority;
				try
				{
					priority = Integer.parseInt(entry.getValue().trim());
				}
				catch (NumberFormatException e)
				{
					priority = 0;
				}
				final String[] splitFrom = Text.standardize(prio).split(",");
				final String optionFrom = splitFrom[0].trim();
				final String targetFrom;
				if (splitFrom.length == 1)
				{
					targetFrom = "";
				}
				else
				{
					targetFrom = splitFrom[1].trim();
				}

				final AbstractComparableEntry prioEntry = newBaseComparableEntry(optionFrom, targetFrom, !Strings.isNullOrEmpty(targetFrom));

				tmp.put(prioEntry, priority);
			}
		}

		map.clear();
		map.putAll(tmp);
	}

	private Predicate<String> targetSwap(String string)
	{
		return (in) -> in.toLowerCase().contains(string);
	}

	private void addSwaps()
	{
		for (String option : new String[]{"attack", "talk-to"})
		{
			mesPlugin.swapContains(option, (s) -> true, "pickpocket", config::swapPickpocket);
		}

		mesPlugin.swap("remove", targetSwap("burning amulet"), "chaos temple", () -> config.getBurningAmuletMode() == BurningAmuletMode.CHAOS_TEMPLE);
		mesPlugin.swap("remove", targetSwap("burning amulet"), "bandit camp", () -> config.getBurningAmuletMode() == BurningAmuletMode.BANDIT_CAMP);
		mesPlugin.swap("remove", targetSwap("burning amulet"), "lava maze", () -> config.getBurningAmuletMode() == BurningAmuletMode.LAVA_MAZE);

		mesPlugin.swap("remove", targetSwap("combat bracelet"), "warriors' guild", () -> config.getCombatBraceletMode() == CombatBraceletMode.WARRIORS_GUILD);
		mesPlugin.swap("remove", targetSwap("combat bracelet"), "champions' guild", () -> config.getCombatBraceletMode() == CombatBraceletMode.CHAMPIONS_GUILD);
		mesPlugin.swap("remove", targetSwap("combat bracelet"), "edgeville monastery", () -> config.getCombatBraceletMode() == CombatBraceletMode.EDGEVILLE_MONASTERY);
		mesPlugin.swap("remove", targetSwap("combat bracelet"), "ranging guild", () -> config.getCombatBraceletMode() == CombatBraceletMode.RANGING_GUILD);

		mesPlugin.swap("remove", targetSwap("games necklace"), "burthorpe", () -> config.getGamesNecklaceMode() == GamesNecklaceMode.BURTHORPE);
		mesPlugin.swap("remove", targetSwap("games necklace"), "barbarian outpost", () -> config.getGamesNecklaceMode() == GamesNecklaceMode.BARBARIAN_OUTPOST);
		mesPlugin.swap("remove", targetSwap("games necklace"), "corporeal beast", () -> config.getGamesNecklaceMode() == GamesNecklaceMode.CORPOREAL_BEAST);
		mesPlugin.swap("remove", targetSwap("games necklace"), "tears of guthix", () -> config.getGamesNecklaceMode() == GamesNecklaceMode.TEARS_OF_GUTHIX);
		mesPlugin.swap("remove", targetSwap("games necklace"), "wintertodt camp", () -> config.getGamesNecklaceMode() == GamesNecklaceMode.WINTER);

		mesPlugin.swap("remove", targetSwap("ring of dueling"), "duel arena", () -> config.getDuelingRingMode() == DuelingRingMode.DUEL_ARENA);
		mesPlugin.swap("remove", targetSwap("ring of dueling"), "castle wars", () -> config.getDuelingRingMode() == DuelingRingMode.CASTLE_WARS);
		mesPlugin.swap("remove", targetSwap("ring of dueling"), "ferox enclave", () -> config.getDuelingRingMode() == DuelingRingMode.FEROX_ENCLAVE);

		mesPlugin.swap("remove", targetSwap("amulet of glory"), "edgeville", () -> config.getGloryMode() == GloryMode.EDGEVILLE);
		mesPlugin.swap("remove", targetSwap("amulet of glory"), "karamja", () -> config.getGloryMode() == GloryMode.KARAMJA);
		mesPlugin.swap("remove", targetSwap("amulet of glory"), "al kharid", () -> config.getGloryMode() == GloryMode.AL_KHARID);
		mesPlugin.swap("remove", targetSwap("amulet of glory"), "draynor village", () -> config.getGloryMode() == GloryMode.DRAYNOR_VILLAGE);
		mesPlugin.swap("remove", targetSwap("amulet of eternal glory"), "edgeville", () -> config.getGloryMode() == GloryMode.EDGEVILLE);
		mesPlugin.swap("remove", targetSwap("amulet of eternal glory"), "karamja", () -> config.getGloryMode() == GloryMode.KARAMJA);
		mesPlugin.swap("remove", targetSwap("amulet of eternal glory"), "al kharid", () -> config.getGloryMode() == GloryMode.AL_KHARID);
		mesPlugin.swap("remove", targetSwap("amulet of eternal glory"), "draynor village", () -> config.getGloryMode() == GloryMode.DRAYNOR_VILLAGE);

		mesPlugin.swap("remove", targetSwap("skills necklace"), "fishing guild", () -> config.getSkillsNecklaceMode() == SkillsNecklaceMode.FISHING_GUILD);
		mesPlugin.swap("remove", targetSwap("skills necklace"), "mining guild", () -> config.getSkillsNecklaceMode() == SkillsNecklaceMode.MINING_GUILD);
		mesPlugin.swap("remove", targetSwap("skills necklace"), "farming guild", () -> config.getSkillsNecklaceMode() == SkillsNecklaceMode.FARMING_GUILD);
		mesPlugin.swap("remove", targetSwap("skills necklace"), "cooking guild", () -> config.getSkillsNecklaceMode() == SkillsNecklaceMode.COOKING_GUILD);
		mesPlugin.swap("remove", targetSwap("skills necklace"), "woodcutting guild", () -> config.getSkillsNecklaceMode() == SkillsNecklaceMode.WOODCUTTING_GUILD);
		mesPlugin.swap("remove", targetSwap("skills necklace"), "crafting guild", () -> config.getSkillsNecklaceMode() == SkillsNecklaceMode.CRAFTING_GUILD);

		mesPlugin.swap("remove", targetSwap("necklace of passage"), "wizards' tower", () -> config.getNecklaceofPassageMode() == NecklaceOfPassageMode.WIZARDS_TOWER);
		mesPlugin.swap("remove", targetSwap("necklace of passage"), "the outpost", () -> config.getNecklaceofPassageMode() == NecklaceOfPassageMode.THE_OUTPOST);
		mesPlugin.swap("remove", targetSwap("necklace of passage"), "eagles' eyrie", () -> config.getNecklaceofPassageMode() == NecklaceOfPassageMode.EAGLES_EYRIE);

		mesPlugin.swap("remove", targetSwap("digsite pendant"), "digsite", () -> config.getDigsitePendantMode() == DigsitePendantMode.DIGSITE);
		mesPlugin.swap("remove", targetSwap("digsite pendant"), "fossil island", () -> config.getDigsitePendantMode() == DigsitePendantMode.FOSSIL_ISLAND);
		mesPlugin.swap("remove", targetSwap("digsite pendant"), "lithkren dungeon", () -> config.getDigsitePendantMode() == DigsitePendantMode.LITHKREN);

		mesPlugin.swap("remove", targetSwap("ring of wealth"), "miscellania", () -> config.getRingofWealthMode() == RingOfWealthMode.MISCELLANIA);
		mesPlugin.swap("remove", targetSwap("ring of wealth"), "grand exchange", () -> config.getRingofWealthMode() == RingOfWealthMode.GRAND_EXCHANGE);
		mesPlugin.swap("remove", targetSwap("ring of wealth"), "falador", () -> config.getRingofWealthMode() == RingOfWealthMode.FALADOR);
		mesPlugin.swap("remove", targetSwap("ring of wealth"), "dondakan", () -> config.getRingofWealthMode() == RingOfWealthMode.DONDAKAN);

		mesPlugin.swap("remove", targetSwap("talisman"), "xeric's glade", () -> config.getXericsTalismanMode() == XericsTalismanMode.XERICS_GLADE);
		mesPlugin.swap("remove", targetSwap("talisman"), "xeric's look-out", () -> config.getXericsTalismanMode() == XericsTalismanMode.XERICS_LOOKOUT);
		mesPlugin.swap("remove", targetSwap("talisman"), "xeric's inferno", () -> config.getXericsTalismanMode() == XericsTalismanMode.XERICS_INFERNO);
		mesPlugin.swap("remove", targetSwap("talisman"), "xeric's heart", () -> config.getXericsTalismanMode() == XericsTalismanMode.XERICS_HEART);
		// Disable this for now as the method does not support the operation to be executed with desired outcome
		//mesPlugin.swap("remove", targetSwap("talisman"), "xeric's honour", () -> config.getXericsTalismanMode() == XericsTalismanMode.XERICS_HONOUR);

		mesPlugin.swap("wear", targetSwap("crafting cape"), "teleport",
				() -> config.getCraftingCapeMode() == CraftingCapeMode.INVENTORY || config.getCraftingCapeMode() == CraftingCapeMode.ALWAYS);
		mesPlugin.swap("remove", targetSwap("crafting cape"), "teleport",
				() -> config.getCraftingCapeMode() == CraftingCapeMode.EQUIPPED || config.getCraftingCapeMode() == CraftingCapeMode.ALWAYS);

		mesPlugin.swap("wear", targetSwap("construct."), "tele to poh",
				() -> config.getConstructionCapeMode() == ConstructionCapeMode.INVENTORY || config.getConstructionCapeMode() == ConstructionCapeMode.ALWAYS);
		mesPlugin.swap("remove", targetSwap("construct."), "tele to poh",
				() -> config.getConstructionCapeMode() == ConstructionCapeMode.EQUIPPED || config.getConstructionCapeMode() == ConstructionCapeMode.ALWAYS);

		mesPlugin.swap("wear", targetSwap("magic cape"), "spellbook", () -> config.getMagicCapeMode() == MagicCapeMode.INVENTORY || config.getMagicCapeMode() == MagicCapeMode.ALWAYS);
		mesPlugin.swap("remove", targetSwap("magic cape"), "spellbook", () -> config.getMagicCapeMode() == MagicCapeMode.EQUIPPED || config.getMagicCapeMode() == MagicCapeMode.ALWAYS);

		mesPlugin.swap("remove", targetSwap("max cape"), "tele to poh", () -> config.getMaxCapeEquippedMode() == MaxCapeEquippedMode.TELE_TO_POH);
		mesPlugin.swap("remove", targetSwap("max cape"), "crafting guild", () -> config.getMaxCapeEquippedMode() == MaxCapeEquippedMode.CRAFTING_GUILD);
		mesPlugin.swap("remove", targetSwap("max cape"), "warriors' guild", () -> config.getMaxCapeEquippedMode() == MaxCapeEquippedMode.WARRIORS_GUILD);
		mesPlugin.swap("remove", targetSwap("max cape"), "fishing teleports", () -> config.getMaxCapeEquippedMode() == MaxCapeEquippedMode.FISHING_TELEPORTS);
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
			if (client.getVar(IN_WILDERNESS) == 1 || WorldType.isAllPvpWorld(client.getWorldType()) && pluginManager.isPluginEnabled(pvpTools) && pvpToolsConfig.hideCast())
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
}
