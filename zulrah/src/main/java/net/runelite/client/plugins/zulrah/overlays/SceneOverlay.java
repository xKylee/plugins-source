package net.runelite.client.plugins.zulrah.overlays;

import com.google.common.base.Strings;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.SetMultimap;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.util.Iterator;
import java.util.Objects;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.config.FontType;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.plugins.zulrah.ZulrahConfig;
import net.runelite.client.plugins.zulrah.ZulrahPlugin;
import net.runelite.client.plugins.zulrah.constants.StandLocation;
import net.runelite.client.plugins.zulrah.constants.ZulrahLocation;
import net.runelite.client.plugins.zulrah.rotationutils.ZulrahNpc;
import net.runelite.client.plugins.zulrah.util.OverlayUtils;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class SceneOverlay extends Overlay
{
	private final Client client;
	private final ZulrahPlugin plugin;
	private final ZulrahConfig config;
	private final SkillIconManager skillIconManager;

	@Inject
	private SceneOverlay(Client client, ZulrahPlugin plugin, ZulrahConfig config, SkillIconManager skillIconManager)
	{
		this.client = client;
		this.plugin = plugin;
		this.config = config;
		this.skillIconManager = skillIconManager;
		this.setPriority(OverlayPriority.HIGH);
		this.setPosition(OverlayPosition.DYNAMIC);
		this.setLayer(OverlayLayer.ABOVE_SCENE);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		Font prevFont = graphics.getFont();
		graphics.setFont(this.config.fontType().getFont());
		if (this.plugin.getZulrahNpc() != null && !this.plugin.getZulrahNpc().isDead())
		{
			this.renderZulrahPhaseTiles(graphics);
			this.renderStandAndStallTiles(graphics);
			this.renderPrayerConservation(graphics);
			this.renderZulrahTicks(graphics);
			this.renderZulrahTile(graphics);
			this.renderProjectiles(graphics);
			this.renderToxicClouds(graphics);
		}

		graphics.setFont(prevFont);
		return null;
	}

	private void renderZulrahPhaseTiles(Graphics2D graphics)
	{
		if (this.config.phaseDisplayType() != ZulrahConfig.DisplayType.OFF && this.config.phaseDisplayType() != ZulrahConfig.DisplayType.OVERLAY)
		{
			SetMultimap<ZulrahLocation, MutablePair<String, ZulrahNpc>> zulrahLocationsGrouped = LinkedHashMultimap.create();
			this.plugin.getZulrahData().forEach((data) -> {
				switch (this.config.phaseDisplayMode())
				{
					case CURRENT:
						data.getCurrentZulrahNpc().ifPresent((npc) -> {
							zulrahLocationsGrouped.put(npc.getZulrahLocation(), new MutablePair<>("Current", npc));
						});
						break;
					case NEXT:
						data.getNextZulrahNpc().ifPresent((npc) -> {
							zulrahLocationsGrouped.put(npc.getZulrahLocation(), new MutablePair<>(this.getZulrahNextString(), npc));
						});
						break;
					case BOTH:
						data.getCurrentZulrahNpc().ifPresent((npc) -> {
							zulrahLocationsGrouped.put(npc.getZulrahLocation(), new MutablePair<>("Current", npc));
						});
						data.getNextZulrahNpc().ifPresent((npc) -> {
							zulrahLocationsGrouped.put(npc.getZulrahLocation(), new MutablePair<>(this.getZulrahNextString(), npc));
						});
						break;
					default:
						throw new IllegalStateException("[SceneOverlay] Invalid 'phaseDisplayMode' config state");
				}

			});
			Iterator var3 = zulrahLocationsGrouped.keys().iterator();

			while (var3.hasNext())
			{
				ZulrahLocation zulrahLocation = (ZulrahLocation) var3.next();
				int offset = 0;

				for (Iterator var6 = zulrahLocationsGrouped.get(zulrahLocation).iterator(); var6.hasNext(); offset += graphics.getFontMetrics().getHeight())
				{
					Pair pair = (Pair) var6.next();
					this.drawZulrahTile(graphics, (ZulrahNpc) pair.getRight(), (String) pair.getLeft(), offset);
				}
			}

		}
	}

	private String getZulrahNextString()
	{
		return this.plugin.getCurrentRotation() != null ? "Next" : "P. Next";
	}

	private void drawZulrahTile(Graphics2D graphics, ZulrahNpc zulrahNpc, String addonText, int offset)
	{
		if (zulrahNpc != null)
		{
			LocalPoint localPoint = zulrahNpc.getZulrahLocation().toLocalPoint();
			Polygon tileAreaPoly = Perspective.getCanvasTileAreaPoly(this.client, localPoint, 5);
			OverlayUtils.renderPolygon(graphics, tileAreaPoly, zulrahNpc.getType().getColor(), this.config.outlineWidth(), this.config.fillAlpha());
			Point basePoint = Perspective.localToCanvas(this.client, localPoint, this.client.getPlane(), 0);
			if (basePoint != null)
			{
				int bx = basePoint.getX();
				int by = basePoint.getY();
				String text = this.getZulrahPhaseString(zulrahNpc, addonText);
				Rectangle2D textBounds = graphics.getFontMetrics().getStringBounds(text, graphics);
				Point textLocation = new Point(bx - (int) textBounds.getWidth() / 2, by - offset);
				Color var10003 = zulrahNpc.getType().getColor();
				ZulrahConfig var10004 = this.config;
				Objects.requireNonNull(var10004);
				OverlayUtils.renderTextLocation(graphics, textLocation, text, var10003, var10004::textOutline);
				if (this.config.phaseHats())
				{
					BufferedImage icon = this.skillIconManager.getSkillImage(zulrahNpc.getType().getSkill(), this.config.fontType() != FontType.BOLD);
					int imgPX = bx - icon.getWidth() / 2;
					int imgPY = by - icon.getHeight() / 2 - offset;
					Point imgPoint = new Point(imgPX, imgPY);
					int imgX = imgPoint.getX() - graphics.getFontMetrics().stringWidth(text) / 2 - 15;
					int imgY = imgPoint.getY() - icon.getHeight() / 2 + 1;
					graphics.drawImage(icon, imgX, imgY, (ImageObserver) null);
				}
			}

		}
	}

	private String getZulrahPhaseString(ZulrahNpc npc, String addonText)
	{
		boolean strip = !this.config.phaseTags() || Strings.isNullOrEmpty(addonText);
		if (npc.isJad())
		{
			return strip ? "Jad" : "[" + addonText + "] Jad";
		}
		else
		{
			return strip ? npc.getType().getName() : "[" + addonText + "] " + npc.getType().getName();
		}
	}

	private void renderStandAndStallTiles(Graphics2D graphics)
	{
		if (this.config.standLocations() || this.config.stallLocations())
		{
			SetMultimap standLocationsGrouped = HashMultimap.create();
			this.plugin.getZulrahData().forEach((data) -> {
				if (this.config.standLocations())
				{
					if (data.standLocationsMatch())
					{
						data.getCurrentDynamicStandLocation().ifPresent((loc) -> {
							standLocationsGrouped.put(loc, new MutablePair("Stand / Next", this.config.standAndNextTileColor()));
						});
					}
					else
					{
						data.getCurrentDynamicStandLocation().ifPresent((loc) -> {
							standLocationsGrouped.put(loc, new MutablePair("Stand", this.config.standTileColor()));
						});
						data.getNextStandLocation().ifPresent((loc) -> {
							standLocationsGrouped.put(loc, new MutablePair("Next", this.config.nextTileColor()));
						});
					}
				}

				if (this.config.stallLocations())
				{
					data.getCurrentStallLocation().ifPresent((loc) -> {
						standLocationsGrouped.put(loc, new MutablePair("Stall", this.config.stallTileColor()));
					});
				}

			});
			Iterator var3 = standLocationsGrouped.keys().iterator();

			while (var3.hasNext())
			{
				StandLocation standLocation = (StandLocation) var3.next();
				int offset = 0;

				for (Iterator var6 = standLocationsGrouped.get(standLocation).iterator(); var6.hasNext(); offset += graphics.getFontMetrics().getHeight())
				{
					Pair pair = (Pair) var6.next();
					this.drawTile(graphics, standLocation.toLocalPoint(), (String) pair.getLeft(), (Color) pair.getRight(), offset);
				}
			}

		}
	}

	private void drawTile(Graphics2D graphics, LocalPoint localPoint, String text, Color color, int offset)
	{
		if (localPoint != null && !Strings.isNullOrEmpty(text))
		{
			Point textLocation = Perspective.getCanvasTextLocation(this.client, graphics, localPoint, text, 0);
			Point var10001 = new Point(textLocation.getX(), textLocation.getY() - offset);
			Color var10003 = new Color(color.getRed(), color.getGreen(), color.getBlue());
			ZulrahConfig var10004 = this.config;
			Objects.requireNonNull(var10004);
			OverlayUtils.renderTextLocation(graphics, var10001, text, var10003, var10004::textOutline);
			Polygon tilePoly = Perspective.getCanvasTilePoly(this.client, localPoint);
			OverlayUtils.renderPolygon(graphics, tilePoly, color, this.config.outlineWidth(), this.config.fillAlpha());
		}
	}

	private void renderPrayerConservation(Graphics2D graphics)
	{
		if (this.config.prayerConservation())
		{
			Player player = this.client.getLocalPlayer();
			if (player != null && (this.plugin.getZulrahNpc().getInteracting() == null || this.plugin.getZulrahNpc().getInteracting() != this.client.getLocalPlayer()) && player.getOverheadIcon() != null)
			{
				String conserveStr = "Turn off overheads to conserve prayer!";
				Point textLocation = player.getCanvasTextLocation(graphics, "Turn off overheads to conserve prayer!", player.getLogicalHeight() - 22);
				Color var10003 = Color.RED;
				ZulrahConfig var10004 = this.config;
				Objects.requireNonNull(var10004);
				OverlayUtils.renderTextLocation(graphics, textLocation, "Turn off overheads to conserve prayer!", var10003, var10004::textOutline);
			}

		}
	}

	private void renderZulrahTicks(Graphics2D graphics)
	{
		if (this.config.phaseTickCounter() || this.config.attackTickCounter())
		{
			StringBuilder sb = new StringBuilder();
			sb = sb.append(this.config.phaseTickCounter() && this.plugin.getPhaseTicks() >= 0 ? this.plugin.getPhaseTicks() : "").append(this.config.phaseTickCounter() && this.config.attackTickCounter() && this.plugin.getPhaseTicks() >= 0 && this.plugin.getAttackTicks() >= 0 ? " : " : "").append(this.config.attackTickCounter() && this.plugin.getAttackTicks() >= 0 ? this.plugin.getAttackTicks() : "");
			if (!Strings.isNullOrEmpty(sb.toString()))
			{
				Point textLocation = this.plugin.getZulrahNpc().getCanvasTextLocation(graphics, sb.toString(), this.plugin.getZulrahNpc().getLogicalHeight() - 130);
				String var10002 = sb.toString();
				Color var10003 = this.config.tickCounterColors();
				ZulrahConfig var10004 = this.config;
				Objects.requireNonNull(var10004);
				OverlayUtils.renderTextLocation(graphics, textLocation, var10002, var10003, var10004::textOutline);
			}
		}
	}

	private void renderZulrahTile(Graphics2D graphics)
	{
		if (this.config.displayZulrahTile())
		{
			Polygon tileAreaPoly = Perspective.getCanvasTileAreaPoly(this.client, this.plugin.getZulrahNpc().getLocalLocation(), 5);
			OverlayUtils.renderPolygon(graphics, tileAreaPoly, this.config.zulrahTileColor(), this.config.outlineWidth(), this.config.fillAlpha());
		}
	}

	private void renderProjectiles(Graphics2D graphics)
	{
		if (this.config.displayProjectiles() && this.plugin.getProjectilesMap().size() > 0)
		{
			this.plugin.getProjectilesMap().forEach((localPoint, ticks) -> {
				Point textLocation = Perspective.getCanvasTextLocation(this.client, graphics, localPoint, "#", 0);
				String var10002 = Integer.toString(ticks);
				Color var10003 = ticks > 0 ? Color.WHITE : Color.RED;
				ZulrahConfig var10004 = this.config;
				Objects.requireNonNull(var10004);
				OverlayUtils.renderTextLocation(graphics, textLocation, var10002, var10003, var10004::textOutline);
				Polygon tilePoly = Perspective.getCanvasTilePoly(this.client, localPoint);
				OverlayUtils.renderPolygon(graphics, tilePoly, this.config.projectilesColor(), this.config.outlineWidth(), this.config.fillAlpha());
			});
		}
	}

	private void renderToxicClouds(Graphics2D graphics)
	{
		if (this.config.displayToxicClouds() && this.plugin.getToxicCloudsMap().size() > 0)
		{
			this.plugin.getToxicCloudsMap().forEach((obj, ticks) -> {
				LocalPoint localPoint = obj.getLocalLocation();
				Polygon tileAreaPoly = Perspective.getCanvasTileAreaPoly(this.client, localPoint, 3);
				OverlayUtils.renderPolygon(graphics, tileAreaPoly, this.config.toxicCloudsColor(), this.config.outlineWidth(), this.config.fillAlpha());
				String ticksString = Integer.toString(ticks);
				Point textLocation = Perspective.getCanvasTextLocation(this.client, graphics, localPoint, ticksString, 0);
				Color var10003 = ticks > 0 ? Color.RED : Color.GREEN;
				ZulrahConfig var10004 = this.config;
				Objects.requireNonNull(var10004);
				OverlayUtils.renderTextLocation(graphics, textLocation, ticksString, var10003, var10004::textOutline);
			});
		}
	}
}
