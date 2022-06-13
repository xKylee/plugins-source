/*
 * Copyright (c) 2020, Charles Xu <github.com/kthisiscvpv>
 * Copyright (c) 2020, capslock13#0001
 * Copyright (c) 2021, Andrew Terra <github.com/andrewterra>
 * Copyright (c) 2021, BickusDiggus <https://github.com/BickusDiggus>

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

package net.runelite.client.plugins.socketdeathindicator;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Provides;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;
import javax.inject.Inject;
import com.openosrs.client.util.WeaponMap;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.Hitsplat;
import net.runelite.api.NPC;
import net.runelite.api.Renderable;
import net.runelite.api.Skill;
import net.runelite.api.VarPlayer;
import net.runelite.api.events.FakeXpDrop;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.StatChanged;
import net.runelite.api.kit.KitType;
import net.runelite.client.callback.Hooks;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginManager;
import net.runelite.client.plugins.socket.SocketPlugin;
import net.runelite.client.plugins.socket.org.json.JSONArray;
import net.runelite.client.plugins.socket.org.json.JSONObject;
import net.runelite.client.plugins.socket.packet.SocketBroadcastPacket;
import net.runelite.client.plugins.socket.packet.SocketReceivePacket;
import net.runelite.client.ui.overlay.OverlayManager;
import org.pf4j.Extension;

@Slf4j
@Extension
@PluginDescriptor(
		name = "Socket Death Indicators",
		description = "Removes Nylos that have been killed",
		tags = {"Socket, death, kill", "nylo"},
		enabledByDefault = false
)
@PluginDependency(SocketPlugin.class)
public class SocketDeathIndicatorPlugin extends Plugin
{
	@Inject
	private Hooks hooks;
	private final Hooks.RenderableDrawListener drawListener = this::shouldDraw;
	@Inject
	private SocketDeathIndicatorsConfig config;
	@Inject
	ConfigManager configManager;
	@Inject
	PluginManager pluginManager;
	@Inject
	private SocketDeathIndicatorsOverlay overlay;
	@Inject
	private Client client;
	@Inject
	private OverlayManager overlayManager;
	@Inject
	private EventBus eventBus;
	private ArrayList<NyloQ> nylos;
	private ArrayList<Method> reflectedMethods;
	private ArrayList<Plugin> reflectedPlugins;
	@Getter
	private ArrayList<NPC> deadNylos;
	@Getter
	private NyloQ maidenNPC;

	private int partySize;
	private int ATTACK;
	private int STRENGTH;
	private int DEFENCE;
	private int RANGED;
	private int MAGIC;
	private boolean inNylo = false;

	@Provides
	SocketDeathIndicatorsConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(SocketDeathIndicatorsConfig.class);
	}

	@Override
	protected void startUp()
	{
		hooks.registerRenderableDrawListener(drawListener);
		ATTACK = -1;
		STRENGTH = -1;
		DEFENCE = -1;
		RANGED = -1;
		MAGIC = -1;
		deadNylos = new ArrayList<>();
		nylos = new ArrayList<>();
		overlayManager.add(overlay);
		reflectedMethods = new ArrayList<>();
		reflectedPlugins = new ArrayList<>();

		for (Plugin p : pluginManager.getPlugins())
		{
			Method m;

			try
			{
				m = p.getClass().getDeclaredMethod("SocketDeathIntegration", Integer.TYPE);
			}
			catch (NoSuchMethodException var5)
			{
				continue;
			}

			reflectedMethods.add(m);
			reflectedPlugins.add(p);
		}
	}

	@Override
	protected void shutDown()
	{
		hooks.unregisterRenderableDrawListener(drawListener);
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned event)
	{
		int smSmallHP = -1;
		int smBigHP = -1;
		int bigHP = -1;
		int smallHP = -1;
		int maidenHP = -1;
		if (partySize == 1)
		{
			bigHP = 16;
			smallHP = 8;
			maidenHP = 2625;
			smSmallHP = 2;
			smBigHP = 3;
		}
		else if (partySize == 2)
		{
			bigHP = 16;
			smallHP = 8;
			maidenHP = 2625;
			smSmallHP = 4;
			smBigHP = 6;
		}
		else if (partySize == 3)
		{
			bigHP = 16;
			smallHP = 8;
			maidenHP = 2625;
			smSmallHP = 6;
			smBigHP = 9;
		}
		else if (partySize == 4)
		{
			bigHP = 19;
			smallHP = 9;
			maidenHP = 3062;
			smSmallHP = 8;
			smBigHP = 12;
		}
		else if (partySize == 5)
		{
			bigHP = 22;
			smallHP = 11;
			maidenHP = 3500;
			smSmallHP = 10;
			smBigHP = 15;
		}

		int id = event.getNpc().getId();
		switch (id)
		{
			case 8342:
			case 8343:
			case 8344:
			case 10791:
			case 10792:
			case 10793:
				nylos.add(new NyloQ(event.getNpc(), 0, smallHP));
				break;
			case 8345:
			case 8346:
			case 8347:
			case 8351:
			case 8352:
			case 8353:
			case 10783:
			case 10784:
			case 10785:
			case 10794:
			case 10795:
			case 10796:
			case 10800:
			case 10801:
			case 10802:
				nylos.add(new NyloQ(event.getNpc(), 0, bigHP));
				break;
			case 8360:
				NyloQ maidenTemp = new NyloQ(event.getNpc(), 0, maidenHP);
				nylos.add(maidenTemp);
				maidenNPC = maidenTemp;
				break;
			case 10774:
			case 10775:
			case 10776:
				nylos.add(new NyloQ(event.getNpc(), 0, smSmallHP));
				break;
			case 10777:
			case 10778:
			case 10779:
				nylos.add(new NyloQ(event.getNpc(), 0, smBigHP));
		}

	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned event)
	{
		if (nylos.size() != 0)
		{
			nylos.removeIf((q) -> q.npc.equals(event.getNpc()));
		}

		if (deadNylos.size() != 0)
		{
			deadNylos.removeIf((q) -> q.equals(event.getNpc()));
		}

		int id = event.getNpc().getId();
		switch (id)
		{
			case 8360:
			case 8361:
			case 8362:
			case 8363:
			case 8364:
			case 8365:
				maidenNPC = null;
				break;
		}
	}

	private boolean inRegion(int... regions)
	{
		if (client.getMapRegions() != null)
		{
			int[] mapRegions = client.getMapRegions();

			return Arrays.stream(mapRegions).anyMatch(i -> Arrays.stream(regions).anyMatch(j -> i == j));
		}

		return false;
	}

	private void postHit(int index, int dmg)
	{
		JSONArray data = new JSONArray();
		JSONObject message = new JSONObject();
		message.put("index", index);
		message.put("damage", dmg);
		data.put(message);
		JSONObject send = new JSONObject();
		send.put("sDeath", data);
		eventBus.post(new SocketBroadcastPacket(send));
	}

	@Subscribe
	public void onHitsplatApplied(HitsplatApplied hitsplatApplied)
	{
		if (inNylo)
		{
			Iterator<NyloQ> nyloQIterator = nylos.iterator();

			while (true)
			{
				NyloQ q;
				do
				{
					if (!nyloQIterator.hasNext())
					{
						return;
					}

					q = nyloQIterator.next();
				} while (!hitsplatApplied.getActor().equals(q.npc));

				if (hitsplatApplied.getHitsplat().getHitsplatType().equals(Hitsplat.HitsplatType.HEAL))
				{
					q.hp += hitsplatApplied.getHitsplat().getAmount();
				}
				else
				{
					q.hp -= hitsplatApplied.getHitsplat().getAmount();
					q.queuedDamage -= hitsplatApplied.getHitsplat().getAmount();
				}

				if (q.hp <= 0)
				{
					NyloQ finalQ = q;
					deadNylos.removeIf((o) -> o.equals(finalQ.npc));
				}
				else if (q.npc.getId() == 8360 || q.npc.getId() == 8361 || q.npc.getId() == 8362 || q.npc.getId() == 8363
						|| q.npc.getId() == 10822 || q.npc.getId() == 10823 || q.npc.getId() == 10824 || q.npc.getId() == 10825)
				{
					double percent = (double) q.hp / (double) q.maxHP;
					if (percent < 0.7D && q.phase == 0)
					{
						q.phase = 1;
					}

					if (percent < 0.5D && q.phase == 1)
					{
						q.phase = 2;
					}

					if (percent < 0.3D && q.phase == 2)
					{
						q.phase = 3;
					}
				}
			}
		}
	}

	@Subscribe
	public void onSocketReceivePacket(SocketReceivePacket event)
	{
		if (inNylo)
		{
			try
			{
				JSONObject payload = event.getPayload();
				if (payload.has("sDeath"))
				{
					JSONArray data = payload.getJSONArray("sDeath");
					JSONObject jsonmsg = data.getJSONObject(0);
					int index = jsonmsg.getInt("index");
					int damage = jsonmsg.getInt("damage");
					Iterator<NyloQ> nyloQIterator = nylos.iterator();

					while (true)
					{
						NyloQ q;
						do
						{
							if (!nyloQIterator.hasNext())
							{
								return;
							}

							q = nyloQIterator.next();
						} while (q.npc.getIndex() != index);

						q.queuedDamage += damage;
						NyloQ finalQ = q;
						if (q.npc.getId() == 8360 || q.npc.getId() == 8361 || q.npc.getId() == 8362 || q.npc.getId() == 8363
								|| q.npc.getId() == 10822 || q.npc.getId() == 10823 || q.npc.getId() == 10824 || q.npc.getId() == 10825)
						{
							if (q.queuedDamage > 0)
							{
								double percent = ((double) q.hp - (double) q.queuedDamage) / (double) q.maxHP;
								if (percent < 0.7D && q.phase == 0)
								{
									q.phase = 1;
								}

								if (percent < 0.5D && q.phase == 1)
								{
									q.phase = 2;
								}

								if (percent < 0.3D && q.phase == 2)
								{
									q.phase = 3;
								}
							}
						}
						else if (q.hp - q.queuedDamage <= 0 && deadNylos.stream().noneMatch((o) -> o.getIndex() == finalQ.npc.getIndex()))
						{
							if (config.hideNylo())
							{
								deadNylos.add(q.npc);
								q.hidden = true;
								if (reflectedPlugins.size() == reflectedMethods.size())
								{
									for (int i = 0; i < reflectedPlugins.size(); ++i)
									{
										try
										{
											Method tm = reflectedMethods.get(i);
											tm.setAccessible(true);
											tm.invoke(reflectedPlugins.get(i), q.npc.getIndex());
										}
										catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | ExceptionInInitializerError | NullPointerException var11)
										{
											log.debug("Failed on plugin: " + reflectedPlugins.get(i).getName());
										}
									}
								}
							}
						}
					}
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	void addToDamageQueue(int damage)
	{
		if (damage != -1)
		{
			Actor interacted = Objects.requireNonNull(client.getLocalPlayer()).getInteracting();
			if (interacted instanceof NPC)
			{
				NPC interactedNPC = (NPC) interacted;
				postHit(interactedNPC.getIndex(), damage);
			}

		}
	}

	@Subscribe
	public void onFakeXpDrop(FakeXpDrop event) throws InterruptedException
	{
		if (!inNylo)
		{
			return;
		}

		int xpdiff = event.getXp();
		String skill = event.getSkill().toString();
		if (!(skill.equals("RANGED") || skill.equals("MAGIC") || skill.equals("STRENGTH") || skill.equals("ATTACK") || skill.equals("DEFENCE")))
		{
			return;
		}

		processXpDrop(String.valueOf(xpdiff), skill);
	}

	@Subscribe
	public void onStatChanged(StatChanged event) throws InterruptedException
	{
		if (!inNylo)
		{
			return;
		}

		int xpdiff = 0;
		String skill = event.getSkill().toString();
		if (!(skill.equals("RANGED") || skill.equals("MAGIC") || skill.equals("STRENGTH") || skill.equals("ATTACK") || skill.equals("DEFENCE")))
		{
			return;
		}

		switch (skill)
		{
			case "MAGIC":
			{
				xpdiff = event.getXp() - MAGIC;
				MAGIC = event.getXp();
				break;
			}
			case "RANGED":
			{
				xpdiff = event.getXp() - RANGED;
				RANGED = event.getXp();
				break;
			}
			case "STRENGTH":
			{
				xpdiff = event.getXp() - STRENGTH;
				STRENGTH = event.getXp();
				break;
			}
			case "ATTACK":
			{
				xpdiff = event.getXp() - ATTACK;
				ATTACK = event.getXp();
				break;
			}
			case "DEFENCE":
			{
				xpdiff = event.getXp() - DEFENCE;
				DEFENCE = event.getXp();
			}
		}

		processXpDrop(String.valueOf(xpdiff), skill);
	}

	private void processXpDrop(String xpDrop, String skill) throws InterruptedException
	{
		int damage = 0;
		int weaponUsed = Objects.requireNonNull(Objects.requireNonNull(client.getLocalPlayer()).getPlayerComposition()).getEquipmentId(KitType.WEAPON);
		if (client.getLocalPlayer().getAnimation() != 1979)
		{
			if (skill.equals("MAGIC"))
			{
				// sang/tridents
				if ((weaponUsed == 22323 || weaponUsed == 11905 || weaponUsed == 11907 || weaponUsed == 12899 || weaponUsed == 22292 || weaponUsed == 25731) && client.getVar(VarPlayer.ATTACK_STYLE) != 3)
				{
					damage = (int)((double)Integer.parseInt(xpDrop) / 2.0D);
				}
			}
			else if (!skill.equals("ATTACK") && !skill.equals("STRENGTH") && !skill.equals("DEFENCE"))
			{
				if (skill.equals("RANGED"))
				{
					// :gottago: if chins
					if (weaponUsed == 11959)
					{
						return;
					}

					if (client.getVar(VarPlayer.ATTACK_STYLE) == 3)
					{
						damage = (int)((double)Integer.parseInt(xpDrop) / 2.0D);
					}
					else
					{
						damage = (int)((double)Integer.parseInt(xpDrop) / 4.0D);
					}
				}
			}
			else
			{
				if (weaponUsed == 22325 || weaponUsed == 25739 || weaponUsed == 25736) //Don't apply if weapon is scythe
				{
					return;
				}

				if (weaponUsed != 22323 && weaponUsed != 11905 && weaponUsed != 11907 && weaponUsed != 12899 && weaponUsed != 22292 && weaponUsed != 25731) //Powered Staves
				{
					if (WeaponMap.StyleMap.get(weaponUsed).toString().equals("MELEE"))
					{
						damage = (int)((double)Integer.parseInt(xpDrop) / 4.0D);
					}
				}
				else
				{
					// :gottago: if barrage
					if (client.getLocalPlayer().getAnimation() == 1979)
					{
						return;
					}

					if (client.getVar(VarPlayer.ATTACK_STYLE) == 3)
					{
						damage = Integer.parseInt(xpDrop);
					}
				}
			}

			addToDamageQueue(damage);
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (client.getLocalPlayer() != null && MAGIC == -1)
		{
			initStatXp();
		}

		if (!inNylo)
		{
			if (inRegion(13122))
			{
				inNylo = true;
				partySize = 0;

				for (int i = 330; i < 335; i++)
				{
					if (client.getVarcStrValue(i) != null && !client.getVarcStrValue(i).equals(""))
					{
						partySize++;
					}
				}
			}
		}
		else if (!inRegion(13122))
		{
			inNylo = false;
			if (!nylos.isEmpty() || !deadNylos.isEmpty())
			{
				nylos.clear();
				deadNylos.clear();
			}
		}

		for (NyloQ q : nylos)
		{
			if (q.hidden)
			{
				q.hiddenTicks++;
				if (q.npc.getHealthRatio() != 0 && q.hiddenTicks > 5)
				{
					q.hiddenTicks = 0;
					q.hidden = false;
					deadNylos.removeIf((x) -> x.equals(q.npc));
				}
			}
		}

	}

	private void initStatXp()
	{
		ATTACK = client.getSkillExperience(Skill.ATTACK);
		STRENGTH = client.getSkillExperience(Skill.STRENGTH);
		DEFENCE = client.getSkillExperience(Skill.DEFENCE);
		RANGED = client.getSkillExperience(Skill.RANGED);
		MAGIC = client.getSkillExperience(Skill.MAGIC);
	}

	@VisibleForTesting
	boolean shouldDraw(Renderable renderable, boolean drawingUI)
	{
		if (renderable instanceof NPC)
		{
			NPC npc = (NPC) renderable;

			return !deadNylos.contains(npc);
		}

		return true;
	}
}
