package com.zully.zulrah.arena;
import net.runelite.api.coords.WorldPoint;
/** Arena-local coordinate system, anchored from the observed centre spawn. */
public final class ZulrahArena { private final WorldPoint center; public ZulrahArena(WorldPoint center){this.center=center;} public WorldPoint center(){return center;} }
