package net.runelite.client.plugins.godbook;

import com.google.inject.Provides;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import org.pf4j.Extension;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Extension
@PluginDescriptor(
		name = "Godbook",
		enabledByDefault = false,
		description = "Displays how long since someone preached.",
		tags = {"preach", "godbook"}
)
public class GodbookPlugin extends Plugin
{

	/* Thanks Caps lock 13 for his amazing work */

	@Inject
	private OverlayManager overlayManager;
	@Inject
	private GodbookOverlay overlay;
	@Inject
	private GodbookConfig config;
	@Inject
	private Client client;

	@Getter
	private ArrayList<String> names;
	@Getter
	private ArrayList<Integer> ticks;

	@Getter
	private boolean active;
	private boolean isShowing;

	private ArrayList<Integer> animationList;

	@Provides
	GodbookConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(GodbookConfig.class);
	}

	@Override
	protected void startUp()
	{
		active = false;
		isShowing = false;
		names = new ArrayList<>();
		ticks = new ArrayList<>();
		animationList = new ArrayList<>();
		updateAnimationList();
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(overlay);
		isShowing = false;
	}

	@Subscribe
	protected void onConfigChanged(ConfigChanged event)
	{
		updateAnimationList();
	}

	private void updateAnimationList()
	{
		String[] parsedAnimations = config.animations().split(",");
		animationList.clear();
		Arrays.stream(parsedAnimations).forEach(s ->
		{
			try
			{
				if (!Objects.equals(s, "")) animationList.add(Integer.parseInt(s));
			}
			catch (NumberFormatException e)
			{
				log.warn("Failed to parse: " + s);
			}
		});
	}
	private void addPlayer(String name)
	{
		active = true;
		ticks.add(0);
		names.add(name);
	}

	public void stop()
	{
		active = false;
		ticks.clear();
		names.clear();
	}

	private boolean containsAnimation(int id)
	{
		return animationList.contains(id);
	}

	public void increment()
	{
		ArrayList<Integer> toRemove = IntStream.range(0, ticks.size())
				.filter(i -> ticks.get(i) > config.maxTicks() - 2)
				.boxed()
				.collect(Collectors.toCollection(ArrayList::new));

		IntStream.range(0, ticks.size())
				.filter(toRemove::contains)
				.forEach(i ->
				{
					ticks.remove(i);
					names.remove(i);
				});

		if (ticks.size() == 0)
		{
			stop();
		}

		IntStream.range(0, ticks.size())
				.forEach(i ->
				{
					int temp = ticks.get(i) + 1;
					ticks.set(i, temp);
				});
	}

	@Subscribe
	public void onAnimationChanged(final AnimationChanged event)
	{
		if ((config.verzikOnly() && inRegion(12611, 12612)) || !config.verzikOnly())
		{
			if (event.getActor().getAnimation() == 7155 || event.getActor().getAnimation() == 7154 || event.getActor().getAnimation() == 1336 || containsAnimation(event.getActor().getAnimation()))
			{
				if (!active)
				{
					overlayManager.add(overlay);
					isShowing = true;
					active = true;
				}
				addPlayer(event.getActor().getName());
			}
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (isShowing && ticks.size() == 0)
		{
			overlayManager.remove(overlay);
			isShowing = false;
		}
		if (active)
		{
			increment();
		}
	}

	public boolean inRegion(int... regions)
	{
		if (client.getMapRegions() != null)
		{
			return Arrays.stream(client.getMapRegions())
					.anyMatch(i -> Arrays.stream(regions)
							.anyMatch(j -> i == j));
		}
		return false;
	}
}
