/*
 * THIS PLUGIN WAS WRITTEN BY A KEYBOARD-WIELDING MONKEY BOI BUT SHUFFLED BY A KANGAROO WITH THUMBS.
 * The plugin and it's refactoring was intended for xKylee's Externals but I'm sure if you're reading this, you're probably planning to yoink..
 * or you're just genuinely curious. If you're trying to yoink, it doesn't surprise me.. just don't claim it as your own. Cheers.
 */

package net.runelite.client.plugins.theatre.Bloat;

import java.awt.Color;
import java.awt.Polygon;
import java.util.HashMap;
import javax.inject.Inject;
import lombok.Getter;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.NPCDefinition;
import net.runelite.api.NpcID;
import net.runelite.api.GraphicsObject;
import net.runelite.api.GameState;
import net.runelite.api.Varbits;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.GraphicsObjectCreated;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.theatre.Room;
import net.runelite.client.plugins.theatre.RoomOverlay;
import net.runelite.client.plugins.theatre.TheatreConfig;
import net.runelite.client.plugins.theatre.TheatrePlugin;

public class Bloat extends Room
{
	@Inject
	private Client client;

	@Inject
	private BloatOverlay bloatOverlay;

	@Inject
	protected Bloat(final Client client, TheatrePlugin plugin, TheatreConfig config)
	{
		super(plugin, config);
	}

	@Getter
	private boolean bloatActive;

	@Getter
	private NPC bloatNPC;

	@Getter
	private HashMap<WorldPoint, Integer> bloatHands = new HashMap<>();

	@Getter
	private int bloatTickCount = -1;

	private int bloatDownCount = 0;
	private int bloatState = 0;

	private boolean bloatStarted;

	@Override
	public void load()
	{
		overlayManager.add(bloatOverlay);
	}

	@Override
	public void unload()
	{
		overlayManager.remove(bloatOverlay);
		bloatDownCount = 0;
		bloatState = 0;
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned npcSpawned)
	{
		NPC npc = npcSpawned.getNpc();
		if (npc.getId() == NpcID.PESTILENT_BLOAT)
		{
			bloatActive = true;
			bloatNPC = npc;
			bloatTickCount = 0;
			bloatStarted = false;
		}
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned npcDespawned)
	{
		NPC npc = npcDespawned.getNpc();
		if (npc.getId() == NpcID.PESTILENT_BLOAT)
		{
			bloatActive = false;
			bloatNPC = null;
			bloatTickCount = -1;
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged change)
	{
		if (change.getKey().equals("mirrorMode"))
		{
			bloatOverlay.determineLayer();
			overlayManager.remove(bloatOverlay);
			overlayManager.add(bloatOverlay);
		}
	}

	@Subscribe
	public void onAnimationChanged(AnimationChanged event)
	{
		if (client.getGameState() != GameState.LOGGED_IN || event.getActor() != bloatNPC)
		{
			return;
		}

		bloatTickCount = 0;
	}

	@Subscribe
	public void onGraphicsObjectCreated(GraphicsObjectCreated graphicsObjectC)
	{
		if (bloatActive)
		{
			GraphicsObject graphicsObject = graphicsObjectC.getGraphicsObject();
			if (graphicsObject.getId() >= 1560 && graphicsObject.getId() <= 1590)
			{
				WorldPoint point = WorldPoint.fromLocal(client, graphicsObject.getLocation());
				if (!bloatHands.containsKey(point))
				{
					bloatHands.put(point, 4);
				}
			}
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (bloatActive)
		{
			bloatDownCount++;
			bloatTickCount++;

			bloatHands.values().removeIf(v -> v <= 0);
			bloatHands.replaceAll((k, v) -> v - 1);

			if (bloatNPC.getAnimation() == -1) // 1 = UP, 2 = DOWN, 3 = WARN, 4 = PAST THRESHOLD;
			{
				bloatDownCount = 0;
				if (bloatNPC.getHealthScale() == 0)
				{
					bloatState = 2;
				}
				else if (bloatTickCount >= 38)
				{
					bloatState = 4;
				}
				else
				{
					bloatState = 1;
				}
			}
			else
			{
				if (bloatTickCount >= 38)
				{
					bloatState = 4;
				}
				else if (25 < bloatDownCount && bloatDownCount < 35)
				{
					bloatState = 3;
				}
				else if (bloatDownCount < 26)
				{
					bloatState = 2;
				}
				else if (bloatNPC.getModelHeight() == 568)
				{
					bloatState = 2;
				}
				else
				{
					if (bloatTickCount >= 38)
					{
						bloatState = 4;
					}
					else
					{
						bloatState = 1;
					}
				}
			}
		}
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		if (client.getVar(Varbits.BLOAT_DOOR) == 1 && !bloatStarted)
		{
			bloatTickCount = 0;
			bloatStarted = true;
		}
	}

	Polygon getBloatTilePoly()
	{
		if (bloatNPC == null)
		{
			return null;
		}

		int size = 1;
		NPCDefinition composition = bloatNPC.getTransformedDefinition();
		if (composition != null)
		{
			size = composition.getSize();
		}

		LocalPoint lp;

		switch (bloatState)
		{
			case 1:
			case 4:
				lp = bloatNPC.getLocalLocation();

				if (lp == null)
				{
					return null;
				}

				return RoomOverlay.getCanvasTileAreaPoly(client, lp, size, true);
			case 2:
			case 3:
				lp = LocalPoint.fromWorld(client, bloatNPC.getWorldLocation());

				if (lp == null)
				{
					return null;
				}

				return RoomOverlay.getCanvasTileAreaPoly(client, lp, size, false);
		}

		return null;
	}

	Color getBloatStateColor()
	{
		Color col = config.bloatIndicatorColorUP();
		switch (bloatState)
		{
			case 2:
				col = config.bloatIndicatorColorDOWN();
				break;
			case 3:
				col = config.bloatIndicatorColorWARN();
				break;
			case 4:
				col = config.bloatIndicatorColorTHRESH();
				break;
		}
		return col;
	}

}
