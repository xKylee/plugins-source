/*
 * THIS PLUGIN WAS WRITTEN BY A KEYBOARD-WIELDING MONKEY BOI BUT SHUFFLED BY A KANGAROO WITH THUMBS.
 * The plugin and it's refactoring was intended for xKylee's Externals but I'm sure if you're reading this, you're probably planning to yoink..
 * or you're just genuinely curious. If you're trying to yoink, it doesn't surprise me.. just don't claim it as your own. Cheers.
 */

package net.runelite.client.plugins.theatre;

import java.awt.Color;
import java.awt.Font;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;

@ConfigGroup("Theatre")
public interface TheatreConfig extends Config
{
	/*====== General Section ======*/

	@ConfigSection(
		name = "General",
		description = "General Configurartion",
		position = 0,
		keyName = "generalSection"
	)
	String generalSection = "General";

	@Range(max = 20)
	@ConfigItem(
		position = 1,
		keyName = "theatreFontSize",
		name = "Theatre Overlay Font Size",
		description = "Sets the font size for all theatre text overlays.",
		section = generalSection
	)
	default int theatreFontSize()
	{
		return 12;
	}

	@ConfigItem(
		keyName = "fontStyle",
		name = "Font Style",
		description = "Bold/Italics/Plain.",
		position = 2,
		section = generalSection
	)
	default FontStyle fontStyle()
	{
		return FontStyle.BOLD;
	}


	/*====== Prayer Section ======*/

	@ConfigSection(
		name = "Prayer",
		description = "Prayer Configuration",
		position = 100,
		keyName = "prayerSection"
	)
	String prayerSection = "Prayer";

	@ConfigItem(
		position = 101,
		keyName = "prayerHelper",
		name = "Prayer Helper",
		description = "Display prayer indicator in the prayer tab or in the bottom right corner of the screen",
		section = prayerSection
	)
	default boolean prayerHelper()
	{
		return true;
	}

	@ConfigItem(
		position = 102,
		keyName = "descendingBoxes",
		name = "Prayer Descending Boxes",
		description = "Draws timing boxes above the prayer icons, as if you were playing Guitar Hero",
		hidden = true,
		unhide = "prayerHelper",
		section = prayerSection
	)
	default boolean descendingBoxes()
	{
		return false;
	}

	@ConfigItem(
		position = 103,
		keyName = "indicateNonPriorityDescendingBoxes",
		name = "Indicate Non-Priority Boxes",
		description = "Render descending boxes for prayers that are not the priority prayer for that tick",
		hidden = true,
		unhide = "descendingBoxes",
		section = prayerSection
	)
	default boolean indicateNonPriorityDescendingBoxes()
	{
		return true;
	}

	@ConfigItem(
		position = 104,
		keyName = "alwaysShowPrayerHelper",
		name = "Always Show Prayer Helper",
		description = "Render prayer helper at all time, even when other inventory tabs are open.",
		hidden = true,
		unhide = "prayerHelper",
		section = prayerSection
	)
	default boolean alwaysShowPrayerHelper()
	{
		return true;
	}

	@Alpha
	@ConfigItem(
		position = 105,
		keyName = "prayerColor",
		name = "Box Color",
		description = "Color for descending box normal",
		hidden = true,
		unhide = "descendingBoxes",
		section = prayerSection
	)
	default Color prayerColor()
	{
		return Color.ORANGE;
	}

	@Alpha
	@ConfigItem(
		position = 106,
		keyName = "prayerColorDanger",
		name = "Box Color Danger",
		description = "Color for descending box one tick before damage",
		hidden = true,
		unhide = "descendingBoxes",
		section = prayerSection
	)
	default Color prayerColorDanger()
	{
		return Color.RED;
	}

	@ConfigItem(
		position = 107,
		keyName = "verzikPrayerHelper",
		name = "Verzik",
		description = "Render prayers during the verzik fight",
		hidden = true,
		unhide = "prayerHelper",
		section = prayerSection
	)
	default boolean verzikPrayerHelper()
	{
		return true;
	}

	@ConfigItem(
		position = 108,
		keyName = "sotetsegPrayerHelper",
		name = "Sotetseg",
		description = "Render prayers during the sotetseg fight",
		hidden = true,
		unhide = "prayerHelper",
		section = prayerSection
	)
	default boolean sotetsegPrayerHelper()
	{
		return false;
	}


	/*====== Maiden Section ======*/


	@ConfigSection(
		name = "Maiden",
		description = "Maiden's Configuration",
		position = 200,
		keyName = "maidenSection",
		closedByDefault = true
	)
	String maidenSection = "Maiden";

	@ConfigItem(
		position = 201,
		keyName = "maidenBlood",
		name = "Maiden Blood Attack Marker",
		description = "Highlights Maiden's Blood Pools.",
		section = maidenSection
	)
	default boolean maidenBlood()
	{
		return true;
	}

	@ConfigItem(
		position = 202,
		keyName = "maidenSpawns",
		name = "Maiden Blood Spawns Marker",
		description = "Highlights Maiden Blood Spawns (Tomatoes).",
		section = maidenSection
	)
	default boolean maidenSpawns()
	{
		return true;
	}

	@ConfigItem(
		position = 203,
		keyName = "maidenReds",
		name = "Maiden Reds Health Overlay",
		description = "Displays the health of each red crab.",
		section = maidenSection
	)
	default boolean maidenRedsHealth()
	{
		return true;
	}

	@ConfigItem(
		position = 204,
		keyName = "maidenRedsDistance",
		name = "Maiden Reds Distance Overlay",
		description = "Displays the distance of each red crab to reach Maiden.",
		section = maidenSection
	)
	default boolean maidenRedsDistance()
	{
		return true;
	}

	@ConfigItem(
		position = 205,
		keyName = "MaidenTickCounter",
		name = "Maiden Tank Tick Counter",
		description = "Displays the tick counter for when she decides who to choose for tanking.",
		section = maidenSection
	)
	default boolean maidenTickCounter()
	{
		return true;
	}



	/*====== Bloat Section ======*/


	@ConfigSection(
		name = "Bloat",
		description = "Bloat's Configuration",
		position = 300,
		keyName = "bloatSection",
		closedByDefault = true
	)
	String bloatSection = "Bloat";

	@ConfigItem(
		position = 301,
		keyName = "bloatIndicator",
		name = "Bloat Tile Indicator",
		description = "Highlights Bloat's Tile.",
		section = bloatSection
	)
	default boolean bloatIndicator()
	{
		return true;
	}

	@Alpha
	@ConfigItem(
		position = 302,
		keyName = "bloatIndicatorColorUP",
		name = "Bloat Indicator Color - UP",
		description = "Select a color for when Bloat is UP.",
		section = bloatSection
	)
	default Color bloatIndicatorColorUP()
	{
		return Color.CYAN;
	}

	@Alpha
	@ConfigItem(
		position = 303,
		keyName = "bloatIndicatorColorTHRESH",
		name = "Bloat Indicator Color - THRESHOLD",
		description = "Select a color for when Bloat UP and goes over 37 ticks, which allows you to know when he can go down.",
		section = bloatSection
	)
	default Color bloatIndicatorColorTHRESH()
	{
		return Color.ORANGE;
	}

	@Alpha
	@ConfigItem(
		position = 304,
		keyName = "bloatIndicatorColorDOWN",
		name = "Bloat Indicator Color - DOWN",
		description = "Select a color for when Bloat is DOWN.",
		section = bloatSection
	)
	default Color bloatIndicatorColorDOWN()
	{
		return Color.WHITE;
	}

	@Alpha
	@ConfigItem(
		position = 305,
		keyName = "bloatIndicatorColorWARN",
		name = "Bloat Indicator Color - WARN",
		description = "Select a color for when Bloat is DOWN and about to get UP.",
		section = bloatSection
	)
	default Color bloatIndicatorColorWARN()
	{
		return Color.RED;
	}

	@ConfigItem(
		position = 306,
		keyName = "bloatTickCounter",
		name = "Bloat Tick Counter",
		description = "Displays the tick counter for how long Bloat has been DOWN or UP.",
		section = bloatSection
	)
	default boolean bloatTickCounter()
	{
		return true;
	}

	@ConfigItem(
		position = 307,
		keyName = "BloatTickCountStyle",
		name = "Bloat Tick Time Style",
		description = "Count up or Count down options on bloat downed state",
		section = bloatSection
	)
	default BLOATTIMEDOWN BloatTickCountStyle()
	{
		return BLOATTIMEDOWN.COUNTDOWN;
	}

	@ConfigItem(
		position = 308,
		keyName = "bloatHands",
		name = "Bloat Hands Overlay",
		description = "Highlights the tiles where Bloat's hands will fall.",
		section = bloatSection
	)
	default boolean bloatHands()
	{
		return false;
	}

	@Alpha
	@ConfigItem(
		position = 309,
		keyName = "bloatHandsColor",
		name = "Bloat Hands Overlay Color",
		description = "Select a color for the Bloat Hands Overlay to be.",
		section = bloatSection
	)
	default Color bloatHandsColor()
	{
		return Color.CYAN;
	}

	@Range(max = 10)
	@ConfigItem(
		position = 310,
		keyName = "bloatHandsWidth",
		name = "Bloat Hands Overlay Thickness",
		description = "Sets the stroke width of the tile overlay where the hands fall. (BIGGER = THICKER).",
		section = bloatSection
	)
	default int bloatHandsWidth()
	{
		return 2;
	}

	@ConfigItem(
		name = "Hide Bloat Tank",
		keyName = "hideBloatTank",
		description = "Hides the entire Bloat tank in the center of the room",
		position = 311,
		section = bloatSection
	)
	default boolean hideBloatTank()
	{
		return false;
	}

	@ConfigItem(
		name = "Hide Ceiling Chains",
		keyName = "hideCeilingChains",
		description = "Hides the chains hanging from the ceiling in the Bloat room",
		position = 312,
		section = bloatSection
	)
	default boolean hideCeilingChains()
	{
		return false;
	}

	/*====== Nylocas Section ======*/

	@ConfigSection(
		name = "Nylocas",
		description = "Nylocas' Configuration",
		position = 400,
		keyName = "nylocasSection",
		closedByDefault = true
	)
	String nylocasSection = "Nylocas";

	@ConfigItem(
		position = 401,
		keyName = "nyloPillars",
		name = "Nylocas Pillar Health Overlay",
		description = "Displays the health percentage of the pillars.",
		section = nylocasSection
	)
	default boolean nyloPillars()
	{
		return true;
	}

	@ConfigItem(
		position = 402,
		keyName = "nyloExplosions",
		name = "Nylocas Explosion Warning",
		description = "Highlights a Nylocas that is about to explode.",
		section = nylocasSection
	)
	default boolean nyloExplosions()
	{
		return true;
	}

	@Range(max = 52)
	@ConfigItem(
		position = 403,
		keyName = "nyloExplosionDisplayTicks",
		name = "Nylocas Display Last Ticks",
		description = "Displays the last 'x' amount of ticks for a Nylocas. (ex: to see the last 10 ticks, you set it to 10).",
		section = nylocasSection
	)
	default int nyloExplosionDisplayTicks()
	{
		return 46;
	}

	@ConfigItem(
		position = 404,
		keyName = "nyloExplosionDisplayStyle",
		name = "Nylocas Display Explosion Style",
		description = "How to display when a nylocas is about to explode.",
		section = nylocasSection
	)
	default EXPLOSIVENYLORENDERSTYLE nyloExplosionOverlayStyle()
	{
		return EXPLOSIVENYLORENDERSTYLE.RECOLOR_TICK;
	}

	@ConfigItem(
		position = 405,
		keyName = "nyloTimeAlive",
		name = "Nylocas Tick Time Alive",
		description = "Displays the tick counter of each nylocas spawn (Explodes on 52).",
		section = nylocasSection
	)
	default boolean nyloTimeAlive()
	{
		return false;
	}

	@ConfigItem(
		position = 406,
		keyName = "nyloTimeAliveCountStyle",
		name = "Nylocas Tick Time Alive Style",
		description = "Count up or Count down options on the tick time alive.",
		section = nylocasSection
	)
	default NYLOTIMEALIVE nyloTimeAliveCountStyle()
	{
		return NYLOTIMEALIVE.COUNTDOWN;
	}

	@ConfigItem(
		position = 407,
		keyName = "nyloRecolorMenu",
		name = "Nylocas Recolor Menu Options",
		description = "Recolors the menu options of each Nylocas to it's respective attack style.",
		section = nylocasSection
	)
	default boolean nyloRecolorMenu()
	{
		return false;
	}

	@ConfigItem(
		position = 408,
		keyName = "nyloHighlightOverlay",
		name = "Nylocas Highlight Overlay",
		description = "Select your role to highlight respective Nylocas to attack.",
		section = nylocasSection
	)
	default boolean nyloHighlightOverlay()
	{
		return false;
	}

	@ConfigItem(
		position = 409,
		keyName = "nyloAliveCounter",
		name = "Nylocas Alive Counter Panel",
		description = "Displays how many Nylocas are currently alive.",
		section = nylocasSection
	)
	default boolean nyloAlivePanel()
	{
		return false;
	}

	@ConfigItem(
		position = 410,
		keyName = "nyloAggressiveOverlay",
		name = "Highlight Aggressive Nylocas",
		description = "Highlights aggressive Nylocas after they spawn.",
		section = nylocasSection
	)
	default boolean nyloAggressiveOverlay()
	{
		return true;
	}

	@ConfigItem(
		position = 411,
		keyName = "nyloAggressiveOverlayStyle",
		name = "Highlight Aggressive Nylocas Style",
		description = "Highlight style for aggressive Nylocas after they spawn.",
		section = nylocasSection
	)
	default AGGRESSIVENYLORENDERSTYLE nyloAggressiveOverlayStyle()
	{
		return AGGRESSIVENYLORENDERSTYLE.TILE;
	}


	@ConfigItem(
		position = 412,
		keyName = "removeNyloEntries",
		name = "Remove Attack Options",
		description = "Removes the attack options for Nylocas immune to your current attack style.",
		section = nylocasSection
	)
	default boolean removeNyloEntries()
	{
		return true;
	}

	@ConfigItem(
		position = 413,
		keyName = "nylocasWavesHelper",
		name = "Nylocas Waves Helper",
		description = "Overlay's squares with wave numbers on nylo entry bridges for upcoming nylos",
		section = nylocasSection
	)
	default boolean nyloWavesHelper()
	{
		return false;
	}

	@ConfigItem(
		position = 414,
		keyName = "nylocasTicksUntilWave",
		name = "Nylocas Ticks Until Wave",
		description = "Prints how many ticks until the next wave could spawn",
		section = nylocasSection
	)
	default boolean nyloTicksUntilWaves()
	{
		return false;
	}

	@ConfigItem(
		position = 415,
		keyName = "nyloInstanceTimer",
		name = "Nylocas Instance Timer",
		description = "Displays an instance timer when the next set will potentially spawn - ENTER ON ZERO.",
		section = nylocasSection
	)
	default boolean nyloInstanceTimer()
	{
		return false;
	}

	@ConfigItem(
		position = 416,
		keyName = "nyloStallMessage",
		name = "Nylocas Stall Wave Messages",
		description = "Sends a chat message when you have stalled the next wave of Nylocas to spawn due to being capped.",
		section = nylocasSection
	)
	default boolean nyloStallMessage()
	{
		return false;
	}

	@ConfigItem(
		position = 417,
		keyName = "nylocasBigSplitsHelper",
		name = "Nylocas Big Splits",
		description = "Tells you when bigs will spawn little nylos",
		section = nylocasSection
	)
	default boolean bigSplits()
	{
		return false;
	}

	@ConfigItem(
		position = 418,
		keyName = "nylocasBigSplitsHighlightColor",
		name = "Highlight Color",
		description = "Color of the NPC highlight",
		section = nylocasSection,
		hidden = true,
		unhide = "nylocasBigSplitsHelper"
	)
	@Alpha
	default Color getBigSplitsHighlightColor()
	{
		return Color.YELLOW;
	}

	@ConfigItem(
		position = 419,
		keyName = "nylocasBigSplitsTileColor2",
		name = "Highlight Color Tick 2",
		description = "Color of the NPC highlight on tick 1",
		section = nylocasSection,
		hidden = true,
		unhide = "nylocasBigSplitsHelper"
	)
	@Alpha
	default Color getBigSplitsTileColor2()
	{
		return Color.ORANGE;
	}

	@ConfigItem(
		position = 420,
		keyName = "nylocasBigSplitsTileColor1",
		name = "Highlight Color Tick 1",
		description = "Color of the NPC highlight on tick 0",
		section = nylocasSection,
		hidden = true,
		unhide = "nylocasBigSplitsHelper"
	)
	@Alpha
	default Color getBigSplitsTileColor1()
	{
		return Color.RED;
	}

	@ConfigItem(
		position = 421,
		keyName = "nylocasBigSplitsTextColor2",
		name = "Text Color Tick 2",
		description = "Color of the baby tick counter on tick 2",
		section = nylocasSection,
		hidden = true,
		unhide = "nylocasBigSplitsHelper"
	)
	@Alpha
	default Color getBigSplitsTextColor2()
	{
		return Color.ORANGE;
	}

	@ConfigItem(
		position = 422,
		keyName = "nylocasBigSplitsTextColor1",
		name = "Text Color Tick 1",
		description = "Color of the baby tick counter on tick 1",
		section = nylocasSection,
		hidden = true,
		unhide = "nylocasBigSplitsHelper"
	)
	@Alpha
	default Color getBigSplitsTextColor1()
	{
		return Color.RED;
	}

	@ConfigItem(
		position = 423,
		keyName = "nyloBossAttackTickCount",
		name = "Nylocas Boss Attack Tick Counter",
		description = "Displays the ticks left until the Nylocas Boss will attack next (LEFT-MOST).",
		section = nylocasSection
	)
	default boolean nyloBossAttackTickCount()
	{
		return false;
	}

	@ConfigItem(
		position = 424,
		keyName = "nyloBossSwitchTickCount",
		name = "Nylocas Boss Switch Tick Counter",
		description = "Displays the ticks left until the Nylocas Boss will switch next (MIDDLE).",
		section = nylocasSection
	)
	default boolean nyloBossSwitchTickCount()
	{
		return true;
	}

	@ConfigItem(
		position = 425,
		keyName = "nyloBossTotalTickCount",
		name = "Nylocas Boss Total Tick Counter",
		description = "Displays the total ticks since the Nylocas Boss has spawned (RIGHT-MOST).",
		section = nylocasSection
	)
	default boolean nyloBossTotalTickCount()
	{
		return false;
	}

	@ConfigItem(
		position = 426,
		keyName = "removeNyloBossEntries",
		name = "Nylocas Boss Remove Attack Options",
		description = "Removes the attack options for Nylocas Boss when immune to your current attack style.",
		section = nylocasSection
	)
	default boolean removeNyloBossEntries()
	{
		return true;
	}

	/*====== Sotetseg Section ======*/

	@ConfigSection(
		name = "Sotetseg",
		description = "Sotetseg's Configuration",
		position = 500,
		keyName = "sotetsegSection",
		closedByDefault = true
	)
	String sotetsegSection = "Sotetseg";

	@ConfigItem(
		position = 501,
		keyName = "sotetsegMaze",
		name = "Sotetseg Maze",
		description = "Memorizes Solo Mazes and displays tiles of other chosen players.",
		section = sotetsegSection
	)
	default boolean sotetsegMaze()
	{
		return true;
	}

	@ConfigItem(
		position = 502,
		keyName = "sotetsegOrbAttacksTicks",
		name = "Sotetseg Small Attack Orb Ticks",
		description = "Displays the amount of ticks until it will hit you (change prayers when you see 1).",
		section = sotetsegSection
	)
	default boolean sotetsegOrbAttacksTicks()
	{
		return true;
	}

	@ConfigItem(
		position = 503,
		keyName = "sotetsegAutoAttacksTicks",
		name = "Sotetseg Auto Attack Ticks",
		description = "Displays a tick counter for when Sotetseg will attack next.",
		section = sotetsegSection
	)
	default boolean sotetsegAutoAttacksTicks()
	{
		return true;
	}

	@ConfigItem(
		position = 504,
		keyName = "sotetsegAttackCounter",
		name = "Sotetseg Attack Counter",
		description = "Countdown until death ball.",
		section = sotetsegSection
	)
	default boolean sotetsegAttackCounter()
	{
		return true;
	}

	@ConfigItem(
		position = 505,
		keyName = "sotetsegBigOrbTicks",
		name = "Sotetseg Big Ball Tick Overlay",
		description = "Displays how many ticks until the ball will explode (eat when you see 0).",
		section = sotetsegSection
	)
	default boolean sotetsegBigOrbTicks()
	{
		return true;
	}

	@Alpha
	@ConfigItem(
		position = 506,
		keyName = "sotetsegBigOrbTickColor",
		name = "Sotetseg Big Ball Tick Color",
		description = "Select a color for the Sotetseg Big Ball tick countdown text.",
		section = sotetsegSection
	)
	default Color sotetsegBigOrbTickColor()
	{
		return Color.WHITE;
	}

	@Alpha
	@ConfigItem(
		position = 507,
		keyName = "sotetsegBigOrbTileColor",
		name = "Sotetseg Big Ball Tile Color",
		description = "Select a color for the Sotetseg Big Ball tile color.",
		section = sotetsegSection
	)
	default Color sotetsegBigOrbTileColor()
	{
		return new Color(188, 74, 74, 255);
	}

	/*====== Xarpus Section ======*/

	@ConfigSection(
		name = "Xarpus",
		description = "Xarpus's Configuration",
		position = 600,
		keyName = "xarpusSection",
		closedByDefault = true
	)
	String xarpusSection = "Xarpus";

	@ConfigItem(
		position = 601,
		keyName = "xarpusInstanceTimer",
		name = "Xarpus Instance Timer",
		description = "Displays the Xarpus Instance timer to be tick efficient with the first spawn of an exhumed - ENTER ON ZERO.",
		section = xarpusSection
	)
	default boolean xarpusInstanceTimer()
	{
		return true;
	}

	@ConfigItem(
		position = 602,
		keyName = "xarpusExhumed",
		name = "Xarpus Exhumed Markers",
		description = "Highlights the tiles of exhumed spawns.",
		section = xarpusSection
	)
	default boolean xarpusExhumed()
	{
		return true;
	}

	@ConfigItem(
		position = 603,
		keyName = "xarpusExhumedTick",
		name = "Xarpus Exhumed Ticks",
		description = "Displays how many ticks until the exhumeds will despawn.",
		section = xarpusSection
	)
	default boolean xarpusExhumedTick()
	{
		return true;
	}

	@ConfigItem(
		position = 604,
		keyName = "xarpusExhumedCount",
		name = "Xarpus Exhumed Count",
		description = "Count the amount of exhumeds.",
		section = xarpusSection
	)
	default XARPUS_EXHUMED_COUNT xarpusExhumedCount()
	{
		return XARPUS_EXHUMED_COUNT.DOWN;
	}

	@ConfigItem(
		position = 605,
		keyName = "xarpusTickP2",
		name = "Xarpus Attack Tick - P2",
		description = "Displays a tick counter for when Xarpus faces a new target to spit at.",
		section = xarpusSection
	)
	default boolean xarpusTickP2()
	{
		return true;
	}

	@ConfigItem(
		position = 606,
		keyName = "xarpusTickP3",
		name = "Xarpus Attack Tick - P3",
		description = "Displays a tick counter for when Xarpus will rotate.",
		section = xarpusSection
	)
	default boolean xarpusTickP3()
	{
		return true;
	}

	@ConfigItem(
		position = 607,
		name = "Line of Sight",
		keyName = "xarpusLineOfSight",
		description = "Displays Xarpus's Line of Sight on P3<br>Melee Tiles: Displays only the melee tiles that Xarpus can see<br>Square: Displays the whole region that Xarpus can see",
		section = xarpusSection
	)
	default XARPUS_LINE_OF_SIGHT xarpusLineOfSight()
	{
		return XARPUS_LINE_OF_SIGHT.OFF;
	}

	@Alpha
	@ConfigItem(
		position = 608,
		name = "Line of Sight Color",
		keyName = "xarpusLineOfSightColor",
		description = "Customize the color for Xarpus's Line of Sight",
		section = xarpusSection
	)
	default Color xarpusLineOfSightColor()
	{
		return Color.RED;
	}


	/*====== Verzik Section ======*/


	@ConfigSection(
		name = "Verzik",
		description = "Verzik's Configuration",
		position = 700,
		keyName = "verzikSection",
		closedByDefault = true
	)
	String verzikSection = "Verzik";

	@ConfigItem(
		position = 701,
		keyName = "verzikTileOverlay",
		name = "Verzik Tile Indicator",
		description = "Highlights Verzik's tile - If you are next to or inside of the indicator, you can be meleed.",
		section = verzikSection
	)
	default boolean verzikTileOverlay()
	{
		return true;
	}

	@ConfigItem(
		position = 702,
		keyName = "verzikProjectiles",
		name = "Verzik Range Tile Markers",
		description = "Highlights the tiles of Verzik's range projectiles.",
		section = verzikSection
	)
	default boolean verzikProjectiles()
	{
		return true;
	}

	@Alpha
	@ConfigItem(
		position = 703,
		keyName = "verzikProjectilesColor",
		name = "Verzik Range Tile Markers Color",
		description = "Select a color for the Verzik's Range Projectile Tile Overlay to be.",
		section = verzikSection
	)
	default Color verzikProjectilesColor()
	{
		return new Color(255, 0, 0, 50);
	}

	@ConfigItem(
		position = 704,
		keyName = "VerzikRedHP",
		name = "Verzik Reds Health Overlay",
		description = "Displays the health of red crabs during Verzik.",
		section = verzikSection
	)
	default boolean verzikReds()
	{
		return true;
	}

	@ConfigItem(
		position = 705,
		keyName = "verzikAutosTick",
		name = "Verzik Attack Tick Counter",
		description = "Displays the ticks until Verzik will attack next.",
		section = verzikSection
	)
	default boolean verzikAutosTick()
	{
		return true;
	}

	@ConfigItem(
		position = 706,
		keyName = "verzikAttackCounter",
		name = "Verzik Attack Counter",
		description = "Displays Verzik's Attack Count (useful for when P2 reds as they despawn after the 7th attack).",
		section = verzikSection
	)
	default boolean verzikAttackCounter()
	{
		return true;
	}

	@ConfigItem(
		position = 707,
		keyName = "verzikTotalTickCounter",
		name = "Verzik Total Tick Counter",
		description = "Displays the total amount of ticks Verzik has been alive for.",
		section = verzikSection
	)
	default boolean verzikTotalTickCounter()
	{
		return true;
	}

	@ConfigItem(
		position = 708,
		keyName = "verzikNyloPersonalWarning",
		name = "Verzik Nylo Direct Aggro Warning",
		description = "Highlights the Nylocas that are targeting YOU and ONLY you.",
		section = verzikSection
	)
	default boolean verzikNyloPersonalWarning()
	{
		return true;
	}

	@ConfigItem(
		position = 709,
		keyName = "verzikNyloOtherWarning",
		name = "Verzik Nylo Indirect Aggro Warnings",
		description = "Highlights the Nylocas that are targeting OTHER players.",
		section = verzikSection
	)
	default boolean verzikNyloOtherWarning()
	{
		return true;
	}

	@ConfigItem(
		position = 710,
		keyName = "lightningAttackHelper",
		name = "Lightning Attack Helper",
		description = "Displays the number of attacks before a lightning ball.",
		section = verzikSection
	)
	default boolean lightningAttackHelper()
	{
		return false;
	}

	@ConfigItem(
		position = 711,
		keyName = "lightningAttackTick",
		name = "Lightning Attack Tick",
		description = "Displays the number of ticks before a lightning ball hits you.",
		section = verzikSection
	)
	default boolean lightningAttackTick()
	{
		return false;
	}

	@ConfigItem(
		position = 712,
		keyName = "verzikAttackPurpleNyloMES",
		name = "Remove Purple Nylo MES",
		description = "Removes the ability to attack the Purple nylo if you cannot poison it",
		section = verzikSection
	)
	default boolean purpleCrabAttackMES()
	{
		return false;
	}

	@ConfigItem(
		position = 713,
		keyName = "weaponSet",
		name = "Poison Weapons",
		description = "If a weapon is added to this set, it will NOT deprio attack on Nylocas Athanatos.",
		section = verzikSection
	)
	default String weaponSet()
	{
		return "12926, 12006, 22292, 12899";
	}

	@ConfigItem(
		position = 714,
		keyName = "verzikNyloExplodeAOE",
		name = "Verzik Nylo Explosion Area",
		description = "Highlights the area of explosion for the Nylocas (Personal or Indirect Warnings MUST be enabled).",
		section = verzikSection
	)
	default boolean verzikNyloExplodeAOE()
	{
		return true;
	}

	@ConfigItem(
		position = 715,
		keyName = "verzikDisplayTank",
		name = "Verzik Display Tank",
		description = "Highlights the tile of the player tanking to help clarify.",
		section = verzikSection
	)
	default boolean verzikDisplayTank()
	{
		return true;
	}

	@ConfigItem(
		position = 716,
		keyName = "verzikYellows",
		name = "Verzik Yellows Overlay",
		description = "Highlights the yellow pools and displays the amount of ticks until you can move away or tick eat.",
		section = verzikSection
	)
	default boolean verzikYellows()
	{
		return true;
	}

	@ConfigItem(
		position = 717,
		keyName = "verzikGreenBall",
		name = "Verzik Green Ball Tank",
		description = "Displays who the green ball is targeting.",
		section = verzikSection
	)
	default boolean verzikGreenBall()
	{
		return true;
	}

	@Alpha
	@ConfigItem(
		position = 718,
		keyName = "verzikGreenBallColor",
		name = "Verzik Green Ball Highlight Color",
		description = "Select a color for the Verzik's Green Ball Tile Overlay to be.",
		section = verzikSection
	)
	default Color verzikGreenBallColor()
	{
		return new Color(59, 140, 83, 255);
	}

	@ConfigItem(
		position = 719,
		keyName = "verzikGreenBallMarker",
		name = "Verzik Green Ball Marker",
		description = "Choose between a tile or 3-by-3 area marker.",
		section = verzikSection
	)
	default VERZIKBALLTILE verzikGreenBallMarker()
	{
		return VERZIKBALLTILE.TILE;
	}

	@ConfigItem(
		position = 720,
		keyName = "verzikGreenBallTick",
		name = "Verzik Green Ball Tick",
		description = "Displays the number of ticks until the green ball nukes you.",
		section = verzikSection
	)
	default boolean verzikGreenBallTick()
	{
		return false;
	}

	@ConfigItem(
		position = 721,
		keyName = "verzikTornado",
		name = "Verzik Personal Tornado Highlight",
		description = "Displays the tornado that is targeting you.",
		section = verzikSection
	)
	default boolean verzikTornado()
	{
		return true;
	}

	@ConfigItem(
		position = 722,
		keyName = "verzikPersonalTornadoOnly",
		name = "Verzik ONLY Highlight Personal",
		description = "Displays the tornado that is targeting you ONLY after it solves which one is targeting you.",
		section = verzikSection
	)
	default boolean verzikPersonalTornadoOnly()
	{
		return false;
	}

	@Alpha
	@ConfigItem(
		position = 723,
		keyName = "verzikTornadoColor",
		name = "Verzik Tornado Highlight Color",
		description = "Select a color for the Verzik Tornadoes Overlay to be.",
		section = verzikSection
	)
	default Color verzikTornadoColor()
	{
		return Color.RED;
	}

	@ConfigItem(
		position = 724,
		keyName = "verzikPoisonTileHighlight",
		name = "Verzik Poison Tile Highlight",
		description = "Highlight tile with disappearing poison",
		section = verzikSection
	)
	default boolean verzikPoisonTileHighlight()
	{
		return true;
	}

	@Alpha
	@ConfigItem(
		position = 725,
		keyName = "verzikPoisonTileHighlightColor",
		name = "Verzik Poison Tile Highlight Color",
		description = "Select a color for the Verzik poison tiles.",
		section = verzikSection,
		hidden = true,
		unhide = "verzikPoisonTileHighlight"
	)
	default Color verzikPoisonTileHighlightColor()
	{
		return new Color(184, 246, 196, 152);
	}


	/*====== Misc Section ======*/

	@ConfigSection(
		name = "Misc",
		description = "Misc Configuration",
		position = 800,
		keyName = "verzikSection",
		hidden = true
	)
	String miscSection = "Misc";

	@ConfigItem(
		keyName = "highlightMelee",
		name = "",
		description = "",
		hidden = true,
		section = miscSection,
		position = 801
	)
	default boolean getHighlightMeleeNylo()
	{
		return false;
	}

	@ConfigItem(
		keyName = "highlightMelee",
		name = "",
		description = "",
		hidden = true,
		section = miscSection,
		position = 802
	)
	void setHighlightMeleeNylo(boolean set);

	@ConfigItem(
		keyName = "highlightMage",
		name = "",
		description = "",
		hidden = true,
		section = miscSection,
		position = 803
	)
	default boolean getHighlightMageNylo()
	{
		return false;
	}

	@ConfigItem(
		keyName = "highlightMage",
		name = "",
		description = "",
		hidden = true,
		section = miscSection,
		position = 804
	)
	void setHighlightMageNylo(boolean set);

	@ConfigItem(
		keyName = "highlightRange",
		name = "",
		description = "",
		hidden = true,
		section = miscSection,
		position = 805
	)
	default boolean getHighlightRangeNylo()
	{
		return false;
	}

	@ConfigItem(
		keyName = "highlightRange",
		name = "",
		description = "",
		hidden = true,
		section = miscSection,
		position = 806
	)
	void setHighlightRangeNylo(boolean set);

	@Getter(AccessLevel.PACKAGE)
	@AllArgsConstructor
	enum FontStyle
	{
		BOLD("Bold", Font.BOLD),
		ITALIC("Italic", Font.ITALIC),
		PLAIN("Plain", Font.PLAIN);

		private final String name;
		private final int font;

		@Override
		public String toString()
		{
			return getName();
		}
	}

	enum BLOATTIMEDOWN
	{
		COUNTUP,
		COUNTDOWN
	}

	enum NYLOTIMEALIVE
	{
		COUNTUP,
		COUNTDOWN
	}

	enum EXPLOSIVENYLORENDERSTYLE
	{
		TILE,
		RECOLOR_TICK
	}

	enum AGGRESSIVENYLORENDERSTYLE
	{
		TILE,
		HULL
	}

	enum XARPUS_EXHUMED_COUNT
	{
		OFF,
		DOWN,
		UP;
	}

	enum XARPUS_LINE_OF_SIGHT
	{
		OFF,
		SQUARE,
		MELEE_TILES;
	}

	enum VERZIKBALLTILE
	{
		TILE,
		AREA
	}
}
