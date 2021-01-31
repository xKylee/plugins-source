package net.runelite.client.plugins.socketsotetsegextended;

import com.google.inject.Provides;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.stream.IntStream;
import javax.inject.Inject;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.widgets.Widget;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginType;
import net.runelite.client.plugins.socket.org.json.JSONArray;
import net.runelite.client.plugins.socket.org.json.JSONObject;
import net.runelite.client.plugins.socket.packet.SocketReceivePacket;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ColorUtil;
import org.pf4j.Extension;

@Extension
@Slf4j
@PluginDescriptor(
		name = "Socket Sotetseg Extended",
		description = "Additional socket sote features",
		tags = {"sote", "maze"},
		type = PluginType.UTILITY,
		enabledByDefault = false
)
public class SocketSotetsegExtendedPlugin extends Plugin
{
	@Inject
	private Client client;
	@Inject
	private OverlayManager overlayManager;
	@Inject
	private SocketSotetsegExtendedConfig config;
	@Inject
	private SocketSotetsegExtendedOverlay overlay;
	private ArrayList<Point> mazeTiles;
	@Getter
	public ArrayList<Point> mazeSolved;
	private ArrayList<Point> mazePoints;
	@Getter
	private NPC sotetsegNPC;
	@Getter
	private boolean sotetsegActive;
	@Getter
	private boolean mazeActive;
	private boolean showFirstTile;
	@Getter
	@Setter
	private boolean flashScreen = false;
	@Getter
	private int instanceTime;
	@Getter
	private int mazeSolvedIndex;
	@Getter
	private int movementCount = -1;
	private int mazeStartTick = -1;
	private int mazeEndTick = -1;
	private int soteEntryTick = 0;
	private int allOffMaze = 0;
	private int firstOffMaze = 0;
	@Getter
	private boolean chosen = false;
	private boolean checkingOffMaze = false;
	private boolean checkingAnyOffMaze = false;
	private boolean packetReceived = false;
	private boolean instanceActive = false;
	private boolean started = false;
	private boolean madePlayerList = false;
	private ArrayList<Player> playersRunningMaze;
	private static final int UNDERWORLD_REGION_ID = 13379;

	@Provides
	SocketSotetsegExtendedConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(SocketSotetsegExtendedConfig.class);
	}

	protected void startUp()
	{
		packetReceived = false;
		mazePoints = new ArrayList<>();
		mazeTiles = new ArrayList<>();
		mazeSolved = new ArrayList<>();
		overlayManager.add(overlay);
		sotetsegNPC = null;
		sotetsegActive = false;
		mazeSolvedIndex = -1;
		started = false;
		checkingOffMaze = false;
		checkingAnyOffMaze = false;
		instanceTime = 2;
		playersRunningMaze = new ArrayList<>();
	}

	protected void shutDown()
	{
		overlayManager.remove(overlay);
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned event)
	{
		NPC npc = event.getNpc();
		switch (npc.getId())
		{
			case 8387:
			case 8388:
				mazePoints.clear();
				mazeSolved.clear();
				sotetsegActive = true;
				sotetsegNPC = npc;
				mazeActive = false;
			default:
		}
	}

	private void checkInstanceCreation()
	{
		if (inRegion(13122))
		{
			for (Player p : client.getPlayers())
			{
				WorldPoint wp = p.getWorldLocation();
				if (wp.getRegionX() == 32 && (wp.getRegionY() == 51 || wp.getRegionY() == 52))
				{
					instanceTime = 2;
					instanceActive = true;
					return;
				}
			}
		}

	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned event)
	{
		NPC npc = event.getNpc();
		switch (npc.getId())
		{
			case 8387:
			case 8388:
				if (client.getPlane() != 3)
				{
					sotetsegActive = false;
					sotetsegNPC = null;
					instanceActive = false;
					instanceTime = -1;
					started = false;
					int splitTime = client.getTickCount() - mazeEndTick;
					String splitMessage = "";
					if (mazeEndTick != -1 && config.showBetweenSplits())
					{
						splitMessage = splitMessage + "'Sotetseg Phase 3' completed! - Duration: <col=ff0000>" + String.format("%.1f", (double) splitTime * 0.6D) + "s";
						client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", splitMessage, null);
						soteEntryTick = -1;
					}
				}
			default:
		}
	}

	public boolean inRegion(int... regions)
	{
		if (client.getMapRegions() != null)
		{
			for (int i : client.getMapRegions())
			{
				if (Arrays.stream(regions, 0, regions.length).anyMatch(j -> i == j))
				{
					return true;
				}
			}
		}

		return false;
	}

	private boolean isInUnderWorld()
	{
		return client.getMapRegions().length > 0 && client.getMapRegions()[0] == UNDERWORLD_REGION_ID;
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		handleInstanceTimer();
		if (sotetsegActive)
		{
			updateMovementTimer();
			adjustIndexRunners();
			adjustMazeState();
			checkOffMaze();
		}

	}

	private void checkOffMaze()
	{
		List<Player> players;
		boolean anyOffMaze;
		Iterator<Player> playerIterator;
		Player partyMember;
		if (mazeActive && client.getTickCount() - mazeStartTick > 5 && checkingOffMaze && client.getLocalPlayer().getWorldLocation().getPlane() != 3)
		{
			players = client.getPlayers();
			anyOffMaze = true;
			playerIterator = players.iterator();

			while (playerIterator.hasNext())
			{
				partyMember = playerIterator.next();
				if (partyMember.getWorldLocation().getRegionX() >= 8 && partyMember.getWorldLocation().getRegionY() <= 36)
				{
					anyOffMaze = false;
				}
			}

			if (anyOffMaze && checkingOffMaze)
			{
				checkingOffMaze = false;
				allOffMaze = client.getTickCount();
			}
		}

		if (mazeActive && client.getTickCount() - mazeStartTick > 5 && checkingAnyOffMaze && client.getLocalPlayer().getWorldLocation().getPlane() != 3)
		{
			players = client.getPlayers();
			anyOffMaze = false;
			playerIterator = players.iterator();

			while (true)
			{
				do
				{
					do
					{
						if (!playerIterator.hasNext())
						{
							if (anyOffMaze && checkingAnyOffMaze)
							{
								checkingAnyOffMaze = false;
								firstOffMaze = client.getTickCount();
							}

							return;
						}

						partyMember = playerIterator.next();
					} while (partyMember.getWorldLocation().getRegionX() <= 8);
				} while (partyMember.getWorldLocation().getRegionY() <= 36);

				if (!chosen)
				{

					for (Player mazeRunner : playersRunningMaze)
					{
						if (mazeRunner.getName() != null && mazeRunner.getName().equals(partyMember.getName()))
						{
							anyOffMaze = true;
						}
					}
				}
				else if (partyMember.getName() != null && !partyMember.getName().equals(client.getLocalPlayer().getName()))
				{
					anyOffMaze = true;
				}
			}
		}
	}

	private void adjustMazeState()
	{
		if (sotetsegNPC.getId() == 8388 && !started)
		{
			started = true;
			mazeActive = false;
			soteEntryTick = client.getTickCount();
		}
		else if (sotetsegNPC.getId() == 8388 && mazeActive)
		{
			mazeEnds();
			mazePoints.clear();
			mazeSolved.clear();
			mazeSolvedIndex = -1;
			mazeActive = false;
		}
		else if ((sotetsegNPC.getId() == 8387 || isInUnderWorld()) && !mazeActive && started)
		{
			mazeActive = true;
			mazeStarts();
		}

	}

	private void adjustIndexRunners()
	{
		if (sotetsegNPC.getId() == 8387 && mazeActive)
		{
			if (!madePlayerList && client.getTickCount() - mazeStartTick > 5)
			{

				for (Player runner : client.getPlayers())
				{
					if (runner.getWorldLocation().getRegionX() > 8 && runner.getWorldLocation().getRegionY() > 14)
					{
						playersRunningMaze.add(runner);
					}
				}

				madePlayerList = true;
			}

			WorldPoint wp = client.getLocalPlayer().getWorldLocation();
			Point p = new Point(wp.getRegionX(), wp.getRegionY());

			int i;
			for (i = 0; i < mazeSolved.size(); ++i)
			{
				if (mazeSolved.get(i).getX() == p.getX() && mazeSolved.get(i).getY() == p.getY())
				{
					mazeSolvedIndex = i;
				}
			}
		}
		else if (mazeActive)
		{
			if (!showFirstTile)
			{
				showFirstTile = true;
			}

			if (client.getLocalPlayer().getWorldLocation().getRegionY() > 21 && showFirstTile)
			{
				showFirstTile = false;
			}
		}

	}

	private void updateMovementTimer()
	{
		if (movementCount != -1)
		{
			if (movementCount == 0)
			{
				movementCount = -1;
			}
			else
			{
				movementCount--;
			}
		}

	}

	private void handleInstanceTimer()
	{
		if (!instanceActive && inRegion(13122))
		{
			checkInstanceCreation();
		}

		if (instanceActive)
		{
			instanceTime++;
			if (instanceTime > 3)
			{
				instanceTime = 0;
			}
		}

	}

	private void mazeStarts()
	{
		packetReceived = false;
		if (client.getWidget(28, 1) != null)
		{
			Widget[] widgetsOfSotetseg = client.getWidget(28, 1).getChildren();

			for (Widget widget : widgetsOfSotetseg)
			{
				if (!widget.getText().isEmpty())
				{
					log.info("You have been chosen");
					chosen = true;
					flashScreen = true;
				}
			}
		}

		movementCount = 5;
		showFirstTile = true;
		mazeStartTick = client.getTickCount();
		checkingOffMaze = true;
		checkingAnyOffMaze = true;
		String splitMessage = "";
		int splitTime;
		if (soteEntryTick != -1 && config.showBetweenSplits())
		{
			splitTime = mazeStartTick - soteEntryTick;
			splitMessage = splitMessage + "'Sotetseg Phase 1' completed! - Duration: <col=ff0000>" + String.format("%.1f", (double) splitTime * 0.6D) + "s";
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", splitMessage, null);
			soteEntryTick = -1;
		}
		else if (soteEntryTick == -1 && config.showBetweenSplits())
		{
			splitTime = mazeStartTick - mazeEndTick;
			splitMessage = splitMessage + "'Sotetseg Phase 2' completed! - Duration: <col=ff0000>" + String.format("%.1f", (double) splitTime * 0.6D) + "s";
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", splitMessage, null);
		}

		mazeEndTick = 0;
	}

	private void mazeEnds()
	{
		showFirstTile = false;
		mazeEndTick = client.getTickCount();
		int mazeTicks = mazeSolved.size() > 0 ? mazeSolved.size() : -1;
		int mazeDiff = mazeEndTick - mazeStartTick;
		int lost = allOffMaze - mazeStartTick;
		int firstOff = firstOffMaze - mazeStartTick;
		mazeStartTick = 0;
		int perfectTick = -1;
		if (mazeSolved.size() != 0)
		{
			perfectTick = 5 + (int) Math.ceil((double) Math.abs(mazeSolved.get(0).getX() - 12) / 2.0D) + mazeTicks;
		}

		int ticksLost = (int) ((double) (mazeDiff - perfectTick) / 4.0D) * 4;
		if (config.showMazeSplits())
		{
			String color = "<col=" + ColorUtil.colorToHexCode(config.getSplitsMessageColor()) + ">";
			String red = "<col=ff0000>";
			String splitMessage = "Maze Duration: " + red + String.format("%.1f", (double) mazeDiff * 0.6D) + "s (";
			if (perfectTick == -1)
			{
				int cycleEnd = client.getTickCount();
				int offset = (cycleEnd - firstOff + mazeStartTick) % 4;
				int adjustEnd = firstOff + offset;
				ticksLost = cycleEnd - adjustEnd + mazeStartTick;
			}

			splitMessage = splitMessage + String.format("%.1f", (double) ticksLost * 0.6D) + "s)" + color;
			if (config.showDetailedSplits())
			{
				if (mazeSolved.size() != 0)
				{
					splitMessage = splitMessage + color + ", Maze: " + red + mazeSolved.size();
				}

				splitMessage = splitMessage + color + ", First: " + red + firstOff;
				splitMessage = splitMessage + color + ", Last: " + red + lost;
				if (perfectTick != -1)
				{
					splitMessage = splitMessage + color + ", Perfect: " + red + perfectTick;
				}
			}

			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", splitMessage, null);
		}

		playersRunningMaze.clear();
		madePlayerList = false;
		chosen = false;
	}

	@Subscribe
	public void onSocketReceivePacket(SocketReceivePacket event)
	{
		try
		{
			JSONObject payload = event.getPayload();
			if (!payload.has("sotetseg-extended"))
			{
				return;
			}

			if (packetReceived)
			{
				return;
			}

			mazeTiles.clear();
			JSONArray data = payload.getJSONArray("sotetseg-extended");

			int jsonIndex;
			for (jsonIndex = 0; jsonIndex < data.length(); jsonIndex++)
			{
				JSONObject jsonWorldPoint = data.getJSONObject(jsonIndex);
				int x = jsonWorldPoint.getInt("x");
				int y = jsonWorldPoint.getInt("y");
				WorldPoint wp = new WorldPoint(x, y, 0);
				Point p = new Point(wp.getRegionX(), wp.getRegionY());
				mazeTiles.add(p);
			}

			mazePoints.clear();
			mazeTiles.sort(Comparator.comparing(Point::getY).thenComparing(Point::getY));
			arrangeMazeTiles();
			addStartingTiles(mazePoints);
			mazeSolved.clear();
			mazeSolved = solveMaze(mazePoints);
			if (mazeSolved.get(mazeSolved.size() - 1).getY() > 35)
			{
				packetReceived = true;
			}

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	private void addStartingTiles(ArrayList<Point> mazePoints)
	{
		int i;
		if (mazePoints.get(0).getX() > 12)
		{
			for (i = mazePoints.get(0).getX() - 12; i >= 0; --i)
			{
				mazePoints.add(0, new Point(12 + i, 21));
			}
		}
		else if (mazePoints.get(0).getX() < 12)
		{
			for (i = 12 - mazePoints.get(0).getX(); i >= 0; --i)
			{
				mazePoints.add(0, new Point(12 - i, 21));
			}
		}
		else
		{
			mazePoints.add(0, new Point(12, 21));
		}

	}

	private void arrangeMazeTiles()
	{
		IntStream.range(mazeTiles.get(0).getY(), mazeTiles.get(mazeTiles.size() - 1).getY() + 1).forEach(i -> {
			List<Point> wpl = new ArrayList<>(mazeTiles);
			wpl.removeIf((x) -> i != x.getY());
			if (mazePoints.size() != 0 && wpl.get(0).getX() != mazePoints.get(mazePoints.size() - 1).getX())
			{
				Collections.reverse(wpl);
			}
			mazePoints.addAll(wpl);
		});
	}

	private ArrayList<Point> solveMaze(ArrayList<Point> tiles)
	{
		ArrayList<Point> solvedTiles = new ArrayList<>();
		if (tiles.size() != 0)
		{
			solvedTiles.add(tiles.get(0));

			int index = 0;

			while (solvedTiles.get(solvedTiles.size() - 1).getY() != tiles.get(tiles.size() - 1).getY() ||
					solvedTiles.get(solvedTiles.size() - 1).getY() == tiles.get(tiles.size() - 1).getY() &&
							solvedTiles.get(solvedTiles.size() - 1).getY() == 21 &&
							solvedTiles.get(solvedTiles.size() - 1).getX() != tiles.get(tiles.size() - 1).getX())
			{
				int cPosX = tiles.get(index).getX();
				int cPosY = tiles.get(index).getY();
				int validMove = -1;

				for (int i = tiles.size() - index > 4 ? 4 : tiles.size() - index - 1; i > 0; i--)
				{
					if (validMove == -1)
					{
						int xDiff = tiles.get(index + i).getX() - tiles.get(index).getX();
						int yDiff = tiles.get(index + i).getY() - tiles.get(index).getY();
						if (i < 3)
						{
							validMove = i;
						}
						else if (i == 3)
						{
							if (yDiff > 1 && cPosY + 1 == tiles.get(index + 1).getY() && Math.abs(xDiff) == 1)
							{
								validMove = i;
							}
							else if (yDiff == 1 && Math.abs(tiles.get(index + 1).getX() - cPosX) == 1)
							{
								validMove = i;
							}
						}
						else if (i == 4 && yDiff == 2 && Math.abs(xDiff) == 2 && Math.abs(tiles.get(index + 2).getX() - tiles.get(index).getX()) == 1 && tiles.get(index + 2).getY() - tiles.get(index).getY() == 1)
						{
							validMove = i;
						}
					}
				}

				index += validMove;
				solvedTiles.add(tiles.get(index));
			}

			if (solvedTiles.size() > 1)
			{
				Point last = solvedTiles.get(solvedTiles.size() - 1);
				Point secondLast = solvedTiles.get(solvedTiles.size() - 2);
				if (last.getY() - 1 == secondLast.getY())
				{
					Point p;
					if ((last.getX() - 1 == secondLast.getX() || last.getX() + 1 == secondLast.getX()) && last.getX() != 9 && last.getX() != 22)
					{
						p = new Point(last.getX() + (last.getX() - secondLast.getX()), last.getY() + 1);
						solvedTiles.set(solvedTiles.size() - 1, p);
					}
					else if (last.getX() == secondLast.getX())
					{
						p = new Point(last.getX(), last.getY() + 1);
						solvedTiles.set(solvedTiles.size() - 1, p);
					}
				}
			}
		}

		return solvedTiles;
	}

}