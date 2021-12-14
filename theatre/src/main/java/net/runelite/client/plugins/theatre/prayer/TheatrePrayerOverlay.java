package net.runelite.client.plugins.theatre.prayer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.Queue;
import javax.inject.Inject;
import lombok.AccessLevel;
import lombok.Getter;
import net.runelite.api.Client;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.plugins.theatre.TheatreConfig;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.OverlayUtil;

public abstract class TheatrePrayerOverlay extends Overlay
{
	private static final int TICK_PIXEL_SIZE = 60;
	private static final int BOX_WIDTH = 10;
	private static final int BOX_HEIGHT = 5;

	@Getter(AccessLevel.PROTECTED)
	private final TheatreConfig config;
	private final Client client;

	@Inject
	protected TheatrePrayerOverlay(final Client client, final TheatreConfig config)
	{
		this.client = client;
		this.config = config;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
		setPriority(OverlayPriority.HIGHEST);
	}

	protected abstract Queue<TheatreUpcomingAttack> getAttackQueue();

	protected abstract long getLastTick();

	protected abstract boolean isEnabled();

	@Override
	public Dimension render(Graphics2D graphics)
	{
		final Widget meleePrayerWidget = client.getWidget(WidgetInfo.PRAYER_PROTECT_FROM_MELEE);
		final Widget rangePrayerWidget = client.getWidget(WidgetInfo.PRAYER_PROTECT_FROM_MISSILES);
		final Widget magicPrayerWidget = client.getWidget(WidgetInfo.PRAYER_PROTECT_FROM_MAGIC);


		var prayerWidgetHidden = meleePrayerWidget == null
			|| rangePrayerWidget == null
			|| magicPrayerWidget == null
			|| meleePrayerWidget.isHidden()
			|| rangePrayerWidget.isHidden()
			|| magicPrayerWidget.isHidden();

		if ((config.prayerHelper() && isEnabled()) && (!prayerWidgetHidden || config.alwaysShowPrayerHelper()))
		{
			renderPrayerIconOverlay(graphics);

			if (config.descendingBoxes())
			{
				renderDescendingBoxes(graphics);
			}
		}

		return null;
	}

	private void renderDescendingBoxes(Graphics2D graphics)
	{
		var tickPriorityMap = TheatrePrayerUtil.getTickPriorityMap(getAttackQueue());

		getAttackQueue().forEach(attack -> {
			int tick = attack.getTicksUntil();
			final Color color = tick == 1 ? config.prayerColorDanger() : config.prayerColor();
			final Widget prayerWidget = client.getWidget(attack.getPrayer().getWidgetInfo());

			if (prayerWidget == null)
			{
				return;
			}

			int baseX = (int) prayerWidget.getBounds().getX();
			baseX += prayerWidget.getBounds().getWidth() / 2;
			baseX -= BOX_WIDTH / 2;

			int baseY = (int) prayerWidget.getBounds().getY() - tick * TICK_PIXEL_SIZE - BOX_HEIGHT;
			baseY += TICK_PIXEL_SIZE - ((getLastTick() + 600 - System.currentTimeMillis()) / 600.0 * TICK_PIXEL_SIZE);

			final Rectangle boxRectangle = new Rectangle(BOX_WIDTH, BOX_HEIGHT);
			boxRectangle.translate(baseX, baseY);

			if (attack.getPrayer().equals(tickPriorityMap.get(attack.getTicksUntil()).getPrayer()))
			{
				OverlayUtil.renderPolygon(graphics, boxRectangle, color, color, new BasicStroke(2));
			}
			else if (config.indicateNonPriorityDescendingBoxes())
			{
				OverlayUtil.renderPolygon(graphics, boxRectangle, color, new Color(0, 0, 0, 0), new BasicStroke(2));
			}
		});
	}

	private void renderPrayerIconOverlay(Graphics2D graphics)
	{
		var attack = getAttackQueue().peek();
		if (attack == null)
		{
			return;
		}

		if (!client.isPrayerActive(attack.getPrayer()))
		{
			final Widget prayerWidget = client.getWidget(attack.getPrayer().getWidgetInfo());
			if (prayerWidget == null)
			{
				return;
			}

			final Rectangle prayerRectangle = new Rectangle((int) prayerWidget.getBounds().getWidth(), (int) prayerWidget.getBounds().getHeight());
			prayerRectangle.translate((int) prayerWidget.getBounds().getX(), (int) prayerWidget.getBounds().getY());

			OverlayUtil.renderPolygon(graphics, prayerRectangle, config.prayerColorDanger());
		}
	}
}
