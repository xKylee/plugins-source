package net.runelite.client.plugins.socketsotetseg;

import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;

public class MazeTrueTileOverlay extends Overlay
{

	private final Client client;
	private final SotetsegPlugin plugin;
	private final SotetsegConfig config;

	@Inject
	private MazeTrueTileOverlay(Client client, SotetsegPlugin plugin, SotetsegConfig config)
	{
		this.client = client;
		this.plugin = plugin;
		this.config = config;

		setPosition(OverlayPosition.DYNAMIC);
		setPriority(OverlayPriority.HIGHEST);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (config.trueMaze())
		{
			if (plugin.isMazeActive())
			{
				WorldPoint playerPos = client.getLocalPlayer().getWorldLocation();
				if (playerPos == null) return null;
				LocalPoint playerPosLocal = LocalPoint.fromWorld(client, playerPos);
				if (playerPosLocal == null) return null;
				renderTile(graphics, playerPosLocal, Color.red);
			}

		}
		return null;
	}

	private void renderTile(Graphics2D graphics, @Nonnull LocalPoint dest, Color color)
	{
		Polygon poly = Perspective.getCanvasTilePoly(client, dest);
		if (poly == null) return;
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		graphics.setColor(color);
		graphics.setStroke(new BasicStroke(2));
		graphics.draw(poly);
		graphics.setColor(new Color(0, 0, 0, 50));

	}
}
