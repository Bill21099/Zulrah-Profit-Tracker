package com.zully.zulrah.tracker;
import com.zully.zulrah.model.*;
/** Explicit attack state for Jad phases; advanced only by observed attacks/animations. */
public final class JadTracker {
    private int attack; private ZulrahAttackStyle start=ZulrahAttackStyle.NONE; private int total;
    public void start(ZulrahPhase phase) { attack=0; start=phase.jadStart(); total=phase.attacks(); }
    public void attackObserved() { if (attack < total) attack++; }
    public ZulrahAttackStyle current() { return styleAt(attack); }
    public ZulrahAttackStyle next() { return styleAt(attack+1); }
    private ZulrahAttackStyle styleAt(int index) { if(start==ZulrahAttackStyle.NONE || index>=total) return ZulrahAttackStyle.NONE; return index % 2 == 0 ? start : (start==ZulrahAttackStyle.RANGED ? ZulrahAttackStyle.MAGIC : ZulrahAttackStyle.RANGED); }
    public int attack(){return attack;} public void reset(){attack=0;start=ZulrahAttackStyle.NONE;total=0;}
}
