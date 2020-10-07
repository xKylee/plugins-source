/*
 * THIS PLUGIN WAS WRITTEN BY A KEYBOARD-WIELDING MONKEY BOI BUT SHUFFLED BY A KANGAROO WITH THUMBS.
 * The plugin and it's refactoring was intended for xKylee's Externals but I'm sure if you're reading this, you're probably planning to yoink..
 * or you're just genuinely curious. If you're trying to yoink, it doesn't surprise me.. just don't claim it as your own. Cheers.
 */

package net.runelite.client.plugins.theatre.Maiden;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.text.DecimalFormat;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Point;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.theatre.RoomOverlay;
import net.runelite.client.plugins.theatre.TheatreConfig;
import net.runelite.client.ui.overlay.OverlayLayer;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class MaidenOverlay extends RoomOverlay
{
	private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#0.0");

	@Inject
	private Maiden maiden;

	@Inject
	private Client client;

	@Inject
	protected MaidenOverlay(TheatreConfig config)
	{
		super(config);
		determineLayer();
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (maiden.isMaidenActive())
		{
			if (config.maidenBlood())
			{
				for (WorldPoint point : maiden.getMaidenBloodSplatters())
				{
					drawTile(graphics, point, new Color(0, 150, 200), 2, 150, 10);
				}
			}

			if (config.maidenSpawns())
			{
				for (WorldPoint point : maiden.getMaidenBloodSpawnLocations())
				{
					drawTile(graphics, point, new Color(0, 150, 200), 2, 180, 20);
				}
				for (WorldPoint point : maiden.getMaidenBloodSpawnTrailingLocations())
				{
					drawTile(graphics, point, new Color(0, 150, 200), 1, 120, 10);
				}
			}

			if (config.maidenTickCounter() && maiden.getMaidenNPC() != null && !maiden.getMaidenNPC().isDead())
			{
				String text = String.valueOf(maiden.getTicksUntilAttack());
				Point canvasPoint = maiden.getMaidenNPC().getCanvasTextLocation(graphics, text, 15);

				if (canvasPoint != null)
				{
					Color col = maiden.maidenSpecialWarningColor();
					renderTextLocation(graphics, text, col, canvasPoint);
				}
			}

			if (maiden.isMaidenActive() && (config.maidenRedsHealth() || config.maidenRedsDistance()))
			{
				maiden.getMaidenReds().forEach((k, v) ->
				{
					String string = "";
					Color color = Color.WHITE;
					if (config.maidenRedsHealth())
					{
						int v_health = v.getValue();
						int v_healthRation = v.getKey();
						if (k.getName() != null && k.getHealthScale() > 0)
						{
							v_health = k.getHealthScale();
							v_healthRation = Math.min(v_healthRation, k.getHealthRatio());
						}
						float percentage = ((float) v_healthRation / (float) v_health) * 100f;
						color = percentageToColor(percentage);
						string = (String.valueOf(DECIMAL_FORMAT.format(percentage)));
					}

					if (config.maidenRedsHealth() && config.maidenRedsDistance())
					{
						string += " - ";
					}

					if (config.maidenRedsDistance())
					{
						final int maidenX = maiden.getMaidenNPC().getWorldLocation().getX() + maiden.getMaidenNPC().getTransformedDefinition().getSize();

						int deltaX = Math.max(0, k.getWorldLocation().getX() - maidenX);
						string += deltaX;
					}

					Point textLocation = k.getCanvasTextLocation(graphics, string, 80);

					if (textLocation != null)
					{
						renderTextLocation(graphics, string, color, textLocation);
					}

				});

				NPC[] reds = maiden.getMaidenReds().keySet().toArray(new NPC[0]);
				for (NPC npc : reds)
				{
					if (npc.getName() != null && npc.getHealthScale() > 0 && npc.getHealthRatio() < 100)
					{
						Pair<Integer, Integer> newVal = new MutablePair<>(npc.getHealthRatio(), npc.getHealthScale());
						if (maiden.getMaidenReds().containsKey(npc))
						{
							maiden.getMaidenReds().put(npc, newVal);
						}
					}
				}
			}
		}
		return null;
	}

	private Color percentageToColor(float percentage)
	{
		percentage = Math.max(Math.min(100.0F, percentage), 0.0F);
		double rMod = 130.0D * percentage / 100.0D;
		double gMod = 235.0D * percentage / 100.0D;
		double bMod = 125.0D * percentage / 100.0D;
		return new Color((int) Math.min(255.0D, 255.0D - rMod), Math.min(255, (int)(20.0D + gMod)), Math.min(255, (int)(0.0D + bMod)));
	}

	public void determineLayer()
	{
		setLayer(config.mirrorMode() ? OverlayLayer.AFTER_MIRROR : OverlayLayer.ABOVE_SCENE);
	}
}
