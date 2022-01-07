package net.runelite.client.plugins.nex;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.text.DecimalFormat;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
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

	private final int TIMEOUT_MAX = 50;
	private final int TIMEOUT_FLASH_START = 70;
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

		if (plugin.getNex().isDead())
		{
			return null;
		}

		if (config.shadowsIndicator())
		{
			String count = Integer.toString(plugin.getShadowsTicks());
			graphics.setFont(new Font("Arial", Font.BOLD, 12));

			for (GameObject graphicsObject : plugin.getShadows())
			{
				LocalPoint lp = graphicsObject.getLocalLocation();
				Polygon poly = Perspective.getCanvasTilePoly(client, lp);
				Player localPlayer = client.getLocalPlayer();


				if (poly != null && localPlayer != null)
				{
					WorldPoint playerWorldPoint = localPlayer.getWorldLocation();
					WorldPoint shadowsWorldPoint = WorldPoint.fromLocal(client, lp);

					if (playerWorldPoint.distanceTo(shadowsWorldPoint) <= config.shadowsRenderDistance())
					{
						graphics.setPaintMode();
						graphics.setColor(config.shadowsBorderColour());
						graphics.draw(poly);
						graphics.setColor(config.shadowsColour());
						graphics.fill(poly);

						if (config.shadowsTickCounter())
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

		if (config.coughTileIndicator() && !plugin.getCoughingPlayers().isEmpty())
		{
			drawInfectionArea(graphics);
		}

		if (config.indicateNexVulnerability().showInvulnerable() && plugin.nexDisable())
		{
			outliner.drawOutline(plugin.getNex(), config.invulnerableWidth(), config.invulnerableColor(), 0);
			if (config.indicateInvulnerableNexTicks() && plugin.getNexTicksUntilClick() > 0)
			{
				graphics.setFont(new Font("Arial", Font.BOLD, config.indicateInvulnerableNexTicksFontSize()));
				var text = String.valueOf(plugin.getNexTicksUntilClick());
				var color = plugin.getNexTicksUntilClick() == 1 ? Color.WHITE : Color.LIGHT_GRAY;

				final Point canvasPoint = plugin.getNex().getCanvasTextLocation(
					graphics, text, 0);

				OverlayUtil.renderTextLocation(graphics, canvasPoint, text, color);
			}
		}
		else if (config.indicateNexVulnerability().showVulnerable() && !plugin.nexDisable())
		{
			// TODO: Handle late join showing vulnerable while startup
			outliner.drawOutline(plugin.getNex(), config.vulnerableWidth(), config.vulnerableColor(), 0);
		}

		if (config.indicateMinionVulnerability().showInvulnerable() && !plugin.nexDisable())
		{
			drawMinion(graphics, false);
		}
		else if (config.indicateMinionVulnerability().showVulnerable() && plugin.nexDisable())
		{
			drawMinion(graphics, plugin.minionCoolDownExpired());
		}

		if (config.drawNexHp())
		{
			drawNexHpOverlay(graphics);
		}

		if (config.drawMinionHP() && plugin.isMinionActive())
		{
			drawMinionHP(graphics);
		}


		if (plugin.isFlash() && config.flash())
		{
			final Color flash = graphics.getColor();
			graphics.setColor(new Color(255, 0, 0, TIMEOUT_FLASH_START - Math.min(TIMEOUT_FLASH_START, ((TIMEOUT_FLASH_START - TIMEOUT_FLASH_FINISH) * timeout / TIMEOUT_MAX))));
			graphics.fill(new Rectangle(client.getCanvas().getSize()));
			graphics.setColor(flash);
			timeout++;
			if (timeout >= TIMEOUT_MAX)
			{
				timeout = 0;
				plugin.setFlash(false);
			}
		}

		return null;
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
		outliner.drawOutline(minion, width, color, 0);
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
		graphics.setColor(config.coughBorderColour());
		graphics.draw(infecetedTiles);
		graphics.setColor(config.coughColour());
		graphics.fill(infecetedTiles);
	}

	private void drawNexHpOverlay(Graphics2D graphics)
	{
		if (config.indicateInvulnerableNexTicks() && plugin.getNexTicksUntilClick() > 0)
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
}