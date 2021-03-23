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
import java.util.List;
import java.util.function.Predicate;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import static net.runelite.api.ItemID.FIRE_TIARA;
import static net.runelite.api.ItemID.MAX_CAPE;
import static net.runelite.api.ItemID.RUNECRAFT_CAPE;
import static net.runelite.api.ItemID.RUNECRAFT_CAPET;
import net.runelite.api.MenuEntry;
import net.runelite.api.Player;
import net.runelite.api.Varbits;
import net.runelite.api.WorldType;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.MenuOpened;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.kit.KitType;
import net.runelite.api.util.Text;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.input.KeyManager;
import net.runelite.client.menus.MenuManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginManager;
import net.runelite.client.plugins.menuentryswapper.MenuEntrySwapperPlugin;
import net.runelite.client.plugins.menuentryswapperextended.util.teleportmode.BurningAmuletMode;
import net.runelite.client.plugins.menuentryswapperextended.util.teleportmode.CombatBraceletMode;
import net.runelite.client.plugins.menuentryswapperextended.util.teleportmode.DigsitePendantMode;
import net.runelite.client.plugins.menuentryswapperextended.util.teleportmode.DuelingRingMode;
import net.runelite.client.plugins.menuentryswapperextended.util.teleportmode.GamesNecklaceMode;
import net.runelite.client.plugins.menuentryswapperextended.util.teleportmode.GloryMode;
import net.runelite.client.plugins.menuentryswapperextended.util.teleportmode.NecklaceOfPassageMode;
import net.runelite.client.plugins.menuentryswapperextended.util.teleportmode.RingOfWealthMode;
import net.runelite.client.plugins.menuentryswapperextended.util.teleportmode.SkillsNecklaceMode;
import net.runelite.client.plugins.menuentryswapperextended.util.teleportmode.XericsTalismanMode;
import net.runelite.client.plugins.pvptools.PvpToolsConfig;
import net.runelite.client.plugins.pvptools.PvpToolsPlugin;
import org.pf4j.Extension;

@Extension
@PluginDescriptor(
	name = "Menu Entry Swapper Extended",
	enabledByDefault = false,
	description = "Change the default option that is displayed when hovering over objects",
	tags = {"pickpocket", "equipped items", "inventory", "items", "equip", "construction"}
)
@PluginDependency(PvpToolsPlugin.class)
public class MenuEntrySwapperExtendedPlugin extends Plugin
{
	private static final Object HOTKEY = new Object();
	private static final Object HOTKEY_CHECK = new Object();

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

	@Inject
	private MenuEntrySwapperPlugin mesPlugin;

	private boolean buildingMode;
	private boolean inTobRaid = false;
	private boolean inCoxRaid = false;

	private static final int FIRE_ALTAR = 10315;
	private static final int CWARS = 9776;
	private static final int CRAFT_GUILD = 11571;

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
		setCastOptions(true);
		loadSwaps();
	}

	@Override
	public void shutDown()
	{
		removeSwaps();
		if (client.getGameState() == GameState.LOGGED_IN)
		{
			resetCastOptions();
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
		if (event.getGameState() == GameState.LOGGED_IN)
		{
			removeSwaps();
			loadSwaps();
		}
	}

	@Subscribe
	private void onVarbitChanged(VarbitChanged event)
	{
		buildingMode = client.getVarbitValue(2176) == 1;
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

	private void loadSwaps()
	{
		addSwaps();
		loadConstructionItems();
	}

	private Predicate<String> sW(String s)
	{
		return (in) -> in.toLowerCase().startsWith(s);
	}

	private void addSwaps()
	{

		mesPlugin.swapContains("", (s) -> true, "pickpocket", config::swapPickpocket);

		mesPlugin.swap("rub", sW("burning amulet"), "chaos temple", () -> config.getBurningAmuletMode() == BurningAmuletMode.CHAOS_TEMPLE);
		mesPlugin.swap("rub", sW("burning amulet"), "bandit camp", () -> config.getBurningAmuletMode() == BurningAmuletMode.BANDIT_CAMP);
		mesPlugin.swap("rub", sW("burning amulet"), "lava maze", () -> config.getBurningAmuletMode() == BurningAmuletMode.LAVA_MAZE);

		mesPlugin.swap("rub", sW("combat bracelet"), "warriors' guild", () -> config.getCombatBraceletMode() == CombatBraceletMode.WARRIORS_GUILD);
		mesPlugin.swap("rub", sW("combat bracelet"), "champions' guild", () -> config.getCombatBraceletMode() == CombatBraceletMode.CHAMPIONS_GUILD);
		mesPlugin.swap("rub", sW("combat bracelet"), "edgeville monastery", () -> config.getCombatBraceletMode() == CombatBraceletMode.EDGEVILLE_MONASTERY);
		mesPlugin.swap("rub", sW("combat bracelet"), "ranging guild", () -> config.getCombatBraceletMode() == CombatBraceletMode.RANGING_GUILD);

		mesPlugin.swap("rub", sW("games necklace"), "burthorpe", () -> config.getGamesNecklaceMode() == GamesNecklaceMode.BURTHORPE);
		mesPlugin.swap("rub", sW("games necklace"), "barbarian outpost", () -> config.getGamesNecklaceMode() == GamesNecklaceMode.BARBARIAN_OUTPOST);
		mesPlugin.swap("rub", sW("games necklace"), "corporeal beast", () -> config.getGamesNecklaceMode() == GamesNecklaceMode.CORPOREAL_BEAST);
		mesPlugin.swap("rub", sW("games necklace"), "tears of guthix", () -> config.getGamesNecklaceMode() == GamesNecklaceMode.TEARS_OF_GUTHIX);
		mesPlugin.swap("rub", sW("games necklace"), "wintertodt camp", () -> config.getGamesNecklaceMode() == GamesNecklaceMode.WINTER);

		mesPlugin.swap("rub", sW("ring of dueling"), "duel arena", () -> config.getDuelingRingMode() == DuelingRingMode.DUEL_ARENA);
		mesPlugin.swap("rub", sW("ring of dueling"), "castle wars", () -> config.getDuelingRingMode() == DuelingRingMode.CASTLE_WARS);
		mesPlugin.swap("rub", sW("ring of dueling"), "ferox enclave", () -> config.getDuelingRingMode() == DuelingRingMode.FEROX_ENCLAVE);

		mesPlugin.swap("rub", sW("amulet of glory"), "edgeville", () -> config.getGloryMode() == GloryMode.EDGEVILLE);
		mesPlugin.swap("rub", sW("amulet of glory"), "karamja", () -> config.getGloryMode() == GloryMode.KARAMJA);
		mesPlugin.swap("rub", sW("amulet of glory"), "al kharid", () -> config.getGloryMode() == GloryMode.AL_KHARID);
		mesPlugin.swap("rub", sW("amulet of glory"), "draynor village", () -> config.getGloryMode() == GloryMode.DRAYNOR_VILLAGE);

		mesPlugin.swap("rub", sW("skills necklace"), "fishing guild", () -> config.getSkillsNecklaceMode() == SkillsNecklaceMode.FISHING_GUILD);
		mesPlugin.swap("rub", sW("skills necklace"), "mining guild", () -> config.getSkillsNecklaceMode() == SkillsNecklaceMode.MINING_GUILD);
		mesPlugin.swap("rub", sW("skills necklace"), "farming guild", () -> config.getSkillsNecklaceMode() == SkillsNecklaceMode.FARMING_GUILD);
		mesPlugin.swap("rub", sW("skills necklace"), "cooking guild", () -> config.getSkillsNecklaceMode() == SkillsNecklaceMode.COOKING_GUILD);
		mesPlugin.swap("rub", sW("skills necklace"), "woodcutting guild", () -> config.getSkillsNecklaceMode() == SkillsNecklaceMode.WOODCUTTING_GUILD);
		mesPlugin.swap("rub", sW("skills necklace"), "crafting guild", () -> config.getSkillsNecklaceMode() == SkillsNecklaceMode.CRAFTING_GUILD);

		mesPlugin.swap("rub", sW("necklace of passage"), "wizards' tower", () -> config.getNecklaceofPassageMode() == NecklaceOfPassageMode.WIZARDS_TOWER);
		mesPlugin.swap("rub", sW("necklace of passage"), "the outpost", () -> config.getNecklaceofPassageMode() == NecklaceOfPassageMode.THE_OUTPOST);
		mesPlugin.swap("rub", sW("necklace of passage"), "eagles' eyrie", () -> config.getNecklaceofPassageMode() == NecklaceOfPassageMode.EAGLES_EYRIE);

		mesPlugin.swap("rub", sW("digsite pendant"), "digsite", () -> config.getDigsitePendantMode() == DigsitePendantMode.DIGSITE);
		mesPlugin.swap("rub", sW("digsite pendant"), "fossil island", () -> config.getDigsitePendantMode() == DigsitePendantMode.FOSSIL_ISLAND);
		mesPlugin.swap("rub", sW("digsite pendant"), "lithkren dungeon", () -> config.getDigsitePendantMode() == DigsitePendantMode.LITHKREN);

		mesPlugin.swap("rub", sW("ring of wealth"), "Miscellania", () -> config.getRingofWealthMode() == RingOfWealthMode.MISCELLANIA);
		mesPlugin.swap("rub", sW("ring of wealth"), "Grand Exchange", () -> config.getRingofWealthMode() == RingOfWealthMode.GRAND_EXCHANGE);
		mesPlugin.swap("rub", sW("ring of wealth"), "Falador", () -> config.getRingofWealthMode() == RingOfWealthMode.FALADOR);
		mesPlugin.swap("rub", sW("ring of wealth"), "Dondakan", () -> config.getRingofWealthMode() == RingOfWealthMode.DONDAKAN);

		mesPlugin.swap("rub", sW("talisman"), "Xeric's Glade", () -> config.getXericsTalismanMode() == XericsTalismanMode.XERICS_GLADE);
		mesPlugin.swap("rub", sW("ring of wealth"), "Xeric's Look-out", () -> config.getXericsTalismanMode() == XericsTalismanMode.XERICS_LOOKOUT);
		mesPlugin.swap("rub", sW("ring of wealth"), "Xeric's Inferno", () -> config.getXericsTalismanMode() == XericsTalismanMode.XERICS_INFERNO);
		mesPlugin.swap("rub", sW("ring of wealth"), "Xeric's Heart", () -> config.getXericsTalismanMode() == XericsTalismanMode.XERICS_HEART);
		mesPlugin.swap("rub", sW("ring of wealth"), "Xeric's Honour", () -> config.getXericsTalismanMode() == XericsTalismanMode.XERICS_HONOUR);
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
		if (client.getLocalPlayer() == null || client.getLocalPlayer().getPlayerComposition() == null)
		{
			return false;
		}
		return client.getLocalPlayer().getPlayerComposition().getEquipmentId(KitType.HEAD) == FIRE_TIARA
			|| client.getLocalPlayer().getPlayerComposition().getEquipmentId(KitType.CAPE) == RUNECRAFT_CAPE
			|| client.getLocalPlayer().getPlayerComposition().getEquipmentId(KitType.CAPE) == RUNECRAFT_CAPET
			|| client.getLocalPlayer().getPlayerComposition().getEquipmentId(KitType.CAPE) == MAX_CAPE;
	}
}
