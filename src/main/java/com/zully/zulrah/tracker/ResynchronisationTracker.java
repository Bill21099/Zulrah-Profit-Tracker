package com.zully.zulrah.tracker;
import com.zully.zulrah.model.*;
import java.util.*;
/** Only returns a state when all matching observations agree; otherwise caller exposes uncertainty. */
public final class ResynchronisationTracker {
    private final RotationDetector detector=new RotationDetector();
    public RotationDetector.Match unique(ZulrahForm form, ZulrahPosition position) { List<RotationDetector.Match> m=detector.find(form,position); return m.size()==1 ? m.get(0) : null; }
}
