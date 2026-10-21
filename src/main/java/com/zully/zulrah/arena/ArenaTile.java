package com.zully.zulrah.arena;
import com.zully.zulrah.model.ZulrahPosition;
import net.runelite.api.coords.WorldPoint;
public final class ArenaTile { private final ZulrahPosition logical; private final WorldPoint point; public ArenaTile(ZulrahPosition logical, WorldPoint point){this.logical=logical;this.point=point;} public ZulrahPosition logical(){return logical;} public WorldPoint point(){return point;} }
