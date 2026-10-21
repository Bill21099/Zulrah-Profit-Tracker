package com.zully.zulrah.tracker;
import com.zully.zulrah.data.ZulrahRotations;
import com.zully.zulrah.model.*;
import java.util.*;
/** Pure state machine. Its inputs are observations, never elapsed wall-clock time. */
public final class EncounterTracker {
    private final RotationDetector detector=new RotationDetector(); private final PhaseTracker phases=new PhaseTracker(); private final AttackTracker attacks=new AttackTracker(); private final JadTracker jad=new JadTracker(); private final EventTracker events=new EventTracker();
    private Set<Integer> candidates=new LinkedHashSet<>(); private EncounterState.Status status=EncounterState.Status.INACTIVE; private ZulrahForm form; private ZulrahPosition position=ZulrahPosition.UNKNOWN;
    public void start(ZulrahForm observed, ZulrahPosition at) { reset(); status=EncounterState.Status.ACTIVE; form=observed; position=at; phases.set(0); candidates=detector.candidatesAt(0,observed,at); if(candidates.isEmpty()) status=EncounterState.Status.UNCERTAIN; }
    public void observePhase(ZulrahForm observed, ZulrahPosition at) {
        if(status==EncounterState.Status.INACTIVE){start(observed,at);return;} form=observed; position=at; int expected=phases.phase()+1; Set<Integer> narrowed=detector.narrow(candidates,expected,observed,at);
        if(!narrowed.isEmpty()){candidates=narrowed; phases.set(expected); attacks.reset(); if(phase()!=null && phase().isJad()) jad.start(phase()); return;}
        List<RotationDetector.Match> matches=detector.find(observed,at); if(matches.size()==1){RotationDetector.Match m=matches.get(0); candidates=new LinkedHashSet<>(Collections.singleton(m.rotation)); phases.set(m.phase); attacks.reset(); if(phase()!=null && phase().isJad()) jad.start(phase()); status=EncounterState.Status.ACTIVE;} else status=EncounterState.Status.UNCERTAIN;
    }
    public void attackObserved(){ attacks.observed(); if(phase()!=null && phase().isJad()) jad.attackObserved(); }
    public void snakelingSpawned(int i){events.spawned(i);} public void snakelingDespawned(int i){events.despawned(i);}
    public void reset(){status=EncounterState.Status.INACTIVE;candidates.clear();phases.reset();attacks.reset();jad.reset();events.reset();form=null;position=ZulrahPosition.UNKNOWN;}
    public ZulrahPhase phase(){Integer r=rotation();return r==null?null:ZulrahRotations.byNumber(r).phase(phases.phase());}
    public Integer rotation(){return candidates.size()==1?candidates.iterator().next():null;}
    public EncounterState state(){return new EncounterState(status,candidates,rotation(),phases.phase(),phase()!=null&&phase().isJad()?jad.attack():attacks.count(),events.active(),form,position);}
    public ZulrahPrayer currentPrayer(){ZulrahPhase p=phase(); if(p==null)return ZulrahPrayer.NONE; if(!p.isJad())return p.prayer(); return prayer(jad.current());}
    public ZulrahPrayer nextPrayer(){ZulrahPhase p=phase(); if(p==null)return ZulrahPrayer.NONE; if(p.isJad())return prayer(jad.next()); ZulrahPhase n=nextPhase();return n==null?ZulrahPrayer.NONE:n.prayer();}
    /** Prayer call for the next spawn, including the first Jad prayer where applicable. */
    public ZulrahPrayer nextPhasePrayer(){ZulrahPhase n=nextPhase();return n==null?ZulrahPrayer.NONE:(n.isJad()?prayer(n.jadStart()):n.prayer());}
    private ZulrahPrayer prayer(ZulrahAttackStyle s){return s==ZulrahAttackStyle.RANGED?ZulrahPrayer.PROTECT_FROM_MISSILES:s==ZulrahAttackStyle.MAGIC?ZulrahPrayer.PROTECT_FROM_MAGIC:ZulrahPrayer.NONE;}
    public ZulrahPhase nextPhase(){ZulrahPhase agreed=null;for(Integer rotation:candidates){ZulrahPhase candidate=ZulrahRotations.byNumber(rotation).phase(phases.phase()+1);if(candidate==null)return null;if(agreed==null)agreed=candidate;else if(candidate.form()!=agreed.form()||candidate.position()!=agreed.position()||candidate.prayer()!=agreed.prayer())return null;}return agreed;} public ZulrahPhase phaseAfterNext(){Integer r=rotation();return r==null?null:ZulrahRotations.byNumber(r).phase(phases.phase()+2);}
}
