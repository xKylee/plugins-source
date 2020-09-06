/*
 * THIS PLUGIN WAS WRITTEN BY A KEYBOARD-WIELDING MONKEY BOI BUT SHUFFLED BY A KANGAROO WITH THUMBS.
 * The plugin and it's refactoring was intended for xKylee's Externals but I'm sure if you're reading this, you're probably planning to yoink..
 * or you're just genuinely curious. If you're trying to yoink, it doesn't surprise me.. just don't claim it as your own. Cheers.
 */

package net.runelite.client.plugins.theatre.Nylocas;

import java.awt.Color;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.Actor;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.MenuEntry;
import net.runelite.api.MenuOpcode;
import net.runelite.api.NPC;
import net.runelite.api.NpcID;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.api.Skill;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOpened;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.NpcDefinitionChanged;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.input.MouseManager;
import net.runelite.client.plugins.theatre.Room;
import net.runelite.client.plugins.theatre.TheatreConfig;
import net.runelite.client.plugins.theatre.TheatreInputListener;
import net.runelite.client.plugins.theatre.TheatrePlugin;
import net.runelite.client.ui.overlay.components.InfoBoxComponent;
import net.runelite.client.util.ColorUtil;
import net.runelite.client.util.WeaponMap;
import net.runelite.client.util.WeaponStyle;

public class Nylocas extends Room
{
	@Inject
	private Client client;

	@Inject
	private NylocasOverlay nylocasOverlay;

	@Inject
	protected Nylocas(TheatrePlugin plugin, TheatreConfig config)
	{
		super(plugin, config);
	}

	@Inject
	private NylocasAliveCounterOverlay nylocasAliveCounterOverlay;

	@Inject
	private TheatreInputListener theatreInputListener;

	@Inject
	private MouseManager mouseManager;

	@Inject
	private SkillIconManager skillIconManager;

	@Getter
	private boolean nyloActive;

	@Getter
	private NPC nyloBossNPC;

	@Getter
	private boolean nyloBossAlive;

	@Setter
	@Getter
	private static Runnable wave31Callback = null;
	@Setter
	@Getter
	private static Runnable endOfWavesCallback = null;

	@Getter
	private HashMap<NPC, Integer> nylocasPillars = new HashMap<>();

	@Getter
	private HashMap<NPC, Integer> nylocasNpcs = new HashMap<>();

	@Getter
	private HashSet<NPC> aggressiveNylocas = new HashSet<>();

	@Getter
	private Instant nyloWaveStart;

	@Getter
	private int instanceTimer = 0;

	@Getter
	private boolean isInstanceTimerRunning = false;

	@Getter
	private NyloSelectionManager nyloSelectionManager;

	@Getter
	private int nyloBossAttackTickCount = -1;

	@Getter
	private int nyloBossSwitchTickCount = -1;

	@Getter
	private int nyloBossTotalTickCount = -1;

	@Getter
	private int nyloBossStage = 0;

	private WeaponStyle weaponStyle = null;
	private int weaponTypeTimeout = 0;

	private HashMap<NyloNPC, NPC> currentWave = new HashMap<>();

	private int varbit6447 = -1;
	private boolean nextInstance = true;
	private int nyloWave = 0;

	private int ticksSinceLastWave = 0;
	private int totalStalledWaves = 0;

	private static final int NPCID_NYLOCAS_PILLAR = 8358;
	private static final int NYLO_MAP_REGION = 13122;
	private static final int BLOAT_MAP_REGION = 13125;

	@Override
	public void init()
	{
		InfoBoxComponent box = new InfoBoxComponent();
		box.setImage(skillIconManager.getSkillImage(Skill.ATTACK));
		NyloSelectionBox nyloMeleeOverlay = new NyloSelectionBox(box);
		nyloMeleeOverlay.setSelected(config.getHighlightMeleeNylo());

		box = new InfoBoxComponent();
		box.setImage(skillIconManager.getSkillImage(Skill.MAGIC));
		NyloSelectionBox nyloMageOverlay = new NyloSelectionBox(box);
		nyloMageOverlay.setSelected(config.getHighlightMageNylo());

		box = new InfoBoxComponent();
		box.setImage(skillIconManager.getSkillImage(Skill.RANGED));
		NyloSelectionBox nyloRangeOverlay = new NyloSelectionBox(box);
		nyloRangeOverlay.setSelected(config.getHighlightRangeNylo());

		nyloSelectionManager = new NyloSelectionManager(nyloMeleeOverlay, nyloMageOverlay, nyloRangeOverlay);
		nyloSelectionManager.setHidden(!config.nyloHighlightOverlay());
		nylocasAliveCounterOverlay.setHidden(!config.nyloAlivePanel());
		nylocasAliveCounterOverlay.setNyloAlive(0);
		nylocasAliveCounterOverlay.setMaxNyloAlive(12);

		nyloBossNPC = null;
		nyloBossAlive = false;
	}

	private void startupNyloOverlay()
	{
		mouseManager.registerMouseListener(theatreInputListener);

		if (nyloSelectionManager != null)
		{
			overlayManager.add(nyloSelectionManager);
			nyloSelectionManager.setHidden(!config.nyloHighlightOverlay());
		}

		if (nylocasAliveCounterOverlay != null)
		{
			overlayManager.add(nylocasAliveCounterOverlay);
			nylocasAliveCounterOverlay.setHidden(!config.nyloAlivePanel());
		}
	}

	private void shutdownNyloOverlay()
	{
		mouseManager.unregisterMouseListener(theatreInputListener);

		if (nyloSelectionManager != null)
		{
			overlayManager.remove(nyloSelectionManager);
			nyloSelectionManager.setHidden(true);
		}

		if (nylocasAliveCounterOverlay != null)
		{
			overlayManager.remove(nylocasAliveCounterOverlay);
			nylocasAliveCounterOverlay.setHidden(true);
		}
	}

	public void load()
	{
		overlayManager.add(nylocasOverlay);
	}

	public void unload()
	{
		overlayManager.remove(nylocasOverlay);

		shutdownNyloOverlay();
		nyloBossNPC = null;
		nyloBossAlive = false;
		nyloWaveStart = null;
	}

	private void resetNylo()
	{
		nyloBossNPC = null;
		nyloBossAlive = false;
		nylocasPillars.clear();
		nylocasNpcs.clear();
		aggressiveNylocas.clear();
		setNyloWave(0);
		currentWave.clear();
		totalStalledWaves = 0;
		weaponStyle = null;
		weaponTypeTimeout = 0;
	}

	private void setNyloWave(int wave)
	{
		nyloWave = wave;
		nylocasAliveCounterOverlay.setWave(wave);

		if (wave >= 3)
		{
			isInstanceTimerRunning = false;
		}

		if (wave != 0)
		{
			ticksSinceLastWave = NylocasWave.waves.get(wave).getWaveDelay();
		}

		if (wave >= 20)
		{
			if (nylocasAliveCounterOverlay.getMaxNyloAlive() != 24)
			{
				nylocasAliveCounterOverlay.setMaxNyloAlive(24);
			}
		}

		if (wave < 20)
		{
			if (nylocasAliveCounterOverlay.getMaxNyloAlive() != 12)
			{
				nylocasAliveCounterOverlay.setMaxNyloAlive(12);
			}
		}

		if (wave == NylocasWave.MAX_WAVE && wave31Callback != null)
		{
			wave31Callback.run();
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged change)
	{
		if (change.getKey().equals("nyloHighlightOverlay"))
		{
			nyloSelectionManager.setHidden(!config.nyloHighlightOverlay());
		}

		if (change.getKey().equals("nyloAliveCounter"))
		{
			nylocasAliveCounterOverlay.setHidden(!config.nyloAlivePanel());
		}
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned npcSpawned)
	{
		NPC npc = npcSpawned.getNpc();
		switch (npc.getId())
		{
			case NPCID_NYLOCAS_PILLAR:
				nyloActive = true;
				if (nylocasPillars.size() > 3)
				{
					nylocasPillars.clear();
				}
				if (!nylocasPillars.containsKey(npc))
				{
					nylocasPillars.put(npc, 100);
				}
				break;
			case 8342:
			case 8343:
			case 8344:
			case 8345:
			case 8346:
			case 8347:
			case 8348:
			case 8349:
			case 8350:
			case 8351:
			case 8352:
			case 8353:
				if (nyloActive)
				{
					nylocasNpcs.put(npc, 52);
					nylocasAliveCounterOverlay.setNyloAlive(nylocasNpcs.size());

					NyloNPC nyloNPC = matchNpc(npc);
					if (nyloNPC != null)
					{
						currentWave.put(nyloNPC, npc);
						if (currentWave.size() > 2)
						{
							matchWave();
						}
					}
				}
				break;
			case NpcID.NYLOCAS_VASILIAS:
			case NpcID.NYLOCAS_VASILIAS_8355:
			case NpcID.NYLOCAS_VASILIAS_8356:
			case NpcID.NYLOCAS_VASILIAS_8357:
				nyloBossTotalTickCount = -4;
				nyloBossAlive = true;
				isInstanceTimerRunning = false;
				nyloBossNPC = npc;
				break;
		}
	}

	private void matchWave()
	{
		HashSet<NyloNPC> potentialWave;
		Set<NyloNPC> currentWaveKeySet = currentWave.keySet();

		for (int wave = nyloWave + 1; wave <= NylocasWave.MAX_WAVE; wave++)
		{
			boolean matched = true;
			potentialWave = NylocasWave.waves.get(wave).getWaveData();
			for (NyloNPC nyloNpc : potentialWave)
			{
				if (!currentWaveKeySet.contains(nyloNpc))
				{
					matched = false;
					break;
				}
			}

			if (matched)
			{
				setNyloWave(wave);
				for (NyloNPC nyloNPC : potentialWave)
				{
					if (nyloNPC.isAggressive())
					{
						aggressiveNylocas.add(currentWave.get(nyloNPC));
					}
				}
				currentWave.clear();
				return;
			}
		}
	}

	private NyloNPC matchNpc(NPC npc)
	{
		WorldPoint p = WorldPoint.fromLocalInstance(client, npc.getLocalLocation());
		Point point = new Point(p.getRegionX(), p.getRegionY());
		NylocasSpawnPoint spawnPoint = NylocasSpawnPoint.getLookupMap().get(point);

		if (spawnPoint == null)
		{
			return null;
		}

		NylocasType nylocasType = NylocasType.getLookupMap().get(npc.getId());

		if (nylocasType == null)
		{
			return null;
		}

		return new NyloNPC(nylocasType, spawnPoint);
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned npcDespawned)
	{
		NPC npc = npcDespawned.getNpc();
		switch (npc.getId())
		{
			case NPCID_NYLOCAS_PILLAR:
				if (nylocasPillars.containsKey(npc))
				{
					nylocasPillars.remove(npc);
				}
				if (nylocasPillars.size() < 1)
				{
					nyloWaveStart = null;
					nyloActive = false;
				}
				break;
			case 8342:
			case 8343:
			case 8344:
			case 8345:
			case 8346:
			case 8347:
			case 8348:
			case 8349:
			case 8350:
			case 8351:
			case 8352:
			case 8353:
				if (nylocasNpcs.remove(npc) != null)
				{
					nylocasAliveCounterOverlay.setNyloAlive(nylocasNpcs.size());
				}
				aggressiveNylocas.remove(npc);
				if (nyloWave == NylocasWave.MAX_WAVE && nylocasNpcs.size() == 0 && endOfWavesCallback != null)
				{
					endOfWavesCallback.run();
				}
				break;
			case NpcID.NYLOCAS_VASILIAS:
			case NpcID.NYLOCAS_VASILIAS_8355:
			case NpcID.NYLOCAS_VASILIAS_8356:
			case NpcID.NYLOCAS_VASILIAS_8357:
				nyloBossAlive = false;
				nyloBossAttackTickCount = -1;
				nyloBossSwitchTickCount = -1;
				nyloBossTotalTickCount = -1;
				break;
		}
	}

	@Subscribe
	public void onAnimationChanged(AnimationChanged event)
	{
		Actor actor = event.getActor();
		if (actor instanceof NPC)
		{
			switch (((NPC) actor).getId())
			{
				case 8355:
				case 8356:
				case 8357:
					if (event.getActor().getAnimation() == 8004 ||
						event.getActor().getAnimation() == 7999 ||
						event.getActor().getAnimation() == 7989)
					{
						nyloBossAttackTickCount = 5;
						nyloBossStage++;
					}
			}
		}
	}

	@Subscribe
	public void onNpcDefinitionChanged(NpcDefinitionChanged npcDefinitionChanged)
	{
		int npcId = npcDefinitionChanged.getNpc().getId();

		switch (npcId)
		{
			case 8355:
			case 8356:
			case 8357:
			{
				nyloBossAttackTickCount = 3;
				nyloBossSwitchTickCount = 11;
				nyloBossStage = 0;
			}
		}
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		int[] varps = client.getVarps();
		int newVarbit6447 = client.getVarbitValue(varps, 6447);

		if (isInNyloRegion() && newVarbit6447 != 0 && newVarbit6447 != varbit6447)
		{
			nyloWaveStart = Instant.now();

			if (nylocasAliveCounterOverlay != null)
			{
				nylocasAliveCounterOverlay.setNyloWaveStart(nyloWaveStart);
			}
		}

		varbit6447 = newVarbit6447;
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}

		if (isInNyloRegion())
		{
			startupNyloOverlay();
		}
		else
		{
			if (!nyloSelectionManager.isHidden() || !nylocasAliveCounterOverlay.isHidden())
			{
				shutdownNyloOverlay();
			}
			resetNylo();

			isInstanceTimerRunning = false;
		}

		nextInstance = true;
	}

	public final int getEquippedWeapon()
	{
		ItemContainer itemContainer = client.getItemContainer(InventoryID.EQUIPMENT);
		if (itemContainer != null)
		{
			Item[] items = itemContainer.getItems();
			Item weapon = items[3];
			if (weapon != null)
			{
				return weapon.getId();
			}
		}

		return -1;
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		if (event.getMenuOpcode() == MenuOpcode.ITEM_SECOND_OPTION)
		{
			WeaponStyle menuEntryWeaponStyle = WeaponMap.StyleMap.get(event.getIdentifier());
			if (menuEntryWeaponStyle == null)
			{
				return;
			}

			weaponStyle = menuEntryWeaponStyle;
			weaponTypeTimeout = 1;
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (nyloActive && isInNyloRegion())
		{
			if (weaponTypeTimeout > 0)
			{
				weaponTypeTimeout--;
			}
			else if (weaponTypeTimeout == 0)
			{
				weaponStyle = WeaponMap.StyleMap.get(getEquippedWeapon());
			}

			for (Iterator<NPC> it = nylocasNpcs.keySet().iterator(); it.hasNext(); )
			{
				NPC npc = it.next();
				int ticksLeft = nylocasNpcs.get(npc);

				if (ticksLeft < 0)
				{
					it.remove();
					continue;
				}
				nylocasNpcs.replace(npc, ticksLeft - 1);
			}

			for (NPC pillar : nylocasPillars.keySet())
			{
				int healthPercent = pillar.getHealthRatio();
				if (healthPercent > -1)
				{
					nylocasPillars.replace(pillar, healthPercent);
				}
			}

			if (config.nyloStallMessage() && (instanceTimer + 1) % 4 == 1 && nyloWave < NylocasWave.MAX_WAVE && ticksSinceLastWave < 2)
			{
				if (nylocasAliveCounterOverlay.getNyloAlive() >= nylocasAliveCounterOverlay.getMaxNyloAlive())
				{
					totalStalledWaves++;
					client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Stalled Wave: <col=EF1020>" +
						nyloWave + "/" + NylocasWave.MAX_WAVE + "<col=00> - Time:<col=EF1020> " + nylocasAliveCounterOverlay.getFormattedTime() +
						" <col=00>- Nylos Alive: <col=EF1020>" + nylocasAliveCounterOverlay.getNyloAlive() + "/" + nylocasAliveCounterOverlay.getMaxNyloAlive() +
						" <col=00>- Total Stalled Waves: <col=EF1020>" + totalStalledWaves, "");
				}
			}

			ticksSinceLastWave = Math.max(0, ticksSinceLastWave - 1);
		}

		if (nyloActive && nyloBossAlive)
		{
			nyloBossAttackTickCount--;
			nyloBossSwitchTickCount--;
			nyloBossTotalTickCount++;
		}

		instanceTimer = (instanceTimer + 1) % 4;
	}

	public void onClientTick(ClientTick event)
	{
		List<Player> players = client.getPlayers();
		for (Player player : players)
		{
			if (player.getWorldLocation() != null)
			{
				LocalPoint lp = player.getLocalLocation();

				WorldPoint wp = WorldPoint.fromRegion(player.getWorldLocation().getRegionID(), 5, 33, 0);
				LocalPoint lp1 = LocalPoint.fromWorld(client, wp.getX(), wp.getY());
				if (lp1 != null)
				{
					Point base = new Point(lp1.getSceneX(), lp1.getSceneY());
					Point point = new Point(lp.getSceneX() - base.getX(), lp.getSceneY() - base.getY());

					if (isInBloatRegion() && point.getX() == -1 && (point.getY() == -1 || point.getY() == -2 || point.getY() == -3) && nextInstance)
					{
						client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Nylo instance timer started.", "");
						instanceTimer = 3;
						isInstanceTimerRunning = true;
						nextInstance = false;
					}
				}
			}
		}
	}

	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded entry)
	{
		if (!nyloActive)
		{
			return;
		}

		if (config.removeNyloEntries()  && entry.getMenuOpcode() == MenuOpcode.NPC_SECOND_OPTION)
		{
			if (entry.getTarget().contains("Nylocas Ischyros") && (weaponStyle == WeaponStyle.MAGIC || weaponStyle == WeaponStyle.RANGE))
			{
				client.setMenuOptionCount(client.getMenuOptionCount() - 1);
			}
			if (entry.getTarget().contains("Nylocas Hagios") && (weaponStyle == WeaponStyle.RANGE || weaponStyle == WeaponStyle.MELEE))
			{
				client.setMenuOptionCount(client.getMenuOptionCount() - 1);
			}
			if (entry.getTarget().contains("Nylocas Toxobolos") && (weaponStyle == WeaponStyle.MAGIC || weaponStyle == WeaponStyle.MELEE))
			{
				client.setMenuOptionCount(client.getMenuOptionCount() - 1);
			}
		}

		if (config.nyloRecolorMenu() && entry.getOption().equals("Attack"))
		{
			MenuEntry[] entries = client.getMenuEntries();
			MenuEntry toEdit = entries[entries.length - 1];

			String target = entry.getTarget();
			String strippedTarget = stripColor(target);

			if (strippedTarget.startsWith("Nylocas Hagios"))
			{
				toEdit.setTarget(ColorUtil.prependColorTag(strippedTarget, Color.CYAN));
			}
			else if (strippedTarget.startsWith("Nylocas Ischyros"))
			{
				toEdit.setTarget(ColorUtil.prependColorTag(strippedTarget, new Color(255, 188, 188)));
			}
			else if (strippedTarget.startsWith("Nylocas Toxobolos"))
			{
				toEdit.setTarget(ColorUtil.prependColorTag(strippedTarget, Color.GREEN));
			}
			client.setMenuEntries(entries);
		}
	}

	static String stripColor(String str)
	{
		return str.replaceAll("(<col=[0-9a-f]+>|</col>)", "");
	}

	@Subscribe
	public void onMenuOpened(MenuOpened menu)
	{
		if (!config.nyloRecolorMenu() || !nyloActive || nyloBossAlive)
		{
			return;
		}

		// Filter all entries with Examine
		client.setMenuEntries(Arrays.stream(menu.getMenuEntries()).filter(s -> !s.getOption().equals("Examine")).toArray(MenuEntry[]::new));
	}

	boolean isInNyloRegion()
	{
		return client.isInInstancedRegion() && client.getMapRegions().length > 0 && client.getMapRegions()[0] == NYLO_MAP_REGION;
	}

	private boolean isInBloatRegion()
	{
		return client.isInInstancedRegion() && client.getMapRegions().length > 0 && client.getMapRegions()[0] == BLOAT_MAP_REGION;
	}
}
