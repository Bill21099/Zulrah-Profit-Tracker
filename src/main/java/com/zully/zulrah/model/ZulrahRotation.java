package com.zully.zulrah.model;
import java.util.*;
public final class ZulrahRotation {
    private final int number; private final List<ZulrahPhase> phases;
    public ZulrahRotation(int number, List<ZulrahPhase> phases) { this.number = number; this.phases = Collections.unmodifiableList(new ArrayList<>(phases)); }
    public int number() { return number; } public List<ZulrahPhase> phases() { return phases; }
    public ZulrahPhase phase(int index) { return index >= 0 && index < phases.size() ? phases.get(index) : null; }
}
