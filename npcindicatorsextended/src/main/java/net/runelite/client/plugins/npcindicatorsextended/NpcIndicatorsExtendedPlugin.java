package net.runelite.client.plugins.npcindicatorsextended;

import com.google.inject.Provides;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.NPC;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.util.Text;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginManager;
import net.runelite.client.plugins.npchighlight.NpcIndicatorsPlugin;
import net.runelite.client.plugins.npchighlight.NpcIndicatorsConfig;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.events.PluginChanged;
import net.runelite.client.util.WildcardMatcher;
import lombok.AccessLevel;
import lombok.Getter;
import org.pf4j.Extension;

@Extension
@PluginDescriptor(
	name = "NPC Indicators Extended",
	description = "Highlights the true tiles of NPCs that are highlyted by the NPC Indicators-plugin",
	tags = {"highlight", "minimap", "npcs", "overlay", "respawn", "tags", "true", "tile"},
	enabledByDefault = false
)

@Slf4j
@PluginDependency(NpcIndicatorsPlugin.class)
public class NpcIndicatorsExtendedPlugin extends Plugin
{

	@Inject
	private Client client;

	@Inject
	private PluginManager pluginManager;

	@Inject
	private NpcIndicatorsExtendedConfig config;

	@Inject
	@Getter(AccessLevel.PACKAGE)
	private NpcIndicatorsPlugin NpcIndicatorsPlugin;

	@Inject
	@Getter(AccessLevel.PACKAGE)
	private NpcIndicatorsConfig NpcIndicatorsConfig;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private NpcIndicatorsExtendedOverlay npcIndicatorsExtendedOverlay;

	@Inject
	private ClientThread clientThread;

	@Getter(AccessLevel.PACKAGE)
	private final Set<NPC> highlightedNpcs = new HashSet<>();

	private List<String> highlights = new ArrayList<>();

	@Provides
	NpcIndicatorsExtendedConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(NpcIndicatorsExtendedConfig.class);
	}

	@Override
	protected void startUp()
	{
		overlayManager.add(npcIndicatorsExtendedOverlay);
		clientThread.invoke(() ->
		{
			rebuildAllNpcs();
		});
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(npcIndicatorsExtendedOverlay);
		clientThread.invoke(() ->
		{
			highlightedNpcs.clear();
		});
	}

	@Subscribe
	private void onConfigChanged(ConfigChanged configChanged)
	{
		if (configChanged.getGroup().indexOf("npcindicators") != 0)
		{
			return;
		}

		rebuildAllNpcs();
	}

	private List<String> getHighlights()
	{
		final String configNpcs = NpcIndicatorsConfig.getNpcToHighlight().toLowerCase();

		if (configNpcs.isEmpty())
		{
			return Collections.emptyList();
		}

		return Text.fromCSV(configNpcs);
	}

	@Subscribe
	private void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGIN_SCREEN ||
				event.getGameState() == GameState.HOPPING)
		{
			highlightedNpcs.clear();
			return;
		}
		if (client.getGameState() != GameState.LOGGED_IN &&
				client.getGameState() != GameState.LOADING)
		{
			return;
		}

		rebuildAllNpcs();
	}

	@Subscribe
	private void onNpcDespawned(NpcDespawned npcDespawned)
	{
		final NPC npc = npcDespawned.getNpc();
		highlightedNpcs.remove(npc);
	}

	@Subscribe
	private void onNpcSpawned(NpcSpawned npcSpawned)
	{
		final NPC npc = npcSpawned.getNpc();
		highlightNpcIfMatch(npc);
	}

	private void highlightNpcIfMatch(final NPC npc)
	{
		final String npcName = npc.getName();
		if (npcName != null)
		{
			for (String highlight : highlights)
			{
				if (WildcardMatcher.matches(highlight, npcName))
				{
					highlightedNpcs.add(npc);
					return;
				}
			}
		}

		highlightedNpcs.remove(npc);
	}

	@Subscribe
	public void onPluginChanged(final PluginChanged event)
	{
		if (event.getPlugin() == NpcIndicatorsPlugin)
		{
			rebuildAllNpcs();
		}
	}

	private void rebuildAllNpcs()
	{
		highlightedNpcs.clear();

		if (client.getGameState() != GameState.LOGGED_IN &&
				client.getGameState() != GameState.LOADING)
		{
			// NPCs are still in the client after logging out,
			// but we don't want to highlight those.
			return;
		}

		if (!pluginManager.isPluginEnabled(NpcIndicatorsPlugin))
		{
			return;
		}

		highlights = getHighlights();

		for (NPC npc : client.getNpcs())
		{
			highlightNpcIfMatch(npc);
		}
	}
}