package net.runelite.client.plugins.socketsotetsegextended;


import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.IntStream;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.OverlayUtil;

public class SocketSotetsegExtendedOverlay extends Overlay
{
	private final Client client;
	private final SocketSotetsegExtendedConfig config;
	private final SocketSotetsegExtendedPlugin plugin;

	@Inject
	private SocketSotetsegExtendedOverlay(Client client, SocketSotetsegExtendedPlugin plugin, SocketSotetsegExtendedConfig config)
	{
		super(plugin);
		this.client = client;
		this.plugin = plugin;
		this.config = config;
		determineLayer();
		setPosition(OverlayPosition.DYNAMIC);
		setPriority(OverlayPriority.HIGH);
	}

	private void drawPoints(Graphics2D graphics, ArrayList<Point> points, int index)
	{
		WorldPoint player = Objects.requireNonNull(client.getLocalPlayer()).getWorldLocation();

		IntStream.range(0, points.size()).forEach(i -> {
			Point p = points.get(i);
			WorldPoint wp = WorldPoint.fromRegion(player.getRegionID(), p.getX(), p.getY(), player.getPlane());
			LocalPoint lp = LocalPoint.fromWorld(client, wp);
			if (config.numbersOn() && index < i)
			{
				try
				{
					Point textPoint = Perspective.getCanvasTextLocation(client, graphics, lp, String.valueOf(i), 0);
					Point canvasCenterPoint = new Point(textPoint.getX(), (int) ((double) textPoint.getY() + Math.floor((double) config.getFontSize() / 2.0D)));
					OverlayUtil.renderTextLocation(graphics, canvasCenterPoint, String.valueOf(i), Color.WHITE);
				}
				catch (NullPointerException ignored)
				{
				}
			}
			if (config.highlightTiles() && index < i)
			{
				try
				{
					Polygon poly = Perspective.getCanvasTilePoly(client, lp);
					if (poly != null)
					{
						Color colorTile = config.getHighlightTileOutline();
						graphics.setColor(colorTile);
						Stroke originalStroke = graphics.getStroke();
						int strokeSize = 1;
						graphics.setStroke(new BasicStroke(strokeSize));
						if (i < points.size())
						{
							graphics.draw(poly);

							int alpha = Math.min(Math.max(35, 0), 255);
							Color realFill = new Color(colorTile.getRed(), colorTile.getGreen(), colorTile.getBlue(), alpha);
							graphics.setColor(realFill);
							graphics.fill(poly);

							graphics.setStroke(originalStroke);
						}
					}
				}
				catch (NullPointerException ignored)
				{
				}
			}
		});

	}

	public Dimension render(Graphics2D graphics)
	{
		if (plugin.isSotetsegActive())
		{

			if (!plugin.isChosen())
			{
				drawPoints(graphics, plugin.getMazeSolved(), plugin.getMazeSolvedIndex());
			}

			String text;
			int width;
			int drawX;
			int drawY;

			if (plugin.getMovementCount() != -1 && !plugin.isFlashScreen())
			{
				try
				{
					text = String.valueOf(plugin.getMovementCount());
					width = graphics.getFontMetrics().stringWidth(text);
					drawX = client.getViewportWidth() / 2 - width / 2;
					drawY = client.getViewportHeight() - client.getViewportHeight() / 2 - 30;
					OverlayUtil.renderTextLocation(graphics, new Point(drawX, drawY), text, Color.WHITE);
				}
				catch (NullPointerException ignored)
				{
				}
			}

			if (plugin.isMazeActive())
			{
				try
				{
					text = String.valueOf(plugin.getInstanceTime());
					width = graphics.getFontMetrics().stringWidth(text);
					Point base = Perspective.localToCanvas(client, plugin.getSotetsegNPC().getLocalLocation(), client.getPlane(), plugin.getSotetsegNPC().getLogicalHeight());
					Point actual = new Point(base.getX() - width / 2, base.getY() + 100);
					OverlayUtil.renderTextLocation(graphics, actual, text, Color.white);
				}
				catch (NullPointerException ignored)
				{
				}
			}
		}

		return null;
	}

	public void determineLayer()
	{
		if (config.mirrorMode())
		{
			setLayer(OverlayLayer.AFTER_MIRROR);
		}
	}
}
