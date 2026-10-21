package com.zully.zulrah.tracker;
import com.zully.zulrah.data.ZulrahRotations;
import com.zully.zulrah.model.*;
import java.util.*;
/** Narrows candidates using observations at the same phase index. */
public final class RotationDetector {
    public Set<Integer> candidatesAt(int phase, ZulrahForm form, ZulrahPosition position) {
        Set<Integer> result = new LinkedHashSet<>();
        for (ZulrahRotation r : ZulrahRotations.all()) { ZulrahPhase p=r.phase(phase); if (p != null && p.matches(form,position)) result.add(r.number()); }
        return result;
    }
    public Set<Integer> narrow(Set<Integer> candidates, int phase, ZulrahForm form, ZulrahPosition position) {
        Set<Integer> match=candidatesAt(phase,form,position); match.retainAll(candidates); return match;
    }
    /** Finds all possible phase locations; used for safe recovery after an unexpected transition. */
    public List<Match> find(ZulrahForm form, ZulrahPosition position) {
        List<Match> matches=new ArrayList<>(); for (ZulrahRotation r:ZulrahRotations.all()) for(int i=0;i<r.phases().size();i++) if(r.phase(i).matches(form,position)) matches.add(new Match(r.number(),i)); return matches;
    }
    public static final class Match { public final int rotation, phase; Match(int rotation,int phase){this.rotation=rotation;this.phase=phase;} }
}
