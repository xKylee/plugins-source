package net.runelite.client.plugins.nightmare;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.Area;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.GraphicsObject;
import net.runelite.api.NPC;
import net.runelite.api.Perspective;
import static net.runelite.api.Perspective.getCanvasTileAreaPoly;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.OverlayUtil;

@Singleton
class NightmareOverlay extends Overlay
{
	private static final int NIGHTMARE_REGION_ID = 15256;
	private final Client client;
	private final NightmarePlugin plugin;
	private final NightmareConfig config;

	// Nightmare's NPC IDs
	private static final int NIGHTMARE_PHASE1 = 9425;
	private static final int NIGHTMARE_PHASE2 = 9426;
	private static final int NIGHTMARE_PHASE3 = 9427;
	private static final int NIGHTMARE_PILLAR1 = 9428;
	private static final int NIGHTMARE_PILLAR2 = 9429;
	private static final int NIGHTMARE_PHASE10Z = 9432;


	// Non-Nightmare Objects
	private static final int NIGHTMARE_WALKER_1 = 9446;
	private static final int NIGHTMARE_WALKER_2 = 9447;
	private static final int NIGHTMARE_WALKER_3 = 9448;
	private static final int NIGHTMARE_WALKER_4 = 9449;
	private static final int NIGHTMARE_WALKER_5 = 9450;
	private static final int NIGHTMARE_WALKER_6 = 9451;
	private static final int NIGHTMARE_PARASITE = 9452;
	private static final int NIGHTMARE_HUSK = 9454;
	private static final int NIGHTMARE_SHADOW = 1767;   // graphics object

	private static final int NIGHTMARE_MUSHROOM = 37739;

	private static final int NM_PRE_REGION = 15256;


	@Inject
	private NightmareOverlay(final Client client, final NightmarePlugin plugin, final NightmareConfig config)
	{
		this.client = client;
		this.plugin = plugin;
		this.config = config;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
		setPriority(OverlayPriority.LOW);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!client.isInInstancedRegion() || !plugin.isInFight())
		{
			return null;
		}

		if (config.highlightShadows())
		{
			for (GraphicsObject graphicsObject : client.getGraphicsObjects())
			{
				Color color;

				if (graphicsObject.getId() == NIGHTMARE_SHADOW)
				{
					color = Color.ORANGE;
				}
				else
				{
					continue;
				}

				LocalPoint lp = graphicsObject.getLocation();
				Polygon poly = Perspective.getCanvasTilePoly(client, lp);

				if (poly != null)
				{
					OverlayUtil.renderPolygon(graphics, poly, color);
				}
			}
		}

		int ticksUntilNext = plugin.getTicksUntilNextAttack();
		if (config.ticksCounter() && ticksUntilNext > 0 && plugin.getNm() != null)
		{
			String str = Integer.toString(ticksUntilNext);

			LocalPoint lp = plugin.getNm().getLocalLocation();
			Point point = Perspective.getCanvasTextLocation(client, graphics, lp, str, 0);

			Color tickColor = Color.WHITE;

			NightmareAttack nextAttack = plugin.getPendingNightmareAttack();
			if (ticksUntilNext >= 4 && nextAttack != null)
			{
				tickColor = nextAttack.getTickColor();
			}

			renderTextLocation(graphics, str, 20, Font.BOLD, tickColor, point);
		}

		int ticksUntilNextParasite = plugin.getTicksUntilParasite();
		if (config.showTicksUntilParasite() && ticksUntilNextParasite > 0)
		{
			String str = Integer.toString(ticksUntilNextParasite);

			for (Player player : plugin.getParasiteTargets().values())
			{
				LocalPoint lp = player.getLocalLocation();
				Point point = Perspective.getCanvasTextLocation(client, graphics, lp, str, 0);

				renderTextLocation(graphics, str, 14, Font.BOLD, Color.RED, point);
			}
		}

		if (config.highlightTotems())
		{
			for (MemorizedTotem totem : plugin.getTotems().values())
			{
				if (totem.getCurrentPhase().isActive())
				{
					renderNpcOverlay(graphics, totem.getNpc(), totem.getCurrentPhase().getColor());
				}
			}
		}

		if (config.highlightSpores())
		{
			drawPoisonArea(graphics, plugin.getSpores());
		}

		return null;
	}

	protected void renderTextLocation(Graphics2D graphics, String txtString, int fontSize, int fontStyle, Color fontColor, Point canvasPoint)
	{
		graphics.setFont(new Font("Arial", fontStyle, fontSize));
		if (canvasPoint != null)
		{
			final Point canvasCenterPoint = new Point(canvasPoint.getX(), canvasPoint.getY());
			final Point canvasCenterPointShadow = new Point(canvasPoint.getX() + 1, canvasPoint.getY() + 1);

			OverlayUtil.renderTextLocation(graphics, canvasCenterPointShadow, txtString, Color.BLACK);
			OverlayUtil.renderTextLocation(graphics, canvasCenterPoint, txtString, fontColor);
		}
	}

	private void renderNpcOverlay(Graphics2D graphics, NPC actor, Color color)
	{
		final Shape objectClickbox = actor.getConvexHull();
		graphics.setColor(color);
		graphics.draw(objectClickbox);
	}

	private void drawPoisonArea(Graphics2D graphics, Map<LocalPoint, GameObject> spores)
	{
		if (spores.size() < 1)
		{
			return;
		}

		Area poisonTiles = new Area();

		for (Map.Entry<LocalPoint, GameObject> entry : spores.entrySet())
		{
			LocalPoint point = entry.getKey();
			Polygon poly = getCanvasTileAreaPoly(client, point, 3);

			if (poly != null)
			{
				poisonTiles.add(new Area(poly));
			}
		}

		graphics.setPaintMode();
		graphics.setColor(config.poisonBorderCol());
		graphics.draw(poisonTiles);
		graphics.setColor(config.poisonCol());
		graphics.fill(poisonTiles);
	}
}