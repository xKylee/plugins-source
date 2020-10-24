package net.runelite.client.plugins.godbook;


import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.PanelComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;
import javax.inject.Inject;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.stream.IntStream;

class GodbookOverlay extends Overlay
{

	private final GodbookPlugin plugin;
	private final PanelComponent panelComponent = new PanelComponent();

	@Inject
	private GodbookOverlay(GodbookPlugin plugin)
	{
		setPosition(OverlayPosition.ABOVE_CHATBOX_RIGHT);
		this.plugin = plugin;
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (plugin.isActive())
		{
			panelComponent.getChildren().clear();
			String overlayTitle = "Tick:";

			panelComponent.getChildren().add(TitleComponent.builder().text(overlayTitle).color(Color.green).build());
			int max = 0;
			for (int i = 0; i < plugin.getNames().size(); i++)
			{
				int width = graphics.getFontMetrics().stringWidth(plugin.getNames().get(i)) + graphics.getFontMetrics().stringWidth(overlayTitle);
				if (width > max)
				{
					max = width;
				}
			}

			panelComponent.setPreferredSize(new Dimension(max + 10, 0));

			IntStream.range(0, plugin.getTicks().size())
					.forEach(i -> panelComponent.getChildren().add(LineComponent.builder().left(plugin.getNames().get(i)).right(Integer.toString(plugin.getTicks().get(i))).build()));
		}
		return panelComponent.render(graphics);
	}
}
