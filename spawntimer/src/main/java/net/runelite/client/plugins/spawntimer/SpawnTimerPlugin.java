package net.runelite.client.plugins.spawntimer;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Provides;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import lombok.AccessLevel;
import lombok.Getter;
import net.runelite.api.GameState;
import net.runelite.api.NPC;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.util.Text;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import org.pf4j.Extension;

@Extension
@PluginDescriptor(
	name = "Spawn Timer",
	enabledByDefault = false,
	description = "Shows NPC'S time since spawned",
	tags = {"highlight", "minimap", "npcs", "overlay", "spawn", "tags"}
)
public class SpawnTimerPlugin extends Plugin
{
	@Inject
	private OverlayManager overlayManager;

	@Getter(AccessLevel.PACKAGE)
	private final Set<NPC> highlightedNpcs = new HashSet<>();

	@Getter(AccessLevel.PACKAGE)
	Set<SpawnTimer> ticks = new HashSet<>();

	@Inject
	private net.runelite.client.plugins.spawntimer.SpawnTimerOverlay SpawnTimerOverlay;

	@Inject
	private SpawnTimerConfig config;

	@Provides
	SpawnTimerConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(SpawnTimerConfig.class);
	}

	@Getter(AccessLevel.PACKAGE)
	public int currentTick;

	@Override
	protected void startUp()
	{
		currentTick = 0;
		overlayManager.add(SpawnTimerOverlay);
	}


	@Override
	protected void shutDown()
	{
		ticks.clear();
		highlightedNpcs.clear();
		overlayManager.remove(SpawnTimerOverlay);
	}

	@Subscribe
	private void onGameTick(GameTick g)
	{
		currentTick++;
	}

	@Subscribe
	private void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGIN_SCREEN ||
			event.getGameState() == GameState.HOPPING)
		{
			highlightedNpcs.clear();
			ticks.clear();
		}
	}

	@Subscribe
	private void onNpcSpawned(NpcSpawned n)
	{
		if (n.getNpc() != null)
		{
			final NPC npc = n.getNpc();
			highlightedNpcs.add(npc);
			SpawnTimer temp = new SpawnTimer();
			temp.setNpc(npc);
			temp.setTick(currentTick);
			ticks.add(temp);
		}
	}

	@Subscribe
	private void onNpcDespawned(NpcDespawned n)
	{
		final NPC npc = n.getNpc();
		if (highlightedNpcs.contains(npc))
		{
			highlightedNpcs.remove(npc);
			//currentTick = 0;
			ticks.removeIf(t -> t.getNpc() == npc);
		}
	}

	@VisibleForTesting
	public List<String> getHighlights()
	{
		final String configNpcs = config.getNpcToHighlight().toLowerCase();

		if (configNpcs.isEmpty())
		{
			return Collections.emptyList();
		}

		return Text.fromCSV(configNpcs);
	}
}