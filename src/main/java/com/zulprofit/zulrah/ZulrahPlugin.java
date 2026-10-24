package com.zulprofit.zulrah;

import com.zulprofit.zulrah.tracker.SessionTracker;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;

/**
 * Zulrah Profit Tracker — session-based loot and cost tracker for Zulrah.
 */
@PluginDescriptor(
	name        = "Zul Profit",
	description = "Tracks Zulrah session time, XP, loot and costs",
	tags        = {"zulrah", "gp", "profit", "tracker"},
	enabledByDefault = true
)
@Singleton
public final class ZulrahPlugin extends Plugin
{
	// ── Zulrah NPC IDs ──────────────────────────────────────────────────────────
	// 2042 = green (range), 2043 = red (melee), 2044 = blue (magic)
	private static final int[] ZULRAH_IDS = {2042, 2043, 2044};

	// ── Zulrah instance region IDs ──────────────────────────────────────────────
	private static final int[] ZULRAH_REGIONS = {9007, 9008};

	@Inject private Client client;
	@Inject private ClientToolbar toolbar;
	@Inject private SessionTracker session;
	@Inject private ItemManager itemManager;

	private NPC zulrah;
	private ZulrahPanel panel;
	private NavigationButton button;
	private boolean deathRecorded;
	private WorldPoint lootOrigin;
	private int pendingItemId = -1;
	private long pendingItemClickTime;
	private Map<Integer, Integer> lastInventory = new HashMap<>();
	private final Map<Integer, Long> droppedItems = new HashMap<>();

	// ── Lifecycle ────────────────────────────────────────────────────────────────

	@Override
	protected void startUp()
	{
		panel = new ZulrahPanel(session, client, itemManager);
		button = NavigationButton.builder()
			.tooltip("Zul Profit")
			.priority(8)
			.icon(ZulrahIcon.create())
			.panel(panel)
			.build();
		toolbar.addNavigation(button);
	}

	@Override
	protected void shutDown()
	{
		if (button != null) toolbar.removeNavigation(button);
		zulrah = null;
		panel = null;
		droppedItems.clear();
	}

	// ── Game tick: instance detection & panel refresh ─────────────────────────

	@Subscribe
	public void onGameTick(GameTick event)
	{
		long now = System.currentTimeMillis();
		boolean inInstance = isInZulrahInstance();

		// Start session when player enters the Zulrah instance
		if (!session.isStarted() && inInstance)
		{
			session.start(now, combatExperience());
		}

		session.tickTimers(now, inInstance);

		if (zulrah == null)
		{
			for (NPC npc : client.getNpcs())
			{
				if (isZulrah(npc))
				{
					zulrah = npc;
					deathRecorded = false;
					break;
				}
			}
		}

		if (panel != null)
		{
			panel.refresh();
		}
	}

	// ── NPC tracking ─────────────────────────────────────────────────────────

	@Subscribe
	public void onNpcSpawned(NpcSpawned event)
	{
		if (isZulrah(event.getNpc())) { zulrah = event.getNpc(); deathRecorded = false; }
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned event)
	{
		if (event.getNpc() == zulrah) { zulrah = null; deathRecorded = false; }
	}

	// ── Kill / death recording ─────────────────────────────────────────────────

	@Subscribe
	public void onActorDeath(ActorDeath event)
	{
		if (event.getActor() == zulrah && !deathRecorded)
		{
			deathRecorded = true;
			lootOrigin = zulrah.getWorldLocation();
			session.recordKill(System.currentTimeMillis());
		}
		else if (event.getActor() == client.getLocalPlayer())
		{
			session.recordDeath();
		}
	}

	// ── Loot tracking ─────────────────────────────────────────────────────────

	@Subscribe
	public void onItemSpawned(ItemSpawned event)
	{
		WorldPoint location = event.getTile().getWorldLocation();
		if (!session.acceptsLoot(System.currentTimeMillis())
			|| lootOrigin == null
			|| location.distanceTo2D(lootOrigin) > 8) return;

		TileItem item = event.getItem();
		Long droppedTime = droppedItems.get(item.getId());
		if (droppedTime != null && System.currentTimeMillis() - droppedTime < 3000L)
		{
			return;
		}

		int id = item.getId();
		int quantity = item.getQuantity();
		long price = (long) quantity * itemManager.getItemPrice(id);
		session.recordLoot(id, quantity, price);
	}

	// ── Consumable / cost tracking ─────────────────────────────────────────────

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		int itemId = event.getItemId();
		if (itemId <= 0) return;

		ItemComposition def = itemManager.getItemComposition(itemId);
		if (def == null || def.getName() == null) return;

		String name   = def.getName().toLowerCase(Locale.ROOT);
		String option = event.getMenuOption() == null ? "" : event.getMenuOption().toLowerCase(Locale.ROOT);

		// Never count depositing/storing items as a cost
		if (option.startsWith("deposit") || option.startsWith("store")) return;

		if ("drop".equals(option))
		{
			droppedItems.put(itemId, System.currentTimeMillis());
			return;
		}

		boolean isTeleport = name.contains("zul-andra teleport");

		// Only track consumable usage (eat/drink) or Zul-andra teleports if in instance, or Zul-andra teleport outside
		if (isTeleport)
		{
			pendingItemId = itemId;
			pendingItemClickTime = System.currentTimeMillis();
			return;
		}

		// Consumables (food, potions) only tracked when in Zulrah instance
		if (!isInZulrahInstance())
		{
			return;
		}

		boolean trackedAction = "eat".equals(option)
			|| "drink".equals(option)
			|| "teleport".equals(option)
			|| "break".equals(option)
			|| "read".equals(option);

		// Fall back: check the item's own inventory actions for eat/drink/teleport/break
		if (!trackedAction && def.getInventoryActions() != null)
			for (String action : def.getInventoryActions())
				if (action != null && action.equalsIgnoreCase(option)
					&& ("eat".equalsIgnoreCase(action) || "drink".equalsIgnoreCase(action)
					|| "teleport".equalsIgnoreCase(action) || "break".equalsIgnoreCase(action)
					|| "read".equalsIgnoreCase(action)))
				{
					trackedAction = true;
					break;
				}

		if (trackedAction) { pendingItemId = itemId; pendingItemClickTime = System.currentTimeMillis(); }
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		if (event.getContainerId() != InventoryID.INVENTORY.getId()
			|| event.getItemContainer() == null) return;

		Map<Integer, Integer> current = new HashMap<>();
		for (Item item : event.getItemContainer().getItems())
			if (item != null && item.getId() > 0)
				current.put(item.getId(), current.getOrDefault(item.getId(), 0) + item.getQuantity());

		if (!lastInventory.isEmpty() && System.currentTimeMillis() - pendingItemClickTime < 3000)
		{
			for (Map.Entry<Integer, Integer> entry : lastInventory.entrySet())
			{
				int id = entry.getKey();
				int consumed = entry.getValue() - current.getOrDefault(id, 0);
				if (id == pendingItemId && consumed > 0)
				{
					ItemComposition def = itemManager.getItemComposition(id);
					if (def != null && def.getName() != null
						&& isTrackedConsumable(def, def.getName().toLowerCase(Locale.ROOT)))
					{
						long unitCost = calculateItemCost(def.getName().toLowerCase(Locale.ROOT),
							itemManager.getItemPrice(id));
						session.recordItemSpent(unitCost * consumed);
					}
					pendingItemId = -1;
					break;
				}
			}
		}
		lastInventory = current;
	}

	// ── Private helpers ───────────────────────────────────────────────────────

	/**
	 * Returns true when the player is inside the Zulrah instanced region.
	 */
	public boolean isInZulrahInstance()
	{
		if (!client.isInInstancedRegion()) return false;
		int[] regions = client.getMapRegions();
		if (regions == null) return false;
		for (int region : regions)
			for (int zulRegion : ZULRAH_REGIONS)
				if (region == zulRegion) return true;
		return false;
	}

	private boolean isTrackedConsumable(ItemComposition def, String name)
	{
		if (name.contains("zul-andra teleport")
			|| name.matches(".*\\([1-4]\\)$")) return true;
		String[] actions = def.getInventoryActions();
		if (actions != null)
			for (String action : actions)
				if ("eat".equalsIgnoreCase(action) || "drink".equalsIgnoreCase(action)
					|| "break".equalsIgnoreCase(action) || "teleport".equalsIgnoreCase(action)) return true;
		return false;
	}

	/**
	 * Converts a full-item price to a per-use (per-dose) cost.
	 * Teleport tablets / scrolls are single-use; potions are divided by their current dose count.
	 */
	private long calculateItemCost(String name, long price)
	{
		if (name.contains("zul-andra teleport")) return price;
		// Potions: "(4)" "(3)" "(2)" "(1)" — pro-rate by dose
		Matcher matcher = Pattern.compile(".*\\((\\d)\\)$").matcher(name);
		if (matcher.matches())
		{
			int doses = Integer.parseInt(matcher.group(1));
			if (doses > 0) return Math.max(1, price / doses);
		}
		return price;
	}

	private boolean isZulrah(NPC npc)
	{
		if (npc == null) return false;
		int id = npc.getId();
		for (int zid : ZULRAH_IDS)
			if (id == zid) return true;
		return npc.getName() != null && npc.getName().equalsIgnoreCase("Zulrah");
	}

	private long combatExperience()
	{
		long total = 0;
		for (Skill skill : new Skill[]{Skill.ATTACK, Skill.STRENGTH, Skill.DEFENCE, Skill.HITPOINTS, Skill.RANGED, Skill.MAGIC})
			total += client.getSkillExperience(skill);
		return total;
	}
}

