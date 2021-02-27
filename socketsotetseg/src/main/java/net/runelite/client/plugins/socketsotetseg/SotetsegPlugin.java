/*
 * Copyright (c) 2020, Charles Xu <github.com/kthisiscvpv>
 * Copyright (c) 2020, caps lock13#0001
  * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.runelite.client.plugins.socketsotetseg;

import com.google.inject.Provides;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;
import javax.inject.Inject;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GroundObject;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.api.Tile;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.GroundObjectSpawned;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.widgets.Widget;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginManager;
import net.runelite.client.plugins.socket.SocketPlugin;
import net.runelite.client.plugins.socket.org.json.JSONArray;
import net.runelite.client.plugins.socket.org.json.JSONObject;
import net.runelite.client.plugins.socket.packet.SocketBroadcastPacket;
import net.runelite.client.plugins.socket.packet.SocketReceivePacket;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ColorUtil;
import org.pf4j.Extension;

@Extension
@PluginDescriptor(
	name = "Socket Sotetseg",
	description = "Extended plugin handler for Sotetseg in the Theatre of Blood.",
	tags = {"socket", "server", "discord", "connection", "broadcast", "sotetseg", "theatre", "tob"},
	enabledByDefault = false
)
@PluginDependency(SocketPlugin.class)
public class SotetsegPlugin extends Plugin
{

	@Inject
	private Client client;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private EventBus eventBus;

	@Inject
	private PluginManager pluginManager;

	@Inject
	private SocketPlugin socketPlugin;

	@Inject
	private SotetsegConfig config;
	private boolean flashFlag;

	@Provides
	SotetsegConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(SotetsegConfig.class);
	}

	@Inject
	private SotetsegOverlay overlay;

	// This boolean states whether or not the room is currently active.
	@Getter(AccessLevel.PUBLIC)
	private boolean sotetsegActive;

	// This NPC represents the boss.
	@Getter(AccessLevel.PUBLIC)
	private NPC sotetsegNPC;

	// This represents the bad tiles.
	private LinkedHashSet<Point> redTiles;

	@Getter(AccessLevel.PUBLIC)
	private Set<WorldPoint> mazePings;

	// This represents the amount of times to send data.
	private int dispatchCount;

	// This represents the state of the raid.
	private boolean wasInUnderworld;
	private int overworldRegionID;

	private ArrayList<Point> mazeTiles;
	@Getter
	public ArrayList<Point> mazeSolved;
	private ArrayList<Point> mazePoints;
	@Getter
	private boolean mazeActive;
	private boolean showFirstTile;
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

	@Getter
	@Setter
	private boolean flashScreen = false;

	@Override
	protected void startUp()
	{
		sotetsegActive = false;
		sotetsegNPC = null;

		redTiles = new LinkedHashSet<>();
		mazePings = Collections.synchronizedSet(new HashSet<>());

		dispatchCount = 5;
		wasInUnderworld = false;
		overworldRegionID = -1;

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
		flashFlag = true;

		overlayManager.add(overlay);
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(overlay);
	}

	@Subscribe // Boss has entered the scene. Played has entered the room.
	public void onNpcSpawned(NpcSpawned event)
	{
		final NPC npc = event.getNpc();
		switch (npc.getId())
		{
			case 8388:
				flashFlag = true;
				// fall through into below
			case 8387:
				mazePoints.clear();
				mazeSolved.clear();
				mazeActive = false;
				sotetsegActive = true;
				sotetsegNPC = npc;
				break;
			default:
				break;
		}
	}

	@Subscribe // Boss has left the scene. Player left, died, or the boss was killed.
	public void onNpcDespawned(NpcDespawned event)
	{
		final NPC npc = event.getNpc();
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
					flashFlag = true;
					int splitTime = client.getTickCount() - mazeEndTick;
					String splitMessage = "";
					if (mazeEndTick != -1 && config.showBetweenSplits())
					{
						splitMessage = splitMessage + "'Sotetseg Phase 3' completed! - Duration: <col=ff0000>" + String.format("%.1f", (double) splitTime * 0.6D) + "s";
						client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", splitMessage, null);
						soteEntryTick = -1;
					}
				}

				break;
			default:
				break;
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		handleInstanceTimer();
		if (sotetsegActive)
		{
			final Player player = client.getLocalPlayer();

			// This check resets all the data if sotetseg is attackable.
			if (sotetsegNPC != null && sotetsegNPC.getId() == 8388)
			{
				redTiles.clear();
				mazePings.clear();
				dispatchCount = 5;
				flashFlag = true;

				if (isInOverWorld())
				{ // Set the overworld flags.
					wasInUnderworld = false;
					if (player != null && player.getWorldLocation() != null)
					{
						WorldPoint wp = player.getWorldLocation();
						overworldRegionID = wp.getRegionID();
					}
				}
			}

			if (!redTiles.isEmpty() && wasInUnderworld)
			{
				if (dispatchCount > 0)
				{ // Ensure we only send the data a couple times.
					dispatchCount--;

					if (pluginManager.isPluginEnabled(socketPlugin))
					{
						JSONArray data = new JSONArray();

						for (final Point p : redTiles)
						{
							WorldPoint wp = translateMazePoint(p);

							JSONObject jsonwp = new JSONObject();
							jsonwp.put("x", wp.getX());
							jsonwp.put("y", wp.getY());
							jsonwp.put("plane", wp.getPlane());

							data.put(jsonwp);
						}

						JSONObject payload = new JSONObject();
						payload.put("sotetseg-extended", data);

						eventBus.post(new SocketBroadcastPacket(payload));
					}
				}
			}

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

	private void mazeEnds()
	{
		if (this.client.getWidget(28, 1) != null)
		{
			this.hideWidget(this.client.getWidget(28, 1), false);
		}

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

	private void hideWidget(Widget widget, boolean hidden)
	{
		if (widget != null)
		{
			widget.setHidden(hidden);
		}
	}

	private void mazeStarts()
	{
		packetReceived = false;

		if (client.getWidget(28, 1) != null)
		{
			Widget[] widgetsOfSotetseg = client.getWidget(28, 1).getChildren();
			hideWidget(client.getWidget(28, 1), true);

			for (Widget widget : widgetsOfSotetseg)
			{
				if (!widget.getText().isEmpty() && widget.getText().contains("Sotetseg chooses you"))
				{
					chosen = true;
					if (flashFlag)
					{
						flashFlag = false;
						flashScreen = true;
					}
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

	/**
	 * timer above sotetseg when he becomes attackable again
	 */
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

			mazePings.clear();
			mazeTiles.clear();
			JSONArray data = payload.getJSONArray("sotetseg-extended");
			for (int i = 0; i < data.length(); i++)
			{
				JSONObject jsonwp = data.getJSONObject(i);
				int x = jsonwp.getInt("x");
				int y = jsonwp.getInt("y");
				int plane = jsonwp.getInt("plane");

				WorldPoint wp = new WorldPoint(x, y, plane);
				mazePings.add(wp);
				Point p = new Point(wp.getRegionX(), wp.getRegionY());
				mazeTiles.add(p);
			}

			if (config.solveMaze())
			{
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

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Subscribe
	public void onGroundObjectSpawned(GroundObjectSpawned event)
	{
		if (sotetsegActive)
		{
			GroundObject o = event.getGroundObject();
			if (o.getId() == 33035)
			{
				final Tile t = event.getTile();
				final WorldPoint p = WorldPoint.fromLocal(client, t.getLocalLocation());
				final Point point = new Point(p.getRegionX(), p.getRegionY());

				if (this
					.isInOverWorld())
				{  // (9, 22) are magical numbers that represent the overworld maze offset.
					redTiles.add(new Point(point.getX() - 9, point.getY() - 22));
				}

				if (this
					.isInUnderWorld())
				{   // (42, 31) are magical numbers that represent the underworld maze offset.
					redTiles.add(new Point(point.getX() - 42, point.getY() - 31));
					wasInUnderworld = true;
				}
			}
		}
	}

	/**
	 * Returns whether or not the current player is in the overworld.
	 *
	 * @return In the overworld?
	 */
	private boolean isInOverWorld()
	{
		return client.getMapRegions().length > 0 && client.getMapRegions()[0] == 13123;
	}

	/**
	 * Returns whether or not the current player is in the underworld.
	 *
	 * @return In the underworld?
	 */
	private boolean isInUnderWorld()
	{
		return client.getMapRegions().length > 0 && client.getMapRegions()[0] == 13379;
	}

	/**
	 * Translates a maze point to a WorldPoint.
	 *
	 * @param mazePoint Point on the maze.
	 * @return WorldPoint
	 */
	private WorldPoint translateMazePoint(final Point mazePoint)
	{
		Player p = client.getLocalPlayer();

		// (9, 22) are magical numbers that represent the overworld maze offset.
		if (overworldRegionID == -1 && p != null)
		{
			WorldPoint wp = p.getWorldLocation();
			return WorldPoint
				.fromRegion(wp.getRegionID(), mazePoint.getX() + 9, mazePoint.getY() + 22, 0);
		}

		return WorldPoint
			.fromRegion(overworldRegionID, mazePoint.getX() + 9, mazePoint.getY() + 22, 0);
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

	// maze solving logic written by caps lock13#0001
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
}
