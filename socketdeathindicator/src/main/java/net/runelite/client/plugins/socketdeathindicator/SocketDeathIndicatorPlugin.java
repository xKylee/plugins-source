/*
 * Copyright (c) 2020, Charles Xu <github.com/kthisiscvpv>
 * Copyright (c) 2020, capslock13#0001
 * Copyright (c) 2021, Andrew Terra <github.com/andrewterra>

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


import com.google.inject.Provides;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import javax.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.Hitsplat.HitsplatType;
import net.runelite.api.NPC;
import net.runelite.api.VarPlayer;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.ScriptPreFired;
import net.runelite.api.kit.KitType;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
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
	private SocketDeathIndicatorsConfig config;
	@Inject
	private SocketDeathIndicatorsOverlay overlay;
	@Inject
	private Client client;
	@Inject
	private OverlayManager overlayManager;
	private ArrayList<NyloQ> nylos;
	@Inject
	private EventBus eventBus;
	private boolean inNylo = false;

	@Getter
	private ArrayList<NPC> deadNylos;
	@Getter
	private NyloQ maidenNPC;

	private ArrayList<Integer> hiddenIndices;

	@Provides
	SocketDeathIndicatorsConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(SocketDeathIndicatorsConfig.class);
	}

	@Override
	protected void startUp()
	{
		deadNylos = new ArrayList<>();
		nylos = new ArrayList<>();
		hiddenIndices = new ArrayList<>();
		overlayManager.add(overlay);
	}

	@Override
	protected void shutDown()
	{
		deadNylos = null;
		nylos = null;
		hiddenIndices = null;
		overlayManager.remove(overlay);
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned event)
	{
		int partySize = -1;

		try
		{
			partySize = (int) Arrays.stream(Objects.requireNonNull(client.getWidget(28, 10)).getStaticChildren()).filter((w) ->
					w.getDynamicChildren() != null && Arrays.stream(w.getDynamicChildren()).anyMatch((r) -> !Objects.equals(r.getText(), ""))).count();
		}
		catch (NullPointerException ignored)
		{
		}

		if (partySize != -1)
		{
			int bigHP = -1;
			int smallHP = -1;
			int maidenHP = -1;
			if (partySize < 4)
			{
				bigHP = 16;
				smallHP = 8;
				maidenHP = 2625;
			}
			else if (partySize == 4)
			{
				bigHP = 19;
				smallHP = 9;
				maidenHP = 3062;
			}
			else if (partySize == 5)
			{
				bigHP = 22;
				smallHP = 11;
				maidenHP = 3500;
			}

			int id = event.getNpc().getId();
			switch (id)
			{
				case 8342:
				case 8343:
				case 8344:
					nylos.add(new NyloQ(event.getNpc(), 0, smallHP));
					break;
				case 8345:
				case 8346:
				case 8347:
				case 8351:
				case 8352:
				case 8353:
					nylos.add(new NyloQ(event.getNpc(), 0, bigHP));
					break;
				case 8360:
					NyloQ maidenTemp = new NyloQ(event.getNpc(), 0, maidenHP);
					nylos.add(maidenTemp);
					maidenNPC = maidenTemp;
			}

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

	}

	@Subscribe
	public void onScriptPreFired(ScriptPreFired scriptPreFired)
	{
		if (inNylo)
		{
			if (scriptPreFired.getScriptId() == 996)
			{
				int[] intStack = client.getIntStack();
				int intStackSize = client.getIntStackSize();
				int widgetId = intStack[intStackSize - 4];

				try
				{
					processXpDrop(widgetId);
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}

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

				if (hitsplatApplied.getHitsplat().getHitsplatType().equals(HitsplatType.HEAL))
				{
					q.hp += hitsplatApplied.getHitsplat().getAmount();
				}
				else
				{
					q.hp -= hitsplatApplied.getHitsplat().getAmount();
				}

				q.queuedDamage -= hitsplatApplied.getHitsplat().getAmount();
				if (q.hp <= 0)
				{
					NyloQ finalQ = q;
					deadNylos.removeIf((o) -> o.equals(finalQ.npc));
				}
				else if (q.npc.getId() == 8360 || q.npc.getId() == 8361 || q.npc.getId() == 8362 || q.npc.getId() == 8363)
				{
					double percent = (double) q.hp / (double) q.maxHP;
					if (percent < 0.7D)
					{
						q.phase = 1;
					}

					if (percent < 0.5D)
					{
						q.phase = 2;
					}

					if (percent < 0.3D)
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
							if (q.hp - q.queuedDamage <= 0 && deadNylos.stream().noneMatch((o) -> o.getIndex() == finalQ.npc.getIndex()))
							{
								deadNylos.add(q.npc);
								if (config.hideNylo())
								{
									setHiddenNpc(q.npc, true);
									q.hidden = true;
								}
							}
						} while (q.npc.getId() != 8360 && q.npc.getId() != 8361 && q.npc.getId() != 8362 && q.npc.getId() != 8363);

						double percent = ((double) q.hp - (double) q.queuedDamage) / (double) q.maxHP;
						if (percent < 0.7D)
						{
							q.phase = 1;
						}

						if (percent < 0.5D)
						{
							q.phase = 2;
						}

						if (percent < 0.3D)
						{
							q.phase = 3;
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

	private void setHiddenNpc(NPC npc, boolean hidden)
	{

		List<Integer> newHiddenNpcIndicesList = client.getHiddenNpcIndices();
		if (hidden)
		{
			newHiddenNpcIndicesList.add(npc.getIndex());
			hiddenIndices.add(npc.getIndex());
		}
		else
		{
			if (newHiddenNpcIndicesList.contains(npc.getIndex()))
			{
				newHiddenNpcIndicesList.remove((Integer) npc.getIndex());
			}
		}
		log.info(newHiddenNpcIndicesList.toString());
		client.setHiddenNpcIndices(newHiddenNpcIndicesList);

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

	private void processXpDrop(int widgetId) throws InterruptedException
	{
		Widget xpDrop = client.getWidget(WidgetInfo.TO_GROUP(widgetId), WidgetInfo.TO_CHILD(widgetId));
		if (xpDrop != null)
		{
			Widget[] children = xpDrop.getChildren();
			Widget text = children[0];
			String cleansedXpDrop = cleanseXpDrop(text.getText());
			int damage = -1;
			int weaponUsed = Objects.requireNonNull(Objects.requireNonNull(client.getLocalPlayer()).getPlayerComposition()).getEquipmentId(KitType.WEAPON);
			if (client.getLocalPlayer().getAnimation() != 1979)
			{
				// magic sprite
				if (Arrays.stream(children).skip(1L).filter(Objects::nonNull).mapToInt(Widget::getSpriteId).anyMatch((id) -> id == 202))
				{
					// sang/tridents
					if (weaponUsed == 22323 || weaponUsed == 11905 || weaponUsed == 11907 || weaponUsed == 12899 || weaponUsed == 22292)
					{
						if (client.getLocalPlayer().getAnimation() == 1979)
						{
							return;
						}

						if (client.getVarbitValue(4696) == 0)
						{
							if (client.getVar(VarPlayer.ATTACK_STYLE) != 3)
							{
								damage = (int) ((double) Integer.parseInt(cleansedXpDrop) / 2.0D);
							}
						}
						else
						{
							if (client.getVar(VarPlayer.ATTACK_STYLE) == 3)
							{
								damage = (int) Math.round((double) Integer.parseInt(cleansedXpDrop) / 3.6667D);
							}
							else
							{
								damage = (int) Math.round((double) Integer.parseInt(cleansedXpDrop) / 3.3334D);
							}
						}
					}
				}
				// att, str, def sprite
				else if (Arrays.stream(children).skip(1L).filter(Objects::nonNull).mapToInt(Widget::getSpriteId).anyMatch((id) -> id == 197 || id == 198 || id == 199))
				{
					if (weaponUsed == 22325)
					{
						return;
					}

					if (client.getVarbitValue(4696) == 0)
					{
						// checking if casting on long range
						if (weaponUsed != 22323 && weaponUsed != 11905 && weaponUsed != 11907 && weaponUsed != 12899 && weaponUsed != 22292)
						{
							if (weaponUsed == 12006)
							{
								if (client.getVar(VarPlayer.ATTACK_STYLE) == 1)
								{
									if (Arrays.stream(children).skip(1L).filter(Objects::nonNull).mapToInt(Widget::getSpriteId).anyMatch((id) -> id == 197))
									{
										damage = (int) Math.round(3.0D * (double) Integer.parseInt(cleansedXpDrop) / 4.0D);
									}
								}
								else
								{
									damage = Integer.parseInt(cleansedXpDrop) / 4;
								}
							}
							else
							{
								damage = Integer.parseInt(cleansedXpDrop) / 4;
							}
						}
						else
						{
							// :gottago: if barrage
							if (client.getLocalPlayer().getAnimation() == 1979)
							{
								return;
							}

							if (client.getVarbitValue(4696) == 0 && client.getVar(VarPlayer.ATTACK_STYLE) == 3)
							{
								damage = Integer.parseInt(cleansedXpDrop);
							}
						}
					}
					else
					{
						damage = (int) Math.round((double) Integer.parseInt(cleansedXpDrop) / 5.3333D);
					}
				}
				// range sprite
				else if (Arrays.stream(children).skip(1L).filter(Objects::nonNull).mapToInt(Widget::getSpriteId).anyMatch((id) -> id == 200))
				{
					// :gottago: if chins
					if (weaponUsed == 11959)
					{
						return;
					}

					if (client.getVarbitValue(4696) == 0)
					{
						damage = (int) ((double) Integer.parseInt(cleansedXpDrop) / 4.0D);
					}
					else
					{
						damage = (int) Math.round((double) Integer.parseInt(cleansedXpDrop) / 5.333D);
					}
				}

				addToDamageQueue(damage);
			}
		}
	}

	/**
	 * should cleanse the XP drop to remove the damage number in parens if the player uses that pluin
	 * @param text the xp drop widget text
	 * @return the base xp drop
	 */
	private String cleanseXpDrop(String text)
	{
		if (text.contains("<"))
		{
			return text.split("<")[0];
		}
		// Shouldn't fall in here often since in my logs the "(" is always prepended with <col=ff0000>
		if (text.contains("("))
		{
			return text.split("\\(")[0];
		}
		return text;
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (inRegion(13122, 12613))
		{
			inNylo = true;

			for (NyloQ q : nylos)
			{
				if (q.hidden)
				{
					q.hiddenTicks++;
					if (q.npc.getHealthRatio() != 0 && q.hiddenTicks > 5)
					{
						q.hiddenTicks = 0;
						q.hidden = false;
						setHiddenNpc(q.npc, false);
						deadNylos.removeIf((x) -> x.equals(q.npc));
					}
				}
			}

		}
		else
		{
			inNylo = false;
			if (!hiddenIndices.isEmpty())
			{
				List<Integer> newHiddenNpcIndicesList = client.getHiddenNpcIndices();
				newHiddenNpcIndicesList.removeAll(hiddenIndices);
				client.setHiddenNpcIndices(newHiddenNpcIndicesList);
				hiddenIndices.clear();
			}
		}
	}

}
