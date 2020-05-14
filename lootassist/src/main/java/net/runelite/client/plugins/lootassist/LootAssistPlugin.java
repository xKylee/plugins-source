package net.runelite.client.plugins.lootassist;

import com.google.inject.Provides;
import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Inject;
import net.runelite.api.Actor;
import net.runelite.api.AnimationID;
import net.runelite.api.Player;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginType;
import net.runelite.client.ui.overlay.OverlayManager;
import org.pf4j.Extension;

@Extension
@PluginDescriptor(
	name = "Loot Assist Plugin",
	enabledByDefault = false,
	description = "Creates a tile overlay with a timer that counts down to when the loot will appear to you",
	tags = {"pklite", "loot", "looting", "loot assist", "assist", "loot assist"},
	type = PluginType.PVP
)
public class LootAssistPlugin extends Plugin
{
	@Inject
	private OverlayManager overlayManager;

	@Inject
	private LootAssistOverlay lootAssistOverlay;

	private LootAssistConfig config;

	static final ConcurrentHashMap<WorldPoint, LootPile> lootPiles = new ConcurrentHashMap<>();

	@Provides
	LootAssistConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(LootAssistConfig.class);
	}


	@Override
	protected void startUp()
	{

		overlayManager.add(lootAssistOverlay);
	}

	@Override
	protected void shutDown()
	{
		lootPiles.clear();
		overlayManager.remove(lootAssistOverlay);
	}

	@Subscribe
	private void onGameStateChanged(GameStateChanged event)
	{
		lootPiles.clear();
	}

	@Subscribe
	private void onAnimationChanged(AnimationChanged event)
	{
		final Actor actor = event.getActor();
		if (actor.getAnimation() == AnimationID.DEATH && actor instanceof Player)
		{
			LootPile pile = new LootPile(actor.getWorldLocation(), actor.getName());
			lootPiles.put(pile.getLocation(), pile);
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (!event.getGroup().equals("lootassist"))
		{
			return;
		}

		if (event.getKey().equals("mirrorMode"))
		{
			lootAssistOverlay.determineLayer();
			overlayManager.remove(lootAssistOverlay);
			overlayManager.add(lootAssistOverlay);
		}
	}

}