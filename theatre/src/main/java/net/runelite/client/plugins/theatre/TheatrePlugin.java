/*
 * THIS SOFTWARE WRITTEN BY A KEYBOARD-WIELDING MONKEY BOI
 * No rights reserved. Use, redistribute, and modify at your own discretion,
 * and in accordance with Yagex and RuneLite guidelines.
 * However, aforementioned monkey would prefer if you don't sell this plugin for profit.
 * Good luck on your raids!
 */

package net.runelite.client.plugins.theatre;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.Client;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.GroundObjectSpawned;
import net.runelite.api.events.NpcDefinitionChanged;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.ProjectileMoved;
import net.runelite.api.events.ProjectileSpawned;
import net.runelite.api.events.SpotAnimationChanged;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.graphics.ModelOutlineRenderer;
import net.runelite.client.menus.MenuManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginType;
import net.runelite.client.plugins.theatre.rooms.BloatHandler;
import net.runelite.client.plugins.theatre.rooms.MaidenHandler;
import net.runelite.client.plugins.theatre.rooms.SotetsegHandler;
import net.runelite.client.plugins.theatre.rooms.VerzikHandler;
import net.runelite.client.plugins.theatre.rooms.nylocas.NyloHandler;
import net.runelite.client.plugins.theatre.rooms.xarpus.XarpusHandler;
import net.runelite.client.ui.overlay.OverlayManager;
import org.pf4j.Extension;

@Extension
@PluginDescriptor(
	name = "Theatre of Blood",
	description = "All-in-one plugin for Theatre of Blood.",
	tags = {"ToB", "Theatre", "Theatre of Blood", "Lyzrd"},
	type = PluginType.PVM
)
@Getter(AccessLevel.PUBLIC)
public class TheatrePlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private EventBus eventBus;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private TheatreOverlay overlay;

	@Inject
	private TheatreConfig config;

	@Inject
	private MenuManager menuManager;

	@Inject
	private ItemManager itemManager;

	@Inject
	private ModelOutlineRenderer modelOutline;

	private BloatHandler bloatHandler;
	private MaidenHandler maidenHandler;
	private NyloHandler nyloHandler;
	private SotetsegHandler sotetsegHandler;
	@Setter(AccessLevel.PUBLIC)
	private TheatreRoom room;
	private VerzikHandler verzikHandler;
	private XarpusHandler xarpusHandler;

	@Provides
	TheatreConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(TheatreConfig.class);
	}

	@Override
	protected void startUp()
	{
		room = TheatreRoom.UNKNOWN;
		maidenHandler = new MaidenHandler(client, this, config, modelOutline);
		bloatHandler = new BloatHandler(client, this, config);
		nyloHandler = new NyloHandler(client, this, config, menuManager, eventBus);
		sotetsegHandler = new SotetsegHandler(client, this, config);
		xarpusHandler = new XarpusHandler(client, this, config);
		verzikHandler = new VerzikHandler(client, this, config);
		overlayManager.add(overlay);
	}

	@Override
	protected void shutDown()
	{
		maidenHandler.onStop();
		maidenHandler = null;
		bloatHandler.onStop();
		bloatHandler = null;
		nyloHandler.startTime = 0L;
		nyloHandler.onStop();
		nyloHandler = null;
		sotetsegHandler.onStop();
		sotetsegHandler = null;
		xarpusHandler.onStop();
		xarpusHandler = null;
		verzikHandler.onStop();
		verzikHandler = null;
		room = TheatreRoom.UNKNOWN;
		overlayManager.remove(overlay);
	}

	@Subscribe
	private void onAnimationChanged(AnimationChanged event)
	{
		if (verzikHandler != null)
		{
			verzikHandler.onAnimationChanged(event);
		}
	}

	@Subscribe
	private void onChatMessage(ChatMessage event)
	{
		if (maidenHandler != null)
		{
			maidenHandler.onChatMessage(event);
		}
	}

	@Subscribe
	private void onConfigChanged(ConfigChanged event)
	{
		if (!event.getGroup().equals("Theatre"))
		{
			return;
		}

		if (nyloHandler != null)
		{
			nyloHandler.onConfigChanged();
		}
	}

	@Subscribe
	private void onGameTick(GameTick event)
	{
		if (maidenHandler != null)
		{
			maidenHandler.onGameTick();
		}

		if (bloatHandler != null)
		{
			bloatHandler.onGameTick();
		}

		if (nyloHandler != null)
		{
			nyloHandler.onGameTick();
		}

		if (sotetsegHandler != null)
		{
			sotetsegHandler.onGameTick();
		}

		if (xarpusHandler != null)
		{
			xarpusHandler.onGameTick();
		}

		if (verzikHandler != null)
		{
			verzikHandler.onGameTick();
		}
	}

	@Subscribe
	private void onGroundObjectSpawned(GroundObjectSpawned event)
	{
		if (sotetsegHandler != null)
		{
			sotetsegHandler.onGroundObjectSpawned(event);
		}

		if (xarpusHandler != null)
		{
			xarpusHandler.onGroundObjectSpawned(event);
		}
	}

	@Subscribe
	private void onNpcDefinitionChanged(NpcDefinitionChanged event)
	{
		if (maidenHandler != null)
		{
			maidenHandler.onNpcDefinitionChanged(event);
		}
	}

	@Subscribe
	private void onNpcDespawned(NpcDespawned event)
	{
		if (maidenHandler != null)
		{
			maidenHandler.onNpcDespawned(event);
		}

		if (bloatHandler != null)
		{
			bloatHandler.onNpcDespawned(event);
		}

		if (nyloHandler != null)
		{
			nyloHandler.onNpcDespawned(event);
		}

		if (sotetsegHandler != null)
		{
			sotetsegHandler.onNpcDespawned(event);
		}

		if (xarpusHandler != null)
		{
			xarpusHandler.onNpcDespawned(event);
		}

	}

	@Subscribe
	private void onNpcSpawned(NpcSpawned event)
	{
		if (maidenHandler != null)
		{
			maidenHandler.onNpcSpawned(event);
		}

		if (bloatHandler != null)
		{
			bloatHandler.onNpcSpawned(event);
		}

		if (nyloHandler != null)
		{
			nyloHandler.onNpcSpawned(event);
		}

		if (sotetsegHandler != null)
		{
			sotetsegHandler.onNpcSpawned(event);
		}

		if (xarpusHandler != null)
		{
			xarpusHandler.onNpcSpawned(event);
		}

		if (verzikHandler != null)
		{
			verzikHandler.onNpcSpawned(event);
		}

	}

	@Subscribe
	private void onProjectileMoved(ProjectileMoved event)
	{
		if (verzikHandler != null)
		{
			verzikHandler.onProjectileMoved(event);
		}
	}

	@Subscribe
	private void onProjectileSpawned(ProjectileSpawned event)
	{
		if (sotetsegHandler != null)
		{
			sotetsegHandler.onProjectileSpawned(event);

		}
	}

	@Subscribe
	private void onSpotAnimationChanged(SpotAnimationChanged event)
	{
		if (maidenHandler != null)
		{
			maidenHandler.onSpotAnimationChanged(event);
		}
	}

	@Subscribe
	private void onVarbitChanged(VarbitChanged event)
	{
		if (bloatHandler != null)
		{
			bloatHandler.onVarbitChanged(event);
		}

		if (xarpusHandler != null)
		{
			xarpusHandler.onVarbitChanged(event);
		}
	}
}