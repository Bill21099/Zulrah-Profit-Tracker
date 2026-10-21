package com.zully.zulrah.data;

import com.zully.zulrah.model.*;
import java.util.*;
import static com.zully.zulrah.model.ZulrahEvent.*;

/** Documented rotations. The event list remains descriptive so the tracker can verify, never schedule, it. */
public final class ZulrahRotations {
    private static final List<ZulrahRotation> ALL = Collections.unmodifiableList(Arrays.asList(rotation1(), rotation2(), rotation3(), rotation4()));
    private ZulrahRotations() { }
    public static List<ZulrahRotation> all() { return ALL; }
    public static ZulrahRotation byNumber(int number) { return ALL.stream().filter(r -> r.number() == number).findFirst().orElse(null); }
    private static ZulrahPhase p(ZulrahForm f, ZulrahPosition x, int attacks, ZulrahEvent... e) { return new ZulrahPhase(f,x,attacks,ZulrahAttackStyle.NONE,Arrays.asList(e)); }
    private static ZulrahPhase jad(ZulrahPosition x, int attacks, ZulrahAttackStyle start, ZulrahEvent... e) { return new ZulrahPhase(ZulrahForm.SERPENTINE,x,attacks,start,Arrays.asList(e)); }
    private static ZulrahEvent[] es(int attacks, int clouds, int snakes) { List<ZulrahEvent> e=new ArrayList<>(); for(int i=0;i<attacks;i++) e.add(ATTACK); for(int i=0;i<clouds;i++) e.add(VENOM_CLOUDS); for(int i=0;i<snakes;i++) e.add(SNAKELINGS); return e.toArray(new ZulrahEvent[0]); }
    private static ZulrahRotation rotation1() { return new ZulrahRotation(1, Arrays.asList(
        p(ZulrahForm.SERPENTINE,ZulrahPosition.CENTER,4,es(0,4,0)), p(ZulrahForm.MAGMA,ZulrahPosition.CENTER,2,es(2,0,0)), p(ZulrahForm.TANZANITE,ZulrahPosition.CENTER,4,es(4,0,0)),
        p(ZulrahForm.SERPENTINE,ZulrahPosition.SOUTH,5,es(5,2,4)), p(ZulrahForm.MAGMA,ZulrahPosition.CENTER,2,es(2,0,0)), p(ZulrahForm.TANZANITE,ZulrahPosition.WEST,5,es(5,0,0)),
        p(ZulrahForm.SERPENTINE,ZulrahPosition.SOUTH,3,es(0,3,4)), p(ZulrahForm.TANZANITE,ZulrahPosition.SOUTH,5,ATTACK,SNAKELINGS,VENOM_CLOUDS,SNAKELINGS),
        jad(ZulrahPosition.WEST,10,ZulrahAttackStyle.RANGED,es(10,4,0)), p(ZulrahForm.MAGMA,ZulrahPosition.CENTER,2,es(2,0,0)), p(ZulrahForm.SERPENTINE,ZulrahPosition.CENTER,5,es(5,4,0)))); }
    private static ZulrahRotation rotation2() { return new ZulrahRotation(2, Arrays.asList(
        p(ZulrahForm.SERPENTINE,ZulrahPosition.CENTER,4,es(0,4,0)), p(ZulrahForm.MAGMA,ZulrahPosition.CENTER,2,es(2,0,0)), p(ZulrahForm.TANZANITE,ZulrahPosition.CENTER,4,es(4,0,0)),
        p(ZulrahForm.SERPENTINE,ZulrahPosition.WEST,5,es(5,2,2)), p(ZulrahForm.TANZANITE,ZulrahPosition.SOUTH,5,ATTACK,SNAKELINGS,VENOM_CLOUDS,SNAKELINGS), p(ZulrahForm.MAGMA,ZulrahPosition.CENTER,2,es(2,0,0)),
        p(ZulrahForm.SERPENTINE,ZulrahPosition.EAST,5,es(5,0,0)), p(ZulrahForm.TANZANITE,ZulrahPosition.SOUTH,5,ATTACK,SNAKELINGS,VENOM_CLOUDS,SNAKELINGS), jad(ZulrahPosition.WEST,10,ZulrahAttackStyle.RANGED,es(10,4,0)), p(ZulrahForm.MAGMA,ZulrahPosition.CENTER,2,es(2,0,0)), p(ZulrahForm.SERPENTINE,ZulrahPosition.CENTER,5,es(5,4,0)))); }
    private static ZulrahRotation rotation3() { return new ZulrahRotation(3, Arrays.asList(
        p(ZulrahForm.SERPENTINE,ZulrahPosition.CENTER,4,es(0,4,0)),p(ZulrahForm.SERPENTINE,ZulrahPosition.EAST,5,es(5,0,2)),p(ZulrahForm.MAGMA,ZulrahPosition.CENTER,2,VENOM_CLOUDS,SNAKELINGS,ATTACK,ATTACK),p(ZulrahForm.TANZANITE,ZulrahPosition.WEST,5,es(5,0,0)),p(ZulrahForm.SERPENTINE,ZulrahPosition.SOUTH,5,es(5,0,0)),p(ZulrahForm.TANZANITE,ZulrahPosition.EAST,5,es(5,0,0)),p(ZulrahForm.SERPENTINE,ZulrahPosition.CENTER,4,es(0,2,2)),p(ZulrahForm.SERPENTINE,ZulrahPosition.WEST,5,es(5,0,0)),p(ZulrahForm.TANZANITE,ZulrahPosition.CENTER,5,es(5,2,2)),jad(ZulrahPosition.EAST,10,ZulrahAttackStyle.MAGIC,es(10,0,0)),p(ZulrahForm.TANZANITE,ZulrahPosition.CENTER,0,es(0,0,4)),p(ZulrahForm.SERPENTINE,ZulrahPosition.CENTER,5,es(5,4,0)))); }
    private static ZulrahRotation rotation4() { return new ZulrahRotation(4, Arrays.asList(
        p(ZulrahForm.SERPENTINE,ZulrahPosition.CENTER,4,es(0,4,0)),p(ZulrahForm.TANZANITE,ZulrahPosition.EAST,6,es(6,0,4)),p(ZulrahForm.SERPENTINE,ZulrahPosition.SOUTH,4,es(4,2,0)),p(ZulrahForm.TANZANITE,ZulrahPosition.WEST,4,es(4,0,4)),p(ZulrahForm.MAGMA,ZulrahPosition.CENTER,2,es(2,2,0)),p(ZulrahForm.SERPENTINE,ZulrahPosition.EAST,4,es(4,0,0)),p(ZulrahForm.SERPENTINE,ZulrahPosition.SOUTH,3,es(0,3,6)),p(ZulrahForm.TANZANITE,ZulrahPosition.WEST,5,es(5,0,4)),p(ZulrahForm.SERPENTINE,ZulrahPosition.CENTER,4,es(4,0,0)),p(ZulrahForm.TANZANITE,ZulrahPosition.CENTER,4,es(4,3,0)),jad(ZulrahPosition.EAST,8,ZulrahAttackStyle.MAGIC,es(8,0,0)),p(ZulrahForm.TANZANITE,ZulrahPosition.CENTER,0,es(0,0,4)),p(ZulrahForm.SERPENTINE,ZulrahPosition.CENTER,5,es(5,4,0)))); }
}
