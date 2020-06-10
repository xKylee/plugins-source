package net.runelite.client.plugins.coxhelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.GraphicID;
import net.runelite.api.GraphicsObject;
import net.runelite.api.NPC;
import net.runelite.api.coords.WorldPoint;

@Getter(AccessLevel.PACKAGE)
@Setter(AccessLevel.PACKAGE)
@Singleton
public class Olm
{
	private final Client client;
	private final CoxPlugin plugin;
	private final CoxConfig config;

	private final List<WorldPoint> healPools = new ArrayList<>();
	private final List<WorldPoint> portals = new ArrayList<>();
	private final Set<Victim> victims = new HashSet<>();
	private int portalTicks = 10;
	private Actor acidTarget = null;

	private boolean active = false; // in fight
	private boolean ready = false; // ready to attack
	private boolean firstPhase = false;

	private NPC hand = null;
	private NPC head = null;
	private int desyncs = 0;

	private int tickCycle = -1;

	private boolean crippled = false;
	private int crippleTicks = 45;

	private PrayAgainst prayer = null;
	private long lastPrayTime = 0;

	@Inject
	private Olm(final Client client, final CoxPlugin plugin, final CoxConfig config)
	{
		this.client = client;
		this.plugin = plugin;
		this.config = config;
	}

	public void nextPhase()
	{
		this.firstPhase = !active;
		this.active = true;
		this.ready = false;
		this.desyncs = 0;
		this.tickCycle = -1;
		this.healPools.clear();
		this.portals.clear();
		this.portalTicks = 10;
		this.crippled = false;
		this.crippleTicks = 45;
		this.prayer = null;
		this.lastPrayTime = 0;
	}

	public void hardRest()
	{
		this.active = false;
		this.ready = false;
		this.firstPhase = false;
		this.hand = null;
		this.head = null;
		this.desyncs = 0;
		this.tickCycle = -1;
		this.healPools.clear();
		this.portals.clear();
		this.portalTicks = 10;
		this.victims.clear();
		this.acidTarget = null;
		this.crippled = false;
		this.crippleTicks = 45;
		this.prayer = null;
		this.lastPrayTime = 0;
	}

	void setPrayer(PrayAgainst pray)
	{
		this.prayer = pray;
		lastPrayTime = System.currentTimeMillis();
	}

	void cripple()
	{
		this.crippled = true;
		this.crippleTicks = 45;
	}

	void uncripple()
	{
		this.crippled = false;
		this.crippleTicks = 45;
	}

	void updateCrippleSticks()
	{
		if (!crippled)
		{
			return;
		}

		this.crippleTicks--;
		if (this.crippleTicks <= 0)
		{
			this.crippled = false;
			crippleTicks = 45;
		}
	}

	void updateVictims()
	{
		if (this.victims.size() > 0)
		{
			this.victims.forEach(Victim::updateTicks);
			this.victims.removeIf(victim -> victim.getTicks() <= 0);
		}
	}

	void updateSpecials()
	{
		healPools.clear();
		portals.clear();
		client.clearHintArrow();

		for (GraphicsObject o : client.getGraphicsObjects())
		{
			if (o.getId() == GraphicID.OLM_TELEPORT)
			{
				portals.add(WorldPoint.fromLocal(client, o.getLocation()));
			}
			if (o.getId() == GraphicID.OLM_HEAL)
			{
				healPools.add(WorldPoint.fromLocal(client, o.getLocation()));
			}
			if (!portals.isEmpty())
			{
				portalTicks--;
				if (portalTicks <= 0)
				{
					client.clearHintArrow();
					portalTicks = 10;
				}
			}
		}
	}

	public void update()
	{
		this.updateVictims();
		this.updateCrippleSticks();
		this.updateSpecials();

		if (!ready && hand != null && hand.getCombatLevel() > 0)
		{
			ready = true;
			tickCycle = firstPhase ? 4 : 1;
			return;
		}


		tickCycle = tickCycle == 16 ? 1 : tickCycle + 1;
	}

	public int ticksUntilNextAction()
	{
		return this.tickCycle % 4 == 0 ?  1 : 4 - (this.tickCycle % 4) + 1;
	}

	public int actionCycle()
	{
		return (int) Math.ceil((double) this.tickCycle / 4);
	}

	public void intermentDesyncs()
	{
		this.desyncs++;
	}
}
