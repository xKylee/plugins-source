/*
 * THIS PLUGIN WAS WRITTEN BY A KEYBOARD-WIELDING MONKEY BOI BUT SHUFFLED BY A KANGAROO WITH THUMBS.
 * The plugin and it's refactoring was intended for xKylee's Externals but I'm sure if you're reading this, you're probably planning to yoink..
 * or you're just genuinely curious. If you're trying to yoink, it doesn't surprise me.. just don't claim it as your own. Cheers.
 */

package net.runelite.client.plugins.theatre.Verzik;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.inject.Inject;
import com.google.common.collect.ImmutableSet;
import lombok.AccessLevel;
import lombok.Getter;
import net.runelite.api.Client;
import net.runelite.api.GraphicsObject;
import net.runelite.api.ItemID;
import net.runelite.api.MenuAction;
import net.runelite.api.NPC;
import net.runelite.api.NpcID;
import net.runelite.api.Player;
import net.runelite.api.PlayerComposition;
import net.runelite.api.Prayer;
import net.runelite.api.Projectile;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.ProjectileMoved;
import net.runelite.api.kit.KitType;
import net.runelite.api.util.Text;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.theatre.Room;
import net.runelite.client.plugins.theatre.TheatreConfig;
import net.runelite.client.plugins.theatre.TheatrePlugin;
import net.runelite.client.plugins.theatre.prayer.TheatrePrayerUtil;
import net.runelite.client.plugins.theatre.prayer.TheatreUpcomingAttack;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class Verzik extends Room
{
	@Inject
	private Client client;

	@Inject
	private VerzikOverlay verzikOverlay;

	@Inject
	private VerzikPrayerOverlay verzikPrayerOverlay;

	@Inject
	private Verzik(TheatrePlugin plugin, TheatreConfig config)
	{
		super(plugin, config);
	}

	@Getter
	private boolean verzikActive;

	@Getter
	private NPC verzikNPC;

	enum Phase
	{
		PHASE1,
		PHASE2,
		PHASE3
	}

	@Getter
	private Phase verzikPhase;

	enum SpecialAttack
	{
		WEB_COOLDOWN,
		WEBS,
		YELLOWS,
		GREEN,
		NONE
	}

	@Getter
	private SpecialAttack verzikSpecial = SpecialAttack.NONE;

	@Getter
	private int verzikYellows;

	@Getter
	private Map<Projectile, WorldPoint> verzikRangeProjectiles = new HashMap<>();

	@Getter
	private HashSet<NPC> verzikAggros = new HashSet<>();

	@Getter
	private Map<NPC, Pair<Integer, Integer>> verzikReds = new HashMap<>();

	@Getter
	private NPC verzikLocalTornado = null;

	@Getter
	private HashSet<MemorizedTornado> verzikTornadoes = new HashSet<>();

	private WorldPoint lastPlayerLocation0;
	private WorldPoint lastPlayerLocation1;

	@Getter
	private int verzikLightningAttacks = 4;

	private final List<Projectile> verzikRangedAttacks = new ArrayList<>();

	private final Predicate<Projectile> isValidVerzikAttack = (p) ->
		p.getRemainingCycles() > 0 && (p.getId() == VERZIK_RANGE_BALL || p.getId() == VERZIK_LIGHTNING_BALL);

	@Getter
	private int verzikTicksUntilAttack = -1;

	@Getter
	private int verzikTotalTicksUntilAttack = 0;

	@Getter
	private int verzikAttackCount;

	@Getter(AccessLevel.PACKAGE)
	private long lastTick;

	@Getter(AccessLevel.PACKAGE)
	Queue<TheatreUpcomingAttack> upcomingAttackQueue = new PriorityQueue<>();

	@Getter(AccessLevel.PACKAGE)
	Set<VerzikPoisonTile> verzikPoisonTiles = new HashSet<>();

	@Getter
	private boolean verzikEnraged = false;

	private boolean verzikFirstEnraged = false;
	private boolean verzikTickPaused = true;
	private boolean verzikRedPhase = false;
	private int verzikLastAnimation = -1;

	private static final int NPC_ID_TORNADO = 8386;
	private static final int VERZIK_RANGE_BALL = 1583;
	private static final int VERZIK_LIGHTNING_BALL = 1585;

	private static final int VERZIK_P1_MAGIC = 8109;
	private static final int VERZIK_P2_REG = 8114;
	private static final int VERZIK_P2_BOUNCE = 8116;
	private static final int VERZIK_P2_POISON = 8116;
	private static final int VERZIK_BEGIN_REDS = 8117;
	private static final int VERZIK_P3_MAGE = 8124;
	private static final int VERZIK_P3_RANGE = 8125;

	private static final int VERZIK_P3_ATTACK_TICKS = 3;

	private static final int P3_CRAB_ATTACK_COUNT = 5;
	private static final int P3_WEB_ATTACK_COUNT = 10;
	private static final int P3_YELLOW_ATTACK_COUNT = 15;
	private static final int P3_GREEN_ATTACK_COUNT = 20;

	private Set<Integer> WEAPON_SET;
	private static final Set<Integer> HELMET_SET = ImmutableSet.of(ItemID.SERPENTINE_HELM, ItemID.TANZANITE_HELM, ItemID.MAGMA_HELM);

	@Getter
	private boolean isHM;
	private static final Set<Integer> VERZIK_HM_ID = ImmutableSet.of(10847, 10848, 10849, 10850, 10851, 10852, 10853);

	private static final int VERZIK_HARDMODE_YELLOW_MAX = 3;
	private int verzikHardmodeYellowCount;

	@Override
	public void load()
	{
		updateConfig();
		overlayManager.add(verzikOverlay);
		overlayManager.add(verzikPrayerOverlay);
	}

	@Override
	public void unload()
	{
		overlayManager.remove(verzikOverlay);
		overlayManager.remove(verzikPrayerOverlay);
		verzikCleanup();
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged change)
	{
		if (change.getKey().equals("weaponSet"))
		{
			updateConfig();
		}
	}

	private void updateConfig()
	{
		WEAPON_SET = new HashSet<>();
		for (String s : Text.COMMA_SPLITTER.split(config.weaponSet()))
		{
			try
			{
				WEAPON_SET.add(Integer.parseInt(s));
			}
			catch (NumberFormatException ignored)
			{
			}

		}
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned npcSpawned)
	{
		NPC npc = npcSpawned.getNpc();
		switch (npc.getId())
		{
			case NpcID.WEB:
			case 10837:
			case 10854:
				if (verzikNPC != null && verzikNPC.getInteracting() == null)
				{
					verzikSpecial = SpecialAttack.WEBS;
				}
				break;
			case NpcID.NYLOCAS_ISCHYROS_8381:
			case NpcID.NYLOCAS_HAGIOS_8383:
			case NpcID.NYLOCAS_TOXOBOLOS_8382:
			case 10858:
			case 10859:
			case 10860:
				verzikAggros.add(npc);
				break;
			case NpcID.NYLOCAS_MATOMENOS_8385:
			case 10845:
			case 10862:
				verzikReds.putIfAbsent(npc, new MutablePair<>(npc.getHealthRatio(), npc.getHealthScale()));
				break;
			case NPC_ID_TORNADO:
			case 10846:
			case 10863:
				if (verzikLocalTornado == null)
				{
					verzikTornadoes.add(new MemorizedTornado(npc));
				}
				if (!verzikEnraged)
				{
					verzikEnraged = true;
					verzikFirstEnraged = true;
				}
				break;
			case NpcID.VERZIK_VITUR_8369:
			case 10830:
			case 10847:
				verzikSpawn(npc);
				break;
			case NpcID.VERZIK_VITUR_8370:
			case 10831:
			case 10848:
				verzikPhase = Phase.PHASE1;
				verzikSpawn(npc);
				break;
			case NpcID.VERZIK_VITUR_8371:
			case 10832:
			case 10849:
				verzikSpawn(npc);
				break;
			case NpcID.VERZIK_VITUR_8372:
			case 10833:
			case 10850:
				verzikPhase = Phase.PHASE2;
				verzikSpawn(npc);
				break;
			case NpcID.VERZIK_VITUR_8373:
			case 10834:
			case 10851:
				verzikSpawn(npc);
				break;
			case NpcID.VERZIK_VITUR_8374:
			case 10835:
			case 10852:
				verzikRangeProjectiles.clear();
				verzikPhase = Phase.PHASE3;
				verzikSpawn(npc);
				break;
			case NpcID.VERZIK_VITUR_8375:
			case 10836:
			case 10853:
				verzikSpawn(npc);
				break;
		}
	}

	@Subscribe
	public void onGameObjectSpawn(GameObjectSpawned gameObject)
	{
		if (verzikActive && verzikPhase == Phase.PHASE2)
		{
			verzikPoisonTiles.add(new VerzikPoisonTile(WorldPoint.fromLocal(client, gameObject.getTile().getLocalLocation())));
		}
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned npcDespawned)
	{
		NPC npc = npcDespawned.getNpc();
		switch (npc.getId())
		{
			case NpcID.NYLOCAS_ISCHYROS_8381:
			case NpcID.NYLOCAS_HAGIOS_8383:
			case NpcID.NYLOCAS_TOXOBOLOS_8382:
			case 10858:
			case 10859:
			case 10860:
				verzikAggros.remove(npc);
				break;
			case NpcID.NYLOCAS_MATOMENOS_8385:
			case 10845:
			case 10862:
				verzikReds.remove(npc);
				break;
			case NPC_ID_TORNADO:
			case 10846:
			case 10863:
				verzikTornadoes.remove(npc);
				if (verzikLocalTornado == npc)
				{
					verzikLocalTornado = null;
				}
				break;
			case NpcID.VERZIK_VITUR_8369:
			case NpcID.VERZIK_VITUR_8370:
			case NpcID.VERZIK_VITUR_8371:
			case NpcID.VERZIK_VITUR_8372:
			case NpcID.VERZIK_VITUR_8373:
			case NpcID.VERZIK_VITUR_8374:
			case NpcID.VERZIK_VITUR_8375:
			case 10830:
			case 10831:
			case 10832:
			case 10833:
			case 10834:
			case 10835:
			case 10836:
			case 10847:
			case 10848:
			case 10849:
			case 10850:
			case 10851:
			case 10852:
			case 10853:
				verzikCleanup();
				break;
		}
	}

	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded entry)
	{
		if (config.purpleCrabAttackMES() && verzikNPC != null && (verzikNPC.getId() == 8372 || verzikNPC.getId() == 10833 || verzikNPC.getId() == 10850))
		{
			if (entry.getTarget().contains("Nylocas Athanatos") && entry.getMenuAction() == MenuAction.NPC_SECOND_OPTION)
			{
				Player player = client.getLocalPlayer();
				PlayerComposition playerComp = player != null ? player.getPlayerComposition() : null;
				if (playerComp == null || WEAPON_SET.contains(playerComp.getEquipmentId(KitType.WEAPON)) || HELMET_SET.contains(playerComp.getEquipmentId(KitType.HEAD)))
				{
					return;
				}

				client.setMenuOptionCount(client.getMenuOptionCount() - 1);
			}
		}
	}

	@Subscribe
	public void onProjectileMoved(ProjectileMoved event)
	{
		if (event.getProjectile().getId() == VERZIK_RANGE_BALL)
		{
			verzikRangeProjectiles.put(event.getProjectile(), WorldPoint.fromLocal(client, event.getPosition()));
		}
	}

	private void handleVerzikAttacks(Projectile p)
	{
		int id = p.getId();
		switch (id)
		{
			case VERZIK_RANGE_BALL:
				if (!verzikRangedAttacks.contains(p))
				{
					verzikRangedAttacks.add(p);
					--verzikLightningAttacks;
				}
				break;
			case VERZIK_LIGHTNING_BALL:
				if (!verzikRangedAttacks.contains(p))
				{
					verzikRangedAttacks.add(p);
					verzikLightningAttacks = 4;
				}
				break;
		}

	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (verzikActive)
		{
			lastTick = System.currentTimeMillis();
			TheatrePrayerUtil.updateNextPrayerQueue(getUpcomingAttackQueue());

			if (verzikPhase == Phase.PHASE2)
			{
				if (verzikNPC.getId() == NpcID.VERZIK_VITUR_8372 || verzikNPC.getId() == 10833 || verzikNPC.getId() == 10850)
				{
					client.getProjectiles().stream().filter(isValidVerzikAttack).findFirst().ifPresent(this::handleVerzikAttacks);
					verzikRangedAttacks.removeIf((Projectile p) -> p.getRemainingCycles() <= 0);
				}

				if (!verzikRangeProjectiles.isEmpty())
				{
					Iterator<Projectile> iterator = verzikRangeProjectiles.keySet().iterator();

					while (iterator.hasNext())
					{
						Projectile projectile = (Projectile) iterator.next();
						if (projectile.getRemainingCycles() < 1)
						{
							iterator.remove();
						}
					}
				}

				if (isHM)
				{
					VerzikPoisonTile.updateTiles(verzikPoisonTiles);
				}
			}

			if (verzikPhase == Phase.PHASE3 && !verzikTornadoes.isEmpty())
			{
				if (lastPlayerLocation1 != null)
				{
					for (MemorizedTornado tornado : verzikTornadoes)
					{
						WorldPoint tornadoLocation = tornado.getNpc().getWorldLocation();

						if (tornado.getCurrentPosition() == null)
						{
							tornado.setCurrentPosition(tornadoLocation);
						}
						else
						{
							tornado.setLastPosition(tornado.getCurrentPosition());
							tornado.setCurrentPosition(tornadoLocation);
						}
					}
				}

				if (lastPlayerLocation1 == null)
				{
					lastPlayerLocation1 = client.getLocalPlayer().getWorldLocation();
					lastPlayerLocation0 = lastPlayerLocation1;
				}
				else
				{
					lastPlayerLocation1 = lastPlayerLocation0;
					lastPlayerLocation0 = client.getLocalPlayer().getWorldLocation();

					verzikTornadoes.removeIf(entry -> entry.getRelativeDelta(lastPlayerLocation1) != -1);
				}

				if (verzikTornadoes.size() == 1 && verzikLocalTornado == null)
				{
					verzikTornadoes.forEach(tornado -> verzikLocalTornado = tornado.getNpc());
				}
			}

			Function<Integer, Integer> adjust_for_enrage = i -> isVerzikEnraged() ? i - 2 : i;

			if (verzikPhase == Phase.PHASE3)
			{
				if (verzikYellows == 0)
				{
					if (isHM && verzikHardmodeYellowCount == VERZIK_HARDMODE_YELLOW_MAX)
					{
						verzikHardmodeYellowCount = 0;
					}
					for (GraphicsObject object : client.getGraphicsObjects())
					{
						if (object.getId() == 1595)
						{
							if (isHM && verzikHardmodeYellowCount != 0)
							{
								verzikYellows = 2;
							}
							else
							{
								verzikYellows = 14;
							}
							verzikHardmodeYellowCount += 1;
							break;
						}
					}
				}
				else
				{
					verzikYellows--;
				}
			}

			if (verzikTickPaused)
			{
				switch (verzikNPC.getId())
				{
					case NpcID.VERZIK_VITUR_8370:
					case 10831:
					case 10848:
						verzikPhase = Phase.PHASE1;
						verzikAttackCount = 0;
						verzikTicksUntilAttack = 18;
						verzikTickPaused = false;
						break;
					case NpcID.VERZIK_VITUR_8372:
					case 10833:
					case 10850:
						verzikPhase = Phase.PHASE2;
						verzikAttackCount = 0;
						verzikTicksUntilAttack = 3;
						verzikTickPaused = false;
						break;
					case NpcID.VERZIK_VITUR_8374:
					case 10835:
					case 10852:
						verzikPhase = Phase.PHASE3;
						verzikAttackCount = 0;
						verzikTicksUntilAttack = 6;
						verzikTickPaused = false;
						break;
				}
			}
			else if (verzikSpecial == SpecialAttack.WEBS)
			{
				verzikTotalTicksUntilAttack++;

				if (verzikNPC.getInteracting() != null)
				{
					verzikSpecial = SpecialAttack.WEB_COOLDOWN;
					verzikAttackCount = 10;
					verzikTicksUntilAttack = 10;
					verzikFirstEnraged = false;
				}
			}
			else
			{
				verzikTicksUntilAttack = Math.max(0, verzikTicksUntilAttack - 1);
				verzikTotalTicksUntilAttack++;

				int animationID = verzikNPC.getAnimation();

				if (animationID > -1 && verzikPhase == Phase.PHASE1 && verzikTicksUntilAttack < 5 && animationID != verzikLastAnimation)
				{
					if (animationID == VERZIK_P1_MAGIC)
					{
						verzikTicksUntilAttack = 14;
						verzikAttackCount++;
					}
				}

				if (animationID > -1 && verzikPhase == Phase.PHASE2 && verzikTicksUntilAttack < 3 && animationID != verzikLastAnimation)
				{
					switch (animationID)
					{
						case VERZIK_P2_REG:
						case VERZIK_P2_BOUNCE:
							verzikTicksUntilAttack = 4;
							verzikAttackCount++;
							if (verzikAttackCount == 7 && verzikRedPhase)
							{
								verzikTicksUntilAttack = 8;
							}
							break;
						case VERZIK_BEGIN_REDS:
							verzikRedPhase = true;
							verzikAttackCount = 0;
							verzikTicksUntilAttack = 12;
							break;
					}
				}

				if (animationID > -1 && verzikPhase == Phase.PHASE3 && animationID != verzikLastAnimation)
				{
					switch (animationID)
					{
						case VERZIK_P3_MAGE:
							upcomingAttackQueue.add(new TheatreUpcomingAttack(VERZIK_P3_ATTACK_TICKS, Prayer.PROTECT_FROM_MAGIC));
							break;
						case VERZIK_P3_RANGE:
							upcomingAttackQueue.add(new TheatreUpcomingAttack(VERZIK_P3_ATTACK_TICKS, Prayer.PROTECT_FROM_MISSILES));
							break;
					}
				}

				verzikLastAnimation = animationID;

				if (verzikPhase == Phase.PHASE3)
				{
					verzikAttackCount = verzikAttackCount % P3_GREEN_ATTACK_COUNT;

					if (verzikTicksUntilAttack <= 0)
					{
						verzikAttackCount++;

						// First 9 Attacks, Including Crabs
						if (verzikAttackCount < P3_WEB_ATTACK_COUNT)
						{
							verzikSpecial = SpecialAttack.NONE;
							verzikTicksUntilAttack = adjust_for_enrage.apply(7);
						}
						// Between Webs and Yellows
						else if (verzikAttackCount < P3_YELLOW_ATTACK_COUNT)
						{
							verzikSpecial = SpecialAttack.NONE;
							verzikTicksUntilAttack = adjust_for_enrage.apply(7);
						}
						// Yellows - Can't Attack
						else if (verzikAttackCount < P3_YELLOW_ATTACK_COUNT + 1)
						{
							verzikSpecial = SpecialAttack.YELLOWS;
							if (isHM)
							{
								verzikTicksUntilAttack = 27;
							}
							else
							{
								verzikTicksUntilAttack = 21;
							}
						}
						// Between Yellows and Green Ball
						else if (verzikAttackCount < P3_GREEN_ATTACK_COUNT)
						{
							verzikSpecial = SpecialAttack.NONE;
							verzikTicksUntilAttack = adjust_for_enrage.apply(7);
						}
						// Ready for Green Ball
						else if (verzikAttackCount < P3_GREEN_ATTACK_COUNT + 1)
						{
							verzikSpecial = SpecialAttack.GREEN;
							// 12 During Purples?
							verzikTicksUntilAttack = 12;
						}
						else
						{
							verzikSpecial = SpecialAttack.NONE;
							verzikTicksUntilAttack = adjust_for_enrage.apply(7);
						}
					}

					if (verzikFirstEnraged)
					{
						verzikFirstEnraged = false;
						if (verzikSpecial != SpecialAttack.YELLOWS || verzikTicksUntilAttack <= 7)
						{
							verzikTicksUntilAttack = 5;
						}
					}
				}
			}
		}
	}

	Color verzikSpecialWarningColor()
	{
		Color col = Color.WHITE;

		if (verzikPhase != Phase.PHASE3)
		{
			return col;
		}

		switch (verzikAttackCount)
		{
			case Verzik.P3_CRAB_ATTACK_COUNT - 1:
				col = Color.MAGENTA;
				break;
			case Verzik.P3_WEB_ATTACK_COUNT - 1:
				col = Color.ORANGE;
				break;
			case Verzik.P3_YELLOW_ATTACK_COUNT - 1:
				col = Color.YELLOW;
				break;
			case Verzik.P3_GREEN_ATTACK_COUNT - 1:
				col = Color.GREEN;
				break;
		}

		return col;
	}

	private void verzikSpawn(NPC npc)
	{
		isHM = VERZIK_HM_ID.contains(npc.getId());
		verzikActive = true;
		verzikNPC = npc;
		verzikSpecial = SpecialAttack.NONE;
		verzikFirstEnraged = false;
		verzikEnraged = false;
		verzikRedPhase = false;
		verzikTickPaused = true;
		verzikAttackCount = 0;
		verzikTicksUntilAttack = 0;
		verzikTotalTicksUntilAttack = 0;
		verzikLastAnimation = -1;
		verzikYellows = 0;
		verzikHardmodeYellowCount = 0;
		verzikLightningAttacks = 4;
	}

	private void verzikCleanup()
	{
		isHM = false;
		verzikNPC = null;
		verzikPhase = null;
		verzikSpecial = SpecialAttack.NONE;
		verzikAggros.clear();
		verzikReds.clear();
		verzikTornadoes.clear();
		verzikPoisonTiles.clear();
		verzikLocalTornado = null;
		verzikEnraged = false;
		verzikFirstEnraged = false;
		verzikRedPhase = false;
		verzikActive = false;
		verzikTickPaused = true;
		verzikTotalTicksUntilAttack = 0;
		verzikLastAnimation = -1;
		verzikYellows = 0;
		verzikHardmodeYellowCount = 0;
		verzikRangedAttacks.clear();
		verzikLightningAttacks = 4;
		upcomingAttackQueue.clear();
	}
}