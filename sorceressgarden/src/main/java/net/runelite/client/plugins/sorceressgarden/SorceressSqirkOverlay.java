package net.runelite.client.plugins.sorceressgarden;

import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Skill;
import net.runelite.client.plugins.xptracker.XpTrackerService;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.TitleComponent;
import net.runelite.client.ui.overlay.components.table.TableAlignment;
import net.runelite.client.ui.overlay.components.table.TableComponent;

@Slf4j
public class SorceressSqirkOverlay extends OverlayPanel
{
	final SorceressGardenPlugin plugin;
	private final SorceressGardenConfig config;
	private final SorceressSession sorceressSession;

	@Inject
	XpTrackerService xpTrackerService;

	@Inject
	public SorceressSqirkOverlay(final SorceressGardenPlugin plugin, final SorceressGardenConfig config, final SorceressSession sorceressSession)
	{
		super(plugin);
		setPosition(OverlayPosition.TOP_LEFT);
		this.plugin = plugin;
		this.config = config;
		this.sorceressSession = sorceressSession;
		determineLayer();
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!plugin.isInGarden())
		{
			return null;
		}

		if (config.showGardenStats())
		{
			renderGardenStats(graphics);
		}

		if (config.showSqirksStats())
		{
			renderSqirksStats(graphics);
		}

		return super.render(graphics);
	}

	public void determineLayer()
	{
		setLayer(config.mirrorMode() ? OverlayLayer.AFTER_MIRROR : OverlayLayer.ABOVE_SCENE);
	}

	private void renderGardenStats(Graphics2D graphics)
	{
		TableComponent tableComponent = new TableComponent();
		tableComponent.setColumnAlignments(TableAlignment.LEFT, TableAlignment.RIGHT);

		int farmingActions = xpTrackerService.getActions(Skill.FARMING);
		if (farmingActions > 0)
		{
			panelComponent.getChildren().add(TitleComponent.builder().text("Sorceress' Garden").build());
			tableComponent.addRow("Sq'irks Picked: ", Integer.toString(xpTrackerService.getActions(Skill.FARMING)));
			tableComponent.addRow("Sq'irks/Hr: ", Integer.toString(xpTrackerService.getActionsHr(Skill.FARMING)));
		}

		panelComponent.getChildren().add(tableComponent);
	}

	private void renderSqirksStats(Graphics2D graphics)
	{
		SorceressSession session = sorceressSession;

		int winterSqirks = session.getWinterSqirk();
		int springSqirks = session.getSpringSqirk();
		int autumnSqirks = session.getAutumnSqirk();
		int summerSqirks = session.getSummerSqirk();

		if (winterSqirks == 0 && springSqirks == 0 && autumnSqirks == 0 && summerSqirks == 0)
		{
			return;
		}

		TableComponent tableComponent = new TableComponent();
		tableComponent.setColumnAlignments(TableAlignment.LEFT, TableAlignment.RIGHT);
		panelComponent.getChildren().add(TitleComponent.builder().text("Sq'irks").build());

		if (winterSqirks > 0)
		{
			int xpFromWinter = winterSqirks * 70;
			tableComponent.addRow("Winter Sq'irks: ", winterSqirks + " (" + xpFromWinter + " XP)");
		}
		if (springSqirks > 0)
		{
			double xpFromSpring = springSqirks * 337.5;
			tableComponent.addRow("Spring Sq'irks: ", springSqirks + " (" + xpFromSpring + " XP)");
		}
		if (autumnSqirks > 0)
		{
			double xpFromAutumn = autumnSqirks * 783.3;
			tableComponent.addRow("Autumn Sq'irks: ", autumnSqirks + " (" + xpFromAutumn + " XP)");
		}
		if (summerSqirks > 0)
		{
			int xpFromSummer = summerSqirks * 1500;
			tableComponent.addRow("Summer Sq'irks: ", summerSqirks + " (" + xpFromSummer + " XP)");
		}

		panelComponent.getChildren().add(tableComponent);
	}

}
