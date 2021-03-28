package net.runelite.client.plugins.fightcavespawnrotation.overlay;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.client.plugins.fightcavespawnrotation.FightCavesSpawnRotationConfig;
import net.runelite.client.plugins.fightcavespawnrotation.FightCavesSpawnRotationPlugin;
import static net.runelite.client.plugins.fightcavespawnrotation.FightCavesSpawnRotationPlugin.TZHAAR_REGION;
import net.runelite.client.plugins.fightcavespawnrotation.util.FightCavesStartLocations;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.components.LineComponent;

public class FightCavesRotationOverlay extends OverlayPanel
{
	private final FightCavesSpawnRotationPlugin plugin;
	private final FightCavesSpawnRotationConfig config;

	@Inject
	private FightCavesRotationOverlay(FightCavesSpawnRotationPlugin plugin, FightCavesSpawnRotationConfig config)
	{
		this.plugin = plugin;
		this.config = config;
		setPriority(OverlayPriority.HIGH);
		setPosition(OverlayPosition.BOTTOM_LEFT);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!plugin.inRegion(TZHAAR_REGION) || !config.outsideCavesInfoOverlay())
		{
			return null;
		}
		int rotation = plugin.getRotationTime();
		panelComponent.setPreferredSize(new Dimension(150, 0));
		panelComponent.getChildren().add(LineComponent.builder().left("Current Rotation:").leftColor(Color.WHITE).right(Integer.toString(FightCavesStartLocations.translateRotation(rotation))).rightColor(Color.GREEN).build());
		panelComponent.getChildren().add(LineComponent.builder().left("Time Until Next Rotation: ").leftColor(Color.WHITE).right((60 - plugin.getUTCTime().getSecond()) + "s").rightColor(Color.GREEN).build());
		panelComponent.getChildren().add(LineComponent.builder().left("Next Rotation: ").leftColor(Color.WHITE).right(rotation + 1 > 15 ? "4" : Integer.toString(FightCavesStartLocations.translateRotation(rotation + 1))).rightColor(Color.GREEN).build());
		return super.render(graphics);
	}
}