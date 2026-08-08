package com.didyouunpause;

/**
 * Converts plain text into Unicode small-caps lookalikes (ᴅɪᴅ ʏᴏᴜ ᴜɴᴘᴀᴜꜱᴇ) for
 * a nicer HUD/chat look, since the vanilla font has no small-caps variant of its own.
 */
public final class SmallCaps {
	private static final String NORMAL = "abcdefghijklmnopqrstuvwxyz";
	private static final String[] CAPS = {
		"ᴀ", "ʙ", "ᴄ", "ᴅ", "ᴇ", "ꜰ", "ɢ", "ʜ",
		"ɪ", "ᴊ", "ᴋ", "ʟ", "ᴍ", "ɴ", "ᴏ", "ᴘ",
		"ǫ", "ʀ", "ꜱ", "ᴛ", "ᴜ", "ᴠ", "ᴡ", "x",
		"ʏ", "ᴢ"
	};

	private SmallCaps() {
	}

	public static String convert(String text) {
		StringBuilder builder = new StringBuilder(text.length());
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			int index = NORMAL.indexOf(Character.toLowerCase(c));
			if (index >= 0) {
				builder.append(CAPS[index]);
			} else {
				builder.append(c);
			}
		}
		return builder.toString();
	}
}
