package com.zully.zulrah.tracker;

import org.junit.Test;
import static org.junit.Assert.*;

public class SessionTrackerTest
{
	@Test public void calculatesSessionRatesAndLootBasedOnInstanceTime()
	{
		SessionTracker tracker = new SessionTracker();
		tracker.startIfNeeded(1_000L, 10_000L);
		// Simulate ticks inside Zulrah instance: tick every second for 3600 seconds
		for (long t = 1_000L; t <= 3_601_000L; t += 1_000L) {
			tracker.tickInstanceTimer(t, true);
		}

		tracker.recordKill(1_000L);
		tracker.recordKill(1_800_000L);
		tracker.recordDeath();
		assertTrue(tracker.acceptsLoot(1_800_001L));
		tracker.recordLoot(250_000L);

		// Elapsed wall clock time could be 10 hours (36,000,000ms), but active instance time is 1 hour (3,600,000ms)
		SessionTracker.Snapshot stats = tracker.snapshot(36_000_000L, 12_000L);
		assertEquals(2, stats.kills);
		assertEquals(1, stats.deaths);
		assertEquals(3_600_000L, stats.instanceTimeMillis);
		assertEquals(2_000L, stats.xp);
		assertEquals(250_000L, stats.gpGained);
		assertEquals(100_000L, stats.gpSpent);
		assertEquals(150_000L, stats.netGp);

		// Rates should be extrapolated based on 1 hour active instance time, ignoring 10 hour wall time
		assertEquals(2L, stats.killsPerHour);
		assertEquals(2_000L, stats.xpPerHour);
		assertEquals(250_000L, stats.gpPerHour);
		assertEquals(150_000L, stats.netGpPerHour);
	}
}
