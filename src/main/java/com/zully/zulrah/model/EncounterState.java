package com.zully.zulrah.model;
import java.util.*;
public final class EncounterState {
    public enum Status { INACTIVE, ACTIVE, UNCERTAIN }
    private final Status status; private final Set<Integer> candidates; private final Integer rotation; private final int phaseIndex; private final int attack; private final int activeSnakelings; private final ZulrahForm observedForm; private final ZulrahPosition observedPosition;
    public EncounterState(Status status, Set<Integer> candidates, Integer rotation, int phaseIndex, int attack, int activeSnakelings, ZulrahForm form, ZulrahPosition position) {
        this.status=status; this.candidates=Collections.unmodifiableSet(new LinkedHashSet<>(candidates)); this.rotation=rotation; this.phaseIndex=phaseIndex; this.attack=attack; this.activeSnakelings=activeSnakelings; this.observedForm=form; this.observedPosition=position;
    }
    public static EncounterState inactive() { return new EncounterState(Status.INACTIVE, Collections.emptySet(), null, -1, 0, 0, null, ZulrahPosition.UNKNOWN); }
    public Status status(){return status;} public Set<Integer> candidates(){return candidates;} public Integer rotation(){return rotation;} public int phaseIndex(){return phaseIndex;} public int attack(){return attack;} public int activeSnakelings(){return activeSnakelings;} public ZulrahForm observedForm(){return observedForm;} public ZulrahPosition observedPosition(){return observedPosition;}
}
