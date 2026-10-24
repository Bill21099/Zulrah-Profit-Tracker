package com.zulprofit.zulrah.tracker;

import org.junit.Test;
import static org.junit.Assert.*;

public class SessionTrackerTest
{
	@Test
	public void calculatesSessionRatesAndLootBasedOnInstanceTime()
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
		tracker.recordItemSpent(25_000L); // e.g. food + potion + zul-andra teleport

		// Elapsed wall clock time could be 10 hours (36,000,000ms), but active instance time is 1 hour (3,600,000ms)
		SessionTracker.Snapshot stats = tracker.snapshot(36_000_000L, 12_000L);
		assertEquals(2, stats.kills);
		assertEquals(1, stats.deaths);
		assertEquals(3_600_000L, stats.instanceTimeMillis);
		assertEquals(3_600_000L, stats.sessionTimeMillis);
		assertEquals(0L, stats.idleTimeMillis);
		assertEquals(2_000L, stats.xp);
		assertEquals(250_000L, stats.gpGained);
		assertEquals(125_000L, stats.gpSpent); // 100k death reclaim + 25k supplies
		assertEquals(125_000L, stats.netGp);

		// Rates should be extrapolated based on 1 hour active instance time
		assertEquals(2L, stats.killsPerHour);
		assertEquals(2_000L, stats.xpPerHour);
		assertEquals(250_000L, stats.gpPerHour);
		assertEquals(125_000L, stats.netGpPerHour);
	}

	@Test
	public void calculatesSessionTimeAndIdleTimeCorrectly()
	{
		SessionTracker tracker = new SessionTracker();
		tracker.start(1_000L, 10_000L);

		// 1000 seconds inside the instance (active fight) from 2_000L to 1_001_000L
		for (long t = 2_000L; t <= 1_001_000L; t += 1_000L) {
			tracker.tickTimers(t, true);
		}
		// 500 seconds outside the instance (banking / idle) from 1_002_000L to 1_501_000L
		for (long t = 1_002_000L; t <= 1_501_000L; t += 1_000L) {
			tracker.tickTimers(t, false);
		}
		// 500 seconds back inside the instance (next kill) from 1_502_000L to 2_001_000L
		for (long t = 1_502_000L; t <= 2_001_000L; t += 1_000L) {
			tracker.tickTimers(t, true);
		}

		SessionTracker.Snapshot stats = tracker.snapshot(2_001_000L, 10_000L);
		assertEquals(2_000_000L, stats.sessionTimeMillis);
		assertEquals(1_500_000L, stats.instanceTimeMillis);
		assertEquals(500_000L, stats.idleTimeMillis);
		assertEquals(stats.sessionTimeMillis, stats.instanceTimeMillis + stats.idleTimeMillis);
	}

	@Test
	public void tracksLootItemsAndQuantities()
	{
		SessionTracker tracker = new SessionTracker();
		tracker.start(1_000L, 10_000L);

		// Zulrah drops scales (id 12934) and magic logs (id 1514)
		tracker.recordLoot(12934, 250, 60_000L);
		tracker.recordLoot(1514, 100, 30_000L);
		// Next kill drops more scales and Zulrah's scales
		tracker.recordLoot(12934, 150, 36_000L);

		SessionTracker.Snapshot stats = tracker.snapshot(2_000L, 10_000L);
		assertEquals(126_000L, stats.gpGained);
		assertEquals(2, stats.items.size());
		assertEquals(Integer.valueOf(400), stats.items.get(12934));
		assertEquals(Integer.valueOf(100), stats.items.get(1514));
	}

	@Test
	public void testResetSession()
	{
		SessionTracker tracker = new SessionTracker();
		tracker.startIfNeeded(1_000L, 10_000L);
		tracker.recordKill(1_000L);
		tracker.recordLoot(12934, 200, 100_000L);
		tracker.recordItemSpent(5_000L);

		SessionTracker.Snapshot before = tracker.snapshot(2_000L, 10_000L);
		assertEquals(1, before.kills);
		assertEquals(100_000L, before.gpGained);

		tracker.resetSession(2_500L, 10_000L);
		SessionTracker.Snapshot after = tracker.snapshot(3_000L, 10_000L);
		assertEquals(0, after.kills);
		assertEquals(0L, after.gpGained);
		assertEquals(0L, after.gpSpent);
		assertEquals(0L, after.sessionTimeMillis);
		assertEquals(0, after.items.size());
	}
}

