package net.runelite.client.plugins.zulrah;

import com.google.common.base.Preconditions;
import com.google.inject.Provides;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.NPC;
import net.runelite.api.Projectile;
import net.runelite.api.Renderable;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.FocusChanged;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.ProjectileMoved;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.input.KeyListener;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.zulrah.overlays.InstanceTimerOverlay;
import net.runelite.client.plugins.zulrah.overlays.PhaseOverlay;
import net.runelite.client.plugins.zulrah.overlays.PrayerHelperOverlay;
import net.runelite.client.plugins.zulrah.overlays.PrayerMarkerOverlay;
import net.runelite.client.plugins.zulrah.overlays.SceneOverlay;
import net.runelite.client.plugins.zulrah.rotationutils.RotationType;
import net.runelite.client.plugins.zulrah.rotationutils.ZulrahData;
import net.runelite.client.plugins.zulrah.rotationutils.ZulrahPhase;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.infobox.Counter;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;
import net.runelite.client.util.ImageUtil;
import org.pf4j.Extension;


@Extension
@PluginDescriptor(
	name = "Zulrah",
	description = "All-in-One tool to help during the Zulrah fight",
	tags = {"zulrah", "zul", "andra", "snakeling", "zhuri/nicole", "girls rule boys drool"},
	enabledByDefault = false//cringe
)
public class ZulrahPlugin extends Plugin implements KeyListener
{

	public static final BufferedImage[] ZULRAH_IMAGES = new BufferedImage[3];
	private static final BufferedImage CLOCK_ICON = ImageUtil.getResourceStreamFromClass(ZulrahPlugin.class, "clock.png");
	private static boolean flipStandLocation = false;
	private static boolean flipPhasePrayer = false;
	private static boolean zulrahReset = false;

	static
	{
		ZULRAH_IMAGES[0] = ImageUtil.getResourceStreamFromClass(ZulrahPlugin.class, "zulrah_range.png");
		ZULRAH_IMAGES[1] = ImageUtil.getResourceStreamFromClass(ZulrahPlugin.class, "zulrah_melee.png");
		ZULRAH_IMAGES[2] = ImageUtil.getResourceStreamFromClass(ZulrahPlugin.class, "zulrah_magic.png");
	}

	private final Map<LocalPoint, Integer> projectilesMap = new HashMap();
	private final Map<GameObject, Integer> toxicCloudsMap = new HashMap();
	private final Collection<Renderable> snakelings = new ArrayList();
	@Inject
	private Client client;
	@Inject
	private KeyManager keyManager;
	@Inject
	private InfoBoxManager infoBoxManager;
	@Inject
	private OverlayManager overlayManager;
	@Inject
	private InstanceTimerOverlay instanceTimerOverlay;
	@Inject
	private PhaseOverlay phaseOverlay;
	@Inject
	private PrayerHelperOverlay prayerHelperOverlay;
	@Inject
	private PrayerMarkerOverlay prayerMarkerOverlay;
	@Inject
	private SceneOverlay sceneOverlay;
	@Inject
	@com.google.inject.Inject
	private ZulrahConfig config;
	private NPC zulrahNpc = null;
	private int stage = 0;
	private int phaseTicks = -1;
	private final BiConsumer<RotationType, RotationType> phaseTicksHandler = (current, potential) -> {
		if (zulrahReset)
		{
			phaseTicks = 38;
		}
		else
		{
			ZulrahPhase p = current != null ? getCurrentPhase(current) : getCurrentPhase(potential);
			Preconditions.checkNotNull(p, "Attempted to set phase ticks but current Zulrah phase was somehow null. Stage: " + stage);
			phaseTicks = p.getAttributes().getPhaseTicks();
		}

	};
	private int attackTicks = -1;
	private int totalTicks = 0;
	private RotationType currentRotation = null;
	private List<RotationType> potentialRotations = new ArrayList();
	private boolean holdingSnakelingHotkey = false;
	private Counter zulrahTotalTicksInfoBox;

	private static void setHidden(Renderable renderable, boolean hidden)
	{
		Method setHidden = null;

		try
		{
			setHidden = renderable.getClass().getMethod("setHidden", Boolean.TYPE);
		}
		catch (NoSuchMethodException var5)
		{

			return;
		}

		try
		{
			setHidden.invoke(renderable, hidden);
		}
		catch (InvocationTargetException | IllegalAccessException var4)
		{

		}

	}

	public static boolean isFlipStandLocation()
	{
		return flipStandLocation;
	}

	public static boolean isFlipPhasePrayer()
	{
		return flipPhasePrayer;
	}

	public static boolean isZulrahReset()
	{
		return zulrahReset;
	}

	@Provides
	public ZulrahConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ZulrahConfig.class);
	}

	@Override
	protected void startUp()
	{
		overlayManager.add(instanceTimerOverlay);
		overlayManager.add(phaseOverlay);
		overlayManager.add(prayerHelperOverlay);
		overlayManager.add(prayerMarkerOverlay);
		overlayManager.add(sceneOverlay);
		keyManager.registerKeyListener(this);
	}

	@Override
	protected void shutDown()
	{
		reset();
		overlayManager.remove(instanceTimerOverlay);
		overlayManager.remove(phaseOverlay);
		overlayManager.remove(prayerHelperOverlay);
		overlayManager.remove(prayerMarkerOverlay);
		overlayManager.remove(sceneOverlay);
		keyManager.unregisterKeyListener(this);
	}

	private void reset()
	{
		zulrahNpc = null;
		stage = 0;
		phaseTicks = -1;
		attackTicks = -1;
		totalTicks = 0;
		currentRotation = null;
		potentialRotations.clear();
		projectilesMap.clear();
		toxicCloudsMap.clear();
		flipStandLocation = false;
		flipPhasePrayer = false;
		instanceTimerOverlay.resetTimer();
		zulrahReset = false;
		clearSnakelingCollection();
		holdingSnakelingHotkey = false;
		handleTotalTicksInfoBox(true);

	}

	public void keyTyped(KeyEvent e)
	{
	}

	public void keyPressed(KeyEvent e)
	{
		if (config.snakelingSetting() == ZulrahConfig.SnakelingSettings.MES && config.snakelingMesHotkey().matches(e))
		{
			holdingSnakelingHotkey = true;
		}

	}

	public void keyReleased(KeyEvent e)
	{
		if (config.snakelingSetting() == ZulrahConfig.SnakelingSettings.MES && config.snakelingMesHotkey().matches(e))
		{
			holdingSnakelingHotkey = false;
		}

	}

	@Subscribe
	private void onConfigChanged(ConfigChanged event)
	{
		if (event.getGroup().equalsIgnoreCase("znzulrah"))
		{
			String var2 = event.getKey();
			byte var3 = -1;
			switch (var2.hashCode())
			{
				case -1906254917:
					if (var2.equals("totalTickCounter"))
					{
						var3 = 1;
					}
					break;
				case -1662720966:
					if (var2.equals("snakelingSetting"))
					{
						var3 = 0;
					}
			}

			switch (var3)
			{
				case 0:
					if (config.snakelingSetting() != ZulrahConfig.SnakelingSettings.ENTITY)
					{
						clearSnakelingCollection();
					}

					if (config.snakelingSetting() != ZulrahConfig.SnakelingSettings.MES)
					{
						holdingSnakelingHotkey = false;
					}
					break;
				case 1:
					if (!config.totalTickCounter())
					{
						handleTotalTicksInfoBox(true);
					}
			}
		}

	}

	private void clearSnakelingCollection()
	{
		snakelings.forEach((npc) -> {
			setHidden(npc, false);
		});
		snakelings.clear();
	}

	@Subscribe
	private void onClientTick(ClientTick event)
	{
		if (client.getGameState() == GameState.LOGGED_IN && zulrahNpc != null)
		{
			if (config.snakelingSetting() == ZulrahConfig.SnakelingSettings.ENTITY)
			{
				snakelings.addAll((Collection) client.getNpcs().stream().filter((npc) -> {
					return npc != null && npc.getName() != null && npc.getName().equalsIgnoreCase("snakeling") && npc.getCombatLevel() == 90;
				}).collect(Collectors.toList()));
				snakelings.forEach((npc) -> {
					setHidden(npc, true);
				});
			}

		}
	}

	@Subscribe
	private void onGameTick(GameTick event)
	{
		if (client.getGameState() == GameState.LOGGED_IN && zulrahNpc != null)
		{
			++totalTicks;
			if (attackTicks >= 0)
			{
				--attackTicks;
			}

			if (phaseTicks >= 0)
			{
				--phaseTicks;
			}

			if (projectilesMap.size() > 0)
			{
				projectilesMap.values().removeIf((v) -> v <= 0);
				projectilesMap.replaceAll((k, v) -> v - 1);
			}

			if (toxicCloudsMap.size() > 0)
			{
				toxicCloudsMap.values().removeIf((v) -> v <= 0);
				toxicCloudsMap.replaceAll((k, v) -> v - 1);
			}

			handleTotalTicksInfoBox(false);
		}
	}

	@Subscribe
	private void onAnimationChanged(AnimationChanged event)
	{
		if (event.getActor() instanceof NPC)
		{
			NPC npc = (NPC) event.getActor();
			if (npc.getName() == null || npc.getName().equalsIgnoreCase("zulrah"))
			{
				switch (npc.getAnimation())
				{
					case 5069:
						attackTicks = 4;
						if (currentRotation != null && getCurrentPhase(currentRotation).getZulrahNpc().isJad() && zulrahNpc.getInteracting() == client.getLocalPlayer())
						{
							flipPhasePrayer = !flipPhasePrayer;
						}
						break;
					case 5071:
						zulrahNpc = npc;
						instanceTimerOverlay.setTimer();
						potentialRotations = RotationType.findPotentialRotations(npc, stage);
						phaseTicksHandler.accept(currentRotation, potentialRotations.get(0));

						break;
					case 5072:
						if (zulrahReset)
						{
							zulrahReset = false;
						}

						if (currentRotation != null && isLastPhase(currentRotation))
						{
							stage = -1;
							currentRotation = null;
							potentialRotations.clear();
							snakelings.clear();
							flipStandLocation = false;
							flipPhasePrayer = false;
							zulrahReset = true;

						}
						break;
					case 5073:
						++stage;
						if (currentRotation == null)
						{
							potentialRotations = RotationType.findPotentialRotations(npc, stage);
							currentRotation = potentialRotations.size() == 1 ? potentialRotations.get(0) : null;
						}

						phaseTicksHandler.accept(currentRotation, potentialRotations.get(0));
						break;
					case 5804:
						reset();
						break;
					case 5806:
					case 5807:
						attackTicks = 8;
						flipStandLocation = !flipStandLocation;
				}

			}
		}
	}

	@Subscribe
	private void onFocusChanged(FocusChanged event)
	{
		if (!event.isFocused())
		{
			holdingSnakelingHotkey = false;
		}

	}

	@Subscribe
	private void onMenuEntryAdded(MenuEntryAdded event)
	{
		if (config.snakelingSetting() == ZulrahConfig.SnakelingSettings.MES && zulrahNpc != null && !zulrahNpc.isDead())
		{
			if (!holdingSnakelingHotkey && event.getTarget().contains("Snakeling") && event.getOption().equalsIgnoreCase("attack"))
			{
				NPC npc = client.getCachedNPCs()[event.getIdentifier()];
				if (npc == null)
				{
					return;
				}

				client.setMenuEntries(Arrays.copyOf(client.getMenuEntries(), client.getMenuEntries().length - 1));
			}

		}
	}

	@Subscribe
	private void onProjectileMoved(ProjectileMoved event)
	{
		if (zulrahNpc != null)
		{
			Projectile p = event.getProjectile();
			switch (p.getId())
			{
				case 1045:
				case 1047:
					projectilesMap.put(event.getPosition(), p.getRemainingCycles() / 30);
				default:
			}
		}
	}

	@Subscribe
	private void onGameObjectSpawned(GameObjectSpawned event)
	{
		if (zulrahNpc != null)
		{
			GameObject obj = event.getGameObject();
			if (obj.getId() == 11700)
			{
				toxicCloudsMap.put(obj, 30);
			}

		}
	}

	@Subscribe
	private void onGameStateChanged(GameStateChanged event)
	{
		if (zulrahNpc != null)
		{
			switch (event.getGameState())
			{
				case LOADING:
				case CONNECTION_LOST:
				case HOPPING:
					reset();
				default:
			}
		}
	}

	@Nullable
	private ZulrahPhase getCurrentPhase(RotationType type)
	{
		return stage >= type.getZulrahPhases().size() ? null : type.getZulrahPhases().get(stage);
	}

	@Nullable
	private ZulrahPhase getNextPhase(RotationType type)
	{
		return isLastPhase(type) ? null : type.getZulrahPhases().get(stage + 1);
	}

	private boolean isLastPhase(RotationType type)
	{
		return stage == type.getZulrahPhases().size() - 1;
	}

	public Set<ZulrahData> getZulrahData()
	{
		Set<ZulrahData> zulrahDataSet = new LinkedHashSet<>();
		if (currentRotation == null)
		{
			potentialRotations.forEach((type) -> {
				zulrahDataSet.add(new ZulrahData(getCurrentPhase(type), getNextPhase(type)));
			});
		}
		else
		{
			zulrahDataSet.add(new ZulrahData(getCurrentPhase(currentRotation), getNextPhase(currentRotation)));
		}

		return (Set<ZulrahData>) (zulrahDataSet.size() > 0 ? zulrahDataSet : Collections.emptySet());
	}

	private void handleTotalTicksInfoBox(boolean remove)
	{
		if (remove)
		{
			infoBoxManager.removeInfoBox(zulrahTotalTicksInfoBox);
			zulrahTotalTicksInfoBox = null;
		}
		else if (config.totalTickCounter())
		{
			if (zulrahTotalTicksInfoBox == null)
			{
				zulrahTotalTicksInfoBox = new Counter(CLOCK_ICON, this, totalTicks);
				zulrahTotalTicksInfoBox.setTooltip("Total Ticks Alive");
				infoBoxManager.addInfoBox(zulrahTotalTicksInfoBox);
			}
			else
			{
				zulrahTotalTicksInfoBox.setCount(totalTicks);
			}
		}

	}

	public NPC getZulrahNpc()
	{
		return zulrahNpc;
	}

	public int getPhaseTicks()
	{
		return phaseTicks;
	}

	public int getAttackTicks()
	{
		return attackTicks;
	}

	public RotationType getCurrentRotation()
	{
		return currentRotation;
	}

	public Map<LocalPoint, Integer> getProjectilesMap()
	{
		return projectilesMap;
	}

	public Map<GameObject, Integer> getToxicCloudsMap()
	{
		return toxicCloudsMap;
	}
}
