package com.zully.zulrah;

import net.runelite.api.coords.WorldPoint;

/** Fixed world tiles used on the Zul-Andra shore and the Zulrah island. */
public final class ZulrahLocations
{
	public static final WorldPoint START = new WorldPoint(2272, 3077, 0);
	public static final WorldPoint EAST_MELEE_SAFE = new WorldPoint(2274, 3077, 0);
	public static final WorldPoint WEST_MELEE_SAFE = new WorldPoint(2262, 3076, 0);
	public static final WorldPoint CENTRE_MELEE_SAFE_WEST = new WorldPoint(2264, 3072, 0);
	public static final WorldPoint CENTRE_MELEE_SAFE_EAST = new WorldPoint(2272, 3072, 0);
	public static final WorldPoint[] BOAT_TILES = {new WorldPoint(2215,3056,0),new WorldPoint(2215,3057,0),new WorldPoint(2215,3058,0),new WorldPoint(2216,3056,0),new WorldPoint(2216,3057,0),new WorldPoint(2216,3058,0),new WorldPoint(2217,3056,0)};
	private ZulrahLocations() { }
	public static boolean isSuppliedZulAndraBounds(WorldPoint point) { return point != null && point.getPlane() == 0 && point.getX() >= 2181 && point.getX() <= 2212 && point.getY() >= 3041 && point.getY() <= 3072; }
	public static boolean isZulrahArena(WorldPoint point) { return point != null && point.getPlane() == 0 && point.getX() >= 2255 && point.getX() <= 2280 && point.getY() >= 3065 && point.getY() <= 3085; }
	public static boolean isBoatArea(WorldPoint point) { return point != null && point.getPlane() == 0 && point.getX() >= 2214 && point.getX() <= 2218 && point.getY() >= 3055 && point.getY() <= 3059; }
	public static boolean isRelevant(WorldPoint point) { return isSuppliedZulAndraBounds(point) || isBoatArea(point) || isZulrahArena(point); }
}
