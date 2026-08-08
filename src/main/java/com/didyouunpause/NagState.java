package com.didyouunpause;

/**
 * Temporary, non-persisted nag suppression state, controlled by the snooze and
 * disable-until-unpause chat buttons. Reset whenever the recording pauses or unpauses.
 */
public final class NagState {
	private NagState() {
	}

	private static long snoozeUntilMillis = 0;
	private static boolean disabledUntilUnpause = false;

	public static boolean isSuppressed() {
		return disabledUntilUnpause || System.currentTimeMillis() < snoozeUntilMillis;
	}

	public static void snooze() {
		snoozeUntilMillis = System.currentTimeMillis() + DidYouUnpause.CONFIG.snoozeMinutes * 60_000L;
	}

	public static void disable() {
		disabledUntilUnpause = true;
	}

	public static void reset() {
		snoozeUntilMillis = 0;
		disabledUntilUnpause = false;
	}
}
