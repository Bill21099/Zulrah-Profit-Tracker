package com.zully.zulrah.model;

import java.util.*;

/** Immutable phase data. Events describe the observable sequence, not a timer. */
public final class ZulrahPhase {
    private final ZulrahForm form; private final ZulrahPosition position; private final int attacks;
    private final ZulrahAttackStyle jadStart; private final List<ZulrahEvent> events;
    public ZulrahPhase(ZulrahForm form, ZulrahPosition position, int attacks, ZulrahAttackStyle jadStart, List<ZulrahEvent> events) {
        this.form = Objects.requireNonNull(form); this.position = Objects.requireNonNull(position); this.attacks = attacks;
        this.jadStart = jadStart == null ? ZulrahAttackStyle.NONE : jadStart;
        this.events = Collections.unmodifiableList(new ArrayList<>(events));
    }
    public ZulrahForm form() { return form; } public ZulrahPosition position() { return position; }
    public int attacks() { return attacks; } public boolean isJad() { return jadStart != ZulrahAttackStyle.NONE; }
    public ZulrahAttackStyle jadStart() { return jadStart; } public List<ZulrahEvent> events() { return events; }
    public ZulrahPrayer prayer() { return isJad() ? ZulrahPrayer.ALTERNATE : form == ZulrahForm.SERPENTINE ? ZulrahPrayer.PROTECT_FROM_MISSILES : form == ZulrahForm.TANZANITE ? ZulrahPrayer.PROTECT_FROM_MAGIC : ZulrahPrayer.NONE; }
    public boolean matches(ZulrahForm observedForm, ZulrahPosition observedPosition) { return form == observedForm && position == observedPosition; }
}
