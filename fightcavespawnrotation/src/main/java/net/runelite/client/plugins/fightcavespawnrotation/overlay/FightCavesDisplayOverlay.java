package net.runelite.client.plugins.fightcavespawnrotation.overlay;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.util.List;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.fightcavespawnrotation.util.FightCavesNpc;
import net.runelite.client.plugins.fightcavespawnrotation.util.FightCavesNpcSpawn;
import net.runelite.client.plugins.fightcavespawnrotation.util.FightCavesSpawnLocations;
import net.runelite.client.plugins.fightcavespawnrotation.FightCavesSpawnRotationConfig;
import net.runelite.client.plugins.fightcavespawnrotation.FightCavesSpawnRotationPlugin;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.OverlayUtil;
import org.jetbrains.annotations.Nullable;

public class FightCavesDisplayOverlay extends Overlay
{
	private final Client client;
	private final FightCavesSpawnRotationPlugin plugin;
	private final FightCavesSpawnRotationConfig config;

	@Inject
	private FightCavesDisplayOverlay(Client client, FightCavesSpawnRotationPlugin plugin, FightCavesSpawnRotationConfig config)
	{
		this.client = client;
		this.plugin = plugin;
		this.config = config;
		setPriority(OverlayPriority.HIGH);
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	public Dimension render(Graphics2D graphics)
	{
		if (plugin.inRegion(FightCavesSpawnRotationPlugin.TZHAAR_REGION) && config.displayMode() != FightCavesSpawnRotationConfig.DisplayMode.OFF && FightCavesSpawnRotationPlugin.getCurrentWave() > 0)
		{
			int wave = FightCavesSpawnRotationPlugin.getCurrentWave() - 1;
			List<FightCavesNpcSpawn> currentWaveContents = FightCavesSpawnRotationPlugin.getWaveData().get(wave);
			switch (config.displayMode())
			{
				case CURRENT_WAVE:
					renderWaveContents(graphics, currentWaveContents, config.currentWaveColor());
					break;
				case NEXT_WAVE:
					if (wave == 0)
					{
						renderWaveContents(graphics, currentWaveContents, config.currentWaveColor());
					}

					if (wave != 63)
					{
						renderWaveContents(graphics, FightCavesSpawnRotationPlugin.getWaveData().get(wave + 1), config.nextWaveColor());
					}
					break;
				case BOTH:
					renderWaveContents(graphics, currentWaveContents, config.currentWaveColor());
					if (wave != 63)
					{
						renderWaveContents(graphics, FightCavesSpawnRotationPlugin.getWaveData().get(wave + 1), config.nextWaveColor());
					}
					break;
			}
		}
		return null;
	}

	private void renderWaveContents(Graphics2D graphics, List<FightCavesNpcSpawn> waveContents, Color color)
	{
		waveContents.forEach((fightCaveNpc) -> {
			FightCavesNpc fightCavesNpc = fightCaveNpc.getFightCavesNpc();
			String name = fightCavesNpc.getName();
			int size = fightCavesNpc.getSize();
			FightCavesSpawnLocations fightCavesSpawnLocations = FightCavesSpawnLocations.lookup(fightCaveNpc.getSpawnLocation());
			if (fightCavesSpawnLocations != null)
			{
				LocalPoint localPoint = getCenterLocalPoint(fightCavesSpawnLocations.getRegionX(), fightCavesSpawnLocations.getRegionY(), size);
				if (localPoint != null)
				{
					Polygon poly = Perspective.getCanvasTileAreaPoly(client, localPoint, size);
					renderPolygon(graphics, poly, color);
					Point textLocation = Perspective.getCanvasTextLocation(client, graphics, localPoint, name, 0);
					if (textLocation != null)
					{
						OverlayUtil.renderTextLocation(graphics, textLocation, name, config.colorSpawnOverlays() ? color : Color.WHITE);
					}

				}
			}
		});
	}

	private LocalPoint getCenterLocalPoint(int regionX, int regionY, int size)
	{
		LocalPoint localPoint = LocalPoint.fromWorld(client, WorldPoint.fromRegion(client.getLocalPlayer().getWorldLocation().getRegionID(), regionX, regionY, client.getPlane()));
		return localPoint == null ? null : new LocalPoint(localPoint.getX() + (size - 1) * 64, localPoint.getY() + (size - 1) * 64);
	}

	private void renderPolygon(Graphics2D graphics, @Nullable Shape poly, @Nonnull Color color)
	{
		if (poly != null)
		{
			graphics.setColor(color);
			graphics.setStroke(new BasicStroke(config.overlayStrokeSize()));
			graphics.draw(poly);
			graphics.setColor(new Color(0, 0, 0, 50));
			graphics.fill(poly);
		}
	}
}