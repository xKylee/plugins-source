package net.runelite.client.plugins.socketdeathindicator;

import net.runelite.api.NPC;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.OverlayUtil;
import javax.inject.Inject;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Shape;

public class SocketDeathIndicatorsOverlay extends Overlay
{

	private final SocketDeathIndicatorsConfig config;
	private final SocketDeathIndicatorPlugin plugin;

	@Inject
	public SocketDeathIndicatorsOverlay(SocketDeathIndicatorPlugin plugin, SocketDeathIndicatorsConfig config)
	{
		super(plugin);
		this.plugin = plugin;
		this.config = config;
		setPosition(OverlayPosition.DYNAMIC);
		setPriority(OverlayPriority.HIGH);
		setLayer(OverlayLayer.ABOVE_SCENE);

	}

	public Dimension render(Graphics2D graphics)
	{
		if (config.showOutline())
		{
			for (NPC n : plugin.getDeadNylos())
			{
				Shape objectClickbox = n.getConvexHull();
				renderPoly(graphics, Color.red, objectClickbox);
			}
		}

		if (plugin.getMaidenNPC() != null && config.maidenMarkers())
		{
			Color maidenColor;
			switch (plugin.getMaidenNPC().phase)
			{
				case 0:
					maidenColor = Color.WHITE;
					break;
				case 1:
					maidenColor = Color.BLACK;
					break;
				case 2:
					maidenColor = Color.GREEN;
					break;
				default:
					maidenColor = Color.BLUE;
					break;
			}

			if (plugin.getMaidenNPC() != null && plugin.getMaidenNPC().npc != null && plugin.getMaidenNPC().npc.getConvexHull() != null)
			{
				Shape objectClickbox = plugin.getMaidenNPC().npc.getConvexHull();
				OverlayUtil.renderPolygon(graphics, objectClickbox, maidenColor);
			}

		}

		return null;
	}

	private void renderPoly(Graphics2D graphics, Color color, Shape polygon)
	{
		if (polygon != null)
		{
			graphics.setColor(color);
			graphics.setStroke(new BasicStroke(2));
			graphics.draw(polygon);
			graphics.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 20));
			graphics.fill(polygon);
		}
	}
}
