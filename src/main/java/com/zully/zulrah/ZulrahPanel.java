package com.zully.zulrah;

import com.zully.zulrah.model.*;
import com.zully.zulrah.tracker.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import net.runelite.api.*;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;

public final class ZulrahPanel extends PluginPanel {
    private static final Color BACKGROUND = ColorScheme.DARK_GRAY_COLOR;
    private static final Color CARD = ColorScheme.DARKER_GRAY_COLOR;
    private static final Color ACCENT = ColorScheme.BRAND_ORANGE;
    private static final Color MUTED = ColorScheme.LIGHT_GRAY_COLOR;
    private static final Color VALUE = Color.WHITE;

    private final EncounterTracker tracker;
    private final SessionTracker session;
    private final Client client;

    private final JLabel status = v();
    private final JLabel rotation = v();
    private final JLabel current = v();
    private final JLabel currentPrayer = v();
    private final JLabel next = v();
    private final JLabel nextPrayer = v();
    private final JLabel then = v();
    private final JLabel thenPrayer = v();
    private final JLabel kills = v();
    private final JLabel deaths = v();
    private final JLabel sessionTime = v();
    private final JLabel xp = v();
    private final JLabel xpHour = v();
    private final JLabel gpGained = v();
    private final JLabel gpSpent = v();
    private final JLabel totalGpGained = v();
    private final JLabel profitHour = v();

    public ZulrahPanel(EncounterTracker t, SessionTracker s, Client c) {
        super(false);
        tracker = t;
        session = s;
        client = c;
        setLayout(new BorderLayout());
        setBackground(BACKGROUND);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        content.add(header());
        content.add(Box.createVerticalStrut(10));
        content.add(encounter());
        content.add(Box.createVerticalStrut(10));
        content.add(session());

        add(content, BorderLayout.NORTH);
        refresh();
    }

    private JComponent header() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        JLabel title = new JLabel("ZULRAH PROFIT TRACKER");
        title.setForeground(ACCENT);
        title.setFont(FontManager.getRunescapeBoldFont());
        p.add(title, BorderLayout.WEST);
        p.add(status, BorderLayout.EAST);
        return p;
    }

    private JComponent encounter() {
        JPanel p = card();
        row(p, "Rotation", rotation);
        section(p, "NOW");
        row(p, "Phase", current);
        row(p, "Prayer", currentPrayer);
        section(p, "NEXT");
        row(p, "Phase", next);
        row(p, "Prayer", nextPrayer);
        section(p, "THEN");
        row(p, "Phase", then);
        row(p, "Prayer", thenPrayer);
        return p;
    }

    private JComponent session() {
        JPanel p = card();
        section(p, "SESSION");
        row(p, "Time in instance", sessionTime);
        p.add(Box.createVerticalStrut(6));
        row(p, "Kills this session", kills);
        row(p, "Deaths this session", deaths);
        p.add(Box.createVerticalStrut(6));
        row(p, "XP gained", xp);
        row(p, "XP / hour", xpHour);
        p.add(Box.createVerticalStrut(6));
        row(p, "GP Gained", gpGained);
        row(p, "GP Spent", gpSpent);
        row(p, "Total GP Gained", totalGpGained);
        p.add(Box.createVerticalStrut(6));
        row(p, "Profit/Hour", profitHour);
        return p;
    }

    private JPanel card() {
        JPanel p = new JPanel();
        p.setBackground(CARD);
        p.setBorder(new EmptyBorder(8, 10, 8, 10));
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        return p;
    }

    private void section(JPanel p, String title) {
        JLabel l = new JLabel(title);
        l.setForeground(ACCENT);
        l.setFont(FontManager.getRunescapeBoldFont());
        l.setBorder(new EmptyBorder(p.getComponentCount() == 0 ? 0 : 8, 0, 4, 0));
        p.add(l);
    }

    private void row(JPanel p, String name, JLabel value) {
        JPanel r = new JPanel(new BorderLayout());
        r.setOpaque(false);
        JLabel l = new JLabel(name);
        l.setForeground(MUTED);
        l.setFont(FontManager.getRunescapeSmallFont());
        value.setForeground(VALUE);
        value.setFont(FontManager.getRunescapeSmallFont());
        r.add(l, BorderLayout.WEST);
        r.add(value, BorderLayout.EAST);
        p.add(r);
    }

    private static JLabel v() {
        return new JLabel("—");
    }

    public void refresh() {
        EncounterState s = tracker.state();
        SessionTracker.Snapshot stats = session.snapshot(System.currentTimeMillis(), xp());
        boolean active = s.status() != EncounterState.Status.INACTIVE;
        status.setText(active ? "ACTIVE" : "STANDBY");
        status.setForeground(active ? ACCENT : MUTED);
        rotation.setText(s.rotation() == null ? (active ? "Detecting" : "—") : "#" + s.rotation());
        current.setText(active ? observed(s) : "Waiting for Zulrah");
        currentPrayer.setText(active ? prayer(tracker.currentPrayer()) : "—");
        ZulrahPhase n = tracker.nextPhase(), later = tracker.phaseAfterNext();
        next.setText(phase(n));
        nextPrayer.setText(prayer(tracker.nextPhasePrayer()));
        then.setText(phase(later));
        thenPrayer.setText(prayer(later == null ? ZulrahPrayer.NONE : later.prayer()));

        sessionTime.setText(formatDuration(stats.instanceTimeMillis));
        kills.setText(number(stats.kills));
        deaths.setText(number(stats.deaths));
        xp.setText(number(stats.xp));
        xpHour.setText(number(stats.xpPerHour));
        gpGained.setText(number(stats.gpGained));
        gpSpent.setText(number(stats.gpSpent));
        totalGpGained.setText(number(stats.netGp));
        profitHour.setText(number(stats.netGpPerHour));
    }

    private String observed(EncounterState s) {
        return s.observedForm() == null ? "Unknown" : s.observedForm().label() + " · " + s.observedPosition();
    }

    private String phase(ZulrahPhase p) {
        return p == null ? "Unknown" : p.form().label() + " · " + p.position();
    }

    private String prayer(ZulrahPrayer p) {
        switch (p) {
            case PROTECT_FROM_MISSILES:
                return "Protect Range";
            case PROTECT_FROM_MAGIC:
                return "Protect Magic";
            case ALTERNATE:
                return "Alternate";
            default:
                return "—";
        }
    }

    private String number(long n) {
        return String.format("%,d", n);
    }

    private String formatDuration(long millis) {
        long seconds = millis / 1000;
        long h = seconds / 3600;
        long m = (seconds % 3600) / 60;
        long s = seconds % 60;
        if (h > 0) {
            return String.format("%d:%02d:%02d", h, m, s);
        }
        return String.format("%02d:%02d", m, s);
    }

    private long xp() {
        long total = 0;
        for (Skill s : new Skill[]{Skill.ATTACK, Skill.STRENGTH, Skill.DEFENCE, Skill.HITPOINTS, Skill.RANGED, Skill.MAGIC}) {
            total += client.getSkillExperience(s);
        }
        return total;
    }
}

