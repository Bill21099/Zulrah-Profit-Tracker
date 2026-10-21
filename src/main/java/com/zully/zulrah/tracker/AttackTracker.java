package com.zully.zulrah.tracker;
public final class AttackTracker { private int count; public void observed(){count++;} public int count(){return count;} public void reset(){count=0;} }
