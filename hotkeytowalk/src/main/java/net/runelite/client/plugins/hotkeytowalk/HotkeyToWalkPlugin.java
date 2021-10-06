package net.runelite.client.plugins.hotkeytowalk;

import com.google.inject.Provides;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.FocusChanged;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.HotkeyListener;
import static net.runelite.api.MenuAction.MENU_ACTION_DEPRIORITIZE_OFFSET;
import org.pf4j.Extension;

@Extension
@PluginDescriptor(
	name = "Hotkey Walk Here",
	enabledByDefault = false,
	description = "Use Hotkey to toggle the Walk Here menu option. While pressed you will Walk rather than interact with objects."
)
public class HotkeyToWalkPlugin extends Plugin
{
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

	@Subscribe(priority = -1)
	public void onClientTick(ClientTick event)
	{
		if (client.getGameState() == GameState.LOGGED_IN && !client.isMenuOpen() && hotKeyPressed)
		{
			MenuEntry[] entries = client.getMenuEntries();
			int entryIndex = -1;
			for (int i = 0; i < entries.length; i++)
			{
				MenuEntry entry = entries[i];
				int opId = entry.getType();
				if (opId >= 2000)
				{
					opId -= 2000;
				}
				if (opId == MenuAction.WALK.getId())
				{
					entryIndex = i;
				}
			}
			if (entryIndex < 0)
			{
				return;
			}
			for (MenuEntry menuEntry : entries)
			{
				if (menuEntry.getType() < MenuAction.WALK.getId())
				{
					menuEntry.setType(menuEntry.getType() + MENU_ACTION_DEPRIORITIZE_OFFSET);
				}
			}
			MenuEntry first = entries[entries.length - 1];
			entries[entries.length - 1] = entries[entryIndex];
			entries[entryIndex] = first;
			client.setMenuEntries(entries);
		}
	}

	@Subscribe(priority = -1)
	public void onMenuEntryAdded(MenuEntryAdded event)
	{
		if (client.getGameState() == GameState.LOGGED_IN && hotKeyPressed)
		{
			boolean hasWalkHere = false;
			for (MenuEntry menuEntry : client.getMenuEntries())
			{
				int opId = menuEntry.getType();
				if (opId >= 2000)
				{
					opId -= 2000;
				}
				hasWalkHere |= opId == MenuAction.WALK.getId();
			}
			if (!hasWalkHere)
			{
				return;
			}
			if (event.getType() < MenuAction.WALK.getId())
			{
				deprioritizeEntry(event.getIdentifier(), event.getType());
			}
		}
	}

	private void deprioritizeEntry(int id, int op_id)
	{
		MenuEntry[] menuEntries = client.getMenuEntries();

		for (int i = menuEntries.length - 1; i >= 0; --i)
		{
			MenuEntry entry = menuEntries[i];

			if (entry.getType() == op_id && entry.getIdentifier() == id)
			{
				// Raise the priority of the op so it doesn't get sorted later
				entry.setType(op_id + MENU_ACTION_DEPRIORITIZE_OFFSET);
				menuEntries[i] = menuEntries[menuEntries.length - 1];
				menuEntries[menuEntries.length - 1] = entry;

				client.setMenuEntries(menuEntries);
				break;
			}
		}
	}
}