/*
 * THIS PLUGIN WAS WRITTEN BY A KEYBOARD-WIELDING MONKEY BOI BUT SHUFFLED BY A KANGAROO WITH THUMBS.
 * The plugin and it's refactoring was intended for xKylee's Externals but I'm sure if you're reading this, you're probably planning to yoink..
 * or you're just genuinely curious. If you're trying to yoink, it doesn't surprise me.. just don't claim it as your own. Cheers.
 */

package net.runelite.client.plugins.theatre.Nylocas;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.util.Map;
import javax.inject.Inject;
import net.runelite.api.NPC;
import net.runelite.api.Perspective;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.theatre.RoomOverlay;
import net.runelite.client.plugins.theatre.TheatreConfig;
import net.runelite.client.ui.overlay.OverlayLayer;

public class NylocasOverlay extends RoomOverlay
{
	@Inject
	private Nylocas nylocas;

	@Inject
	protected NylocasOverlay(TheatreConfig config)
	{
		super(config);
		determineLayer();
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (nylocas.isInstanceTimerRunning() && nylocas.isInNyloRegion() && config.nyloInstanceTimer())
		{
			Player player = client.getLocalPlayer();
			if (player != null)
			{
				Point point = player.getCanvasTextLocation(graphics, "#", player.getLogicalHeight() + 60);
				if (point != null)
				{
					renderTextLocation(graphics, String.valueOf(nylocas.getInstanceTimer()), Color.CYAN, point);
				}
			}
		}

		if (nylocas.isNyloBossAlive())
		{
			String text = "";
			if (config.nyloBossAttackTickCount() && nylocas.getNyloBossAttackTickCount() >= 0)
			{
				text += "[A] " + nylocas.getNyloBossAttackTickCount();
				if (config.nyloBossSwitchTickCount() || config.nyloBossTotalTickCount())
				{
					text += " : ";
				}
			}

			if (config.nyloBossSwitchTickCount() && nylocas.getNyloBossSwitchTickCount() >= 0)
			{
				text += "[S] " + nylocas.getNyloBossSwitchTickCount();
				if (config.nyloBossTotalTickCount() && nylocas.getNyloBossTotalTickCount() >= 0)
				{
					text += " : ";
				}
			}

			if (config.nyloBossTotalTickCount() && nylocas.getNyloBossTotalTickCount() >= 0)
			{
				text += "(" + nylocas.getNyloBossTotalTickCount() + ")";
			}

			Point canvasPoint = nylocas.getNyloBossNPC().getCanvasTextLocation(graphics, text, 50);
			renderTextLocation(graphics, text, Color.WHITE, canvasPoint);
		}

		if (nylocas.isNyloActive())
		{
			if (config.nyloPillars())
			{
				Map<NPC, Integer> pillars = nylocas.getNylocasPillars();

				for (NPC npc : pillars.keySet())
				{
					final int health = pillars.get(npc);
					final String healthStr = health + "%";

					WorldPoint p = npc.getWorldLocation();
					LocalPoint lp = LocalPoint.fromWorld(client, p.getX() + 1, p.getY() + 1);

					final double rMod = 130.0 * health / 100.0;
					final double gMod = 255.0 * health / 100.0;
					final double bMod = 125.0 * health / 100.0;

					final Color c = new Color((int) (255 - rMod), (int) (0 + gMod), (int) (0 + bMod));

					if (lp != null)
					{
						Point canvasPoint = Perspective.localToCanvas(client, lp, client.getPlane(),
							65);
						renderTextLocation(graphics, healthStr, c, canvasPoint);
					}
				}
			}

			final Map<NPC, Integer> npcMap = nylocas.getNylocasNpcs();

			for (NPC npc : npcMap.keySet())
			{
				if (config.nyloAggressiveOverlay() && nylocas.getAggressiveNylocas().contains(npc) && !npc.isDead())
				{
					if (config.nyloAggressiveOverlayStyle() == TheatreConfig.AGGRESSIVENYLORENDERSTYLE.TILE)
					{
						LocalPoint lp = npc.getLocalLocation();
						if (lp != null)
						{
							Polygon poly = getCanvasTileAreaPoly(client, lp, npc.getDefinition().getSize(), -25);
							renderPoly(graphics, Color.RED, poly, 1);
						}
					}
					else if (config.nyloAggressiveOverlayStyle() == TheatreConfig.AGGRESSIVENYLORENDERSTYLE.HULL)
					{
						Shape objectClickbox = npc.getConvexHull();
						if (objectClickbox != null)
						{
							Color color = Color.RED;
							graphics.setColor(color);
							graphics.setStroke(new BasicStroke(2));
							graphics.draw(objectClickbox);
							graphics.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 20));
							graphics.fill(objectClickbox);
						}
					}

				}

				int ticksLeft = npcMap.get(npc);
				if (ticksLeft > -1 && ticksLeft <= config.nyloExplosionDisplayTicks())
				{
					if (config.nyloTimeAlive() && !npc.isDead())
					{
						int ticksAlive = ticksLeft;
						if (config.nyloTimeAliveCountStyle() == TheatreConfig.NYLOTIMEALIVE.COUNTUP)
						{
							ticksAlive = 52 - ticksLeft;
						}
						Point textLocation = npc.getCanvasTextLocation(graphics, String.valueOf(ticksAlive), 60);
						if (textLocation != null)
						{
							if (config.nyloExplosionOverlayStyle() == TheatreConfig.EXPLOSIVENYLORENDERSTYLE.RECOLOR_TICK
									&& config.nyloExplosions() && ticksLeft <= 6)
							{
								renderTextLocation(graphics, String.valueOf(ticksAlive), Color.RED, textLocation);
							}
							else
							{
								renderTextLocation(graphics, String.valueOf(ticksAlive), Color.WHITE, textLocation);
							}
						}
					}

					if (config.nyloExplosions() && ticksLeft <= 6)
					{
						if (config.nyloExplosionOverlayStyle() == TheatreConfig.EXPLOSIVENYLORENDERSTYLE.TILE)
						{
							LocalPoint lp = npc.getLocalLocation();
							if (lp != null)
							{
								renderPoly(graphics, Color.YELLOW, getCanvasTileAreaPoly(client, lp, npc.getDefinition().getSize(), -15), 1);
							}
						}
					}
				}

				String name = npc.getName();

				if (config.nyloHighlightOverlay() && !npc.isDead())
				{
					LocalPoint lp = npc.getLocalLocation();
					if (lp != null)
					{
						if (config.getHighlightMeleeNylo() && "Nylocas Ischyros".equals(name))
						{
							renderPoly(graphics, new Color(255, 188, 188), Perspective.getCanvasTileAreaPoly(client, lp, npc.getDefinition().getSize()), 1);
						}
						else if (config.getHighlightRangeNylo() && "Nylocas Toxobolos".equals(name))
						{
							renderPoly(graphics, Color.GREEN, Perspective.getCanvasTileAreaPoly(client, lp, npc.getDefinition().getSize()), 1);
						}
						else if (config.getHighlightMageNylo() && "Nylocas Hagios".equals(name))
						{
							renderPoly(graphics, Color.CYAN, Perspective.getCanvasTileAreaPoly(client, lp, npc.getDefinition().getSize()), 1);
						}
					}
				}
			}
		}
		return null;
	}

	public void determineLayer()
	{
		setLayer(config.mirrorMode() ? OverlayLayer.AFTER_MIRROR : OverlayLayer.ABOVE_SCENE);
	}
}
