/*
 * THIS PLUGIN WAS WRITTEN BY A KEYBOARD-WIELDING MONKEY BOI BUT SHUFFLED BY A KANGAROO WITH THUMBS.
 * The plugin and it's refactoring was intended for xKylee's Externals but I'm sure if you're reading this, you're probably planning to yoink..
 * or you're just genuinely curious. If you're trying to yoink, it doesn't surprise me.. just don't claim it as your own. Cheers.
 * Extra contributors: BickusDiggus#0161
 */

package net.runelite.client.plugins.theatre.Xarpus;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import com.google.common.collect.ImmutableSet;
import lombok.Getter;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.GroundObject;
import net.runelite.api.NPC;
import net.runelite.api.NpcID;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.api.Varbits;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.GroundObjectSpawned;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.theatre.Room;
import net.runelite.client.plugins.theatre.TheatreConfig;
import net.runelite.client.plugins.theatre.TheatrePlugin;
import net.runelite.client.ui.overlay.infobox.Counter;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;
import net.runelite.client.util.ImageUtil;
import org.apache.commons.lang3.tuple.Pair;

public class Xarpus extends Room
{
	@Inject
	private Client client;

	@Inject
	private XarpusOverlay xarpusOverlay;

	@Inject
	private InfoBoxManager infoBoxManager;

	@Inject
	private TheatrePlugin p; //DO NOT USE. Just here for the exhumedCounter constructor.

	@Inject
	protected Xarpus(TheatrePlugin plugin, TheatreConfig config)
	{
		super(plugin, config);
	}

	@Getter
	private boolean xarpusActive;
	private boolean xarpusStarted = false;

	@Getter
	private NPC xarpusNPC;

	@Getter
	private int instanceTimer = 0;

	@Getter
	private boolean isInstanceTimerRunning = false;
	private boolean nextInstance = true;

	@Getter
	private boolean exhumedSpawned = false;

	@Getter
	private final Map<Long, Pair<GroundObject, Integer>> xarpusExhumeds = new HashMap<>();

	@Getter
	private Counter exhumedCounter;

	private int exhumedCount;

	@Getter
	private int xarpusTicksUntilAttack;

	@Getter
	private boolean postScreech = false;

	private boolean xarpusStare;

	private static BufferedImage EXHUMED_COUNT_ICON;
	private static final int GROUNDOBJECT_ID_EXHUMED = 32743;

	@Getter
	private boolean isHM;
	private static final Set<Integer> XARPUS_HM_ID = ImmutableSet.of(10770, 10771, 10772, 10773);

	@Override
	public void init()
	{
		EXHUMED_COUNT_ICON = ImageUtil.resizeCanvas(ImageUtil.getResourceStreamFromClass(TheatrePlugin.class, "1067-POISON.png"), 26, 26);
	}

	@Override
	public void load()
	{
		overlayManager.add(xarpusOverlay);
	}

	@Override
	public void unload()
	{
		overlayManager.remove(xarpusOverlay);

		infoBoxManager.removeInfoBox(exhumedCounter);

		exhumedCounter = null;
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned npcSpawned)
	{
		NPC npc = npcSpawned.getNpc();
		switch (npc.getId())
		{
			case NpcID.XARPUS:
			case NpcID.XARPUS_8339:
			case NpcID.XARPUS_8340:
			case NpcID.XARPUS_8341:
			case 10766:
			case 10767:
			case 10768:
			case 10769:
			case 10770:
			case 10771:
			case 10772:
			case 10773:
				isHM = XARPUS_HM_ID.contains(npc.getId());
				xarpusActive = true;
				xarpusNPC = npc;
				xarpusStare = false;
				xarpusTicksUntilAttack = 9;
				exhumedSpawned = false;
				postScreech = false;
				break;
		}
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned npcDespawned)
	{
		NPC npc = npcDespawned.getNpc();
		switch (npc.getId())
		{
			case NpcID.XARPUS:
			case NpcID.XARPUS_8339:
			case NpcID.XARPUS_8340:
			case NpcID.XARPUS_8341:
			case 10766:
			case 10767:
			case 10768:
			case 10769:
			case 10770:
			case 10771:
			case 10772:
			case 10773:
				isHM = false;
				xarpusActive = false;
				xarpusNPC = null;
				xarpusStare = false;
				xarpusTicksUntilAttack = 9;
				xarpusExhumeds.clear();
				infoBoxManager.removeInfoBox(exhumedCounter);
				exhumedCounter = null;
				isInstanceTimerRunning = false;
				exhumedSpawned = false;
				postScreech = false;
				exhumedCount = -1;
				break;
		}
	}

	@Subscribe
	public void onGroundObjectSpawned(GroundObjectSpawned event)
	{
		if (xarpusActive)
		{
			GroundObject o = event.getGroundObject();
			if (o.getId() == GROUNDOBJECT_ID_EXHUMED)
			{
				long hash = o.getHash();
				if (xarpusExhumeds.containsKey(hash))
				{
					return;
				}
				exhumedSpawned = true;

				if (exhumedCounter == null)
				{
					switch (TheatrePlugin.partySize)
					{
						case 5:
							exhumedCount = isHM ? 24 : 18;
							break;
						case 4:
							exhumedCount = isHM ? 20 : 15;
							break;
						case 3:
							exhumedCount = isHM ? 16 : 12;
							break;
						case 2:
							exhumedCount = isHM ? 13 : 9;
							break;
						default:
							exhumedCount = isHM ? 9 : 7;
					}

					if (config.xarpusExhumedCount() != TheatreConfig.XARPUS_EXHUMED_COUNT.OFF)
					{
						exhumedCounter = new Counter(EXHUMED_COUNT_ICON, p, config.xarpusExhumedCount() == TheatreConfig.XARPUS_EXHUMED_COUNT.DOWN ? exhumedCount - 1 : 1);
						infoBoxManager.addInfoBox(exhumedCounter);
					}
				}
				else
				{

					exhumedCounter.setCount(config.xarpusExhumedCount() == TheatreConfig.XARPUS_EXHUMED_COUNT.DOWN ? exhumedCounter.getCount() - 1 : exhumedCounter.getCount() + 1);
				}

				xarpusExhumeds.put(hash, Pair.of(o, isHM ? 9 : 11));
			}
		}
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		if (!xarpusStarted && inRoomRegion(TheatrePlugin.XARPUS_REGION) && client.getVarbitValue(client.getVarps(), 6447) == 2 && (client.getVarbitValue(Varbits.MULTICOMBAT_AREA) == 1))
		{
			xarpusStarted = true;
			isInstanceTimerRunning = false;
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (xarpusActive)
		{
			if (!xarpusExhumeds.isEmpty())
			{
				xarpusExhumeds.replaceAll((k, v) -> Pair.of(v.getLeft(), v.getRight() - 1));
				xarpusExhumeds.values().removeIf((p) -> p.getRight() <= 0);
			}

			if (xarpusNPC.getOverheadText() != null && !xarpusStare)
			{
				xarpusStare = true;
				xarpusTicksUntilAttack = 9;
			}

			if (xarpusStare)
			{
				xarpusTicksUntilAttack--;
				if (xarpusTicksUntilAttack <= 0)
				{
					if (!postScreech)
					{
						postScreech = true;
					}
					xarpusTicksUntilAttack = 8;
				}
			}
			else if (xarpusNPC.getId() == NpcID.XARPUS_8340 || xarpusNPC.getId() == 10768 || xarpusNPC.getId() == 10772)
			{
				xarpusTicksUntilAttack--;
				if (xarpusTicksUntilAttack <= 0)
				{
					xarpusTicksUntilAttack = 4;
				}
			}

		}

		if (isInstanceTimerRunning)
		{
			instanceTimer = (instanceTimer + 1) % 4;
		}
	}

	@Subscribe
	public void onClientTick(ClientTick event)
	{
		if (client.getLocalPlayer() == null)
		{
			return;
		}
		List<Player> players = client.getPlayers();
		for (Player player : players)
		{
			if (player.getWorldLocation() != null)
			{
				WorldPoint wpPlayer = player.getWorldLocation();
				LocalPoint lpPlayer = LocalPoint.fromWorld(client, wpPlayer.getX(), wpPlayer.getY());

				WorldPoint wpChest = WorldPoint.fromRegion(player.getWorldLocation().getRegionID(), 17, 5, player.getWorldLocation().getPlane());
				LocalPoint lpChest = LocalPoint.fromWorld(client, wpChest.getX(), wpChest.getY());
				if (lpChest != null)
				{
					Point point = new Point(lpChest.getSceneX() - lpPlayer.getSceneX(), lpChest.getSceneY() - lpPlayer.getSceneY());

					if (isInSotetsegRegion() && point.getY() == 1 && (point.getX() == 1 || point.getX() == 2 || point.getX() == 3) && nextInstance)
					{
						client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Xarpus instance timer started", "");
						instanceTimer = 2;
						isInstanceTimerRunning = true;
						nextInstance = false;
					}
				}
			}
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGGED_IN)
		{
			nextInstance = true;
		}
	}

	boolean isInSotetsegRegion()
	{
		return inRoomRegion(TheatrePlugin.SOTETSEG_REGION_OVERWORLD) || inRoomRegion(TheatrePlugin.SOTETSEG_REGION_UNDERWORLD);
	}
}
