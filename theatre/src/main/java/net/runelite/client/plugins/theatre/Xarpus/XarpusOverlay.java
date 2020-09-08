/*
 * THIS PLUGIN WAS WRITTEN BY A KEYBOARD-WIELDING MONKEY BOI BUT SHUFFLED BY A KANGAROO WITH THUMBS.
 * The plugin and it's refactoring was intended for xKylee's Externals but I'm sure if you're reading this, you're probably planning to yoink..
 * or you're just genuinely curious. If you're trying to yoink, it doesn't surprise me.. just don't claim it as your own. Cheers.
 */

package net.runelite.client.plugins.theatre.Xarpus;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Polygon;
import java.awt.BasicStroke;
import javax.inject.Inject;
import net.runelite.api.NPC;
import net.runelite.api.NpcID;
import net.runelite.api.GroundObject;
import net.runelite.api.Perspective;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.plugins.theatre.RoomOverlay;
import net.runelite.client.plugins.theatre.TheatreConfig;
import net.runelite.client.ui.overlay.OverlayLayer;

public class XarpusOverlay extends RoomOverlay
{
	@Inject
	private Xarpus xarpus;

	@Inject
	protected XarpusOverlay(TheatreConfig config)
	{
		super(config);
		determineLayer();
	}

	public Dimension render(Graphics2D graphics)
	{
		if (xarpus.isInstanceTimerRunning() && xarpus.isInXarpusRegion() && config.xarpusInstanceTimer())
		{
			Player player = client.getLocalPlayer();

			if (player != null)
			{
				Point point = player.getCanvasTextLocation(graphics, "#", player.getLogicalHeight() + 60);
				if (point != null)
				{
					renderTextLocation(graphics, String.valueOf(xarpus.getInstanceTimer()), Color.CYAN, point);
				}
			}
		}

		if (xarpus.isXarpusActive())
		{
			NPC boss = xarpus.getXarpusNPC();

			if ((config.xarpusTickP2() && boss.getId() == NpcID.XARPUS_8340)
				|| (config.xarpusTickP3() && boss.getId() == NpcID.XARPUS_8341))
			{
				int tick = xarpus.getXarpusTicksUntilAttack();
				final String ticksLeftStr = String.valueOf(tick);
				Point canvasPoint = boss.getCanvasTextLocation(graphics, ticksLeftStr, 130);
				renderTextLocation(graphics, ticksLeftStr, Color.WHITE, canvasPoint);
			}

			if (config.xarpusExhumed() && boss.getId() == NpcID.XARPUS_8339)
			{
				for (GroundObject o : xarpus.getXarpusExhumeds().keySet())
				{
					Polygon poly = o.getCanvasTilePoly();
					if (poly != null)
					{
						graphics.setColor(new Color(0, 255, 0, 130));
						graphics.setStroke(new BasicStroke(1));
						graphics.draw(poly);

						if (config.xarpusExhumedTick())
						{
							String count = Integer.toString(xarpus.getXarpusExhumeds().get(o) + 1);
							LocalPoint lp = o.getLocalLocation();
							Point point = Perspective.getCanvasTextLocation(client, graphics, lp, count, 0);
							if (point != null)
							{
								renderTextLocation(graphics, count, Color.WHITE, point);
							}
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
