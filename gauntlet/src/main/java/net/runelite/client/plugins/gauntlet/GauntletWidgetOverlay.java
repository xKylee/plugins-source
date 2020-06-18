package net.runelite.client.plugins.gauntlet;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Point;
import net.runelite.api.Prayer;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.OverlayUtil;

public class GauntletWidgetOverlay extends Overlay
{
	private final Client client;
	private final GauntletPlugin plugin;
	private final GauntletConfig config;

	@Inject
	GauntletWidgetOverlay(final Client client, final GauntletPlugin plugin, final GauntletConfig config)
	{
		this.client = client;
		this.plugin = plugin;
		this.config = config;

		setPosition(OverlayPosition.DYNAMIC);
		setPriority(OverlayPriority.HIGH);
		determineLayer();
	}

	@Override
	public Dimension render(final Graphics2D graphics2D)
	{
		if (!plugin.isInGauntlet() || !plugin.isInHunllefRoom())
		{
			return null;
		}

		final GauntletHunllef hunllef = plugin.getHunllef();

		if (hunllef == null)
		{
			return null;
		}

		final NPC npc = hunllef.getNpc();

		if (npc == null || npc.isDead())
		{
			return null;
		}

		renderCorrectPrayer(graphics2D);

		return null;
	}

	void determineLayer()
	{
		setLayer(config.mirrorMode() ? OverlayLayer.AFTER_MIRROR : OverlayLayer.ABOVE_WIDGETS);
	}

	private void renderCorrectPrayer(final Graphics2D graphics2D)
	{
		final GauntletConfig.PrayerHighlightMode prayerHighlightMode = config.correctPrayerOverlay();

		if (prayerHighlightMode == GauntletConfig.PrayerHighlightMode.NONE ||
			prayerHighlightMode == GauntletConfig.PrayerHighlightMode.BOX)
		{
			return;
		}

		// Overlay outline of the prayer widget

		final GauntletHunllef hunllef = plugin.getHunllef();

		final GauntletHunllef.BossAttackPhase phase = hunllef.getCurrentPhase();

		final Prayer prayer = phase.getPrayer();

		final Color phaseColor = phase.getColor();

		final Rectangle rectangle = OverlayUtil.renderPrayerOverlay(graphics2D, client, prayer, phaseColor);

		if (rectangle == null)
		{
			return;
		}

		// Overlay tick count on the prayer widget

		final int ticksUntilAttack = hunllef.getTicksUntilAttack();

		final String text = String.valueOf(ticksUntilAttack);

		final int fontSize = 16;
		final int fontStyle = Font.BOLD;
		final Color fontColor = ticksUntilAttack == 1 ? Color.WHITE : phaseColor;

		final int x = (int) (rectangle.getX() + rectangle.getWidth() / 2);
		final int y = (int) (rectangle.getY() + rectangle.getHeight() / 2);

		final Point point = new Point(x, y);

		final Point canvasPoint = new Point(point.getX() - 3, point.getY() + 6);

		OverlayUtil.renderTextLocation(graphics2D, text, fontSize, fontStyle, fontColor, canvasPoint, true, 0);
	}
}
