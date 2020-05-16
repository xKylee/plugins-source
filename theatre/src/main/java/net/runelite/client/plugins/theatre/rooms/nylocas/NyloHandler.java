package net.runelite.client.plugins.theatre.rooms.nylocas;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.MenuOpcode;
import net.runelite.api.NPC;
import net.runelite.api.Perspective;
import net.runelite.api.Player;
import net.runelite.api.PlayerAppearance;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.kit.KitType;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.menus.MenuManager;
import net.runelite.client.plugins.theatre.RoomHandler;
import net.runelite.client.plugins.theatre.TheatreConfig;
import net.runelite.client.plugins.theatre.TheatreConstant;
import net.runelite.client.plugins.theatre.TheatrePlugin;
import net.runelite.client.plugins.theatre.TheatreRoom;
import net.runelite.client.util.WeaponMap;
import net.runelite.client.util.WeaponStyle;
import org.apache.commons.lang3.ObjectUtils;

@Slf4j
public class NyloHandler extends RoomHandler
{
	private static final String MESNAME = "tobmes";
	private static final String MAGE_NYLO = "Nylocas Hagios";
	private static final String RANGE_NYLO = "Nylocas Toxobolos";
	private static final String MELEE_NYLO = "Nylocas Ischyros";
	final List<NPC> waveSpawns = new ArrayList<>();
	final List<NPC> waveAgros = new ArrayList<>();
	private final MenuManager menuManager;
	private final EventBus eventBus;
	public long startTime = 0L;
	int startTick = 0;
	@Getter(AccessLevel.PUBLIC)
	private Map<NPC, Integer> pillars = new HashMap<>();
	@Getter(AccessLevel.PUBLIC)
	private Map<NPC, Integer> spiders = new HashMap<>();
	@Getter(AccessLevel.PACKAGE)
	@Setter(AccessLevel.PACKAGE)
	private int wave = 0;
	private NyloOverlay overlay = null;
	private NyloPredictor predictor = null;
	private WeaponStyle currentWeaponStyle;
	private boolean skipTickCheck = false;

	public NyloHandler(final Client client, final TheatrePlugin plugin, final TheatreConfig config, final MenuManager menuManager, final EventBus eventBus)
	{
		super(client, plugin, config);
		this.menuManager = menuManager;
		this.eventBus = eventBus;
	}

	@Override
	public void onStart()
	{
		if (plugin.getRoom() == TheatreRoom.NYLOCAS)
		{
			return;
		}

		reset();

		plugin.setRoom(TheatreRoom.NYLOCAS);
		if (overlay == null && config.showNylocasAmount())
		{
			overlay = new NyloOverlay(client, plugin, this, config);
			plugin.getOverlayManager().add(overlay);
		}

		startTime = System.currentTimeMillis();
		startTick = client.getTickCount();
		if (config.nylocasMenuSwap())
		{
			eventBus.subscribe(MenuOptionClicked.class, MESNAME, this::onMenuOptionClicked);
			eventBus.subscribe(MenuEntryAdded.class, MESNAME, this::onMenuEntryAdded);
		}

	}

	@Override
	public void onStop()
	{
		reset();

		plugin.setRoom(TheatreRoom.UNKNOWN);

		if (overlay != null)
		{
			plugin.getOverlayManager().remove(overlay);
			overlay = null;
		}

		predictor = null;

		long elapsedTime = System.currentTimeMillis() - startTime;
		long seconds = elapsedTime / 1000L;

		long minutes = seconds / 60L;
		seconds = seconds % 60;

		if (startTime != 0 && config.extraTimers())
		{
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Wave 'The Nylocas - Waves' " +
				"completed! Duration: <col=ff0000>" + minutes + ":" + twoDigitString(seconds), null);
		}
	}

	private void reset()
	{
		pillars.clear();
		spiders.clear();
		wave = 0;
		predictor = new NyloPredictor(client, this);
		waveSpawns.clear();
		waveAgros.clear();
		predictor.reset();
	}

	public void onConfigChanged()
	{
		if (config.nylocasMenuSwap())
		{
			eventBus.subscribe(MenuOptionClicked.class, MESNAME, this::onMenuOptionClicked);
			eventBus.subscribe(MenuEntryAdded.class, MESNAME, this::onMenuEntryAdded);
		}
		else
		{
			eventBus.unregister(MESNAME);
			reset();
		}
		if (plugin.getRoom() != TheatreRoom.NYLOCAS)
		{
			return;
		}

		if (overlay == null && config.showNylocasAmount())
		{
			overlay = new NyloOverlay(client, plugin, this, config);
			plugin.getOverlayManager().add(overlay);
		}
		else if (overlay != null && !config.showNylocasAmount())
		{
			plugin.getOverlayManager().remove(overlay);
			overlay = null;
		}

		if (config.mirrorMode())
		{
			overlay.determineLayer();
			plugin.getOverlayManager().remove(overlay);
			plugin.getOverlayManager().add(overlay);
		}
	}

	private Color healthColorCode(int health)
	{
		health = Math.max(health, 0);
		health = Math.min(health, 100);

		double rMod = 130.0 * health / 100.0;
		double gMod = 255.0 * health / 100.0;
		double bMod = 125.0 * health / 100.0;

		return new Color((int) (255 - rMod), (int) (0 + gMod), (int) (0 + bMod));
	}

	public void render(Graphics2D graphics)
	{
		if (config.showNyloPillarHealth())
		{
			for (Map.Entry<NPC, Integer> pillars : pillars.entrySet())
			{
				final int health = pillars.getValue();
				final String healthStr = health + "%";
				WorldPoint p = pillars.getKey().getWorldLocation();
				LocalPoint lp = LocalPoint.fromWorld(client, p.getX() + 1, p.getY() + 1);

				Color c = healthColorCode(health);
				Point canvasPoint = null;
				if (lp != null)
				{
					canvasPoint = Perspective.localToCanvas(client, lp, client.getPlane(), 65);
				}
				renderTextLocation(graphics, healthStr, 13, Font.BOLD, c, canvasPoint);
			}
		}

		switch (config.showNylocasExplosions())
		{
			case TILE:
				for (Map.Entry<NPC, Integer> spiders : spiders.entrySet())
				{
					int ticksLeft = spiders.getValue();
					if (ticksLeft > -1 && ticksLeft < 6)
					{
						Color color = new Color(255, 255, 0, 180);
						int outlineWidth = 2;
						int outlineAlpha = 150;
						renderNpcOverlay(graphics, spiders.getKey(), color, outlineWidth, outlineAlpha, 15);
					}
				}
				break;
			case TIMER:
				for (NPC npc : spiders.keySet())
				{
					int ticksLeft = spiders.get(npc);
					if (ticksLeft > -1 && ticksLeft < 15)
					{
						String str = Integer.toString(ticksLeft);
						LocalPoint lp = npc.getLocalLocation();
						Point point = Perspective.getCanvasTextLocation(client, graphics, lp, str, 0);
						renderTextLocation(graphics, str, 13, Font.BOLD, healthColorCode(ticksLeft), point);
					}
				}
				break;
			case NONE:
				break;
		}

		Set<NPC> toHighlight = new HashSet<>();

		if (config.highlightNyloAgros())
		{
			for (NPC npc : new ArrayList<>(waveAgros))
			{
				try
				{
					if (npc.getHealthRatio() == 0 || npc.isDead())
					{
						waveAgros.remove(npc);
						continue;
					}

					toHighlight.add(npc);
				}
				catch (Exception ignored)
				{

				}
			}
		}

		for (NPC npc : toHighlight)
		{
			try
			{
				Shape objectClickbox = npc.getConvexHull();

				Color color;
				String name = npc.getName() != null ? npc.getName() : "";

				if (name.contains("Hagios"))
				{
					color = Color.CYAN;
				}
				else if (name.contains("Toxobolos"))
				{
					color = Color.GREEN;
				}
				else
				{
					color = Color.LIGHT_GRAY;
				}

				graphics.setColor(color);
				graphics.draw(objectClickbox);
			}
			catch (Exception ignored)
			{

			}
		}
	}

	public void onNpcSpawned(NpcSpawned event)
	{
		NPC npc = event.getNpc();
		int id = npc.getId();

		if (id == TheatreConstant.NPC_ID_NYLOCAS_PILLAR)
		{
			onStart();
			pillars.put(npc, 100);
			recalculateLocal();
		}
		else if (npc.getName() != null && plugin.getRoom() == TheatreRoom.NYLOCAS)
		{
			Pattern p = Pattern.compile("Nylocas (Hagios|Toxobolos|Ischyros)");
			Matcher m = p.matcher(npc.getName());
			if (m.matches())
			{
				spiders.put(npc, 52);

				if (predictor != null)
				{
					predictor.onNpcSpawned(event);
				}
			}
			else if (npc.getName().equals("Nylocas Vasilias"))
			{
				onStop();
			}
		}
	}

	public void onNpcDespawned(NpcDespawned event)
	{
		NPC npc = event.getNpc();
		int id = npc.getId();

		waveSpawns.remove(npc);

		waveAgros.remove(npc);

		if (id == TheatreConstant.NPC_ID_NYLOCAS_PILLAR)
		{
			pillars.remove(npc);
		}
		else if (npc.getName() != null && plugin.getRoom() == TheatreRoom.NYLOCAS)
		{
			Pattern p = Pattern.compile("Nylocas (Hagios|Toxobolos|Ischyros)");
			Matcher m = p.matcher(npc.getName());
			if (m.matches())
			{
				spiders.remove(npc);
			}
		}
	}

	private void renderPoly(Graphics2D graphics, Color color, Polygon polygon)
	{
		if (polygon != null)
		{
			graphics.setColor(color);
			graphics.setStroke(new BasicStroke(1));
			graphics.draw(polygon);
			graphics.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 20));
			graphics.fill(polygon);
		}
	}

	public void onGameTick()
	{
		if (plugin.getRoom() != TheatreRoom.NYLOCAS)
		{
			return;
		}
		if (skipTickCheck)
		{
			skipTickCheck = false;
		}
		else
		{
			Player p = client.getLocalPlayer();
			if (p == null)
			{
				return;
			}
			PlayerAppearance pa = p.getPlayerAppearance();
			if (pa == null)
			{
				return;
			}
			int weaponID = ObjectUtils.defaultIfNull(pa.getEquipmentId(KitType.WEAPON), -1);
			currentWeaponStyle = WeaponMap.StyleMap.get(weaponID);
		}
		boolean findPillar = false;

		for (NPC npc : client.getNpcs())
		{
			if (npc.getId() == 8358)
			{
				findPillar = true;
				break;
			}
		}

		if (!findPillar)
		{
			this.onStop();
			return;
		}


		for (NPC spider : new ArrayList<>(spiders.keySet()))
		{
			int ticksLeft = spiders.get(spider);

			if (ticksLeft < 0)
			{
				spiders.remove(spider);
				continue;
			}

			spiders.replace(spider, ticksLeft - 1);
		}

		this.recalculateLocal();
		for (NPC pillar : pillars.keySet())
		{
			int healthPercent = pillar.getHealthRatio();
			if (healthPercent > -1)
			{
				pillars.replace(pillar, healthPercent);
			}
		}
	}

	private void onMenuOptionClicked(MenuOptionClicked event)
	{
		int opcode = event.getOpcode();
		if (opcode == MenuOpcode.ITEM_SECOND_OPTION.getId())
		{
			WeaponStyle newStyle = WeaponMap.StyleMap.get(event.getIdentifier());
			if (newStyle != null)
			{
				skipTickCheck = true;
				currentWeaponStyle = newStyle;
			}
		}
	}

	private void onMenuEntryAdded(MenuEntryAdded event)
	{
		if (config.nylocasMenuSwap())
		{
			if (event.getOpcode() == MenuOpcode.NPC_SECOND_OPTION.getId() && event.getOption().equals("Attack"))
			{
				String target = event.getTarget();
				switch (currentWeaponStyle)
				{
					case MAGIC:
						if (target.equals(RANGE_NYLO) || target.equals(MELEE_NYLO))
						{
							client.setMenuOptionCount(client.getMenuOptionCount() - 1);
						}
						break;
					case RANGE:
						if (target.contains(MAGE_NYLO) || target.contains(MELEE_NYLO))
						{
							client.setMenuOptionCount(client.getMenuOptionCount() - 1);
						}
						break;
					case MELEE:
						if (target.contains(MAGE_NYLO) || target.contains(RANGE_NYLO))
						{
							client.setMenuOptionCount(client.getMenuOptionCount() - 1);
						}
						break;
					default:
				}
			}
		}
	}

	private void recalculateLocal()
	{
		if (pillars != null && pillars.size() == 4)
		{
			int minX = Integer.MAX_VALUE;
			int minY = Integer.MAX_VALUE;
			for (NPC npc : pillars.keySet())
			{
				LocalPoint lp = npc.getLocalLocation();

				if (lp.getSceneX() < minX)
				{
					minX = lp.getSceneX();
				}

				if (lp.getSceneY() < minY)
				{
					minY = lp.getSceneY();
				}
			}

			int centerX = minX + 5;
			int centerY = minY + 5;

			if (predictor != null)
			{
				predictor.southBound = centerY - 12;
				predictor.eastBound = centerX + 13;
				predictor.westBound = centerX - 12;
			}
		}
	}
}
