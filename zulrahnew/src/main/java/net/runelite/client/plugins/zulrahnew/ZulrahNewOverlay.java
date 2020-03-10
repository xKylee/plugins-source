package net.runelite.client.plugins.zulrahnew;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.HeadIcon;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.components.PanelComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

public class ZulrahNewOverlay extends Overlay
{
	private final ZulrahNewConfig config;
	private final ZulrahNewPlugin plugin;
	private final PanelComponent panelComponent = new PanelComponent();

	@Inject
	private Client client;

	@Inject
	private ZulrahNewOverlay(ZulrahNewConfig config, ZulrahNewPlugin plugin)
	{
		this.config = config;
		this.plugin = plugin;
		setLayer(OverlayLayer.ABOVE_SCENE);
		setPosition(OverlayPosition.DYNAMIC);
		setPriority(OverlayPriority.MED);
		panelComponent.setPreferredSize(new Dimension(150, 0));
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!config.EnableZulrahPrayerHelper())
		{
			return null;
		}
		NPC Zulrah = plugin.Zulrah;
		if (Zulrah != null)
		{
			if (plugin.prayerconserve && plugin.nextprayerendticks == 0)
			{
				Player player = client.getLocalPlayer();
				HeadIcon icon = player.getOverheadIcon();
				if (icon != null)
				{
					final String text = "Disable Overhead Prayer";
					final int textWidth = graphics.getFontMetrics().stringWidth(text);
					final int textHeight = graphics.getFontMetrics().getAscent() - graphics.getFontMetrics().getDescent();
					final int width = (int) client.getRealDimensions().getWidth();
					java.awt.Point jpoint = new java.awt.Point((width / 2) - textWidth, textHeight + 75);
					panelComponent.getChildren().clear();
					panelComponent.getChildren().add(TitleComponent.builder().text(text).color(Color.RED).build());
					panelComponent.setPreferredLocation(jpoint);
					panelComponent.render(graphics);
				}
			}
			else if (plugin.nextprayerendticks != 0)
			{
				Player player = client.getLocalPlayer();
				HeadIcon icon = player.getOverheadIcon();
				if (icon == null)
				{
					final String text = "Protect from MAGIC: " + (plugin.nextprayerendticks - plugin.ticks);
					final int textWidth = graphics.getFontMetrics().stringWidth(text);
					final int textHeight = graphics.getFontMetrics().getAscent() - graphics.getFontMetrics().getDescent();
					final int width = (int) client.getRealDimensions().getWidth();
					java.awt.Point jpoint = new java.awt.Point((width / 2) - textWidth, textHeight + 75);
					panelComponent.getChildren().clear();
					panelComponent.getChildren().add(TitleComponent.builder().text(text).color(Color.GREEN).build());
					panelComponent.setPreferredLocation(jpoint);
					panelComponent.render(graphics);
				}
			}
		}
		return null;
	}
}