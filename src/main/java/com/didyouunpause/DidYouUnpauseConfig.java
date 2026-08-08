package com.didyouunpause;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Plain JSON-backed settings, loaded once at startup and re-saved whenever changed
 * from the settings screen.
 */
public final class DidYouUnpauseConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("didyouunpause.json");

	// Presets cycled through by the nag interval button, in seconds.
	public static final int[] INTERVAL_PRESETS = {2, 5, 10, 15, 20, 30};
	// Presets cycled through by the snooze duration button, in minutes.
	public static final int[] SNOOZE_PRESETS = {1, 2, 5, 10, 15, 30};

	public boolean enabled = true;

	public boolean nagOnBreak = true;
	public boolean nagOnPlace = true;
	public boolean nagOnMove = false;
	public boolean nagOnLook = false;

	public int nagIntervalSeconds = 15;
	public int snoozeMinutes = 5;

	// If true, activity while paused silently unpauses the recording instead of nagging.
	public boolean autoUnpauseOnActivity = false;

	public static DidYouUnpauseConfig load() {
		if (Files.exists(PATH)) {
			try (Reader reader = Files.newBufferedReader(PATH, StandardCharsets.UTF_8)) {
				DidYouUnpauseConfig config = GSON.fromJson(reader, DidYouUnpauseConfig.class);
				if (config != null) {
					config.sanitize();
					return config;
				}
			} catch (IOException | RuntimeException e) {
				DidYouUnpause.LOGGER.error("Failed to load config, using defaults", e);
			}
		}
		return new DidYouUnpauseConfig();
	}

	public void save() {
		sanitize();
		try (Writer writer = Files.newBufferedWriter(PATH, StandardCharsets.UTF_8)) {
			GSON.toJson(this, writer);
		} catch (IOException e) {
			DidYouUnpause.LOGGER.error("Failed to save config", e);
		}
	}

	private void sanitize() {
		if (!contains(INTERVAL_PRESETS, this.nagIntervalSeconds)) {
			this.nagIntervalSeconds = 15;
		}
		if (!contains(SNOOZE_PRESETS, this.snoozeMinutes)) {
			this.snoozeMinutes = 5;
		}
	}

	public void cycleInterval() {
		this.nagIntervalSeconds = next(INTERVAL_PRESETS, this.nagIntervalSeconds);
	}

	public void cycleSnooze() {
		this.snoozeMinutes = next(SNOOZE_PRESETS, this.snoozeMinutes);
	}

	private static boolean contains(int[] presets, int value) {
		for (int preset : presets) {
			if (preset == value) {
				return true;
			}
		}
		return false;
	}

	private static int next(int[] presets, int current) {
		for (int i = 0; i < presets.length; i++) {
			if (presets[i] == current) {
				return presets[(i + 1) % presets.length];
			}
		}
		return presets[0];
	}
}
