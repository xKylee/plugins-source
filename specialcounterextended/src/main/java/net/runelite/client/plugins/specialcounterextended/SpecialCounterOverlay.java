package net.runelite.client.plugins.specialcounterextended;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.OverlayUtil;

public class SpecialCounterOverlay extends Overlay
{

	private final Client client;
	private final SpecialCounterExtendedPlugin plugin;
	private final SpecialCounterExtendedConfig config;

	private Map<String, ArrayList<SpecialIcon>> drawings = new HashMap<>();

	@Inject
	private SpecialCounterOverlay(Client client, SpecialCounterExtendedPlugin plugin, SpecialCounterExtendedConfig config)
	{
		this.client = client;
		this.plugin = plugin;
		this.config = config;

		setPosition(OverlayPosition.DYNAMIC);
		setPriority(OverlayPriority.HIGH);
		setLayer(OverlayLayer.ALWAYS_ON_TOP);
	}

	public void addOverlay(String player, SpecialIcon icon)
	{
		if (this.config.showHitOverlay())
		{
			ArrayList<SpecialIcon> icons = new ArrayList<>();
			if (this.drawings.containsKey(player))
			{
				icons = this.drawings.get(player);
			}
			icons.add(icon);
			this.drawings.put(player, icons);
		}
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		ArrayList<String> removePlayers = new ArrayList<>();

		Map<String, LocalPoint> locations = new HashMap<>();
		for (Player player : this.client.getPlayers())
		{
			locations.put(player.getName(), player.getLocalLocation());
		}

		for (String playerName : this.drawings.keySet())
		{
			LocalPoint center = locations.get(playerName);
			if (center != null)
			{
				ArrayList<SpecialIcon> icons = this.drawings.get(playerName);
				ArrayList<SpecialIcon> removeIcons = new ArrayList<>();
				int currentHeight = 200; // Base height for a player.

				for (int i = (icons.size() - 1); i >= 0; i--)
				{
					SpecialIcon icon = icons.get(i);

					long elapsedTime = System.currentTimeMillis() - icon.getStartTime();
					final int fadeDelay = Math.max(config.getFadeDelay(), 1);
					long timeRemaining = fadeDelay - elapsedTime;

					if (timeRemaining <= 0)
					{
						removeIcons.add(icon);
						continue;
					}

					float opacity = ((float) timeRemaining) / ((float) fadeDelay);
					float thresh = Math.min(opacity + 0.2f, 1.0f);
					graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, thresh));

					final int maxHeight = Math.max(config.getMaxHeight(), 1);
					int updatedHeight = maxHeight - (int) (((float) maxHeight) * thresh);

					Point drawPoint = Perspective.getCanvasImageLocation(this.client, center, icon.getImage(), currentHeight + updatedHeight);
					graphics.drawImage(icon.getImage(), drawPoint.getX(), drawPoint.getY(), null);

					if (icon.getText() != null)
					{
						Point textPoint = Perspective.getCanvasTextLocation(this.client, graphics, center, icon.getText(), currentHeight + updatedHeight);
						graphics.setFont(new Font("Arial", Font.BOLD, 16));

						final Point canvasCenterPoint = new Point(textPoint.getX(), textPoint.getY());
						final Point canvasCenterPointShadow = new Point(textPoint.getX() + 1, textPoint.getY() + 1);

						OverlayUtil.renderTextLocation(graphics, canvasCenterPointShadow, icon.getText(), Color.BLACK);
						OverlayUtil.renderTextLocation(graphics, canvasCenterPoint, icon.getText(), Color.WHITE);
					}

					currentHeight += (icon.getImage().getHeight() * 2);
				}

				for (SpecialIcon icon : removeIcons)
				{
					icons.remove(icon);
				}

				if (icons.isEmpty())
				{
					removePlayers.add(playerName);
				}
			}
			else
			{
				removePlayers.add(playerName);
			}
		}

		for (String playerName : removePlayers)
		{
			this.drawings.remove(playerName);
		}

		return null;
	}
}