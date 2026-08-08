package com.didyouunpause;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Message pools for the nag repeats and for the one-off pause/unpause pun lines.
 * All plain text here - small-caps conversion happens at the call site.
 */
public final class Messages {
	private Messages() {
	}

	// Shown (repeatedly, on cooldown) while the recorder is paused and you're still playing.
	private static final List<String> NAG = List.of(
		"Your recording is still paused, genius.",
		"Flashback is not, in fact, recording right now.",
		"That footage you think you're getting? You're not getting it.",
		"Bro forgot to unpause AGAIN.",
		"This is going to be a great clip of absolutely nothing.",
		"The Nether trip was great, shame none of it was recorded.",
		"Still paused. Still not recording. Still you.",
		"Every second right now is being recorded to /dev/null.",
		"You're really out here narrating to no one.",
		"Congratulations, you're filming the inside of your eyelids.",
		"Recording status: paused. Your memory of this: also fading.",
		"Someone's gonna ask for the clip and you're gonna lie.",
		"This would've been such a good moment. Too bad.",
		"Peak content, zero footage.",
		"You paused this on purpose, so why are you still playing?"
	);

	// Shown once, right when the recorder becomes paused.
	private static final List<String> PAUSED = List.of(
		"Recording paused. Try to remember to come back to this.",
		"Paused. The clock on your forgetfulness starts now.",
		"Flashback is taking a nap. Don't let it oversleep.",
		"Paused! Set a reminder. You will not remember.",
		"Recording on hold. Historically, this does not end well.",
		"Paused. We both know what happens next.",
		"Taking a break from recording. See you in twenty minutes, allegedly.",
		"Paused successfully. Unpausing successfully is next week's problem, apparently."
	);

	// Shown once, right when the recorder resumes from paused.
	private static final List<String> UNPAUSED = List.of(
		"Unpaused! Look at you, being responsible.",
		"Recording resumed. Who are you and what have you done with the usual guy?",
		"Back on the record. Shocking.",
		"Unpaused before disaster struck, for once.",
		"Recording resumed. Achievement unlocked: remembering something.",
		"You actually did it. Recording is back on.",
		"Unpaused. The footage gods are pleased.",
		"Back to recording. Let's see how long this lasts."
	);

	public static String randomNag() {
		return pick(NAG);
	}

	public static String randomPaused() {
		return pick(PAUSED);
	}

	public static String randomUnpaused() {
		return pick(UNPAUSED);
	}

	private static String pick(List<String> pool) {
		return pool.get(ThreadLocalRandom.current().nextInt(pool.size()));
	}
}
