package net.runelite.client.plugins.hallowedsepulchre;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Polygon;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.DynamicObject;
import net.runelite.api.Entity;
import net.runelite.api.GameObject;
import net.runelite.api.NPC;
import net.runelite.api.NPCDefinition;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.graphics.ModelOutlineRenderer;
import static net.runelite.client.plugins.hallowedsepulchre.HallowedSepulchreIDs.CROSSBOW_STATUE_ANIM_DEFAULT;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

public class HallowedSepulchreOverlay extends Overlay
{
	private final Client client;
	private final HallowedSepulchrePlugin plugin;
	private final HallowedSepulchreConfig config;
	private final ModelOutlineRenderer modelOutlineRenderer;

	@Inject
	HallowedSepulchreOverlay(final Client client, final HallowedSepulchrePlugin plugin, final HallowedSepulchreConfig config, final ModelOutlineRenderer modelOutlineRenderer)
	{
		this.client = client;
		this.plugin = plugin;
		this.config = config;
		this.modelOutlineRenderer = modelOutlineRenderer;
		setPosition(OverlayPosition.DYNAMIC);
		determineLayer();
	}

	@Override
	public Dimension render(final Graphics2D graphics2D)
	{
		if (!plugin.isPlayerInSepulchre())
		{
			return null;
		}

		renderArrows(graphics2D);

		renderSwords(graphics2D);

		renderCrossbowStatues(graphics2D);

		renderWizardStatues(graphics2D);

		return null;
	}

	private void renderArrows(Graphics2D graphics2D)
	{
		final HallowedSepulchreConfig.HighlightMode highlightArrows = config.highlightArrows();

		if (highlightArrows.equals(HallowedSepulchreConfig.HighlightMode.NONE) || plugin.getArrows().isEmpty())
		{
			return;
		}

		for (NPC npc : plugin.getArrows())
		{
			renderNpcHighlight(npc, highlightArrows, graphics2D, config.highlightArrowsColor());
		}
	}

	private void renderSwords(Graphics2D graphics2D)
	{
		final HallowedSepulchreConfig.HighlightMode highlightSwords = config.highlightSwords();

		if (highlightSwords.equals(HallowedSepulchreConfig.HighlightMode.NONE) || plugin.getSwords().isEmpty())
		{
			return;
		}

		for (NPC npc : plugin.getSwords())
		{
			renderNpcHighlight(npc, highlightSwords, graphics2D, config.highlightSwordsColor());
		}
	}

	private void renderCrossbowStatues(Graphics2D graphics2D)
	{
		if (!config.highlightCrossbowStatues() || plugin.getCrossbowStatues().isEmpty())
		{
			return;
		}

		for (GameObject gameObject : plugin.getCrossbowStatues())
		{
			if (!gameObject.getWorldLocation().isInScene(client))
			{
				continue;
			}

			final Entity entity = gameObject.getEntity();

			if (!(entity instanceof DynamicObject) || ((DynamicObject) entity).getAnimationID() == CROSSBOW_STATUE_ANIM_DEFAULT)
			{
				continue;
			}

			final Color color = config.highlightCrossbowStatueColor();

			modelOutlineRenderer.drawOutline(gameObject, 1, color, color);
		}
	}

	private void renderWizardStatues(Graphics2D graphics2D)
	{
		if (!config.highlightWizardStatues() || plugin.getWizardStatues().isEmpty())
		{
			return;
		}

		for (HallowedSepulchreGameObject sepulchreGameObject : plugin.getWizardStatues())
		{
			final GameObject gameObject = sepulchreGameObject.getGameObject();

			if (!gameObject.getWorldLocation().isInScene(client))
			{
				continue;
			}

			final int ticksLeft = sepulchreGameObject.getTicksUntilNextAnimation();
			final String ticksLeftStr = String.valueOf(ticksLeft);

			if (ticksLeft <= 0)
			{
				continue;
			}

			final Color color = (ticksLeft == 1 ? Color.WHITE : config.wizardStatueTickCounterColor());

			final Point canvasPoint = gameObject.getCanvasTextLocation(graphics2D, ticksLeftStr, 0);

			OverlayUtil.renderTextLocation(graphics2D, ticksLeftStr, 18, Font.PLAIN, color, canvasPoint, false, 0);
		}
	}

	private void renderNpcHighlight(final NPC npc, HallowedSepulchreConfig.HighlightMode highlightMode, Graphics2D graphics2D, final Color color)
	{
		NPCDefinition npcDefinition = npc.getTransformedDefinition();

		if (npcDefinition == null || npc.isDead())
		{
			return;
		}

		if (highlightMode.equals(HallowedSepulchreConfig.HighlightMode.OUTLINE) || highlightMode.equals(HallowedSepulchreConfig.HighlightMode.BOTH))
		{
			modelOutlineRenderer.drawOutline(npc, 1, color);
		}

		if (highlightMode.equals(HallowedSepulchreConfig.HighlightMode.TILE) || highlightMode.equals(HallowedSepulchreConfig.HighlightMode.BOTH))
		{
			NPCDefinition composition = npc.getTransformedDefinition();

			int size = 1;

			if (composition != null)
			{
				size = composition.getSize();
			}

			final LocalPoint localPoint = npc.getLocalLocation();
			final Polygon polygon = Perspective.getCanvasTileAreaPoly(client, localPoint, size);

			if (polygon != null)
			{
				OverlayUtil.renderPolygonThin(graphics2D, polygon, color);
			}
		}
	}

	void determineLayer()
	{
		setLayer(config.mirrorMode() ? OverlayLayer.AFTER_MIRROR : OverlayLayer.ABOVE_SCENE);
	}
}
