package com.didyouunpause;

import com.didyouunpause.gui.DidYouUnpauseSettingsScreen;
import com.mojang.blaze3d.platform.InputConstants;
import com.moulberry.flashback.Flashback;
import com.moulberry.flashback.record.Recorder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.event.client.player.ClientPlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Watches Flashback's recorder and yells at the player if they keep playing
 * while the recording is still paused, the classic "went to the Nether, forgot
 * to unpause" mistake.
 */
public final class DidYouUnpause implements ClientModInitializer {
	public static final String MOD_ID = "didyouunpause";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static DidYouUnpauseConfig CONFIG = new DidYouUnpauseConfig();

	public static final int LIGHT_PURPLE = 0xC9A6FF;
	public static final int GRAY = 0xAAAAAA;
	public static final int LIGHT_BLUE = 0x6EC6FF;
	public static final int YELLOW = 0xFFD966;

	// Squared distance a tick has to cover before it counts as "moving".
	private static final double MOVE_THRESHOLD_SQ = 0.0025; // ~0.05 blocks/tick
	// Squared combined yaw+pitch change (in degrees) before it counts as "looking around".
	private static final double LOOK_THRESHOLD_SQ = 4.0 * 4.0;

	private static final KeyMapping.Category CATEGORY =
		KeyMapping.Category.register(ResourceLocation.fromNamespaceAndPath(MOD_ID, "keybind"));
	public static final KeyMapping OPEN_SETTINGS_KEY = KeyBindingHelper.registerKeyBinding(new KeyMapping(
		"didyouunpause.keybind.open_settings", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_F9, CATEGORY));

	private double lastX, lastY, lastZ;
	private float lastYaw, lastPitch;
	private boolean havePreviousPose = false;

	private boolean wasPaused = false;
	private boolean suppressNextUnpauseMessage = false;
	private int cooldownRemaining = 0;

	@Override
	public void onInitializeClient() {
		CONFIG = DidYouUnpauseConfig.load();

		ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);

		ClientPlayerBlockBreakEvents.AFTER.register((world, player, pos, state) -> {
			if (CONFIG.nagOnBreak) {
				tryNag();
			}
		});

		UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
			if (CONFIG.nagOnPlace) {
				tryNag();
			}
			return InteractionResult.PASS;
		});

		registerCommands();
	}

	private void registerCommands() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			var root = ClientCommandManager.literal("didyouunpause");

			root.then(ClientCommandManager.literal("snooze").executes(ctx -> {
				NagState.snooze();
				sendChatMessage("Snoozed nagging for " + CONFIG.snoozeMinutes + " minutes.");
				return 0;
			}));

			root.then(ClientCommandManager.literal("confirmdisable").executes(ctx -> {
				promptDisableConfirmation();
				return 0;
			}));

			root.then(ClientCommandManager.literal("disable").executes(ctx -> {
				NagState.disable();
				sendChatMessage("Alright, I'll stay quiet until you unpause.");
				return 0;
			}));

			root.then(ClientCommandManager.literal("cancel").executes(ctx -> {
				sendChatMessage("Never mind, then.");
				return 0;
			}));

			dispatcher.register(root);
		});
	}

	private void onClientTick(Minecraft client) {
		while (OPEN_SETTINGS_KEY.consumeClick()) {
			if (client.screen == null) {
				client.setScreen(new DidYouUnpauseSettingsScreen(null));
			}
		}

		if (cooldownRemaining > 0) {
			cooldownRemaining--;
		}

		Recorder recorder = Flashback.RECORDER;
		LocalPlayer player = client.player;
		boolean paused = recorder != null && recorder.isPaused();

		handlePauseTransition(recorder != null, paused);

		if (!CONFIG.enabled || player == null || !paused) {
			havePreviousPose = false;
			return;
		}

		trackMovementAndLook(player);
	}

	private void trackMovementAndLook(LocalPlayer player) {
		double x = player.getX();
		double y = player.getY();
		double z = player.getZ();
		float yaw = player.getYRot();
		float pitch = player.getXRot();

		if (havePreviousPose) {
			double dx = x - lastX;
			double dy = y - lastY;
			double dz = z - lastZ;
			boolean moved = (dx * dx + dy * dy + dz * dz) > MOVE_THRESHOLD_SQ;

			float dYaw = Mth.wrapDegrees(yaw - lastYaw);
			float dPitch = pitch - lastPitch;
			boolean looked = (dYaw * dYaw + dPitch * dPitch) > LOOK_THRESHOLD_SQ;

			if (CONFIG.nagOnMove && moved) {
				tryNag();
			}
			if (CONFIG.nagOnLook && looked) {
				tryNag();
			}
		}

		lastX = x;
		lastY = y;
		lastZ = z;
		lastYaw = yaw;
		lastPitch = pitch;
		havePreviousPose = true;
	}

	private void handlePauseTransition(boolean recorderExists, boolean nowPaused) {
		if (!recorderExists) {
			this.wasPaused = false;
			NagState.reset();
			return;
		}

		if (nowPaused != this.wasPaused) {
			NagState.reset();

			if (CONFIG.enabled) {
				if (nowPaused) {
					sendChatMessage(Messages.randomPaused());
				} else if (!suppressNextUnpauseMessage) {
					sendChatMessage(Messages.randomUnpaused());
				}
			}
			suppressNextUnpauseMessage = false;
		}

		this.wasPaused = nowPaused;
	}

	private void tryNag() {
		if (!CONFIG.enabled || NagState.isSuppressed()) {
			return;
		}

		Recorder recorder = Flashback.RECORDER;
		if (recorder == null || !recorder.isPaused()) {
			return;
		}

		if (CONFIG.autoUnpauseOnActivity) {
			autoUnpause();
			return;
		}

		if (cooldownRemaining > 0) {
			return;
		}

		nag(Minecraft.getInstance());
		cooldownRemaining = CONFIG.nagIntervalSeconds * 20;
	}

	private void autoUnpause() {
		suppressNextUnpauseMessage = true;
		Flashback.pauseRecordingReplay(false);

		Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
		sendChatMessage("Hey, I noticed you were playing, so I unpaused your recording for you.");
	}

	private void nag(Minecraft client) {
		client.gui.setTitle(styled("Did You Unpause?", LIGHT_PURPLE, true));
		client.gui.setSubtitle(styled("You're playing, but nothing is being recorded!", GRAY, false));
		client.gui.setTimes(5, 40, 10);

		client.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.ANVIL_LAND, 1.0f));

		sendChatMessage(Messages.randomNag());
		sendRawChatMessage(actionButtonsLine());
	}

	private static MutableComponent actionButtonsLine() {
		return actionButton("[Snooze " + CONFIG.snoozeMinutes + "m]", LIGHT_BLUE, "/didyouunpause snooze",
				"Stop nagging for " + CONFIG.snoozeMinutes + " minutes")
			.append(Component.literal("   "))
			.append(actionButton("[Disable Until Unpause]", YELLOW, "/didyouunpause confirmdisable",
				"Stop nagging until you next unpause"));
	}

	private void promptDisableConfirmation() {
		MutableComponent message = styled("Stop nagging until you unpause? ", YELLOW, true)
			.append(actionButton("[Yes, Disable]", YELLOW, "/didyouunpause disable", "Confirm"))
			.append(Component.literal("   "))
			.append(actionButton("[Cancel]", GRAY, "/didyouunpause cancel", "Never mind"));
		sendRawChatMessage(message);
	}

	private static MutableComponent actionButton(String label, int color, String command, String tooltip) {
		Style style = Style.EMPTY.withColor(color).withBold(true).withUnderlined(true)
			.withClickEvent(new ClickEvent.RunCommand(command))
			.withHoverEvent(new HoverEvent.ShowText(Component.literal(tooltip)));
		return Component.literal(SmallCaps.convert(label)).setStyle(style);
	}

	private static void sendChatMessage(String body) {
		MutableComponent message = styled("[Did You Unpause?] ", LIGHT_PURPLE, true)
			.append(styled(body, GRAY, false));
		sendRawChatMessage(message);
	}

	private static void sendRawChatMessage(Component message) {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player != null) {
			player.displayClientMessage(message, false);
		}
	}

	public static MutableComponent styled(String text, int color, boolean bold) {
		return Component.literal(SmallCaps.convert(text)).setStyle(Style.EMPTY.withColor(color).withBold(bold));
	}
}
