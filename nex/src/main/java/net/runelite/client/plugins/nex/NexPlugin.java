package net.runelite.client.plugins.nex;

/* Keep the change ya filthy animal
 * Chris
 */

import com.google.common.collect.Sets;
import com.google.inject.Provides;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.GraphicChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
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

	private static final int SHADOW_ID = 42942;
	private static final int ICE_TRAP_ID = 42944;
	private static final int COUGH_GRAPHIC_ID = 1103;

	private static final int SHADOW_TICK_LEN = 5;
	private static final int ICE_TRAP_TICK_LEN = 9;
	private static final int NEX_PHASE_DELAY = 6;
	private static final int NEX_PHASE_MINION_DELAY = 10;
	private static final int NEX_STARTUP_DELAY = 27;
	private static final int NEX_WRATH_TICK_DELAY = 5;
	private static final int NEX_SIPHON_DELAY = 9;

	@Getter
	private boolean inFight;

	@Getter
	@Setter
	private boolean isFlash;

	@Getter
	private NPC nex;

	@Getter
	private NexPhase currentPhase = NexPhase.NONE;

	@Getter
	private boolean minionActive = false;
	private NPC lastActive = null;

	@Getter
	private final Set<GameObject> shadows = new HashSet<>();

	@Getter
	private final Set<NexCoughingPlayer> coughingPlayers = new HashSet<>();

	@Getter
	private final Set<String> healthyPlayers = new HashSet<>();

	@Getter
	private final Set<GameObject> iceTraps = new HashSet<>();

	@Getter
	private List<LocalPoint> healthyPlayersLocations = new ArrayList<>();

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
	private boolean hasDisabledEntityHiderRecently = false;
	private boolean hasEnabledEntityHiderRecently = false;

	// Tick timers

	@Getter
	private final TickTimer shadowsTicks = new TickTimer(shadows::clear);

	@Getter
	private final TickTimer nexTicksUntilClick = new TickTimer();

	// used to not show next minion as vulnerable
	private final TickTimer nexPhaseMinionCoolDown = new TickTimer();

	@Getter
	private final TickTimer iceTrapTicks = new TickTimer(this::clearIceTrap);

	private void clearIceTrap()
	{
		iceTraps.clear();
		isTrappedInIce = false;
	}

	@Getter
	private final TickTimer nexDeathTileTicks = new TickTimer(() -> nexDeathTile = null);

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
		client.setIsHidingEntities(true);
		reset();
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(overlay);
		overlayManager.remove(prayerOverlay);
		overlayManager.remove(prayerInfoBox);
		client.setIsHidingEntities(false);
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged e)
	{
		if (e.getGroup().equals(NexConfig.GROUP))
		{
			// if you disable entity hider you will clobber this plugin,
			// so we should have this to have a way to easily get it back
			if (config.hideHealthyPlayers())
			{
				client.setIsHidingEntities(true);
			}
		}
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
		nexTicksUntilClick.reset();
		iceTraps.clear();
		iceTrapTicks.reset();
		teamSize = 0;
		coughingPlayersChanged = false;
		hasDisabledEntityHiderRecently = false;
		hasEnabledEntityHiderRecently = false;
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
			inFight = true;

			// first discover nex, oh wow, fun boss.
			// Handle edge case where because we arent in fight yet
			// we wont see her spawning text
			nexTicksUntilClick.setTicks(NEX_STARTUP_DELAY);
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
	}

	private void handleTimers()
	{
		nexTicksUntilClick.tick();
		nexPhaseMinionCoolDown.tick();
		nexDeathTileTicks.tick();
		shadowsTicks.tick();
		iceTrapTicks.tick();
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

		isTrappedInIce = iceTraps.stream().filter(trap -> trap.getWorldLocation().distanceTo(player.getWorldLocation()) == 1).count() == iceTraps.size();
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

			var team = players.stream().map(Actor::getName).collect(Collectors.toSet());
			var coughers = coughingPlayers.stream().map(NexCoughingPlayer::getName).collect(Collectors.toSet());
			healthyPlayers.clear();
			healthyPlayers.addAll(Sets.difference(team, coughers));
		}

		// HAS booleans prevent excess calls to client
		if (config.hideHealthyPlayers() && players.size() >= config.hideAboveNumber())
		{
			if (!hasEnabledEntityHiderRecently)
			{
				client.setHideSpecificPlayers(new ArrayList<>(healthyPlayers));
				// prevent us from running again right away
				hasEnabledEntityHiderRecently = true;
				// ensure disable will run if toggled again
				hasDisabledEntityHiderRecently = false;
			}
		}
		else
		{
			if (!hasDisabledEntityHiderRecently)
			{
				clearHiddenEntities();
				// prevent us from running again right away
				hasDisabledEntityHiderRecently = true;
				// ensure enabled will run if toggled again
				hasEnabledEntityHiderRecently = false;
			}
		}

		// update healthy locations if we are sick
		if (selfCoughingPlayer != null)
		{
			healthyPlayersLocations = players
				.stream()
				.filter(player -> client.getLocalPlayer() != player && healthyPlayers.contains(player.getName()))
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
			shadows.add(object);
			shadowsTicks.setTicks(SHADOW_TICK_LEN);
		}
		else if (object.getId() == ICE_TRAP_ID)
		{
			iceTraps.add(object);
			iceTrapTicks.setTicks(ICE_TRAP_TICK_LEN);
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

		if (object.getId() == ICE_TRAP_ID)
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
			clearHiddenEntities();
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

	private void clearHiddenEntities()
	{
		client.setHideSpecificPlayers(new ArrayList<>());
	}
}
