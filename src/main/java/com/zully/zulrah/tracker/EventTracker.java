package com.zully.zulrah.tracker;
import java.util.*;
import com.zully.zulrah.model.*;
public final class EventTracker { private final Set<Integer> snakelings=new HashSet<>(); public void spawned(int index){snakelings.add(index);} public void despawned(int index){snakelings.remove(index);} public int active(){return snakelings.size();} public void reset(){snakelings.clear();} }
