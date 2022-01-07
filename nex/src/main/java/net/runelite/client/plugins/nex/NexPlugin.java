package net.runelite.client.plugins.nex;

/* Keep the change ya filthy animal
 * Chris
 */

import com.google.common.collect.Sets;
import com.google.inject.Provides;
import java.util.HashSet;
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
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.GraphicChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import org.pf4j.Extension;


/*
Todo:
- mesx for when you shouldn't attack boss
	(meta is not solid so figured I don't want to remove free will. Also maybe bugs)
- boss danger zones
- ice trap timers
- charge special
 */

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
	private static final int SHADOW_TICK_LEN = 5;
	private static final int NEX_PHASE_DELAY = 6;
	private static final int NEX_STARTUP_DELAY = 27;
	private static final int COUGH_GRAPHIC_ID = 1103;

	@Getter
	private boolean inFight;

	@Getter
	@Setter
	private boolean isFlash;

	@Getter
	private NPC nex;

	@Getter
	private int shadowsTicks;

	@Getter
	private int nexTicksUntilClick = 0;
	// used to not show next minion as vulnerable
	private int nexPhaseCoolDown = 0;

	@Getter
	private NexPhase currentPhase = NexPhase.NONE;

	@Getter
	private boolean minionActive = false;
	private NPC lastActive = null;

	@Getter
	private final Set<GameObject> shadows = new HashSet<>();

	@Getter
	private Set<NexCoughingPlayer> coughingPlayers = new HashSet<>();

	@Getter
	private NexSpecial currentSpecial;

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
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(overlay);
		client.setIsHidingEntities(false);
		overlayManager.remove(prayerOverlay);
		overlayManager.remove(prayerInfoBox);
		reset();
	}

	private void reset()
	{
		minionActive = false;
		currentPhase = NexPhase.NONE;
		nex = null;
		lastActive = null;
		coughingPlayers.clear();
		nexTicksUntilClick = 0;
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
			nexTicksUntilClick = NEX_STARTUP_DELAY;
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
	}

	private void handleTimers()
	{
		if (nexTicksUntilClick > 0)
		{
			nexTicksUntilClick--;
		}
		if (nexPhaseCoolDown > 0)
		{
			nexPhaseCoolDown--;
		}

		if (shadowsTicks > 0)
		{
			shadowsTicks--;
			if (shadowsTicks == 0)
			{
				shadows.clear();
			}
		}
	}

	private void handleCoughers()
	{
		var team = client.getPlayers().stream().map(Actor::getName).collect(Collectors.toSet());
		coughingPlayers.removeIf(nexCoughingPlayer -> nexCoughingPlayer.shouldRemove(client.getGameCycle()));

		if (config.hideHealthyPlayers() && team.size() >= config.hideAboveNumber())
		{
			client.setIsHidingEntities(true);
			Set<String> coughers = coughingPlayers.stream().map(NexCoughingPlayer::getName).collect(Collectors.toSet());
			client.setHideSpecificPlayers(Sets.difference(team, coughers).immutableCopy().asList());
		}
		else
		{
			client.setIsHidingEntities(false);
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
			shadowsTicks = SHADOW_TICK_LEN;
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
			return;
		}

		if (actor.getGraphic() == COUGH_GRAPHIC_ID)
		{
			coughingPlayers.add(new NexCoughingPlayer(actor.getName(), client.getGameCycle(), (Player) actor));
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
			if (currentPhase == NexPhase.NONE)
			{
				reset();
			}
			else if (currentPhase == NexPhase.STARTING)
			{
				System.out.println("Starting!");
				nex = null; // Just need to grab nex from the new spawn
				nexTicksUntilClick = NEX_STARTUP_DELAY;
			}
			else
			{
				minionActive = false;
				lastActive = null;
				nexTicksUntilClick = NEX_PHASE_DELAY;
				nexPhaseCoolDown = nexTicksUntilClick;
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
				nexTicksUntilClick = 8;
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
		if (nexPhaseCoolDown > 0)
		{
			return null;
		}

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
		return nexTicksUntilClick > 0 || minionActive;
	}
}
