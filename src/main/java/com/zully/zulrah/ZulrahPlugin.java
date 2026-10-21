package com.zully.zulrah;

import com.google.inject.Provides;
import com.zully.zulrah.arena.ArenaPositionResolver;
import com.zully.zulrah.model.*;
import com.zully.zulrah.tracker.*;
import javax.inject.*;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.*;
import net.runelite.client.ui.*;
import net.runelite.client.game.ItemManager;

@PluginDescriptor(
	name = "Zulrah Profit Tracker",
	description = "Zulrah encounter sidebar navigator and profit tracker",
	tags = {"zulrah", "boss", "pvm", "profit", "tracker", "navigator"},
	enabledByDefault = true
)
@Singleton
public final class ZulrahPlugin extends Plugin
{
	private final ZulrahDetector detector = new ZulrahDetector();
	private final ArenaPositionResolver positions = new ArenaPositionResolver();

	@Inject private Client client;
	@Inject private ZulrahConfig config;
	@Inject private ClientToolbar toolbar;
	@Inject private EncounterTracker tracker;
	@Inject private SessionTracker session;
	@Inject private ItemManager itemManager;

	private NPC zulrah;
	private ZulrahForm lastForm;
	private ZulrahPosition lastPosition;
	private ZulrahPanel panel;
	private NavigationButton button;
	private boolean deathRecorded;
	private WorldPoint lootOrigin;

	private int pendingItemId = -1;
	private long pendingItemClickTime = 0;
	private java.util.Map<Integer, Integer> lastInventory = new java.util.HashMap<>();

	@Provides
	ZulrahConfig provideConfig(ConfigManager manager)
	{
		return manager.getConfig(ZulrahConfig.class);
	}

	@Override
	protected void startUp()
	{
		panel = new ZulrahPanel(tracker, session, client);
		button = NavigationButton.builder()
			.tooltip("Zulrah Profit Tracker")
			.priority(8)
			.icon(ZulrahIcon.create())
			.panel(panel)
			.build();
		toolbar.addNavigation(button);
	}

	@Override
	protected void shutDown()
	{
		if (button != null)
		{
			toolbar.removeNavigation(button);
		}
		reset();
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned event)
	{
		NPC npc = event.getNpc();
		if (detector.isZulrah(npc))
		{
			zulrah = npc;
			observe(tracker.state().status() == EncounterState.Status.INACTIVE);
		}
		else if (detector.isSnakeling(npc))
		{
			tracker.snakelingSpawned(npc.getIndex());
		}
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned event)
	{
		NPC npc = event.getNpc();
		if (npc == zulrah)
		{
			if (deathRecorded)
			{
				reset();
			}
			else
			{
				zulrah = null;
				lastForm = null;
				lastPosition = null;
			}
		}
		else if (detector.isSnakeling(npc))
		{
			tracker.snakelingDespawned(npc.getIndex());
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		long now = System.currentTimeMillis();
		session.startIfNeeded(now, combatExperience());
		boolean inInstance = client.isInInstancedRegion();
		if (inInstance && client.getMapRegions() != null)
		{
			inInstance = false;
			for (int regionId : client.getMapRegions())
			{
				if (regionId == 9007 || regionId == 9008)
				{
					inInstance = true;
					break;
				}
			}
		}
		session.tickInstanceTimer(now, inInstance);

		if (zulrah == null)
		{
			for (NPC npc : client.getNpcs())
			{
				if (detector.isZulrah(npc))
				{
					zulrah = npc;
					observe(tracker.state().status() == EncounterState.Status.INACTIVE);
					break;
				}
			}
		}
		else
		{
			observe(false);
		}

		if (panel != null)
		{
			panel.refresh();
		}
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		trackPendingConsumableClick(event);
	}

	private void trackPendingConsumableClick(MenuOptionClicked event)
	{
		int itemId = event.getItemId();
		if (itemId <= 0) return;

		ItemComposition def = itemManager.getItemComposition(itemId);
		if (def == null) return;
		String name = def.getName();
		if (name == null) return;
		String lowerName = name.toLowerCase(java.util.Locale.ROOT);

		String option = event.getMenuOption() != null ? event.getMenuOption().toLowerCase(java.util.Locale.ROOT) : "";

		// Ignore bank deposit / store actions so depositing supplies doesn't count as spending
		if (option.startsWith("deposit") || option.startsWith("store")) return;

		boolean isEat = "eat".equals(option);
		boolean isDrink = "drink".equals(option);
		boolean isTeleport = "teleport".equals(option) || "break".equals(option) || "read".equals(option);

		if (!isEat && !isDrink && !isTeleport)
		{
			String[] actions = def.getInventoryActions();
			if (actions != null)
			{
				for (String act : actions)
				{
					if (act != null)
					{
						String lowerAct = act.toLowerCase(java.util.Locale.ROOT);
						if (lowerAct.equals(option))
						{
							if ("eat".equals(lowerAct)) isEat = true;
							else if ("drink".equals(lowerAct)) isDrink = true;
							else if ("teleport".equals(lowerAct) || "break".equals(lowerAct) || "read".equals(lowerAct)) isTeleport = true;
						}
					}
				}
			}
		}

		if (isEat || isDrink || isTeleport || lowerName.contains("zul-andra teleport"))
		{
			pendingItemId = itemId;
			pendingItemClickTime = System.currentTimeMillis();
		}
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		if (event.getContainerId() != InventoryID.INVENTORY.getId()) return;
		ItemContainer container = event.getItemContainer();
		if (container == null) return;

		java.util.Map<Integer, Integer> currentInventory = new java.util.HashMap<>();
		for (Item item : container.getItems())
		{
			if (item != null && item.getId() > 0)
			{
				currentInventory.put(item.getId(), currentInventory.getOrDefault(item.getId(), 0) + item.getQuantity());
			}
		}

		if (!lastInventory.isEmpty() && (System.currentTimeMillis() - pendingItemClickTime < 3000))
		{
			for (java.util.Map.Entry<Integer, Integer> entry : lastInventory.entrySet())
			{
				int id = entry.getKey();
				int oldQty = entry.getValue();
				int newQty = currentInventory.getOrDefault(id, 0);

				if (oldQty > newQty && id == pendingItemId)
				{
					int consumedQty = oldQty - newQty;
					ItemComposition def = itemManager.getItemComposition(id);
					if (def != null)
					{
						String name = def.getName();
						if (name != null)
						{
							String lowerName = name.toLowerCase(java.util.Locale.ROOT);
							if (isTrackedConsumable(def, lowerName))
							{
								long itemPrice = itemManager.getItemPrice(id);
								long unitCost = calculateItemCost(lowerName, itemPrice);
								if (unitCost > 0)
								{
									session.recordItemSpent(unitCost * consumedQty);
								}
							}
						}
					}
					pendingItemId = -1; // Reset pending action once verified
				}
			}
		}

		lastInventory = currentInventory;
	}

	private boolean isTrackedConsumable(ItemComposition def, String lowerName)
	{
		if (lowerName.equals("zul-andra teleport") || lowerName.contains("zul-andra teleport"))
		{
			return true;
		}
		if (lowerName.matches(".*\\([1-4]\\)$"))
		{
			return true;
		}
		String[] actions = def.getInventoryActions();
		if (actions != null)
		{
			for (String act : actions)
			{
				if (act != null)
				{
					String lowerAct = act.toLowerCase(java.util.Locale.ROOT);
					if ("eat".equals(lowerAct) || "drink".equals(lowerAct))
					{
						return true;
					}
				}
			}
		}
		return false;
	}

	private long calculateItemCost(String lowerName, long itemPrice)
	{
		if (lowerName.equals("zul-andra teleport") || lowerName.contains("zul-andra teleport"))
		{
			return itemPrice;
		}
		java.util.regex.Matcher matcher = java.util.regex.Pattern.compile(".*\\((\\d)\\)$").matcher(lowerName);
		if (matcher.matches())
		{
			int doses = Integer.parseInt(matcher.group(1));
			if (doses > 0)
			{
				return Math.max(1, itemPrice / doses);
			}
		}
		return itemPrice;
	}

	@Subscribe
	public void onActorDeath(ActorDeath event)
	{
		if (event.getActor() == zulrah)
		{
			recordDeathIfNeeded();
		}
		else if (event.getActor() == client.getLocalPlayer())
		{
			session.recordDeath();
		}
	}

	@Subscribe
	public void onItemSpawned(ItemSpawned event)
	{
		WorldPoint location = event.getTile().getWorldLocation();
		if (!session.acceptsLoot(System.currentTimeMillis()) ||
			(!ZulrahLocations.isZulrahArena(location) && (lootOrigin == null || location.distanceTo2D(lootOrigin) > 8)))
		{
			return;
		}
		TileItem item = event.getItem();
		session.recordLoot((long) item.getQuantity() * itemManager.getItemPrice(item.getId()));
	}

	@Subscribe
	public void onAnimationChanged(AnimationChanged event)
	{
		if (event.getActor() == zulrah && zulrah.getAnimation() != -1)
		{
			tracker.attackObserved();
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		GameState state = event.getGameState();
		if (state == GameState.LOGIN_SCREEN || state == GameState.LOGIN_SCREEN_AUTHENTICATOR || state == GameState.CONNECTION_LOST || state == GameState.HOPPING)
		{
			reset();
		}
	}

	private void observe(boolean starting)
	{
		if (zulrah == null) return;
		ZulrahForm form = ZulrahForm.fromNpcId(zulrah.getId());
		if (form == null) form = starting ? ZulrahForm.SERPENTINE : lastForm;
		if (form == null) return;

		if (starting)
		{
			deathRecorded = false;
			lootOrigin = null;
			if (positions.resolveBoss(zulrah.getWorldLocation()) == ZulrahPosition.UNKNOWN)
			{
				positions.anchor(zulrah.getWorldLocation());
			}
		}

		ZulrahPosition position = positions.resolveBoss(zulrah.getWorldLocation());
		if (starting)
		{
			tracker.start(form, position);
		}
		else if (form != lastForm || position != lastPosition)
		{
			tracker.observePhase(form, position);
		}

		lastForm = form;
		lastPosition = position;
	}

	private void recordDeathIfNeeded()
	{
		if (!deathRecorded)
		{
			deathRecorded = true;
			lootOrigin = zulrah == null ? null : zulrah.getWorldLocation();
			session.recordKill(System.currentTimeMillis());
		}
	}

	private long combatExperience()
	{
		long total = 0;
		for (Skill skill : new Skill[]{Skill.ATTACK, Skill.STRENGTH, Skill.DEFENCE, Skill.HITPOINTS, Skill.RANGED, Skill.MAGIC})
		{
			total += client.getSkillExperience(skill);
		}
		return total;
	}

	private void reset()
	{
		tracker.reset();
		positions.reset();
		zulrah = null;
		lastForm = null;
		lastPosition = null;
		if (panel != null)
		{
			panel.refresh();
		}
	}
}
