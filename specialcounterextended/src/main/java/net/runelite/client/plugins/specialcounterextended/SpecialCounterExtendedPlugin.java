package net.runelite.client.plugins.specialcounterextended;

import com.google.inject.Provides;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.GameState;
import net.runelite.api.Hitsplat;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.NPC;
import net.runelite.api.VarPlayer;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.events.InteractingChanged;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginInstantiationException;
import net.runelite.client.plugins.PluginManager;
import net.runelite.client.plugins.PluginType;
import net.runelite.client.plugins.socket.SocketPlugin;
import net.runelite.client.plugins.socket.org.json.JSONObject;
import net.runelite.client.plugins.socket.packet.SocketBroadcastPacket;
import net.runelite.client.plugins.socket.packet.SocketReceivePacket;
import net.runelite.client.plugins.specialcounter.SpecialCounterPlugin;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;
import org.pf4j.Extension;

@Extension
@PluginDescriptor(
	name = "Special Attack Counter (Extended)",
	description = "Track special attacks used on NPCs using server sockets.",
	tags = {"socket", "server", "discord", "connection", "broadcast", "combat", "npcs", "overlay"},
	enabledByDefault = false,
	type = PluginType.PVM
)

@Slf4j
@PluginDependency(SpecialCounterPlugin.class)
@PluginDependency(SocketPlugin.class)
public class SpecialCounterExtendedPlugin extends Plugin
{

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private PluginManager pluginManager;

	@Inject
	private InfoBoxManager infoBoxManager;

	@Inject
	private ItemManager itemManager;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private EventBus eventBus;

	@Inject
	private SpecialCounterOverlay overlay;

	@Inject
	private SpecialCounterExtendedConfig config;

	@Inject
	private SocketPlugin socketPlugin;

	@Inject
	private SpecialCounterPlugin specialCounterPlugin;

	@Provides
	SpecialCounterExtendedConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(SpecialCounterExtendedConfig.class);
	}

	@Override
	protected void startUp()
	{
		currentWorld = -1;
		specialPercentage = -1;
		lastSpecTarget = null;
		lastSpecTick = -1;
		interactedNpcIds.clear();

		specialUsed = false;
		specialExperience = -1;

		this.overlayManager.add(this.overlay);

		try
		{
			pluginManager.setPluginEnabled(specialCounterPlugin, false);
			pluginManager.stopPlugin(specialCounterPlugin);
		}
		catch (PluginInstantiationException ex)
		{
			log.error("error stopping plugin", ex);
		}
	}

	@Override
	protected void shutDown()
	{
		removeCounters();
		this.overlayManager.remove(this.overlay);
	}

	private int currentWorld;

	@Subscribe // If you hop worlds, reset the current spec counter.
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGGED_IN)
		{
			if (currentWorld == -1)
			{
				currentWorld = client.getWorld();
			}
			else if (currentWorld != client.getWorld())
			{
				currentWorld = client.getWorld();
				removeCounters();
			}
		}
	}

	private int specialPercentage;
	private Actor lastSpecTarget;
	private int lastSpecTick;

	private SpecialWeapon specialWeapon;

	private final Set<Integer> interactedNpcIds = new HashSet<>();
	private final SpecialCounter[] specialCounter = new SpecialCounter[SpecialWeapon.values().length];

	private boolean specialUsed = false;
	private long specialExperience = -1;

	@Subscribe // Player changed attack targets after queuing special.
	public void onInteractingChanged(InteractingChanged interactingChanged)
	{
		Actor source = interactingChanged.getSource();
		Actor target = interactingChanged.getTarget();

		if (lastSpecTick != client.getTickCount() || source != client.getLocalPlayer() || target == null)
		{
			return;
		}

		lastSpecTarget = target;
	}

	@Subscribe // Player queues special attack.
	public void onVarbitChanged(VarbitChanged event)
	{
		int specialPercentage = client.getVar(VarPlayer.SPECIAL_ATTACK_PERCENT);

		if (this.specialPercentage == -1 || specialPercentage >= this.specialPercentage)
		{
			this.specialPercentage = specialPercentage;
			return;
		}

		this.specialPercentage = specialPercentage;
		this.specialWeapon = usedSpecialWeapon();

		this.lastSpecTarget = client.getLocalPlayer().getInteracting();
		this.lastSpecTick = client.getTickCount();

		this.specialUsed = true;
		this.specialExperience = this.client.getOverallExperience();
	}

	@Subscribe // For Dawnbringer, EXP tracked.
	public void onGameTick(GameTick event)
	{
		if (client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}

		if (this.specialExperience != -1 && this.specialUsed && this.lastSpecTarget != null && this.lastSpecTarget instanceof NPC)
		{
			this.specialUsed = false;

			long deltaExp = this.client.getOverallExperience() - this.specialExperience;
			this.specialExperience = -1;

			if (this.specialWeapon != null && this.specialWeapon == SpecialWeapon.DAWNBRINGER)
			{
				int damage = (int) (((double) deltaExp) / 3.5d);

				String pName = this.client.getLocalPlayer().getName();
				updateCounter(pName, this.specialWeapon, null, damage);

				if (pluginManager.isPluginEnabled(socketPlugin))
				{
					JSONObject data = new JSONObject();
					data.put("player", pName);
					data.put("target", ((NPC) this.lastSpecTarget).getId());
					data.put("weapon", specialWeapon.ordinal());
					data.put("hit", damage);

					JSONObject payload = new JSONObject();
					payload.put("special-extended", data);
					eventBus.post(SocketBroadcastPacket.class, new SocketBroadcastPacket(payload));
				}

				this.lastSpecTarget = null;
			}
		}
	}

	@Subscribe
	public void onHitsplatApplied(HitsplatApplied hitsplatApplied)
	{
		Actor target = hitsplatApplied.getActor();
		Hitsplat hitsplat = hitsplatApplied.getHitsplat();
		Hitsplat.HitsplatType hitsplatType = hitsplat.getHitsplatType();

		// Ignore all hitsplats other than mine
		if ((hitsplatType != Hitsplat.HitsplatType.DAMAGE_ME && hitsplatType != Hitsplat.HitsplatType.BLOCK_ME) || target == client.getLocalPlayer())
		{
			return;
		}

		log.debug("Hitsplat target: {} spec target: {}", target, lastSpecTarget);

		// If waiting for a spec, ignore hitsplats not on the actor we specced
		if (lastSpecTarget != null && lastSpecTarget != target)
		{
			return;
		}

		boolean wasSpec = lastSpecTarget != null;
		lastSpecTarget = null;
		specialUsed = false;
		specialExperience = -1L;

		if (!(target instanceof NPC))
		{
			return;
		}

		NPC npc = (NPC) target;
		int interactingId = npc.getId();

		// If this is a new NPC reset the counters
		if (!interactedNpcIds.contains(interactingId))
		{
			removeCounters();
			addInteracting(interactingId);
		}

		if (wasSpec && specialWeapon != null && hitsplat.getAmount() > 0)
		{
			int hit = getHit(specialWeapon, hitsplat);
			log.debug("Special attack target: id: {} - target: {} - weapon: {} - amount: {}", interactingId, target, specialWeapon, hit);

			final String pName = this.client.getLocalPlayer().getName();
			updateCounter(pName, specialWeapon, null, hit);

			if (pluginManager.isPluginEnabled(socketPlugin))
			{
				JSONObject data = new JSONObject();
				data.put("player", pName);
				data.put("target", interactingId);
				data.put("weapon", specialWeapon.ordinal());
				data.put("hit", hit);

				JSONObject payload = new JSONObject();
				payload.put("special-extended", data);
				eventBus.post(SocketBroadcastPacket.class, new SocketBroadcastPacket(payload));
			}
		}
	}

	@Subscribe
	public void onSocketReceivePacket(SocketReceivePacket event)
	{
		if (pluginManager.isPluginEnabled(socketPlugin))
		{
			try
			{
				JSONObject payload = event.getPayload();
				if (!payload.has("special-extended"))
				{
					return;
				}

				final String pName = this.client.getLocalPlayer().getName();

				final JSONObject data = payload.getJSONObject("special-extended");
				if (data.getString("player").equals(pName))
				{
					return; // We ignore self.
				}

				clientThread.invoke(() -> {
					SpecialWeapon weapon = SpecialWeapon.values()[data.getInt("weapon")];
					String attacker = data.getString("player");
					updateCounter(attacker, weapon, attacker, data.getInt("hit"));
				});
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	private void addInteracting(int npcId)
	{
		interactedNpcIds.add(npcId);

		// Add alternate forms of bosses
		final Boss boss = Boss.getBoss(npcId);
		if (boss != null)
		{
			interactedNpcIds.addAll(boss.getIds());
		}
	}

	private int getHit(SpecialWeapon specialWeapon, Hitsplat hitsplat)
	{
		return specialWeapon.isDamage() ? hitsplat.getAmount() : 1;
	}

	private SpecialWeapon usedSpecialWeapon()
	{
		ItemContainer equipment = client.getItemContainer(InventoryID.EQUIPMENT);

		if (equipment == null)
		{
			return null;
		}

		Item[] items = equipment.getItems();
		int weaponIdx = EquipmentInventorySlot.WEAPON.getSlotIdx();

		if (items == null || weaponIdx >= items.length)
		{
			return null;
		}

		Item weapon = items[weaponIdx];

		for (SpecialWeapon specialWeapon : SpecialWeapon.values())
		{
			if (specialWeapon.getItemID() == weapon.getId())
			{
				return specialWeapon;
			}
		}

		return null;
	}

	private void updateCounter(String player, SpecialWeapon specialWeapon, String name, int hit)
	{
		SpecialCounter counter = specialCounter[specialWeapon.ordinal()];

		BufferedImage image = itemManager.getImage(specialWeapon.getItemID());
//        this.overlay.addOverlay(player, new SpecialIcon(image, specialWeapon.isDamage() ? Integer.toString(hit) : null, System.currentTimeMillis()));
		this.overlay.addOverlay(player, new SpecialIcon(image, Integer.toString(hit), System.currentTimeMillis()));

		if (counter == null)
		{
			counter = new SpecialCounter(image, this,
				hit, specialWeapon);
			infoBoxManager.addInfoBox(counter);
			specialCounter[specialWeapon.ordinal()] = counter;
		}
		else
		{
			counter.addHits(hit);
		}

		// If in a party, add hit to partySpecs for the infobox tooltip
		Map<String, Integer> partySpecs = counter.getPartySpecs();
		if (partySpecs.containsKey(name))
		{
			partySpecs.put(name, hit + partySpecs.get(name));
		}
		else
		{
			partySpecs.put(name, hit);
		}
	}

	private void removeCounters()
	{
		interactedNpcIds.clear();
		for (int i = 0; i < specialCounter.length; ++i)
		{
			SpecialCounter counter = specialCounter[i];
			if (counter != null)
			{
				infoBoxManager.removeInfoBox(counter);
				specialCounter[i] = null;
			}
		}
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned npcDespawned)
	{
		NPC actor = npcDespawned.getNpc();

		if (lastSpecTarget == actor)
		{
			lastSpecTarget = null;
		}

		if (actor.isDead() && interactedNpcIds.contains(actor.getId()))
		{
			removeCounters();
		}
	}
}