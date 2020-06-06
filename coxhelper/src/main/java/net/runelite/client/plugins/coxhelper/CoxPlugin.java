/*
 * Copyright (c) 2019, xzact <https://github.com/xzact>
 * Copyright (c) 2019, ganom <https://github.com/Ganom>
 * Copyright (c) 2019, gazivodag <https://github.com/gazivodag>
 * Copyright (c) 2019, lyzrds <https://discord.gg/5eb9Fe>
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

package net.runelite.client.plugins.coxhelper;

import com.google.inject.Provides;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.AnimationID;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GraphicID;
import net.runelite.api.GraphicsObject;
import net.runelite.api.NPC;
import net.runelite.api.NpcID;
import net.runelite.api.Player;
import net.runelite.api.Projectile;
import net.runelite.api.ProjectileID;
import net.runelite.api.Varbits;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.ProjectileSpawned;
import net.runelite.api.events.SpotAnimationChanged;
import net.runelite.api.util.Text;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginType;
import net.runelite.client.ui.overlay.OverlayManager;
import org.pf4j.Extension;

@Extension
@PluginDescriptor(
	name = "CoX Helper",
	enabledByDefault = false,
	description = "All-in-one plugin for Chambers of Xeric",
	tags = {"CoX", "chamber", "xeric", "helper"},
	type = PluginType.PVM
)
@Slf4j
@Getter(AccessLevel.PACKAGE)
public class CoxPlugin extends Plugin
{
	private static final int ANIMATION_ID_G1 = 430;
	private static final Pattern TP_REGEX = Pattern.compile("You have been paired with <col=ff0000>(.*)</col>! The magical power will enact soon...");

	@Inject
	@Getter(AccessLevel.NONE)
	private Client client;

	@Inject
	@Getter(AccessLevel.NONE)
	private ChatMessageManager chatMessageManager;

	@Inject
	@Getter(AccessLevel.NONE)
	private CoxOverlay coxOverlay;

	@Inject
	@Getter(AccessLevel.NONE)
	private CoxInfoBox coxInfoBox;

	@Inject
	@Getter(AccessLevel.NONE)
	private CoxConfig config;

	@Inject
	@Getter(AccessLevel.NONE)
	private OverlayManager overlayManager;

	@Inject
	@Getter(AccessLevel.NONE)
	private EventBus eventBus;

	//other
	private int vanguards;
	private boolean tektonActive;
	private int tektonAttackTicks;
	private Map<NPC, NPCContainer> npcContainers = new HashMap<>();

	//olm
	private boolean olmActive;
	private boolean olmReady;
	private int olmPhase = 0;
	private NPC olmHand;
	private NPC olmNPC;
	private int olmTicksUntilAction = -1;
	private int olmActionCycle = -1; //4:0 = auto 3:0 = null 2:0 = auto 1:0 = spec + actioncycle =4
	private int olmNextSpec = -1; // 1= portals 2=lightnig 3=crystals 4= heal hand if p4
	private final List<WorldPoint> olmHealPools = new ArrayList<>();
	private final List<WorldPoint> olmPortals = new ArrayList<>();
	private int portalTicks = 10;
	private final Set<Victim> victims = new HashSet<>();
	private Actor acidTarget;
	private boolean handCrippled;
	private int crippleTimer = 45;
	@Setter(AccessLevel.PACKAGE)
	private PrayAgainst olmPrayer;
	private long lastPrayTime;
	private int sleepcount = 0;

	@Provides
	CoxConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(CoxConfig.class);
	}

	@Override
	protected void startUp()
	{
		overlayManager.add(coxOverlay);
		overlayManager.add(coxInfoBox);
		handCrippled = false;
		olmHand = null;
		olmPortals.clear();
		olmPrayer = null;
		victims.clear();
		crippleTimer = 45;
		portalTicks = 10;
		vanguards = 0;
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(coxOverlay);
		overlayManager.remove(coxInfoBox);
	}

	@Subscribe
	private void onChatMessage(ChatMessage event)
	{
		if (!inRaid())
		{
			return;
		}

		if (event.getType() == ChatMessageType.GAMEMESSAGE)
		{
			final Matcher tpMatcher = TP_REGEX.matcher(event.getMessage());

			if (tpMatcher.matches())
			{
				for (Player player : client.getPlayers())
				{
					final String rawPlayerName = player.getName();

					if (rawPlayerName != null)
					{
						final String fixedPlayerName = Text.sanitize(rawPlayerName);

						if (fixedPlayerName.equals(Text.sanitize(tpMatcher.group(1))))
						{
							victims.add(new Victim(player, Victim.Type.TELEPORT));
						}
					}
				}
			}

			switch (Text.standardize(event.getMessageNode().getValue()))
			{
				case "the great olm rises with the power of acid.":
				case "the great olm rises with the power of crystal.":
				case "the great olm rises with the power of flame.":
					olmPhase = 0;
					resetOlm();
					break;
				case "the great olm is giving its all. this is its final stand.":
					olmPhase = 1;
					resetOlm();
					break;
				case "the great olm fires a sphere of aggression your way. your prayers have been sapped.":
				case "the great olm fires a sphere of aggression your way.":
					olmPrayer = PrayAgainst.MELEE;
					lastPrayTime = System.currentTimeMillis();
					break;
				case "the great olm fires a sphere of magical power your way. your prayers have been sapped.":
				case "the great olm fires a sphere of magical power your way.":
					olmPrayer = PrayAgainst.MAGIC;
					lastPrayTime = System.currentTimeMillis();
					break;
				case "the great olm fires a sphere of accuracy and dexterity your way. your prayers have been sapped.":
				case "the great olm fires a sphere of accuracy and dexterity your way.":
					olmPrayer = PrayAgainst.RANGED;
					lastPrayTime = System.currentTimeMillis();
					break;
				case "the great olm's left claw clenches to protect itself temporarily.":
					handCrippled = true;

			}
		}
	}

	private void resetOlm()
	{
		olmActive = true;
		olmReady = false;
		crippleTimer = 45;
		olmNextSpec = -1;
		olmActionCycle = -1;
	}

	@Subscribe
	private void onProjectileSpawned(ProjectileSpawned event)
	{
		if (!inRaid())
		{
			return;
		}

		final Projectile projectile = event.getProjectile();

		switch (projectile.getId())
		{
			case ProjectileID.OLM_MAGE_ATTACK:
				olmPrayer = PrayAgainst.MAGIC;
				lastPrayTime = System.currentTimeMillis();
				break;
			case ProjectileID.OLM_RANGE_ATTACK:
				olmPrayer = PrayAgainst.RANGED;
				lastPrayTime = System.currentTimeMillis();
				break;
			case ProjectileID.OLM_ACID_TRAIL:
				acidTarget = projectile.getInteracting();
				break;
		}
	}

	@Subscribe
	private void onSpotAnimationChanged(SpotAnimationChanged event)
	{
		if (!inRaid())
		{
			return;
		}

		if (!(event.getActor() instanceof Player))
		{
			return;
		}

		final Player player = (Player) event.getActor();

		if (player.getSpotAnimation() == GraphicID.OLM_BURN)
		{
			int add = 0;

			for (Victim victim : victims)
			{
				if (victim.getPlayer().getName().equals(player.getName()))
				{
					add++;
				}
			}

			if (add == 0)
			{
				victims.add(new Victim(player, Victim.Type.BURN));
			}
		}
	}

	@Subscribe
	private void onNpcSpawned(NpcSpawned event)
	{
		if (!inRaid())
		{
			return;
		}

		final NPC npc = event.getNpc();

		switch (npc.getId())
		{
			case NpcID.TEKTON:
			case NpcID.TEKTON_7541:
			case NpcID.TEKTON_7542:
			case NpcID.TEKTON_7545:
			case NpcID.TEKTON_ENRAGED:
			case NpcID.TEKTON_ENRAGED_7544:
				npcContainers.put(npc, new NPCContainer(npc));
				tektonAttackTicks = 27;
				break;
			case NpcID.MUTTADILE:
			case NpcID.MUTTADILE_7562:
			case NpcID.MUTTADILE_7563:
			case NpcID.GUARDIAN:
			case NpcID.GUARDIAN_7570:
				npcContainers.put(npc, new NPCContainer(npc));
				break;
			case NpcID.VANGUARD:
			case NpcID.VANGUARD_7526:
			case NpcID.VANGUARD_7527:
			case NpcID.VANGUARD_7528:
			case NpcID.VANGUARD_7529:
				vanguards++;
				npcContainers.put(npc, new NPCContainer(npc));
				break;
			case NpcID.GREAT_OLM_LEFT_CLAW:
			case NpcID.GREAT_OLM_LEFT_CLAW_7555:
				olmHand = npc;
				break;
			case NpcID.GREAT_OLM:
				olmNPC = npc;
		}
	}

	@Subscribe
	private void onNpcDespawned(NpcDespawned event)
	{
		if (!inRaid())
		{
			return;
		}

		final NPC npc = event.getNpc();

		switch (npc.getId())
		{
			case NpcID.TEKTON:
			case NpcID.TEKTON_7541:
			case NpcID.TEKTON_7542:
			case NpcID.TEKTON_7545:
			case NpcID.TEKTON_ENRAGED:
			case NpcID.TEKTON_ENRAGED_7544:
			case NpcID.MUTTADILE:
			case NpcID.MUTTADILE_7562:
			case NpcID.MUTTADILE_7563:
			case NpcID.GUARDIAN:
			case NpcID.GUARDIAN_7570:
			case NpcID.GUARDIAN_7571:
			case NpcID.GUARDIAN_7572:
				if (npcContainers.remove(event.getNpc()) != null && !npcContainers.isEmpty())
				{
					npcContainers.remove(event.getNpc());
				}
				break;
			case NpcID.VANGUARD:
			case NpcID.VANGUARD_7526:
			case NpcID.VANGUARD_7527:
			case NpcID.VANGUARD_7528:
			case NpcID.VANGUARD_7529:
				if (npcContainers.remove(event.getNpc()) != null && !npcContainers.isEmpty())
				{
					npcContainers.remove(event.getNpc());
				}
				vanguards--;
				break;
			case NpcID.GREAT_OLM_RIGHT_CLAW_7553:
			case NpcID.GREAT_OLM_RIGHT_CLAW:
				handCrippled = false;
				break;
		}
	}

	@Subscribe
	private void onGameTick(GameTick event)
	{
		if (!inRaid())
		{
			olmPhase = 0;
			sleepcount = 0;
			olmHealPools.clear();
			npcContainers.clear();
			victims.clear();
			olmNPC = null;
			olmHand = null;
			olmPrayer = null;
			olmActive = false;
			olmReady = false;
			return;
		}

		handleNpcs();
		handleVictims();

		if (handCrippled)
		{
			crippleTimer--;
			if (crippleTimer <= 0)
			{
				handCrippled = false;
				crippleTimer = 45;
			}
		}

		if (olmActive)
		{
			handleOlm();
		}
	}

	private void handleVictims()
	{
		if (victims.size() > 0)
		{
			victims.forEach(Victim::updateTicks);
			victims.removeIf(victim -> victim.getTicks() <= 0);
		}
	}

	private void handleNpcs()
	{
		for (NPCContainer npcs : getNpcContainers().values())
		{
			switch (npcs.getNpc().getId())
			{
				case NpcID.TEKTON:
				case NpcID.TEKTON_7541:
				case NpcID.TEKTON_7542:
				case NpcID.TEKTON_7545:
				case NpcID.TEKTON_ENRAGED:
				case NpcID.TEKTON_ENRAGED_7544:
					npcs.setTicksUntilAttack(npcs.getTicksUntilAttack() - 1);
					npcs.setAttackStyle(NPCContainer.Attackstyle.MELEE);
					switch (npcs.getNpc().getAnimation())
					{
						case AnimationID.TEKTON_AUTO1:
						case AnimationID.TEKTON_AUTO2:
						case AnimationID.TEKTON_AUTO3:
						case AnimationID.TEKTON_ENRAGE_AUTO1:
						case AnimationID.TEKTON_ENRAGE_AUTO2:
						case AnimationID.TEKTON_ENRAGE_AUTO3:
							tektonActive = true;
							if (npcs.getTicksUntilAttack() < 1)
							{
								npcs.setTicksUntilAttack(4);
							}
							break;
						case AnimationID.TEKTON_FAST_AUTO1:
						case AnimationID.TEKTON_FAST_AUTO2:
							tektonActive = true;
							if (npcs.getTicksUntilAttack() < 1)
							{
								npcs.setTicksUntilAttack(3);
							}
							break;
						case AnimationID.TEKTON_ANVIL:
							tektonActive = false;
							tektonAttackTicks = 47;
							if (npcs.getTicksUntilAttack() < 1)
							{
								npcs.setTicksUntilAttack(15);
							}
					}
					break;
				case NpcID.GUARDIAN:
				case NpcID.GUARDIAN_7570:
				case NpcID.GUARDIAN_7571:
				case NpcID.GUARDIAN_7572:
					npcs.setTicksUntilAttack(npcs.getTicksUntilAttack() - 1);
					npcs.setAttackStyle(NPCContainer.Attackstyle.MELEE);
					if (npcs.getNpc().getAnimation() == ANIMATION_ID_G1 &&
						npcs.getTicksUntilAttack() < 1)
					{
						npcs.setTicksUntilAttack(5);
					}
					break;
				case NpcID.VANGUARD_7529:
					if (npcs.getAttackStyle() == NPCContainer.Attackstyle.UNKNOWN)
					{
						npcs.setAttackStyle(NPCContainer.Attackstyle.MAGE);
					}
					break;
				case NpcID.VANGUARD_7528:
					if (npcs.getAttackStyle() == NPCContainer.Attackstyle.UNKNOWN)
					{
						npcs.setAttackStyle(NPCContainer.Attackstyle.RANGE);
					}
					break;
				case NpcID.VANGUARD_7527:
					if (npcs.getAttackStyle() == NPCContainer.Attackstyle.UNKNOWN)
					{
						npcs.setAttackStyle(NPCContainer.Attackstyle.MELEE);
					}
					break;
			}
		}
		if (tektonActive && tektonAttackTicks > 0)
		{
			tektonAttackTicks--;
		}
	}

	private void handleOlm()
	{
		olmHealPools.clear();
		olmPortals.clear();
		client.clearHintArrow();
		sleepcount--;

		if (!olmReady && olmNPC != null && olmNPC.getCombatLevel() > 0)
		{
			olmReady = true;
			olmTicksUntilAction = olmActive ? 3 : 4;
			olmActionCycle = 4;
		}

		if (olmTicksUntilAction == 1)
		{
			if (olmActionCycle == 1)
			{
				olmActionCycle = 4;
				olmTicksUntilAction = 4;
				if (olmNextSpec == 1)
				{
					if (olmPhase == 1)
					{
						olmNextSpec = 4; // 4 = heal 3= cry 2 = lightn 1 = swap
					}
					else
					{
						olmNextSpec = 3;
					}
				}
				else
				{
					olmNextSpec--;
				}
			}
			else
			{
				if (olmActionCycle != -1)
				{
					olmActionCycle--;
				}
				olmTicksUntilAction = 4;
			}
		}
		else
		{
			olmTicksUntilAction--;
		}

		for (GraphicsObject o : client.getGraphicsObjects())
		{
			if (sleepcount <= 0)
			{
				if (o.getId() == 1338)
				{
					olmTicksUntilAction = 1;
					olmNextSpec = 2;
					olmActionCycle = 4; //spec=1 null=3
					sleepcount = 5;
				}
				if (o.getId() == 1356)
				{
					olmTicksUntilAction = 4;
					olmNextSpec = 1;
					olmActionCycle = 4; //spec=1 null=3
					sleepcount = 50;
				}
			}
			if (o.getId() == GraphicID.OLM_TELEPORT)
			{
				olmPortals.add(WorldPoint.fromLocal(client, o.getLocation()));
			}
			if (o.getId() == GraphicID.OLM_HEAL)
			{
				olmHealPools.add(WorldPoint.fromLocal(client, o.getLocation()));
			}
			if (!olmPortals.isEmpty())
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

	boolean inRaid()
	{
		return client.getVar(Varbits.IN_RAID) == 1;
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (!event.getGroup().equals("Cox"))
		{
			return;
		}

		if (event.getKey().equals("mirrorMode"))
		{
			coxOverlay.determineLayer();
			coxInfoBox.determineLayer();
			overlayManager.remove(coxOverlay);
			overlayManager.remove(coxInfoBox);
			overlayManager.add(coxOverlay);
			overlayManager.add(coxInfoBox);
		}
	}
}