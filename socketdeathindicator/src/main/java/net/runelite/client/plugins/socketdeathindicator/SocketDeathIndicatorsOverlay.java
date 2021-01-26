package net.runelite.client.plugins.socketdeathindicator;

import net.runelite.api.Actor;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.OverlayUtil;
import javax.inject.Inject;
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
		determineLayer();
		setPosition(OverlayPosition.DYNAMIC);
		setPriority(OverlayPriority.HIGH);

	}

	public Dimension render(Graphics2D graphics)
	{
		if (config.showOutline())
		{
			plugin.getDeadNylos().stream()
					.map(Actor::getConvexHull)
					.forEach(objectClickbox -> OverlayUtil.renderPolygon(graphics, objectClickbox, Color.RED));
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

			Shape objectClickbox = plugin.getMaidenNPC().npc.getConvexHull();
			OverlayUtil.renderPolygon(graphics, objectClickbox, maidenColor);
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
