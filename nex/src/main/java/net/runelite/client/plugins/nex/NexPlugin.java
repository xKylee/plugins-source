package net.runelite.client.plugins.nex;

/* Keep the change ya filthy animal
 * Chris
 */

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import com.google.inject.Provides;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.Renderable;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.GraphicChanged;
import net.runelite.api.events.InteractingChanged;
import net.runelite.client.callback.Hooks;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.nex.maths.MathUtil;
import net.runelite.client.plugins.nex.movement.MovementUtil;
import net.runelite.client.plugins.nex.timer.TickTimer;
import net.runelite.client.ui.overlay.OverlayManager;
import org.pf4j.Extension;

@Extension
@PluginDescriptor(
	name = "Nex Extended",
	enabledByDefault = false,
	description = "Show what prayer to use and which tiles to avoid",
	tags = {"bosses", "combat", "nex", "gwd", "pvm"}
)

@Slf4j
@Singleton
public class NexPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private NexPlugin plugin;

	@Inject
	private NexConfig config;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private NexOverlay overlay;

	@Inject
	private NexPrayerOverlay prayerOverlay;

	@Inject
	private NexPrayerInfoBox prayerInfoBox;

	@Inject
	private Hooks hooks;
	private final Hooks.RenderableDrawListener drawListener = this::shouldDraw;

	private static final int SHADOW_ID = 42942;
	private static final int ICE_TRAP_ID = 42944;
	private static final int COUGH_GRAPHIC_ID = 1103;

	private static final int SHADOW_TICK_LEN = 5;
	private static final int BLOOD_SACRIFICE_LEN = 7;
	private static final int BLOOD_SACRIFICE_DISTANCE = 8;
	private static final int NEX_RANGE_DISTANCE = 11;
	private static final int ICE_TRAP_TICK_LEN = 9;
	private static final int CONTAIN_THIS_TICK_LEN = 6;
	private static final int CONTAIN_THIS_DISTANCE = 2;
	private static final int NEX_PHASE_DELAY = 5;
	private static final int NEX_PHASE_MINION_DELAY = 10;
	private static final int NEX_STARTUP_DELAY = 26;
	private static final int NEX_WRATH_TICK_DELAY = 5;
	private static final int NEX_SIPHON_DELAY = 9;
	private static final int NEX_DASH_TICK_LEN = 4;
	private static final int NEX_DASH_CLICK_DELAY = 8;

	// two ticks after but chat messages happen before onTick
	private static final int NEX_WING_READ_DELAY = 3;
	private static final int NEX_WING_DISTANCE = 10;

	@Getter
	private boolean inFight;

	@Getter
	@Setter
	private boolean isFlash;

	@Getter
	@Setter
	private boolean isShadowFlash;

	@Getter
	private NPC nex;

	@Getter
	private WorldPoint centerTile;

	@Getter
	private final List<LocalPoint> dashLaneTiles = new ArrayList<>();

	@Getter
	private final List<WorldPoint> wingTiles = new ArrayList<>(4);

	@Getter
	private NexPhase currentPhase = NexPhase.NONE;

	@Getter
	private boolean minionActive = false;
	private NPC lastActive = null;

	@Getter
	private final List<LocalPoint> shadows = new ArrayList<>();

	@Getter
	private final Set<NexCoughingPlayer> coughingPlayers = new HashSet<>();

	@Getter
	private final Set<String> healthyPlayers = new HashSet<>();

	@Getter
	private final List<LocalPoint> iceTraps = new ArrayList<>();

	@Getter
	private final List<LocalPoint> containThisSpawns = new ArrayList<>();

	@Getter
	private List<LocalPoint> healthyPlayersLocations = new ArrayList<>();

	@Getter
	private final List<LocalPoint> bloodSacrificeSafeTiles = new ArrayList<>();

	@Getter
	private final List<LocalPoint> nexRangeTiles = new ArrayList<>();

	@Getter
	private NexSpecial currentSpecial;

	@Getter
	private LocalPoint nexDeathTile = null;

	@Getter
	private boolean isTrappedInIce;

	@Getter
	private NexCoughingPlayer selfCoughingPlayer = null;

	private int teamSize;
	private boolean coughingPlayersChanged = false;

	// Tick timers

	@Getter
	private final TickTimer shadowsTicks = new TickTimer(shadows::clear);

	@Getter
	private final TickTimer bloodSacrificeTicks = new TickTimer(bloodSacrificeSafeTiles::clear);

	@Getter
	private final TickTimer nexTicksUntilClick = new TickTimer();

	// used to not show next minion as vulnerable
	private final TickTimer nexPhaseMinionCoolDown = new TickTimer();

	@Getter
	private final TickTimer iceTrapTicks = new TickTimer(this::clearIceTrap);

	@Getter
	private final TickTimer containTrapTicks = new TickTimer(containThisSpawns::clear);

	@Getter
	private final TickTimer orientationReadDelay = new TickTimer(this::handleStartDash);

	@Getter
	private final TickTimer airplaneCoolDown = new TickTimer(dashLaneTiles::clear);

	@Getter
	private final TickTimer drawRangeCoolDown = new TickTimer();

	@Getter
	private final TickTimer nexDeathTileTicks = new TickTimer(() -> nexDeathTile = null);

	@Getter
	private int nexTankAttacks = 0;
	private int nexPreviousAnimation = -1;
	Set<Integer> nexAttackAnimations = Set.of(9189, 9180);

	@Provides
	NexConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(NexConfig.class);
	}

	@Override
	protected void startUp()
	{
		overlayManager.add(overlay);
		overlayManager.add(prayerOverlay);
		overlayManager.add(prayerInfoBox);
		reset();
		hooks.registerRenderableDrawListener(drawListener);
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(overlay);
		overlayManager.remove(prayerOverlay);
		overlayManager.remove(prayerInfoBox);
		hooks.unregisterRenderableDrawListener(drawListener);
	}

	private void reset()
	{
		minionActive = false;
		currentPhase = NexPhase.NONE;
		selfCoughingPlayer = null;
		healthyPlayers.clear();
		healthyPlayersLocations.clear();
		nex = null;
		lastActive = null;
		isTrappedInIce = false;
		coughingPlayers.clear();
		containThisSpawns.clear();
		nexTicksUntilClick.reset();
		iceTraps.clear();
		nexTankAttacks = 0;
		nexPreviousAnimation = -1;
		iceTrapTicks.reset();
		teamSize = 0;
		coughingPlayersChanged = false;
	}

	@Subscribe
	public void onAnimationChanged(AnimationChanged event)
	{
		Actor actor = event.getActor();
		if (!(actor instanceof NPC))
		{
			return;
		}

		NPC npc = (NPC) actor;

		if (nex == null && npc.getName() != null && npc.getName().equalsIgnoreCase("Nex"))
		{
			nex = npc;

			centerTile = getNexCenterTile(nex);
			updateWingTiles(centerTile);

			inFight = true;

			// first discover nex, oh wow, fun boss.
			// Handle edge case where because we arent in fight yet
			// we wont see her spawning text
			nexTicksUntilClick.setTicks(NEX_STARTUP_DELAY);
		}

		if (nex != null && currentPhase == NexPhase.ZAROS)
		{
			if (nex.getAnimation() != nexPreviousAnimation)
			{
				if (nexAttackAnimations.contains(nex.getAnimation()))
				{
					nexTankAttacks += 1;
				}

				nexPreviousAnimation = nex.getAnimation();
			}
		}
	}

	/**
	 * Detect nex changing tank in final phase for tank counter
	 */
	@Subscribe
	public void onInteractingChanged(InteractingChanged event)
	{
		if (!inFight || currentPhase != NexPhase.ZAROS)
		{
			return;
		}

		Actor source = event.getSource();
		Actor target = event.getTarget();

		if (!(source instanceof NPC))
		{
			return;
		}

		if (!(target instanceof Player))
		{
			return;
		}

		NPC npc = (NPC) source;

		if (npc.getName() != null && npc.getName().equalsIgnoreCase("Nex"))
		{
			nexTankAttacks = 0;
		}
	}

	@Subscribe
	private void onGameTick(final GameTick event)
	{
		if (!inFight)
		{
			return;
		}

		handleCoughers();
		handleTimers();
		updateTrappedStatus();
		updateRangeZone();
	}

	private void handleTimers()
	{
		nexTicksUntilClick.tick();
		nexPhaseMinionCoolDown.tick();
		nexDeathTileTicks.tick();
		shadowsTicks.tick();
		iceTrapTicks.tick();
		bloodSacrificeTicks.tick();
		containTrapTicks.tick();
		orientationReadDelay.tick();
		airplaneCoolDown.tick();
		drawRangeCoolDown.tick();
	}

	private void updateTrappedStatus()
	{
		if (currentPhase != NexPhase.ICE)
		{
			return;
		}

		if (iceTraps.isEmpty())
		{
			isTrappedInIce = false;
			return;
		}

		var player = client.getLocalPlayer();

		if (player == null)
		{
			return;
		}

		Set<LocalPoint> traps = new HashSet<>(iceTraps);

		var nearbyIceTraps = traps.stream().filter(trap -> WorldPoint.fromLocal(client, trap).distanceTo(player.getWorldLocation()) == 1).count();
		var possibleIceTraps = traps.size();

		isTrappedInIce = nearbyIceTraps == possibleIceTraps;
	}

	/**
	 * This method has some jank around it with the coughingPlayersChanged & hasEnabled/DisabledEntityHiderRecently.
	 * I was experiencing some performance issues so spent time preventing wasted cpu cycles at the
	 * cost of readable code.
	 */
	private void handleCoughers()
	{
		// update self
		if (selfCoughingPlayer != null && selfCoughingPlayer.shouldRemove(client.getGameCycle()))
		{
			selfCoughingPlayer = null;
		}

		// update others and catch if we remove anyone
		coughingPlayers.removeIf(nexCoughingPlayer -> {
			var shouldRemove = nexCoughingPlayer.shouldRemove(client.getGameCycle());

			if (shouldRemove)
			{
				// we should now change hidden entities
				coughingPlayersChanged = true;
			}

			return shouldRemove;
		});

		var players = client.getPlayers();

		// Sick players have changed, update list of healthy players
		if (coughingPlayersChanged || teamSize != players.size())
		{
			coughingPlayersChanged = false;

			teamSize = players.size();

			var team = players
				.stream()
				.filter(player -> client.getLocalPlayer() != player)
				.map(Actor::getName).
				collect(Collectors.toSet());
			var coughers = coughingPlayers
				.stream()
				.map(NexCoughingPlayer::getName)
				.collect(Collectors.toSet());
			healthyPlayers.clear();
			healthyPlayers.addAll(Sets.difference(team, coughers));
		}

		// update healthy locations if we are sick
		if (selfCoughingPlayer != null)
		{
			healthyPlayersLocations = players
				.stream()
				.filter(player -> healthyPlayers.contains(player.getName()))
				.map(Actor::getLocalLocation)
				.collect(Collectors.toList());
		}
	}

	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned event)
	{
		if (!inFight)
		{
			return;
		}

		GameObject object = event.getGameObject();

		if (object.getId() == SHADOW_ID)
		{
			shadows.add(object.getLocalLocation());

			if (client.getLocalPlayer() != null && object.getWorldLocation().equals(client.getLocalPlayer().getWorldLocation()))
			{
				setShadowFlash(true);
			}

			shadowsTicks.setTicksIfExpired(SHADOW_TICK_LEN);
		}
		else if (object.getId() == ICE_TRAP_ID)
		{
			iceTraps.add(object.getLocalLocation());
			iceTrapTicks.setTicksIfExpired(ICE_TRAP_TICK_LEN);
		}
	}

	@Subscribe
	public void onGameObjectDespawned(GameObjectDespawned event)
	{
		if (!inFight)
		{
			return;
		}

		GameObject object = event.getGameObject();

		if (object.getId() == ICE_TRAP_ID && !iceTraps.isEmpty())
		{
			clearIceTrap();
		}
	}

	@Subscribe
	public void onGraphicChanged(GraphicChanged event)
	{
		if (!inFight)
		{
			return;
		}
		Actor actor = event.getActor();
		if (!(actor instanceof Player))
		{
			return;
		}

		if (actor == client.getLocalPlayer())
		{
			if (actor.getGraphic() == COUGH_GRAPHIC_ID)
			{
				selfCoughingPlayer = new NexCoughingPlayer(actor.getName(), client.getGameCycle(), (Player) actor);
			}
			return;
		}

		if (actor.getGraphic() == COUGH_GRAPHIC_ID)
		{
			coughingPlayers.add(new NexCoughingPlayer(actor.getName(), client.getGameCycle(), (Player) actor));
			coughingPlayersChanged = true;
		}
	}

	@Subscribe
	private void onGameStateChanged(GameStateChanged event)
	{
		GameState gamestate = event.getGameState();

		//if loading happens while inFight, the user has left the area (either via death or teleporting).
		if (gamestate == GameState.LOADING && inFight)
		{
			reset();
			inFight = false;
		}
	}

	@Subscribe
	private void onChatMessage(ChatMessage event)
	{
		if (!inFight || event.getType() != ChatMessageType.GAMEMESSAGE)
		{
			return;
		}
		String message = event.getMessage().toLowerCase().replaceFirst("nex:", "").replaceAll("<[^>]+>", "").strip();

		if (setPhase(message))
		{
			if (currentPhase == NexPhase.ZAROS)
			{
				nexTankAttacks = 0;
				nexPreviousAnimation = -1;
			}

			if (currentPhase == NexPhase.NONE)
			{
				// TASTE MY WRATH!
				// before resetting nex lets grab the tile for death AOE
				nexDeathTile = nex.getLocalLocation();
				nexDeathTileTicks.setTicks(NEX_WRATH_TICK_DELAY);
				reset();
			}
			else if (currentPhase == NexPhase.STARTING)
			{
				nex = null; // Just need to grab nex from the new spawn
				nexTicksUntilClick.setTicks(NEX_STARTUP_DELAY);
			}
			else
			{
				minionActive = false;
				lastActive = null;
				nexTicksUntilClick.setTicks(NEX_PHASE_DELAY);
				nexPhaseMinionCoolDown.setTicks(NEX_PHASE_MINION_DELAY);
			}
			return;
		}

		if (NexMinion.minionActive(message))
		{
			minionActive = true;
			return;
		}

		if (setSpecial(message))
		{
			if (currentSpecial == NexSpecial.BLOOD_SIPHON)
			{
				nexTicksUntilClick.setTicks(NEX_SIPHON_DELAY);
			}
			else if (currentSpecial == NexSpecial.CONTAIN)
			{
				containTrapTicks.setTicksIfExpired(CONTAIN_THIS_TICK_LEN);
				containThisSpawns.addAll(MovementUtil.getWalkableLocalTiles(client, nex.getWorldLocation().dx(1).dy(1), CONTAIN_THIS_DISTANCE));
			}
			else if (currentSpecial == NexSpecial.BLOOD_SACRIFICE_PERSONAL)
			{
				updateSafeZone();
				bloodSacrificeTicks.setTicksIfExpired(BLOOD_SACRIFICE_LEN);
			}
			else if (currentSpecial == NexSpecial.DASH)
			{
				orientationReadDelay.setTicksIfExpired(NEX_WING_READ_DELAY);

			}
			return;
		}

		if (message.equals(NexText.INVALID_ATTACK))
		{
			setFlash(true);
			return;
		}
	}

	private boolean setPhase(String message)
	{
		NexPhase phase = NexPhase.mapPhase(message);

		if (phase == null)
		{
			return false;
		}

		currentPhase = phase;
		return true;
	}

	private boolean setSpecial(String message)
	{
		NexSpecial special = NexSpecial.mapSpecial(message);

		if (special == null)
		{
			return false;
		}

		currentSpecial = special;
		return true;
	}

	public NPC getCurrentActiveMinion()
	{
		if (lastActive == null)
		{
			var currentMinionId = NexPhase.getMinionId(getCurrentPhase());
			var active = client.getNpcs().stream().filter(npc -> npc.getId() == currentMinionId).findFirst().orElse(null);
			lastActive = active;
			return active;
		}

		return lastActive;
	}

	public boolean nexDisable()
	{
		return nexTicksUntilClick.isActive() || minionActive;
	}

	public boolean minionCoolDownExpired()
	{
		return nexPhaseMinionCoolDown.isExpired();
	}

	private void updateSafeZone()
	{
		if (nex == null)
		{
			return;
		}

		bloodSacrificeSafeTiles.clear();
		bloodSacrificeSafeTiles.addAll(MovementUtil.getWalkableLocalTiles(client, getNexCenterTile(nex), BLOOD_SACRIFICE_DISTANCE));
	}

	private void updateRangeZone()
	{
		if (nex == null)
		{
			nexRangeTiles.clear();
			return;
		}

		nexRangeTiles.clear();
		nexRangeTiles.addAll(MovementUtil.getWalkableLocalTiles(client, getNexCenterTile(nex), NEX_RANGE_DISTANCE));
	}

	private void updateWingTiles(WorldPoint centerTile)
	{
		wingTiles.clear();

		wingTiles.add(centerTile.dy(NEX_WING_DISTANCE));
		wingTiles.add(centerTile.dy(-NEX_WING_DISTANCE));
		wingTiles.add(centerTile.dx(NEX_WING_DISTANCE));
		wingTiles.add(centerTile.dx(-NEX_WING_DISTANCE));
	}

	private WorldPoint getNexCenterTile(@NonNull NPC nex)
	{
		return nex.getWorldLocation().dx(1).dy(1);
	}

	private void selectWingTile()
	{
		if (nex == null || wingTiles.isEmpty())
		{
			return;
		}

		dashLaneTiles.clear();

		double angle = (2 * Math.PI * nex.getOrientation()) / 2047.0;
		double[] orientationVec = {-Math.sin(angle), -Math.cos(angle)};

		var nexCenterTile = getNexCenterTile(nex);

		var sims = wingTiles
			.stream()
			.map(wingTile -> MathUtil.cosineSimilarity(orientationVec, MathUtil.unitVec(nexCenterTile, wingTile)))
			.collect(Collectors.toList());

		int maxIndex = IntStream.range(0, sims.size())
			.reduce((acc, val) -> sims.get(acc) < sims.get(val) ? val : acc)
			.orElse(-1);

		if (maxIndex == -1)
		{
			return;
		}

		var selectedWingTile = wingTiles.get(maxIndex);

		int dx = Integer.signum(selectedWingTile.getX() - centerTile.getX());
		int dy = Integer.signum(selectedWingTile.getY() - centerTile.getY());

		if (dy == 0 && dx == 0)
		{
			return;
		}

		var currentTile = centerTile.dx(0).dy(0);

		dashLaneTiles.add(LocalPoint.fromWorld(client, centerTile));

		while (currentTile.getX() != selectedWingTile.getX() || currentTile.getY() != selectedWingTile.getY())
		{
			var newTile = currentTile.dx(dx).dy(dy);
			dashLaneTiles.add(LocalPoint.fromWorld(client, newTile));
			currentTile = newTile;
		}
	}

	private void clearIceTrap()
	{
		iceTraps.clear();
		isTrappedInIce = false;
	}

	private void handleStartDash()
	{
		selectWingTile();
		airplaneCoolDown.setTicksIfExpired(NEX_DASH_TICK_LEN);
		drawRangeCoolDown.setTicksIfExpired(NEX_DASH_CLICK_DELAY - 1);
		nexTicksUntilClick.setTicks(NEX_DASH_CLICK_DELAY);
	}

	@VisibleForTesting
	boolean shouldDraw(Renderable renderable, boolean drawingUI)
	{
		if (renderable instanceof Player)
		{
			Player player = (Player) renderable;

			if (player.getName() == null)
			{
				// player.isFriend() and player.isFriendsChatMember() npe when the player has a null name
				return true;
			}

			if (config.hideHealthyPlayers() && teamSize >= config.hideAboveNumber() && healthyPlayers.contains(player.getName()))
			{
				return false;
			}
		}

		return true;
	}
}
