/*
 * Copyright (c) 2019 Im2be <https://github.com/Im2be>
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

package net.runelite.client.plugins.cerberus;

import com.google.common.collect.ComparisonChain;
import com.google.inject.Provides;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.NPC;
import net.runelite.api.Prayer;
import net.runelite.api.Skill;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.ProjectileSpawned;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginType;
import net.runelite.client.plugins.cerberus.domain.CerberusArena;
import net.runelite.client.plugins.cerberus.domain.CerberusAttack;
import net.runelite.client.plugins.cerberus.domain.CerberusGhost;
import net.runelite.client.plugins.cerberus.domain.CerberusNPC;
import net.runelite.client.plugins.cerberus.domain.CerberusPhase;
import net.runelite.client.plugins.cerberus.overlays.CerberusOverlay;
import net.runelite.client.plugins.cerberus.overlays.CerberusPhaseOverlay;
import net.runelite.client.plugins.cerberus.overlays.CerberusPrayerOverlay;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.http.api.item.ItemStats;
import org.pf4j.Extension;

@Extension
@PluginDescriptor(
	name = "Cerberus",
	enabledByDefault = false,
	description = "Show Cerberus' attacks and what to pray against the summoned souls",
	tags = {"bosses", "combat", "ghosts", "prayer", "pve", "overlay", "souls", "guitar", "hero"},
	type = PluginType.PVM
)

@Slf4j
@Singleton
public class CerberusPlugin extends Plugin
{
	@Getter(AccessLevel.PUBLIC)
	private final List<NPC> ghosts = new ArrayList<>();
	@Getter(AccessLevel.PUBLIC)
	private final List<CerberusAttack> upcomingAttacks = new ArrayList<>(10);
	@Getter(AccessLevel.PUBLIC)
	private CerberusNPC cerberus;
	@Getter(AccessLevel.PUBLIC)
	private Prayer prayer;
	@Getter(AccessLevel.PUBLIC)
	private int gameTick = 0;
	@Getter(AccessLevel.PUBLIC)
	private long lastTick;
	@Inject
	@Getter(AccessLevel.PUBLIC)
	private Client client;
	@Inject
	private OverlayManager overlayManager;
	@Inject
	private ItemManager itemManager;
	@Inject
	private CerberusOverlay attacksOverlay;
	@Inject
	private CerberusPhaseOverlay phaseOverlay;
	@Inject
	private CerberusPrayerOverlay prayerOverlay;
	@Inject
	@Getter(AccessLevel.PUBLIC)
	private CerberusConfig config;

	@Provides
	CerberusConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(CerberusConfig.class);
	}


	private int tickTimestampIndex;
	private final List<Long> tickTimestamps = new ArrayList<>(5);

	@Override
	protected void startUp()
	{
		overlayManager.add(prayerOverlay);
		overlayManager.add(phaseOverlay);
		overlayManager.add(attacksOverlay);
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(prayerOverlay);
		overlayManager.remove(phaseOverlay);
		overlayManager.remove(attacksOverlay);
	}


	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}

		if (cerberus == null)
		{
			return;
		}

		if (tickTimestamps.size() <= tickTimestampIndex)
		{
			tickTimestamps.add(System.currentTimeMillis());
		}
		else
		{
			tickTimestamps.set(tickTimestampIndex, System.currentTimeMillis());
		}

		long min = 0;
		for (int i = 0; i < tickTimestamps.size(); ++i)
		{
			if (min == 0)
			{
				min = tickTimestamps.get(i) + 600 * ((tickTimestampIndex - i + 5) % 5);
			}
			else
			{
				min = Math.min(min, tickTimestamps.get(i) + 600 * ((tickTimestampIndex - i + 5) % 5));
			}
		}
		tickTimestampIndex = (tickTimestampIndex + 1) % 5;
		lastTick = min;
		++gameTick;

		if (gameTick % 10 == 3)
		{
			setPrayer();
		}

		calculateUpcomingAttacks();

		if (ghosts.size() <= 1)
		{
			return;
		}

		ghosts.sort((a, b) -> ComparisonChain.start()
			// First, sort by the southernmost ghost (e.g with lowest y)
			.compare(a.getLocalLocation().getY(), b.getLocalLocation().getY())
			// Then, sort by the westernmost ghost (e.g with lowest x)
			.compare(a.getLocalLocation().getX(), b.getLocalLocation().getX())
			// This will give use the current wave and order of the ghosts based on
			// what ghost will attack first
			.result());
	}


	private void calculateUpcomingAttacks()
	{
		upcomingAttacks.clear();

		final CerberusNPC.Attack lastCerberusAttack = cerberus.getLastAttack();

		if (lastCerberusAttack == null)
		{
			return;
		}

		final int lastCerberusAttackTick = cerberus.getLastAttackTick();
		final int health = cerberus.getHealth();
		final CerberusPhase expectedPhase = cerberus.getNextAttackPhase(1, health);


		final CerberusPhase lastCerberusPhase = cerberus.getLastAttackPhase();

		int tickDelay = 0;
		if (lastCerberusPhase != null)
		{
			tickDelay = lastCerberusPhase.getTickDelay();
		}

		for (int tick = gameTick + 1; tick <= gameTick + 10; ++tick)
		{
			if (ghosts.size() == 3)
			{
				final Optional<CerberusGhost> ghost;
				if (cerberus.getLastGhostYellTick() == tick - 13)
				{
					ghost = CerberusGhost.fromNPC(ghosts.get(ghosts.size() - 3));
				}
				else if (cerberus.getLastGhostYellTick() == tick - 15)
				{
					ghost = CerberusGhost.fromNPC(ghosts.get(ghosts.size() - 2));
				}
				else if (cerberus.getLastGhostYellTick() == tick - 17)
				{
					ghost = CerberusGhost.fromNPC(ghosts.get(ghosts.size() - 1));
				}
				else
				{
					ghost = null;
				}

				if (ghost != null && ghost.isPresent())
				{
					switch (ghost.get().getType())
					{
						case ATTACK:
							upcomingAttacks.add(new CerberusAttack(tick, CerberusNPC.Attack.GHOST_MELEE));
							break;
						case RANGED:
							upcomingAttacks.add(new CerberusAttack(tick, CerberusNPC.Attack.GHOST_RANGED));
							break;
						case MAGIC:
							upcomingAttacks.add(new CerberusAttack(tick, CerberusNPC.Attack.GHOST_MAGIC));
							break;
					}
					continue;
				}
			}

			if (expectedPhase == CerberusPhase.TRIPLE)
			{
				if (cerberus.getLastTripleAttack() == CerberusNPC.Attack.MAGIC)
				{
					if (lastCerberusAttackTick + 4 == tick)
					{
						upcomingAttacks.add(new CerberusAttack(tick, CerberusNPC.Attack.RANGED));
					}
					else if (lastCerberusAttackTick + 7 == tick)
					{
						upcomingAttacks.add(new CerberusAttack(tick, CerberusNPC.Attack.MELEE));
					}
				}
				else if (cerberus.getLastTripleAttack() == CerberusNPC.Attack.RANGED)
				{
					if (lastCerberusAttackTick + 4 == tick)
					{
						upcomingAttacks.add(new CerberusAttack(tick, CerberusNPC.Attack.MELEE));
					}
				}
				else if (cerberus.getLastTripleAttack() == null)
				{
					if (lastCerberusAttackTick + tickDelay + 2 == tick)
					{
						upcomingAttacks.add(new CerberusAttack(tick, CerberusNPC.Attack.MAGIC));
					}
					else if (lastCerberusAttackTick + tickDelay + 5 == tick)
					{
						upcomingAttacks.add(new CerberusAttack(tick, CerberusNPC.Attack.RANGED));
					}
				}
			}
			else if (expectedPhase == CerberusPhase.AUTO)
			{
				if (lastCerberusAttackTick + tickDelay + 1 == tick)
				{
					if (getPrayer() == Prayer.PROTECT_FROM_MAGIC)
					{
						upcomingAttacks.add(new CerberusAttack(tick, CerberusNPC.Attack.MAGIC));
					}
					else if (getPrayer() == Prayer.PROTECT_FROM_MISSILES)
					{
						upcomingAttacks.add(new CerberusAttack(tick, CerberusNPC.Attack.RANGED));
					}
					else if (getPrayer() == Prayer.PROTECT_FROM_MELEE)
					{
						upcomingAttacks.add(new CerberusAttack(tick, CerberusNPC.Attack.MELEE));
					}
				}
			}
		}
	}

	@Subscribe
	private void onGameStateChanged(GameStateChanged event)
	{
		GameState gameState = event.getGameState();
		if (gameState == GameState.LOGIN_SCREEN || gameState == GameState.HOPPING || gameState == GameState.CONNECTION_LOST)
		{
			cerberus = null;
			ghosts.clear();
			upcomingAttacks.clear();
		}
	}

	@Subscribe
	private void onProjectileSpawned(ProjectileSpawned event)
	{
		if (cerberus == null)
		{
			return;
		}
		var projectile = event.getProjectile();
		var hp = cerberus.getHealth();
		var expectedAttack = cerberus.getNextAttackPhase(1, hp);
		switch (projectile.getId())
		{
			case 1242: //Magic
				log.debug(gameTick + " - Attack " + (cerberus.getPhaseCount() + 1) + " - Cerb HP: " + cerberus.getHealth() + " - Expecting " + expectedAttack + " -> Cerberus projectile: MAGIC");
				if (expectedAttack != CerberusPhase.TRIPLE)
				{
					cerberus.nextPhase(CerberusPhase.AUTO);
				}
				else
				{
					cerberus.setLastTripleAttack(CerberusNPC.Attack.MAGIC);
				}
				cerberus.doProjectileOrAnimation(gameTick, CerberusNPC.Attack.MAGIC);
				break;
			case 1245: //Ranged
				log.debug(gameTick + " - Attack " + (cerberus.getPhaseCount() + 1) + " - Cerb HP: " + cerberus.getHealth() + " - Expecting " + expectedAttack + " -> Cerberus projectile: RANGED");
				if (expectedAttack != CerberusPhase.TRIPLE)
				{
					cerberus.nextPhase(CerberusPhase.AUTO);
				}
				else
				{
					cerberus.setLastTripleAttack(CerberusNPC.Attack.RANGED);
				}
				cerberus.doProjectileOrAnimation(gameTick, CerberusNPC.Attack.RANGED);
				break;
			case 34:
				if (!ghosts.isEmpty())
				{
					log.debug(gameTick + " - Attack " + (cerberus.getPhaseCount() + 1) + " - Cerb HP: " + cerberus.getHealth() + " - Expecting " + expectedAttack + " -> Ghost projectile: RANGED");
				}
				break;
			case 100:
				if (!ghosts.isEmpty())
				{
					log.debug(gameTick + " - Attack " + (cerberus.getPhaseCount() + 1) + " - Cerb HP: " + cerberus.getHealth() + " - Expecting " + expectedAttack + " -> Ghost projectile: MAGIC");
				}
				break;
			case 1248:
				if (!ghosts.isEmpty())
				{
					log.debug(gameTick + " - Attack " + (cerberus.getPhaseCount() + 1) + " - Cerb HP: " + cerberus.getHealth() + " - Expecting " + expectedAttack + " -> Ghost projectile: MELEE");
				}
				break;
			case 15:
			case 1247: //Lava
			default:
				break;

		}
	}


	@Subscribe
	private void onAnimationChanged(AnimationChanged event)
	{
		if (cerberus == null)
		{
			return;
		}

		final Actor actor = event.getActor();
		if (!cerberus.getNpc().equals(actor))
		{
			return;
		}


		int animationId = cerberus.getNpc().getAnimation();
		var hp = cerberus.getHealth();
		var expectedAttack = cerberus.getNextAttackPhase(1, hp);

		switch (animationId)
		{
			case 4491: //MELEE
				log.debug(gameTick + " - Attack " + (cerberus.getPhaseCount() + 1) + " - Cerb HP: " + cerberus.getHealth() + " - Expecting " + expectedAttack + " -> Cerberus animation: MELEE");
				cerberus.setLastTripleAttack(null);
				cerberus.nextPhase(expectedAttack);
				cerberus.doProjectileOrAnimation(gameTick, CerberusNPC.Attack.MELEE);
				break;
			case 4493: //LAVA
				log.debug(gameTick + " - Attack " + (cerberus.getPhaseCount() + 1) + " - Cerb HP: " + cerberus.getHealth() + " - Expecting " + expectedAttack + " -> Cerberus animation: LAVA");
				cerberus.nextPhase(CerberusPhase.LAVA);
				cerberus.doProjectileOrAnimation(gameTick, CerberusNPC.Attack.LAVA);
				break;
			case 4494: //GHOSTS
				log.debug(gameTick + " - Attack " + (cerberus.getPhaseCount() + 1) + " - Cerb HP: " + cerberus.getHealth() + " - Expecting " + expectedAttack + " -> Cerberus animation: GHOSTS");
				cerberus.nextPhase(CerberusPhase.GHOSTS);
				cerberus.setLastGhostYellTick(gameTick);
				cerberus.setLastGhostYellTime(System.currentTimeMillis());
				cerberus.doProjectileOrAnimation(gameTick, CerberusNPC.Attack.GHOSTS);
				break;
			case 4487: //Cerberus resets (sits down)
			case 4486: //Start of the fight (cerberus stands up)
				cerberus = new CerberusNPC(cerberus.getNpc());
				gameTick = 0;
				lastTick = System.currentTimeMillis();
				upcomingAttacks.clear();
				tickTimestamps.clear();
				tickTimestampIndex = 0;
				cerberus.doProjectileOrAnimation(gameTick, CerberusNPC.Attack.SPAWN);
				break;
			case -1: //idle
			case 4489: //Cerberus animation: taking damage
			case 4490: //Ranged animation (projectile is tracked)
				break;
			case 4495: //Cerberus death
				cerberus = null;
				ghosts.clear();
				break;
			default:
				log.debug(gameTick + " - Cerberus animation: UNKNOWN (id: " + animationId + ")");
		}
	}

	public void setPrayer()
	{
		int dStab = 0, dMagic = 0, dRange = 0;

		ItemContainer c = client.getItemContainer(InventoryID.EQUIPMENT);
		if (c != null)
		{
			final Item[] items = c.getItems();
			for (var item : items)
			{
				if (item != null)
				{
					final ItemStats stats = itemManager.getItemStats(item.getId(), false);
					if (stats == null)
					{
						continue;
					}
					var equipmentStats = stats.getEquipment();
					if (equipmentStats == null)
					{
						continue;
					}

					dStab += equipmentStats.getDstab();
					dMagic += equipmentStats.getDmagic();
					dRange += equipmentStats.getDrange();
				}
			}
		}


		var magicLvl = client.getBoostedSkillLevel(Skill.MAGIC);
		var defenseLvl = client.getBoostedSkillLevel(Skill.DEFENCE);


		var magDefenceTotal = (int) (((double) magicLvl) * 0.7 + ((double) defenseLvl) * 0.3) + dMagic;
		var ranDefenceTotal = defenseLvl + dRange;
		var melDefenceTotal = defenseLvl + dStab;

		var loc = client.getLocalPlayer().getWorldLocation();
		var cerbLoc = cerberus.getNpc().getWorldLocation();

		//If you're not in melee range, disregard your stab defense
		if (loc.getX() < cerbLoc.getX() - 1 || loc.getX() > cerbLoc.getX() + 5 || loc.getY() < cerbLoc.getY() - 1 || loc.getY() > cerbLoc.getY() + 5)
		{
			melDefenceTotal = Integer.MAX_VALUE;
		}

		if (magDefenceTotal <= ranDefenceTotal && magDefenceTotal <= melDefenceTotal)
		{
			prayer = Prayer.PROTECT_FROM_MAGIC;
		}
		else if (ranDefenceTotal <= melDefenceTotal)
		{
			prayer = Prayer.PROTECT_FROM_MISSILES;
		}
		else
		{
			prayer = Prayer.PROTECT_FROM_MELEE;
		}
	}

	@Subscribe
	private void onNpcSpawned(final NpcSpawned event)
	{

		final NPC npc = event.getNpc();
		if (cerberus == null && npc != null && npc.getName() != null &&
			npc.getName().toLowerCase().contains("cerberus"))
		{
			log.debug("Cerberus-NPC spawned: " + npc.getName() + " (ID: " + npc.getId() + ")");
			cerberus = new CerberusNPC(npc);
			gameTick = 0;
			lastTick = System.currentTimeMillis();
			upcomingAttacks.clear();
			tickTimestamps.clear();
			tickTimestampIndex = 0;
		}

		if (cerberus == null)
		{
			return;
		}

		CerberusGhost.fromNPC(npc).ifPresent(ghost -> ghosts.add(npc));
	}

	@Subscribe
	private void onNpcDespawned(final NpcDespawned event)
	{
		final NPC npc = event.getNpc();


		if (npc != null && npc.getName() != null &&
			npc.getName().toLowerCase().contains("cerberus"))
		{
			cerberus = null;
			ghosts.clear();
			log.debug("Cerberus-NPC despawned: " + npc.getName() + " (ID: " + npc.getId() + ")");
		}

		if (cerberus == null)
		{
			if (ghosts.size() != 0)
			{
				ghosts.clear();
			}
			return;
		}

		ghosts.remove(event.getNpc());
	}

	public boolean inCerberusArena()
	{
		return CerberusArena.getArena(client.getLocalPlayer().getWorldLocation()) != null;
	}
}