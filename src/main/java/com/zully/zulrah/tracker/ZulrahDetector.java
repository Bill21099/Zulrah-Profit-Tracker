package com.zully.zulrah.tracker;
import com.zully.zulrah.model.ZulrahForm;
import net.runelite.api.NPC;
/** NPC ids are validated against RuneLite's current Zulrah forms: 2042, 2043 and 2044. */
public final class ZulrahDetector { public boolean isZulrah(NPC npc){return npc!=null&&(ZulrahForm.fromNpcId(npc.getId())!=null||(npc.getName()!=null&&npc.getName().equalsIgnoreCase("Zulrah")));} public boolean isSnakeling(NPC npc){return npc!=null&&npc.getName()!=null&&npc.getName().equalsIgnoreCase("Snakeling");} }
