/*
 * Copyright (c) 2018, Raqes <j.raqes@gmail.com>
 * Copyright (c) 2020, Charles <github.com/kthisiscvpv>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.runelite.client.plugins.socketspecialcounter;

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
import net.runelite.api.Skill;
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
	name = "Socket Special Attack",
	description = "Track DWH, Arclight, Darklight, and BGS special attacks used on NPCs using server sockets.",
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
	private InfoBoxManager infoBoxManager;

	@Inject
	private ItemManager itemManager;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private PluginManager pluginManager;

	@Inject
	private SocketPlugin socketPlugin;

	@Inject
	private SpecialCounterPlugin specialCounterPlugin;

	@Inject
	private EventBus eventBus;

	@Inject
	private SpecialCounterOverlay overlay;

	@Inject
	private SpecialCounterExtendedConfig config;

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
		magicExperience = -1;

		overlayManager.add(overlay);

		try
		{
			pluginManager.setPluginEnabled(specialCounterPlugin, false);
			pluginManager.stopPlugin(specialCounterPlugin);
		}
		catch (PluginInstantiationException ex)
		{
			log.error("An error occured when trying to stop duplicate plugin Special Counter Plugin", ex);
		}
	}

	@Override
	protected void shutDown()
	{
		removeCounters();
		overlayManager.remove(overlay);
	}

	private int currentWorld;

	@Subscribe // If you hop worlds, reset the current spec counter.
	public void onGameStateChanged(GameStateChanged event)
	{
		GameState state = event.getGameState();
		if (state == GameState.LOGGED_IN)
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
		else if (state == GameState.LOGIN_SCREEN)
		{
			removeCounters();
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
	private long magicExperience = -1;

	@Subscribe // Player changed attack targets after queuing special.
	public void onInteractingChanged(InteractingChanged interactingChanged)
	{
		Actor source = interactingChanged.getSource();
		Actor target = interactingChanged.getTarget();

		if (lastSpecTick != client.getTickCount() || source != client.getLocalPlayer() ||
			target == null)
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
		specialWeapon = usedSpecialWeapon();

		lastSpecTarget = client.getLocalPlayer().getInteracting();
		lastSpecTick = client.getTickCount();

		specialUsed = true;
		specialExperience = client.getOverallExperience();
		magicExperience = client.getSkillExperience(Skill.MAGIC);
	}

	@Subscribe // For Dawnbringer, EXP tracked.
	public void onGameTick(GameTick event)
	{
		if (client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}

		if (!config.guessDawnbringer()) // They want to wait for the Hitsplat instead.
		{
			return;
		}

		if (specialExperience != -1 && specialUsed && lastSpecTarget != null &&
			lastSpecTarget instanceof NPC)
		{
			specialUsed = false;

			long deltaExp = client.getOverallExperience() - specialExperience;
			specialExperience = -1;

			long deltaMagicExp = client.getSkillExperience(Skill.MAGIC) - magicExperience;
			magicExperience = -1;

			if (specialWeapon != null && specialWeapon == SpecialWeapon.DAWNBRINGER)
			{
				int currentAttackStyleVarbit = client.getVar(VarPlayer.ATTACK_STYLE);

				// This formula causes a 1-off error (sometimes) as exp is a float on Jagex's servers.
				int damage;
				if (currentAttackStyleVarbit == 3)
				{ // Defensive Casting
					damage = (int) Math.round(((double) deltaMagicExp) / 1.4d);
				}
				else
				{ // Agressive Casting
					damage = (int) Math.round(((double) deltaExp) / 3.5d);
				}

				String pName = client.getLocalPlayer().getName();
				updateCounter(pName, specialWeapon, null, damage);

				if (pluginManager.isPluginEnabled(socketPlugin))
				{
					JSONObject data = new JSONObject();
					data.put("player", pName);
					data.put("target", ((NPC) lastSpecTarget).getId());
					data.put("weapon", specialWeapon.ordinal());
					data.put("hit", damage);

					JSONObject payload = new JSONObject();
					payload.put("special-extended", data);
					eventBus.post(SocketBroadcastPacket.class, new SocketBroadcastPacket(payload));
				}

				lastSpecTarget = null;
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
		if (!hitsplat.isMine() || target == client.getLocalPlayer())
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
		magicExperience = -1L;

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
			log.debug("Special attack target: id: {} - target: {} - weapon: {} - amount: {}",
				interactingId, target, specialWeapon, hit);

			final String pName = client.getLocalPlayer().getName();
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
		try
		{
			if (client.getGameState() != GameState.LOGGED_IN)
			{
				return;
			}

			JSONObject payload = event.getPayload();
			if (!payload.has("special-extended"))
			{
				return;
			}

			final String pName = client.getLocalPlayer().getName();

			final JSONObject data = payload.getJSONObject("special-extended");
			if (data.getString("player").equals(pName))
			{
				return; // We ignore self.
			}

			clientThread.invoke(() -> {
				SpecialWeapon weapon = SpecialWeapon.values()[data.getInt("weapon")];
				String attacker = data.getString("player");
				int targetId = data.getInt("target");

				// If this is a new NPC reset the counters
				if (!interactedNpcIds.contains(targetId))
				{
					removeCounters();
					addInteracting(targetId);
				}

				updateCounter(attacker, weapon, attacker, data.getInt("hit"));
			});
		}
		catch (Exception e)
		{
			e.printStackTrace();
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
		// Hotfix for ornaments.
		if (specialWeapon == SpecialWeapon.BANDOS_GODSWORD_OR)
		{
			specialWeapon = SpecialWeapon.BANDOS_GODSWORD;
		}

		if (specialWeapon == SpecialWeapon.SARADOMIN_GODSWORD_OR)
		{
			specialWeapon = SpecialWeapon.SARADOMIN_GODSWORD;
		}

		SpecialCounter counter = specialCounter[specialWeapon.ordinal()];

		BufferedImage image = itemManager.getImage(specialWeapon.getItemID());
		overlay.addOverlay(player,
			new SpecialIcon(image, Integer.toString(hit), System.currentTimeMillis()));

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
