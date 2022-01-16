package net.runelite.client.plugins.nex;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.text.DecimalFormat;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import static net.runelite.api.Perspective.getCanvasTileAreaPoly;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;


@Singleton
@Slf4j
class NexOverlay extends Overlay
{
	private final Client client;
	private final NexPlugin plugin;
	private final NexConfig config;
	private final ModelOutlineRenderer outliner;
	private int timeout;

	private final int TIMEOUT_MAX = 100;
	private final int TIMEOUT_FLASH_START = 50;
	private final int TIMEOUT_FLASH_FINISH = 10;

	private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#0.0");


	@Inject
	private NexOverlay(final Client client, final NexPlugin plugin, final NexConfig config, final ModelOutlineRenderer outliner)
	{
		this.client = client;
		this.plugin = plugin;
		this.config = config;
		this.outliner = outliner;

		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
		setPriority(OverlayPriority.HIGHEST);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!plugin.isInFight() || plugin.getNex() == null)
		{
			return null;
		}

		// handle render death blob before we break because dead
		if (config.indicateDeathAOE() && plugin.getNexDeathTile() != null)
		{
			drawTileAOE(
				graphics,
				plugin.getNexDeathTile(),
				7,
				config.indicateDeathAOEColor(),
				plugin.getNexDeathTileTicks().getTicks(),
				true
			);
		}

		if (plugin.getNex().isDead())
		{
			return null;
		}

		if (config.indicateTank())
		{
			drawTank(graphics);
		}

		if (config.indicateContainAOE() && plugin.getContainTrapTicks().isActive())
		{
			drawObjectTickable(
				graphics,
				plugin.getContainTrapTicks().getTicks(),
				plugin.getContainThisSpawns(),
				10000,
				config.indicateContainAOEColor(),
				config.drawTicksOnContain()
			);
		}

		if (config.shadowsIndicator())
		{
			drawObjectTickable(
				graphics,
				plugin.getShadowsTicks().getTicks(),
				plugin.getShadows(),
				config.shadowsRenderDistance(),
				config.shadowsColorBase(),
				config.shadowsTickCounter()
			);
		}

		if (config.drawIceTraps())
		{
			drawObjectTickable(
				graphics,
				plugin.getIceTrapTicks().getTicks(),
				plugin.getIceTraps(),
				10000,
				config.iceColorBase(),
				config.drawTicksOnIceTrap()
			);
		}

		if (config.coughTileIndicator() && !plugin.getCoughingPlayers().isEmpty())
		{
			drawInfectionArea(graphics);
		}

		if (config.healthyTileIndicator() && plugin.getSelfCoughingPlayer() != null)
		{
			drawHealthyPlayers(graphics);
		}

		if (config.indicateSacrificeAOE() && plugin.getBloodSacrificeTicks().isActive())
		{
			drawAreaTiles(graphics, plugin.getBloodSacrificeSafeTiles(), config.indicateSacrificeAOEColor(), 1, false, 0);
		}

		if (config.indicateNexRange() && plugin.getDrawRangeCoolDown().isExpired())
		{
			drawAreaTiles(graphics, plugin.getNexRangeTiles(), config.indicateNexRangeColor(), 1, false, 0);
		}

		if (config.drawDashLane() && plugin.getAirplaneCoolDown().isActive())
		{
			drawAreaTiles(graphics, plugin.getDashLaneTiles(), config.drawDashLaneColor(), 3, true, plugin.getAirplaneCoolDown().getTicks());
		}

		if (plugin.nexDisable())
		{
			if (config.indicateNexVulnerability().showInvulnerable())
			{
				outliner.drawOutline(plugin.getNex(), config.invulnerableWidth(), config.invulnerableColor(), config.outlineFeather());
			}

			if (config.indicateInvulnerableNexTicks() && plugin.getNexTicksUntilClick().isActive())
			{
				graphics.setFont(new Font("Arial", Font.BOLD, config.indicateInvulnerableNexTicksFontSize()));
				var text = String.valueOf(plugin.getNexTicksUntilClick());
				var color = plugin.getNexTicksUntilClick().getTicks() == 1 ? Color.WHITE : Color.LIGHT_GRAY;

				final Point canvasPoint = plugin.getNex().getCanvasTextLocation(
					graphics, text, 0);

				OverlayUtil.renderTextLocation(graphics, canvasPoint, text, color);
			}
		}
		else if (config.indicateNexVulnerability().showVulnerable() && !plugin.nexDisable())
		{
			outliner.drawOutline(plugin.getNex(), config.vulnerableWidth(), config.vulnerableColor(), config.outlineFeather());
		}

		if (config.indicateMinionVulnerability().showInvulnerable() && !plugin.nexDisable())
		{
			drawMinion(graphics, false);
		}
		else if (config.indicateMinionVulnerability().showVulnerable() && plugin.nexDisable())
		{
			drawMinion(graphics, plugin.minionCoolDownExpired() && plugin.isMinionActive());
		}

		if (config.drawNexHp())
		{
			drawNexHpOverlay(graphics);
		}

		if (plugin.getCurrentPhase() == NexPhase.ZAROS && (config.indicateTankSwitchTicks() /* || config.indicatePraySwitchTicks() */))
		{
			drawTankAndPrayTicks(graphics);
		}

		if (config.drawMinionHP() && plugin.isMinionActive())
		{
			drawMinionHP(graphics);
		}

		if (config.flash() && plugin.isFlash())
		{
			drawFlash(graphics, Color.RED, plugin::setFlash);
		}

		if (config.shadowStandingFlash() && plugin.isShadowFlash())
		{
			drawFlash(graphics, config.shadowsColorBase(), plugin::setShadowFlash);
		}

		return null;
	}

	private void drawFlash(Graphics2D graphics, Color color, NexOverlayAction setter)
	{
		final Color flash = graphics.getColor();
		graphics.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), TIMEOUT_FLASH_START - Math.min(TIMEOUT_FLASH_START, ((TIMEOUT_FLASH_START - TIMEOUT_FLASH_FINISH) * timeout / TIMEOUT_MAX))));
		graphics.fill(new Rectangle(client.getCanvas().getSize()));
		graphics.setColor(flash);
		timeout++;
		if (timeout >= TIMEOUT_MAX)
		{
			timeout = 0;
			setter.method(false);
		}
	}

	private void drawObjectTickable(Graphics2D graphics,
									int ticks,
									List<LocalPoint> tiles,
									int renderDistance,
									Color baseColor,
									boolean renderTicks)
	{
		if (tiles.isEmpty())
		{
			return;
		}

		String count = Integer.toString(ticks);
		graphics.setFont(new Font("Arial", Font.BOLD, 12));

		Player localPlayer = client.getLocalPlayer();

		if (localPlayer == null)
		{
			return;
		}

		for (LocalPoint lp : tiles)
		{
			Polygon poly = Perspective.getCanvasTilePoly(client, lp);


			if (poly != null)
			{
				WorldPoint playerWorldPoint = localPlayer.getWorldLocation();
				WorldPoint shadowsWorldPoint = WorldPoint.fromLocal(client, lp);

				if (playerWorldPoint.distanceTo(shadowsWorldPoint) <= renderDistance)
				{
					graphics.setPaintMode();
					graphics.setColor(baseColor);
					graphics.draw(poly);
					graphics.setColor(getFillColor(baseColor));
					graphics.fill(poly);

					if (renderTicks)
					{
						Point point = Perspective.getCanvasTextLocation(client, graphics, lp, count, 0);
						if (point != null)
						{
							OverlayUtil.renderTextLocation(graphics, point, count, Color.WHITE);
						}
					}
				}
			}
		}
	}

	private void drawTank(Graphics2D graphics)
	{
		if (plugin.getNex().getInteracting() != null)
		{
			var interacting = plugin.getNex().getInteracting();
			var tilePoly = interacting.getCanvasTilePoly();

			if (tilePoly != null)
			{
				var baseColor = interacting == client.getLocalPlayer() ? config.tankOtherColorMe() : config.tankOtherColor();
				OverlayUtil.renderPolygon(graphics,
					tilePoly,
					baseColor,
					getFillColor(baseColor, 3),
					new BasicStroke(2));
			}
		}
	}

	private void drawMinion(Graphics2D graphics, boolean vulnerable)
	{
		var minion = plugin.getCurrentActiveMinion();
		if (minion == null)
		{
			return;
		}
		var color = vulnerable ? config.vulnerableColor() : config.invulnerableColor();
		var width = vulnerable ? config.vulnerableWidth() : config.invulnerableWidth();
		outliner.drawOutline(minion, width, color, config.outlineFeather());
	}


	private void drawInfectionArea(Graphics2D graphics)
	{
		Area infecetedTiles = new Area();

		var local = client.getLocalPlayer();
		if (local == null)
		{
			return;
		}

		WorldPoint localWorldLocation = local.getWorldLocation();

		// Draw sick people squares. ew gross
		plugin
			.getCoughingPlayers()
			.stream()
			.filter(nexCoughingPlayer -> {
				var players_loc = WorldPoint.fromLocal(client, nexCoughingPlayer.getPlayer().getLocalLocation());
				return players_loc.distanceTo(localWorldLocation) <= config.coughTileRenderDistance();
			})
			.forEach(p -> {
				Polygon poly = getCanvasTileAreaPoly(client, p.getPlayer().getLocalLocation(), 3);
				if (poly != null)
				{
					infecetedTiles.add(new Area(poly));
				}
			});

		graphics.setPaintMode();
		graphics.setColor(config.coughColorBase());
		graphics.draw(infecetedTiles);
		graphics.setColor(getFillColor(config.coughColorBase()));
		graphics.fill(infecetedTiles);
	}


	private void drawHealthyPlayers(Graphics2D graphics)
	{
		Area infecetedTiles = new Area();

		var local = client.getLocalPlayer();
		if (local == null)
		{
			return;
		}

		WorldPoint localWorldLocation = local.getWorldLocation();


		// Draw sick people squares. ew gross
		plugin
			.getHealthyPlayersLocations()
			.stream()
			.filter(healthyLocation -> {
				var players_loc = WorldPoint.fromLocal(client, healthyLocation);
				return players_loc.distanceTo(localWorldLocation) <= config.healthyTileRenderDistance();
			})
			.forEach(healthyLocation -> {
				Polygon poly = getCanvasTileAreaPoly(client, healthyLocation, 3);
				if (poly != null)
				{
					infecetedTiles.add(new Area(poly));
				}
			});

		graphics.setPaintMode();
		graphics.setColor(config.healthColorBase());
		graphics.draw(infecetedTiles);
		graphics.setColor(getFillColor(config.healthColorBase()));
		graphics.fill(infecetedTiles);
	}

	private void drawAreaTiles(Graphics2D graphics, List<LocalPoint> tiles, Color color, int size, boolean drawTicks, int ticksRemaining)
	{
		if (tiles.isEmpty())
		{
			return;
		}

		Area area = new Area();

		tiles.forEach(p -> {
			Polygon poly = getCanvasTileAreaPoly(client, p, size);

			if (poly != null)
			{
				area.add(new Area(poly));
			}
		});

		graphics.setPaintMode();
		graphics.setColor(color);
		graphics.draw(area);
		graphics.setColor(getFillColor(color));
		graphics.fill(area);

		if (drawTicks)
		{
			graphics.setFont(new Font("Arial", Font.BOLD, 16));
			String ticks = String.valueOf(ticksRemaining);

			OverlayUtil.renderTextLocation(
				graphics,
				Perspective.getCanvasTextLocation(client, graphics, tiles.get(tiles.size() / 2), ticks, 0),
				ticks,
				Color.WHITE
			);
		}
	}


	private void drawTileAOE(Graphics2D graphics, LocalPoint tile, int size, Color color, int ticksRemaining, boolean drawTicks)
	{
		if (tile == null)
		{
			return;
		}

		Polygon area = Perspective.getCanvasTileAreaPoly(client, tile, size);

		OverlayUtil.renderPolygon(
			graphics,
			area,
			color,
			getFillColor(color),
			new BasicStroke(2)
		);

		if (drawTicks)
		{
			graphics.setFont(new Font("Arial", Font.BOLD, 16));
			String ticks = String.valueOf(ticksRemaining);

			OverlayUtil.renderTextLocation(
				graphics,
				Perspective.getCanvasTextLocation(client, graphics, tile, ticks, 0),
				ticks,
				Color.WHITE
			);
		}
	}

	private void drawNexHpOverlay(Graphics2D graphics)
	{
		if (config.indicateInvulnerableNexTicks() && plugin.getNexTicksUntilClick().isActive())
		{
			return;
		}

		// SAFETY: not null checked before call
		var nex = plugin.getNex();

		int healthScale = nex.getHealthScale();
		int healthRatio = nex.getHealthRatio();

		double nexHP = ((double) healthRatio / (double) healthScale) * 100.0F;
		double relativeHP = NexPhase.getHpPercentage(plugin.getCurrentPhase(), nexHP);

		String text = String.valueOf(DECIMAL_FORMAT.format(relativeHP));
		graphics.setFont(new Font("Arial", Font.BOLD, config.drawNexHpFontSize()));
		Point textLocation = nex.getCanvasTextLocation(graphics, text, 0);
		if (!nex.isDead() && textLocation != null)
		{
			Color color = percentageToColor(relativeHP);
			OverlayUtil.renderTextLocation(graphics, textLocation, text, color);
		}
	}

	private void drawTankAndPrayTicks(Graphics2D graphics)
	{
		if (config.indicateInvulnerableNexTicks() && plugin.getNexTicksUntilClick().isActive())
		{
			return;
		}

		var zOffset = 0;
		if (config.drawNexHp())
		{
			zOffset = config.counterZOffset();
		}
		// SAFETY: not null checked before call
		var nex = plugin.getNex();

		StringBuilder text = new StringBuilder();

		if (config.indicateTankSwitchTicks())
		{
			text.append(4 - (plugin.getNexTankAttacks() % 4));
		}

		/* disabled until we know how this works

		if (config.indicatePraySwitchTicks())
		{
			if (text.length() != 0)
			{
				text.append(" | ");
			}
			text.append(5 - (plugin.getNexAttacks() % 5));
		}

		*/

		var finalText = text.toString();
		graphics.setFont(new Font("Arial", Font.BOLD, 12));
		Point textLocation = nex.getCanvasTextLocation(graphics, finalText, 0);

		if (!nex.isDead() && textLocation != null)
		{
			OverlayUtil.renderTextLocation(graphics, new Point(textLocation.getX(), textLocation.getY() + zOffset), finalText, Color.WHITE);
		}
	}

	private void drawMinionHP(Graphics2D graphics)
	{
		var minion = plugin.getCurrentActiveMinion();
		if (minion == null)
		{
			return;
		}

		int healthScale = minion.getHealthScale();
		int healthRatio = minion.getHealthRatio();

		double minionHP = ((double) healthRatio / (double) healthScale) * 100.0F;

		String text = String.valueOf(DECIMAL_FORMAT.format(minionHP));
		graphics.setFont(new Font("Arial", Font.BOLD, config.drawNexHpFontSize()));
		Point textLocation = minion.getCanvasTextLocation(graphics, text, 0);
		if (!minion.isDead() && textLocation != null)
		{
			Color color = percentageToColor(minionHP);
			OverlayUtil.renderTextLocation(graphics, textLocation, text, color);
		}
	}

	private Color percentageToColor(double percentage)
	{
		percentage = Math.max(Math.min(100.0F, percentage), 0.0F);
		double rMod = 130.0D * percentage / 100.0D;
		double gMod = 235.0D * percentage / 100.0D;
		double bMod = 125.0D * percentage / 100.0D;
		return new Color((int) Math.min(255.0D, 255.0D - rMod), Math.min(255, (int) (20.0D + gMod)), Math.min(255, (int) (0.0D + bMod)));
	}

	private Color getFillColor(Color color, int decimator)
	{
		return new Color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha() / decimator);
	}

	private Color getFillColor(Color color)
	{
		return getFillColor(color, 2);
	}
}