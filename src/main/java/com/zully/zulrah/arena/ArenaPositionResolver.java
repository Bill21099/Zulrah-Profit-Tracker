package com.zully.zulrah.arena;
import com.zully.zulrah.model.ZulrahPosition;
import net.runelite.api.coords.WorldPoint;
/** Resolves logical locations relative to the detected instance, never absolute map coordinates. */
public final class ArenaPositionResolver {
    private static final int FIRST_WAVE_NORTH_EAST_OFFSET = 6;
    private ZulrahArena arena;
    public void anchor(WorldPoint center){if(center!=null) arena=new ZulrahArena(center);}
    public ZulrahPosition resolveBoss(WorldPoint point){if(arena==null||point==null)return ZulrahPosition.UNKNOWN; int dx=point.getX()-arena.center().getX(),dy=point.getY()-arena.center().getY(); if(Math.abs(dx)<=2&&Math.abs(dy)<=2)return ZulrahPosition.CENTER; return Math.abs(dx)>Math.abs(dy)?(dx>0?ZulrahPosition.EAST:ZulrahPosition.WEST):(dy>0?ZulrahPosition.NORTH:ZulrahPosition.SOUTH);}
    public ArenaTile target(ZulrahPosition position){if(arena==null)return null; int dx=0,dy=0; switch(position){case EAST:dx=5;break;case WEST:dx=-5;break;case SOUTH:dy=-5;break;case NORTH:dy=5;break;default:break;} return new ArenaTile(position,new WorldPoint(arena.center().getX()+dx,arena.center().getY()+dy,arena.center().getPlane()));}
    /** The far north-east staging square requested for Zulrah's opening phase. */
    public ArenaTile firstWaveStand(){return arena==null?null:new ArenaTile(ZulrahPosition.NORTH,new WorldPoint(arena.center().getX()+FIRST_WAVE_NORTH_EAST_OFFSET,arena.center().getY()+FIRST_WAVE_NORTH_EAST_OFFSET-1,arena.center().getPlane()));}
    /** Standard pillar/safe squares, expressed relative to the detected centre spawn. */
    public ArenaTile standFor(ZulrahPosition bossPosition){if(arena==null||bossPosition==null)return null;int dx=FIRST_WAVE_NORTH_EAST_OFFSET,dy=FIRST_WAVE_NORTH_EAST_OFFSET-1;switch(bossPosition){case EAST:dx=-4;dy=-4;break;case WEST:dx=4;dy=-4;break;case NORTH:dx=-4;dy=-5;break;case SOUTH:dx=4;dy=-5;break;default:break;}return new ArenaTile(bossPosition,new WorldPoint(arena.center().getX()+dx,arena.center().getY()+dy,arena.center().getPlane()));}
    public void reset(){arena=null;}
}
