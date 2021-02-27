package net.runelite.client.plugins.clanmanmode;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.client.util.Text;

@Singleton
public class ClanManModeOverlay extends Overlay
{
	private final ClanManModeService ClanManModeService;
	private final ClanManModeConfig config;

	@Inject
	private ClanManModeOverlay(final ClanManModeConfig config, final ClanManModeService ClanManModeService)
	{
		this.config = config;
		this.ClanManModeService = ClanManModeService;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ALWAYS_ON_TOP);
		setPriority(OverlayPriority.MED);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		ClanManModeService.forEachPlayer((player, color) -> renderPlayerOverlay(graphics, player, color));
		return null;
	}

	private void renderPlayerOverlay(Graphics2D graphics, Player actor, Color color)
	{
		if (!config.drawOverheadPlayerNames() || actor == null)
		{
			return;
		}

		final String name = Text.standardize(actor.getName());
		int offset = actor.getLogicalHeight() + 40;
		Point textLocation = actor.getCanvasTextLocation(graphics, name, offset);

		if (textLocation != null)
		{
			if (config.getClanAttackableColor().equals(color) && config.ShowBold())
			{
				graphics.setFont(FontManager.getRunescapeBoldFont());
			}
			OverlayUtil.renderTextLocation(graphics, textLocation, name, color);
		}
	}
}
