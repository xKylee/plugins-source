/*
 * THIS PLUGIN WAS WRITTEN BY A KEYBOARD-WIELDING MONKEY BOI BUT SHUFFLED BY A KANGAROO WITH THUMBS.
 * The plugin and it's refactoring was intended for xKylee's Externals but I'm sure if you're reading this, you're probably planning to yoink..
 * or you're just genuinely curious. If you're trying to yoink, it doesn't surprise me.. just don't claim it as your own. Cheers.
 */

package net.runelite.client.plugins.theatre.Verzik;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.text.DecimalFormat;
import java.util.Iterator;
import javax.inject.Inject;
import net.runelite.api.GraphicsObject;
import net.runelite.api.NPC;
import net.runelite.api.NPCDefinition;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.Projectile;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.theatre.RoomOverlay;
import net.runelite.client.plugins.theatre.TheatreConfig;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayUtil;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class VerzikOverlay extends RoomOverlay
{
	private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#0.0");
	private static final int VERZIK_GREEN_BALL = 1598;

	@Inject
	private Verzik verzik;

	@Inject
	protected VerzikOverlay(TheatreConfig config)
	{
		super(config);
		determineLayer();
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (verzik.isVerzikActive())
		{
			if (config.verzikTileOverlay())
			{
				int size = 1;
				final NPCDefinition composition = verzik.getVerzikNPC().getTransformedDefinition();
				if (composition != null)
				{
					size = composition.getSize();
				}
				LocalPoint lp = LocalPoint.fromWorld(client, verzik.getVerzikNPC().getWorldLocation());
				if (lp != null)
				{
					Polygon tilePoly = getCanvasTileAreaPoly(client, lp, size, false);
					if (tilePoly != null)
					{
						if (verzik.isVerzikEnraged())
						{
							renderPoly(graphics, new Color(255, 110, 90), tilePoly);
						}
						else
						{
							renderPoly(graphics, new Color(255, 110, 230), tilePoly);
						}
					}
				}
			}

			String tick_text = "";
			if (config.verzikAttackCounter() && verzik.getVerzikSpecial() != Verzik.SpecialAttack.WEBS)
			{
				tick_text += "[A] " + verzik.getVerzikAttackCount();
				if (config.verzikAutosTick() || config.verzikTotalTickCounter())
				{
					tick_text += " : ";
				}
			}
			if (config.verzikAutosTick() && verzik.getVerzikSpecial() != Verzik.SpecialAttack.WEBS)
			{
				tick_text += verzik.getVerzikTicksUntilAttack();
				if (config.verzikTotalTickCounter())
				{
					tick_text += " : ";
				}
			}
			if (config.verzikTotalTickCounter())
			{
				tick_text += "(" + verzik.getVerzikTotalTicksUntilAttack() + ")";
			}
			Point canvasPoint = verzik.getVerzikNPC().getCanvasTextLocation(graphics, tick_text, 60);

			if (canvasPoint != null)
			{
				Color col = verzik.verzikSpecialWarningColor();
				renderTextLocation(graphics, tick_text, col, canvasPoint);
			}

			if (verzik.getVerzikPhase() == Verzik.Phase.PHASE2)
			{
				if (config.verzikProjectiles())
				{
					Iterator iterator = verzik.getVerzikRangeProjectiles().values().iterator();

					while (iterator.hasNext())
					{
						drawTile(graphics, (WorldPoint) iterator.next(), config.verzikProjectilesColor(), 1, 255, 20);
					}
				}

				if (config.verzikReds())
				{
					verzik.getVerzikReds().forEach((k, v) ->
					{
						int v_health = v.getValue();
						int v_healthRation = v.getKey();
						if (k.getName() != null && k.getHealthScale() > 0)
						{
							v_health = k.getHealthScale();
							v_healthRation = Math.min(v_healthRation, k.getHealthRatio());
						}
						float percentage = ((float) v_healthRation / (float) v_health) * 100f;
						Point textLocation = k.getCanvasTextLocation(graphics, String.valueOf(DECIMAL_FORMAT.format(percentage)), 80);

						if (textLocation != null)
						{
							renderTextLocation(graphics, String.valueOf(DECIMAL_FORMAT.format(percentage)), Color.WHITE, textLocation);
						}
					});

					NPC[] reds = verzik.getVerzikReds().keySet().toArray(new NPC[0]);
					for (NPC npc : reds)
					{
						if (npc.getName() != null && npc.getHealthScale() > 0 && npc.getHealthRatio() < 100)
						{
							Pair<Integer, Integer> newVal = new MutablePair<>(npc.getHealthRatio(), npc.getHealthScale());
							if (verzik.getVerzikReds().containsKey(npc))
							{
								verzik.getVerzikReds().put(npc, newVal);
							}
						}
					}
				}
			}

			if (verzik.getVerzikPhase() == Verzik.Phase.PHASE3)
			{
				if (config.verzikDisplayTank())
				{
					if (verzik.getVerzikNPC().getInteracting() != null)
					{
						Polygon tilePoly = verzik.getVerzikNPC().getInteracting().getCanvasTilePoly();
						if (tilePoly != null)
						{
							renderPoly(graphics, Color.LIGHT_GRAY, tilePoly);
						}
					}
				}

				if (config.verzikTornado() && (!config.verzikPersonalTornadoOnly() || (config.verzikPersonalTornadoOnly() && verzik.getVerzikLocalTornado() != null)))
				{
					verzik.getVerzikTornadoes().forEach(k ->
					{
						if (k.getCurrentPosition() != null)
						{
							drawTile(graphics, k.getCurrentPosition(), config.verzikTornadoColor(), 1, 120, 10);
						}

						if (k.getLastPosition() != null)
						{
							drawTile(graphics, k.getLastPosition(), config.verzikTornadoColor(), 2, 180, 20);
						}
					});
				}

				if (config.verzikYellows())
				{
					if (verzik.getVerzikYellows() > 0)
					{
						String text = Integer.toString(verzik.getVerzikYellows());

						for (GraphicsObject object : client.getGraphicsObjects())
						{
							if (object.getId() == 1595)
							{
								drawTile(graphics, WorldPoint.fromLocal(client, object.getLocation()), Color.YELLOW, 1, 255, 0);
								LocalPoint lp = object.getLocation();
								Point point = Perspective.getCanvasTextLocation(client, graphics, lp, text, 0);
								renderTextLocation(graphics, text, Color.WHITE, point);
							}
						}
					}
				}

				if (config.verzikGreenBall())
				{
					for (Projectile p : client.getProjectiles())
					{
						if (p.getId() == VERZIK_GREEN_BALL)
						{
							Polygon tilePoly;
							if (config.verzikGreenBallMarker() == TheatreConfig.VERZIKBALLTILE.TILE)
							{
								tilePoly = p.getInteracting().getCanvasTilePoly();
							}
							else
							{
								tilePoly = getCanvasTileAreaPoly(client, p.getInteracting().getLocalLocation(), 3, true);
							}

							if (tilePoly != null)
							{
								renderPoly(graphics, config.verzikGreenBallColor(), tilePoly);
							}
						}
					}
				}
			}

			if (verzik.getVerzikPhase() == Verzik.Phase.PHASE2 || verzik.getVerzikPhase() == Verzik.Phase.PHASE3)
			{
				if (config.verzikNyloPersonalWarning() || config.verzikNyloOtherWarning())
				{
					verzik.getVerzikAggros().forEach(k ->
					{
						if (k.getInteracting() != null && !k.isDead())
						{
							if ((config.verzikNyloPersonalWarning() && k.getInteracting() == client.getLocalPlayer())
								|| (config.verzikNyloOtherWarning() && k.getInteracting() != client.getLocalPlayer()))
							{
								Color color = Color.LIGHT_GRAY;
								if (k.getInteracting() == client.getLocalPlayer())
								{
									color = Color.YELLOW;
								}

								Point textLocation = k.getCanvasTextLocation(graphics, k.getInteracting().getName(), 80);
								if (textLocation != null)
								{
									OverlayUtil.renderTextLocation(graphics, textLocation, k.getInteracting().getName(), color);
								}

								if (config.verzikNyloExplodeAOE())
								{
									int size = 1;
									int thick_size = 1;
									final NPCDefinition composition = k.getTransformedDefinition();

									if (composition != null)
									{
										size = composition.getSize() + 2 * thick_size;
									}

									LocalPoint lp = LocalPoint.fromWorld(client, k.getWorldLocation());
									if (lp != null)
									{
										lp = new LocalPoint(lp.getX() - thick_size * Perspective.LOCAL_TILE_SIZE, lp.getY() - thick_size * Perspective.LOCAL_TILE_SIZE);

										Polygon tilePoly = getCanvasTileAreaPoly(client, lp, size, false);
										if (tilePoly != null)
										{
											renderPoly(graphics, color, tilePoly);
										}
									}
								}
							}
						}
					});
				}
			}

		}
		return null;
	}

	public void determineLayer()
	{
		setLayer(config.mirrorMode() ? OverlayLayer.AFTER_MIRROR : OverlayLayer.ABOVE_SCENE);
	}

}
