/*
 * Copyright (c) 2020, dutta64 <https://github.com/dutta64>
 * Copyright (c) 2019, kThisIsCvpv <https://github.com/kThisIsCvpv>
 * Copyright (c) 2019, ganom <https://github.com/Ganom>
 * Copyright (c) 2019, kyle <https://github.com/Kyleeld>
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

package net.runelite.client.plugins.gauntlet;

import com.google.inject.Provides;
import java.util.HashSet;
import java.util.Set;
import javax.inject.Inject;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.Actor;
import net.runelite.api.AnimationID;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.HeadIcon;
import net.runelite.api.NPC;
import net.runelite.api.NPCDefinition;
import net.runelite.api.NpcID;
import net.runelite.api.NullNpcID;
import net.runelite.api.ObjectID;
import net.runelite.api.Player;
import net.runelite.api.ProjectileID;
import net.runelite.api.SoundEffectID;
import net.runelite.api.Varbits;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.PlayerDeath;
import net.runelite.api.events.ProjectileSpawned;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.queries.GameObjectQuery;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginType;
import net.runelite.client.plugins.gauntlet.entity.Demiboss;
import net.runelite.client.plugins.gauntlet.entity.Hunllef;
import static net.runelite.client.plugins.gauntlet.entity.Hunllef.BossAttack.LIGHTNING;
import static net.runelite.client.plugins.gauntlet.entity.Hunllef.BossAttack.MAGIC;
import static net.runelite.client.plugins.gauntlet.entity.Hunllef.BossAttack.PRAYER;
import static net.runelite.client.plugins.gauntlet.entity.Hunllef.BossAttack.RANGE;
import net.runelite.client.plugins.gauntlet.entity.Projectile;
import net.runelite.client.plugins.gauntlet.entity.Resource;
import net.runelite.client.plugins.gauntlet.entity.Tornado;
import net.runelite.client.plugins.gauntlet.overlay.Overlay;
import net.runelite.client.plugins.gauntlet.overlay.OverlayGauntlet;
import net.runelite.client.plugins.gauntlet.overlay.OverlayHunllefRoom;
import net.runelite.client.plugins.gauntlet.overlay.OverlayPrayerBox;
import net.runelite.client.plugins.gauntlet.overlay.OverlayPrayerWidget;
import net.runelite.client.plugins.gauntlet.overlay.OverlayTimer;
import net.runelite.client.plugins.gauntlet.resource.ResourceTracker;
import net.runelite.client.ui.overlay.OverlayManager;
import org.pf4j.Extension;

@Extension
@PluginDescriptor(
	name = "Gauntlet",
	enabledByDefault = false,
	description = "All-in-one plugin for the Gauntlet.",
	tags = {"gauntlet"},
	type = PluginType.MINIGAME
)
public class GauntletPlugin extends Plugin
{
	private static final int ONEHAND_SLASH_AXE_ANIMATION = 395;
	private static final int ONEHAND_CRUSH_PICKAXE_ANIMATION = 400;
	private static final int ONEHAND_CRUSH_AXE_ANIMATION = 401;
	private static final int UNARMED_PUNCH_ANIMATION = 422;
	private static final int UNARMED_KICK_ANIMATION = 423;
	private static final int BOW_ATTACK_ANIMATION = 426;
	private static final int ONEHAND_STAB_HALBERD_ANIMATION = 428;
	private static final int ONEHAND_SLASH_HALBERD_ANIMATION = 440;

	private static final Set<Integer> MELEE_ANIM_IDS = Set.of(
		AnimationID.ONEHAND_STAB_SWORD_ANIMATION,
		AnimationID.ONEHAND_SLASH_SWORD_ANIMATION,
		ONEHAND_SLASH_AXE_ANIMATION,
		ONEHAND_CRUSH_PICKAXE_ANIMATION,
		ONEHAND_CRUSH_AXE_ANIMATION,
		UNARMED_PUNCH_ANIMATION,
		UNARMED_KICK_ANIMATION,
		ONEHAND_STAB_HALBERD_ANIMATION,
		ONEHAND_SLASH_HALBERD_ANIMATION
	);

	private static final Set<Integer> PLAYER_ANIM_IDS = Set.of(
		AnimationID.ONEHAND_STAB_SWORD_ANIMATION,
		AnimationID.ONEHAND_SLASH_SWORD_ANIMATION,
		ONEHAND_SLASH_AXE_ANIMATION,
		ONEHAND_CRUSH_PICKAXE_ANIMATION,
		ONEHAND_CRUSH_AXE_ANIMATION,
		UNARMED_PUNCH_ANIMATION,
		UNARMED_KICK_ANIMATION,
		BOW_ATTACK_ANIMATION,
		ONEHAND_STAB_HALBERD_ANIMATION,
		ONEHAND_SLASH_HALBERD_ANIMATION,
		AnimationID.HIGH_LEVEL_MAGIC_ATTACK
	);

	private static final Set<Integer> PROJECTILE_MAGIC_IDS = Set.of(
		ProjectileID.HUNLLEF_MAGE_ATTACK,
		ProjectileID.HUNLLEF_CORRUPTED_MAGE_ATTACK
	);

	private static final Set<Integer> PROJECTILE_RANGE_IDS = Set.of(
		ProjectileID.HUNLLEF_RANGE_ATTACK,
		ProjectileID.HUNLLEF_CORRUPTED_RANGE_ATTACK
	);

	private static final Set<Integer> PROJECTILE_PRAYER_IDS = Set.of(
		ProjectileID.HUNLLEF_PRAYER_ATTACK,
		ProjectileID.HUNLLEF_CORRUPTED_PRAYER_ATTACK
	);

	private static final Set<Integer> PROJECTILE_IDS = Set.of(
		ProjectileID.HUNLLEF_PRAYER_ATTACK,
		ProjectileID.HUNLLEF_CORRUPTED_PRAYER_ATTACK,
		ProjectileID.HUNLLEF_RANGE_ATTACK,
		ProjectileID.HUNLLEF_CORRUPTED_RANGE_ATTACK,
		ProjectileID.HUNLLEF_MAGE_ATTACK,
		ProjectileID.HUNLLEF_CORRUPTED_MAGE_ATTACK
	);

	private static final Set<Integer> HUNLLEF_IDS = Set.of(
		NpcID.CRYSTALLINE_HUNLLEF,
		NpcID.CRYSTALLINE_HUNLLEF_9022,
		NpcID.CRYSTALLINE_HUNLLEF_9023,
		NpcID.CRYSTALLINE_HUNLLEF_9024,
		NpcID.CORRUPTED_HUNLLEF,
		NpcID.CORRUPTED_HUNLLEF_9036,
		NpcID.CORRUPTED_HUNLLEF_9037,
		NpcID.CORRUPTED_HUNLLEF_9038
	);

	private static final Set<Integer> TORNADO_IDS = Set.of(
		NullNpcID.NULL_9025,
		NullNpcID.NULL_9039
	);

	private static final Set<Integer> DEMIBOSS_IDS = Set.of(
		NpcID.CRYSTALLINE_BEAR,
		NpcID.CRYSTALLINE_DARK_BEAST,
		NpcID.CRYSTALLINE_DRAGON,
		NpcID.CORRUPTED_BEAR,
		NpcID.CORRUPTED_DARK_BEAST,
		NpcID.CORRUPTED_DRAGON
	);

	private static final Set<Integer> STRONG_NPC_IDS = Set.of(
		NpcID.CRYSTALLINE_SCORPION,
		NpcID.CRYSTALLINE_UNICORN,
		NpcID.CRYSTALLINE_WOLF,
		NpcID.CORRUPTED_SCORPION,
		NpcID.CORRUPTED_UNICORN,
		NpcID.CORRUPTED_WOLF
	);

	private static final Set<Integer> WEAK_NPC_IDS = Set.of(
		NpcID.CRYSTALLINE_BAT,
		NpcID.CRYSTALLINE_RAT,
		NpcID.CRYSTALLINE_SPIDER,
		NpcID.CORRUPTED_BAT,
		NpcID.CORRUPTED_RAT,
		NpcID.CORRUPTED_SPIDER
	);

	private static final Set<Integer> RESOURCE_IDS = Set.of(
		ObjectID.CRYSTAL_DEPOSIT,
		ObjectID.CORRUPT_DEPOSIT,
		ObjectID.PHREN_ROOTS,
		ObjectID.PHREN_ROOTS_36066,
		ObjectID.FISHING_SPOT_36068,
		ObjectID.FISHING_SPOT_35971,
		ObjectID.GRYM_ROOT,
		ObjectID.GRYM_ROOT_36070,
		ObjectID.LINUM_TIRINUM,
		ObjectID.LINUM_TIRINUM_36072
	);

	private static final Set<Integer> UTILITY_IDS = Set.of(
		ObjectID.SINGING_BOWL_35966,
		ObjectID.SINGING_BOWL_36063,
		ObjectID.RANGE_35980,
		ObjectID.RANGE_36077,
		ObjectID.WATER_PUMP_35981,
		ObjectID.WATER_PUMP_36078
	);

	@Inject
	private Client client;

	@Inject
	private GauntletConfig config;

	@Inject
	private ResourceTracker resourceTracker;

	@Inject
	private SkillIconManager skillIconManager;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private OverlayGauntlet overlayGauntlet;

	@Inject
	private OverlayHunllefRoom overlayHunllefRoom;

	@Inject
	private OverlayPrayerWidget overlayPrayerWidget;

	@Inject
	private OverlayPrayerBox overlayPrayerBox;

	@Inject
	private OverlayTimer overlayTimer;

	private Set<Overlay> overlays;

	@Getter
	private final Set<Resource> resources = new HashSet<>();

	@Getter
	private final Set<GameObject> utilities = new HashSet<>();

	@Getter
	private final Set<Projectile> projectiles = new HashSet<>();

	@Getter
	private final Set<Tornado> tornados = new HashSet<>();

	@Getter
	private final Set<Demiboss> demibosses = new HashSet<>();

	@Getter
	private final Set<NPC> strongNpcs = new HashSet<>();

	@Getter
	private final Set<NPC> weakNpcs = new HashSet<>();

	@Getter
	private Hunllef hunllef;

	@Getter
	@Setter
	private boolean flash;

	@Getter
	private boolean inGauntlet;

	@Provides
	GauntletConfig getConfig(final ConfigManager configManager)
	{
		return configManager.getConfig(GauntletConfig.class);
	}

	@Override
	protected void startUp()
	{
		overlays = Set.of(
			overlayGauntlet,
			overlayHunllefRoom,
			overlayPrayerBox,
			overlayPrayerWidget,
			overlayTimer
		);

		if (client.getGameState() != GameState.LOGGED_IN || !isInTheGauntlet())
		{
			return;
		}

		for (final GameObject gameObject : new GameObjectQuery().result(client))
		{
			addGameObject(gameObject);
		}

		for (final NPC npc : client.getNpcs())
		{
			addNpc(npc);
		}

		resourceTracker.setNamedDropMessage(client);

		overlayTimer.initialize();

		overlays.forEach(o -> overlayManager.add(o));

		inGauntlet = true;
	}

	@Override
	protected void shutDown()
	{
		inGauntlet = false;

		overlays.forEach(o -> overlayManager.remove(o));

		hunllef = null;
		flash = false;

		overlayTimer.reset();
		resourceTracker.reset();

		resources.clear();
		utilities.clear();
		projectiles.clear();
		tornados.clear();
		demibosses.clear();
		strongNpcs.clear();
		weakNpcs.clear();
	}

	@Subscribe
	private void onConfigChanged(final ConfigChanged event)
	{
		if (!event.getGroup().equals("gauntlet"))
		{
			return;
		}

		switch (event.getKey())
		{
			case "resourceIconSize":
				resources.forEach(r -> r.setIconSize(config.resourceIconSize()));
				break;
			case "projectileIconSize":
				projectiles.forEach(p -> p.setIconSize(config.projectileIconSize()));
				break;
			case "hunllefAttackStyleIconSize":
				if (hunllef != null)
				{
					hunllef.setAttackStyleIconSize(config.hunllefAttackStyleIconSize());
				}
				break;
			case "mirrorMode":
				overlays.forEach(overlay -> {
					overlay.determineLayer();

					if (overlayManager.anyMatch(o -> o == overlay))
					{
						overlayManager.remove(overlay);
						overlayManager.add(overlay);
					}
				});
				break;
			default:
				break;
		}
	}

	@Subscribe
	private void onGameStateChanged(final GameStateChanged event)
	{
		if (!inGauntlet)
		{
			return;
		}

		final GameState gameState = event.getGameState();

		switch (gameState)
		{
			case LOGIN_SCREEN:
			case HOPPING:
				shutDown();
				break;
			case LOADING:
				resources.clear();
				utilities.clear();
				break;
			default:
				break;
		}
	}

	@Subscribe
	private void onGameTick(final GameTick event)
	{
		if (!inGauntlet || hunllef == null || !isInHunllefRoom())
		{
			return;
		}

		hunllef.onGameTick();

		if (!projectiles.isEmpty())
		{
			projectiles.removeIf(p -> p.getProjectile().getRemainingCycles() <= 0);
		}

		if (!tornados.isEmpty())
		{
			tornados.forEach(Tornado::updateTimeLeft);
		}
	}

	@Subscribe
	private void onVarbitChanged(final VarbitChanged event)
	{
		if (isInTheGauntlet())
		{
			if (!inGauntlet)
			{
				inGauntlet = true;

				overlays.forEach(o -> overlayManager.add(o));

				resourceTracker.setNamedDropMessage(client);
			}

			overlayTimer.updateState();
		}
		else
		{
			if (inGauntlet)
			{
				shutDown();
			}
		}
	}

	@Subscribe
	private void onGameObjectSpawned(final GameObjectSpawned event)
	{
		if (!inGauntlet)
		{
			return;
		}

		final GameObject gameObject = event.getGameObject();

		addGameObject(gameObject);
	}

	@Subscribe
	private void onGameObjectDespawned(final GameObjectDespawned event)
	{
		if (!inGauntlet)
		{
			return;
		}

		final GameObject gameObject = event.getGameObject();

		removeGameObject(gameObject);
	}

	@Subscribe
	private void onNpcSpawned(final NpcSpawned event)
	{
		if (!inGauntlet)
		{
			return;
		}

		final NPC npc = event.getNpc();

		addNpc(npc);
	}

	@Subscribe
	private void onNpcDespawned(final NpcDespawned event)
	{
		if (!inGauntlet)
		{
			return;
		}

		final NPC npc = event.getNpc();

		removeNpc(npc);
	}

	@Subscribe
	private void onPlayerDeath(final PlayerDeath event)
	{
		if (!inGauntlet)
		{
			return;
		}

		overlayTimer.onPlayerDeath();
	}

	@Subscribe
	private void onProjectileSpawned(final ProjectileSpawned event)
	{
		if (!inGauntlet || hunllef == null || !isInHunllefRoom())
		{
			return;
		}

		final net.runelite.api.Projectile projectile = event.getProjectile();

		addProjectile(projectile);
	}

	@Subscribe
	private void onAnimationChanged(final AnimationChanged event)
	{
		if (!inGauntlet || hunllef == null || !isInHunllefRoom())
		{
			return;
		}

		final Actor actor = event.getActor();

		processAnimation(actor);
	}

	@Subscribe
	private void onChatMessage(final ChatMessage event)
	{
		if (!inGauntlet || isInHunllefRoom() || (event.getType() != ChatMessageType.SPAM
			&& event.getType() != ChatMessageType.GAMEMESSAGE))
		{
			return;
		}

		final String chatMessage = event.getMessage();

		resourceTracker.parseChatMessage(chatMessage);
	}

	private void addGameObject(final GameObject gameObject)
	{
		final int id = gameObject.getId();

		if (RESOURCE_IDS.contains(id))
		{
			resources.add(new Resource(gameObject, config.resourceIconSize(), skillIconManager));
		}
		else if (UTILITY_IDS.contains(id))
		{
			utilities.add(gameObject);
		}
	}

	private void removeGameObject(final GameObject gameObject)
	{
		final int id = gameObject.getId();

		if (RESOURCE_IDS.contains(gameObject.getId()))
		{
			resources.removeIf(o -> o.getGameObject() == gameObject);
		}
		else if (UTILITY_IDS.contains(id))
		{
			utilities.remove(gameObject);
		}
	}

	private void addNpc(final NPC npc)
	{
		final int id = npc.getId();

		if (HUNLLEF_IDS.contains(id))
		{
			hunllef = new Hunllef(npc, config.hunllefAttackStyleIconSize(), skillIconManager);
		}
		else if (TORNADO_IDS.contains(id))
		{
			tornados.add(new Tornado(npc));
		}
		else if (DEMIBOSS_IDS.contains(id))
		{
			demibosses.add(new Demiboss(npc));
		}
		else if (STRONG_NPC_IDS.contains(id))
		{
			strongNpcs.add(npc);
		}
		else if (WEAK_NPC_IDS.contains(id))
		{
			weakNpcs.add(npc);
		}
	}

	private void removeNpc(final NPC npc)
	{
		final int id = npc.getId();

		if (HUNLLEF_IDS.contains(id))
		{
			hunllef = null;
		}
		else if (TORNADO_IDS.contains(id))
		{
			tornados.removeIf(t -> t.getNpc() == npc);
		}
		else if (DEMIBOSS_IDS.contains(id))
		{
			demibosses.removeIf(d -> d.getNpc() == npc);
		}
		else if (STRONG_NPC_IDS.contains(id))
		{
			strongNpcs.remove(npc);
		}
		else if (WEAK_NPC_IDS.contains(id))
		{
			weakNpcs.remove(npc);
		}
	}

	private void addProjectile(final net.runelite.api.Projectile projectile)
	{
		final int id = projectile.getId();

		if (!PROJECTILE_IDS.contains(id))
		{
			return;
		}

		projectiles.add(new Projectile(projectile, config.projectileIconSize(), skillIconManager));

		if (PROJECTILE_MAGIC_IDS.contains(id))
		{
			hunllef.updateAttack(MAGIC);
		}
		else if (PROJECTILE_RANGE_IDS.contains(id))
		{
			hunllef.updateAttack(RANGE);
		}
		else if (PROJECTILE_PRAYER_IDS.contains(id))
		{
			hunllef.updateAttack(PRAYER);

			if (config.hunllefPrayerAudio())
			{
				client.playSoundEffect(SoundEffectID.MAGIC_SPLASH_BOING);
			}
		}
	}

	private void processAnimation(final Actor actor)
	{
		if (actor instanceof Player)
		{
			final Player player = client.getLocalPlayer();

			if (player != actor)
			{
				return;
			}

			processPlayerAnimation(player);
		}
		else if (actor instanceof NPC)
		{
			if (actor.getAnimation() == AnimationID.HUNLEFF_TORNADO)
			{
				hunllef.updateAttack(LIGHTNING);
			}
		}
	}

	private void processPlayerAnimation(final Player player)
	{
		final int animationId = player.getAnimation();

		if (!PLAYER_ANIM_IDS.contains(animationId))
		{
			return;
		}

		final NPCDefinition npcDefinition = hunllef.getNpc().getDefinition();

		if (npcDefinition == null)
		{
			return;
		}

		final HeadIcon headIcon = npcDefinition.getOverheadIcon();

		if (headIcon == null)
		{
			return;
		}

		switch (headIcon)
		{
			case MELEE:
				if (MELEE_ANIM_IDS.contains(animationId))
				{
					flash = true;
					return;
				}
				break;
			case RANGED:
				if (animationId == BOW_ATTACK_ANIMATION)
				{
					flash = true;
					return;
				}
				break;
			case MAGIC:
				if (animationId == AnimationID.HIGH_LEVEL_MAGIC_ATTACK)
				{
					flash = true;
					return;
				}
				break;
			default:
				return;
		}

		hunllef.updatePlayerAttack();
	}

	public int getInstanceRegionId()
	{
		final Player player = client.getLocalPlayer();

		if (player == null)
		{
			return -1;
		}

		final WorldPoint worldPoint = WorldPoint.fromLocalInstance(client, player.getLocalLocation());

		if (worldPoint == null)
		{
			return -1;
		}

		return worldPoint.getRegionID();
	}

	public boolean isInHunllefRoom()
	{
		return client.getVar(Varbits.GAUNTLET_FINAL_ROOM_ENTERED) == 1;
	}

	private boolean isInTheGauntlet()
	{
		return client.getVar(Varbits.GAUNTLET_ENTERED) == 1;
	}

	public boolean isLootDropNotificationsEnabled()
	{
		final boolean lootDrop = client.getVar(Varbits.LOOT_DROP_NOTIFICATIONS) == 1;
		final boolean untradeableLootDrop = client.getVar(Varbits.UNTRADEABLE_LOOT_NOTIFICATIONS) == 1;

		return lootDrop && untradeableLootDrop;
	}
}