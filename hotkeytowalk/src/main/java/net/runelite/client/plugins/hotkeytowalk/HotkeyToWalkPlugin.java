package net.runelite.client.plugins.hotkeytowalk;

import com.google.inject.Provides;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.MenuEntry;
import net.runelite.api.events.FocusChanged;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.HotkeyListener;
import net.runelite.client.util.Text;
import org.pf4j.Extension;

@Extension
@PluginDescriptor(
	name = "HotKey To Walk",
	enabledByDefault = false,
	description = "Hotkey To Walk"
)
public class HotkeyToWalkPlugin extends Plugin
{
	private static final String WALK_HERE = "WALK HERE";
	private static final String CANCEL = "CANCEL";

	@Inject
	private Client client;

	@Inject
	private HotkeyToWalkConfig config;

	@Inject
	private ConfigManager configManager;

	@Inject
	private KeyManager keyManager;

	private boolean hotKeyPressed = false;


	@Provides
	HotkeyToWalkConfig provideConfig(final ConfigManager configManager)
	{
		return configManager.getConfig(HotkeyToWalkConfig.class);
	}

	private final HotkeyListener holdListener = new HotkeyListener(() -> config.hotkeyToWalk())
	{
		@Override
		public void hotkeyPressed()
		{
			hotKeyPressed = true;
		}

		@Override
		public void hotkeyReleased()
		{
			hotKeyPressed = false;
		}
	};

	@Override
	public void startUp()
	{
		keyManager.registerKeyListener(holdListener);
	}

	@Override
	public void shutDown()
	{
		keyManager.unregisterKeyListener(holdListener);
	}

	@Subscribe
	public void onFocusChanged(FocusChanged event)
	{
		if (!event.isFocused())
		{
			hotKeyPressed = false;
		}
	}

	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded event)
	{
		if (client.getGameState() != GameState.LOGGED_IN || !hotKeyPressed)
		{
			return;
		}

		final String pOptionToReplace = Text.removeTags(event.getOption()).toUpperCase();

		//If the option is already to walk there, or cancel we don't need to swap it with anything
		if (pOptionToReplace.equals(CANCEL) || pOptionToReplace.equals(WALK_HERE))
		{
			return;
		}

		stripEntries();
	}

	private void stripEntries()
	{
		MenuEntry walkkHereEntry = null;

		for (MenuEntry entry : client.getMenuEntries())
		{
			if ("Walk here".equals(entry.getOption()))
			{
				walkkHereEntry = entry;
			}
		}
		if (walkkHereEntry != null)
		{
			MenuEntry[] newEntries = new MenuEntry[1];
			newEntries[0] = walkkHereEntry;
			client.setMenuEntries(newEntries);
		}
	}
}