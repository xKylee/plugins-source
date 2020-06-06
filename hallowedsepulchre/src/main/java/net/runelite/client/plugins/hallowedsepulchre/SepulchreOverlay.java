package net.runelite.client.plugins.hallowedsepulchre;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
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
import static net.runelite.client.plugins.hallowedsepulchre.SepulchreIDs.CROSSBOW_STATUE_ANIM_DEFAULT;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

public class SepulchreOverlay extends Overlay
{
	private final Client client;
	private final SepulchrePlugin plugin;
	private final SepulchreConfig config;
	private final ModelOutlineRenderer modelOutlineRenderer;

	@Inject
	SepulchreOverlay(final Client client, final SepulchrePlugin plugin, final SepulchreConfig config, final ModelOutlineRenderer modelOutlineRenderer)
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
		SepulchreConfig.HighlightMode highlightArrows = config.highlightArrows();

		if (!highlightArrows.equals(SepulchreConfig.HighlightMode.NONE) && !plugin.getArrows().isEmpty())
		{
			for (NPC npc : plugin.getArrows())
			{
				renderNpcHighlight(npc, highlightArrows, graphics2D, config.highlightArrowsColor());
			}
		}

		SepulchreConfig.HighlightMode highlightSwords = config.highlightSwords();

		if (!highlightSwords.equals(SepulchreConfig.HighlightMode.NONE) && !plugin.getSwords().isEmpty())
		{
			for (NPC npc : plugin.getSwords())
			{
				renderNpcHighlight(npc, highlightSwords, graphics2D, config.highlightSwordsColor());
			}
		}

		if (config.highlightCrossbowStatues() && !plugin.getCrossbowStatues().isEmpty())
		{
			renderCrossbowStatues(graphics2D);
		}

		if (config.highlightWizardStatues() && !plugin.getWizardStatues().isEmpty())
		{
			renderWizardStatues(graphics2D);
		}

		return null;
	}

	private void renderNpcHighlight(final NPC npc, SepulchreConfig.HighlightMode highlightMode, Graphics2D graphics2D, final Color color)
	{
		NPCDefinition npcDefinition = npc.getTransformedDefinition();

		if (npcDefinition == null || npc.isDead())
		{
			return;
		}

		if (highlightMode.equals(SepulchreConfig.HighlightMode.OUTLINE) || highlightMode.equals(SepulchreConfig.HighlightMode.BOTH))
		{
			modelOutlineRenderer.drawOutline(npc, 1, color);
		}

		if (highlightMode.equals(SepulchreConfig.HighlightMode.TILE) || highlightMode.equals(SepulchreConfig.HighlightMode.BOTH))
		{
			int size = 1;

			NPCDefinition composition = npc.getTransformedDefinition();

			if (composition != null)
			{
				size = composition.getSize();
			}

			final LocalPoint lp = npc.getLocalLocation();
			final Polygon tilePoly = Perspective.getCanvasTileAreaPoly(client, lp, size);
			renderPoly(graphics2D, color, tilePoly);
		}
	}

	private void renderPoly(final Graphics2D graphics2D, final Color color, final Polygon polygon)
	{
		if (polygon != null)
		{
			graphics2D.setColor(color);
			graphics2D.setStroke(new BasicStroke(1));
			graphics2D.draw(polygon);
			graphics2D.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 20));
			graphics2D.fill(polygon);
		}
	}

	private void renderCrossbowStatues(Graphics2D graphics)
	{
		for (GameObject gameObject : plugin.getCrossbowStatues())
		{
			if (!gameObject.getWorldLocation().isInScene(client))
			{
				continue;
			}

			Entity entity = gameObject.getEntity();

			if (!(entity instanceof DynamicObject) || ((DynamicObject) entity).getAnimationID() == CROSSBOW_STATUE_ANIM_DEFAULT)
			{
				continue;
			}

			Shape p = gameObject.getConvexHull();

			if (p != null)
			{
				graphics.setColor(config.highlightCrossbowStatueColor());
				graphics.draw(p);
			}
		}
	}

	private void renderWizardStatues(Graphics2D graphics2D)
	{
		for (SepulchreGameObject sepulchreGameObject : plugin.getWizardStatues())
		{
			GameObject gameObject = sepulchreGameObject.getGameObject();

			if (!gameObject.getWorldLocation().isInScene(client))
			{
				continue;
			}

			int ticksLeft = sepulchreGameObject.getTicksUntilNextAnimation();
			String ticksLeftStr = String.valueOf(ticksLeft);

			if (ticksLeft <= 0)
			{
				continue;
			}

			Color color = (ticksLeft == 1 ? Color.WHITE : config.wizardStatueTickCounterColor());

			final Point canvasPoint = gameObject.getCanvasTextLocation(graphics2D, ticksLeftStr, 0);

			OverlayUtil.renderTextLocation(graphics2D, ticksLeftStr, 18, Font.PLAIN, color, canvasPoint, false, 0);
		}
	}

	void determineLayer()
	{
		setLayer(config.mirrorMode() ? OverlayLayer.AFTER_MIRROR : OverlayLayer.ABOVE_SCENE);
	}
}
