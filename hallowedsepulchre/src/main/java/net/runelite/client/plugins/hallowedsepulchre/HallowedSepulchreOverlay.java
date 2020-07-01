package net.runelite.client.plugins.hallowedsepulchre;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.Stroke;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.DynamicObject;
import net.runelite.api.GameObject;
import net.runelite.api.NPC;
import net.runelite.api.NPCDefinition;
import net.runelite.api.Perspective;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.graphics.ModelOutlineRenderer;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

public class HallowedSepulchreOverlay extends Overlay
{
	private static final int CROSSBOW_STATUE_ANIM_DEFAULT = 8681;
	private static final int CROSSBOW_STATUE_ANIM_FINAL = 8685;

	private final Client client;
	private final HallowedSepulchrePlugin plugin;
	private final HallowedSepulchreConfig config;
	private final ModelOutlineRenderer modelOutlineRenderer;

	private Player player;

	@Inject
	HallowedSepulchreOverlay(final Client client, final HallowedSepulchrePlugin plugin,
					final HallowedSepulchreConfig config, final ModelOutlineRenderer modelOutlineRenderer)
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

		player = client.getLocalPlayer();

		if (player == null)
		{
			return null;
		}

		renderArrows(graphics2D);

		renderSwords(graphics2D);

		renderCrossbowStatues(graphics2D);

		renderWizardStatues(graphics2D);

		renderServerTile(graphics2D);

		return null;
	}

	void determineLayer()
	{
		setLayer(config.mirrorMode() ? OverlayLayer.AFTER_MIRROR : OverlayLayer.ABOVE_SCENE);
	}

	private void renderArrows(final Graphics2D graphics2D)
	{
		final HallowedSepulchreConfig.HighlightMode highlightMode = config.highlightArrows();

		if (highlightMode.equals(HallowedSepulchreConfig.HighlightMode.NONE) || plugin.getArrows().isEmpty())
		{
			return;
		}

		for (final NPC npc : plugin.getArrows())
		{
			if (isOutsideRenderDistance(npc.getLocalLocation()))
			{
				continue;
			}

			renderNpcHighlight(graphics2D, config.arrowsOutlineColor(), config.arrowsFillColor(), npc, highlightMode);
		}
	}

	private void renderSwords(final Graphics2D graphics2D)
	{
		final HallowedSepulchreConfig.HighlightMode highlightMode = config.highlightSwords();

		if (highlightMode.equals(HallowedSepulchreConfig.HighlightMode.NONE) || plugin.getSwords().isEmpty())
		{
			return;
		}

		for (final NPC npc : plugin.getSwords())
		{
			if (isOutsideRenderDistance(npc.getLocalLocation()))
			{
				continue;
			}

			renderNpcHighlight(graphics2D, config.swordsOutlineColor(), config.swordsFillColor(), npc, highlightMode);
		}
	}

	private void renderNpcHighlight(final Graphics2D graphics2D, final Color outlineColor, final Color fillColor,
									final NPC npc, final HallowedSepulchreConfig.HighlightMode highlightMode)
	{
		final NPCDefinition npcDefinition = npc.getTransformedDefinition();

		if (npcDefinition == null)
		{
			return;
		}

		if (highlightMode.equals(HallowedSepulchreConfig.HighlightMode.OUTLINE)
			|| highlightMode.equals(HallowedSepulchreConfig.HighlightMode.BOTH))
		{
			modelOutlineRenderer.drawOutline(npc, 1, outlineColor);
		}

		if (highlightMode.equals(HallowedSepulchreConfig.HighlightMode.TILE)
			|| highlightMode.equals(HallowedSepulchreConfig.HighlightMode.BOTH))
		{
			int size = 1;

			final NPCDefinition composition = npc.getTransformedDefinition();

			if (composition != null)
			{
				size = composition.getSize();
			}

			final LocalPoint localPoint = npc.getLocalLocation();
			final Polygon polygon = Perspective.getCanvasTileAreaPoly(client, localPoint, size);

			if (polygon != null)
			{
				drawStrokeAndFill(graphics2D, outlineColor, fillColor, config.tileOutlineWidth(), polygon);
			}
		}
	}

	private void renderCrossbowStatues(final Graphics2D graphics2D)
	{
		if (!config.highlightCrossbowStatues() || plugin.getCrossbowStatues().isEmpty())
		{
			return;
		}

		for (final GameObject gameObject : plugin.getCrossbowStatues())
		{
			if (!gameObject.getWorldLocation().isInScene(client)
				|| isOutsideRenderDistance(gameObject.getLocalLocation()))
			{
				continue;
			}

			final DynamicObject dynamicObject = (DynamicObject) gameObject.getEntity();

			if (dynamicObject.getAnimationID() == CROSSBOW_STATUE_ANIM_DEFAULT || dynamicObject.getAnimationID() == CROSSBOW_STATUE_ANIM_FINAL)
			{
				continue;
			}

			final Shape shape = gameObject.getConvexHull();

			if (shape != null)
			{
				drawStrokeAndFill(graphics2D, config.crossbowStatueOutlineColor(), config.crossbowStatueFillColor(),
					1.0f, shape);
			}
		}
	}

	private void renderWizardStatues(final Graphics2D graphics2D)
	{
		if (!config.highlightWizardStatues() || plugin.getWizardStatues().isEmpty())
		{
			return;
		}

		for (final HallowedSepulchreWizardStatue sepulchreGameObject : plugin.getWizardStatues())
		{
			final GameObject gameObject = sepulchreGameObject.getGameObject();

			if (!gameObject.getWorldLocation().isInScene(client)
				|| isOutsideRenderDistance(gameObject.getLocalLocation()))
			{
				continue;
			}

			final int ticksLeft = sepulchreGameObject.getTicksUntilNextAnimation();

			if (ticksLeft <= 0)
			{
				continue;
			}

			final String ticksLeftStr = String.valueOf(ticksLeft);

			final Color color = (ticksLeft == 1 ? Color.WHITE : config.wizardStatueTickCounterColor());

			final Point canvasPoint = gameObject.getCanvasTextLocation(graphics2D, ticksLeftStr, 0);

			OverlayUtil.renderTextLocation(graphics2D, ticksLeftStr, config.wizardFontSize(),
				config.fontStyle().getFont(), color, canvasPoint, config.wizardFontShadow(), 0);
		}
	}

	private void renderServerTile(final Graphics2D graphics2D)
	{
		if (!config.highlightServerTile())
		{
			return;
		}

		final WorldPoint worldPoint = player.getWorldLocation();

		if (worldPoint == null)
		{
			return;
		}

		final LocalPoint localPoint = LocalPoint.fromWorld(client, worldPoint);

		if (localPoint == null)
		{
			return;
		}

		final Polygon polygon = Perspective.getCanvasTilePoly(client, localPoint);

		if (polygon == null)
		{
			return;
		}

		drawStrokeAndFill(graphics2D, config.serverTileOutlineColor(), config.serverTileFillColor(),
			config.tileOutlineWidth(), polygon);
	}

	private boolean isOutsideRenderDistance(final LocalPoint localPoint)
	{
		final int maxDistance = config.renderDistance().getDistance();

		if (maxDistance == 0)
		{
			return false;
		}

		return localPoint.distanceTo(player.getLocalLocation()) >= maxDistance;
	}

	private static void drawStrokeAndFill(final Graphics2D graphics2D, final Color outlineColor, final Color fillColor,
						final float strokeWidth, final Shape shape)
	{
		graphics2D.setColor(outlineColor);
		final Stroke originalStroke = graphics2D.getStroke();
		graphics2D.setStroke(new BasicStroke(strokeWidth));
		graphics2D.draw(shape);
		graphics2D.setColor(fillColor);
		graphics2D.fill(shape);
		graphics2D.setStroke(originalStroke);
	}
}
