package com.zully.zulrah.model;

/** Zulrah NPC forms; ids are the current game NPC ids (2042-2044). */
public enum ZulrahForm {
    SERPENTINE(2042, "Serpentine"), MAGMA(2043, "Magma"), TANZANITE(2044, "Tanzanite");
    private final int npcId; private final String label;
    ZulrahForm(int npcId, String label) { this.npcId = npcId; this.label = label; }
    public int npcId() { return npcId; }
    public String label() { return label; }
    public static ZulrahForm fromNpcId(int id) { for (ZulrahForm form : values()) if (form.npcId == id) return form; return null; }
}
