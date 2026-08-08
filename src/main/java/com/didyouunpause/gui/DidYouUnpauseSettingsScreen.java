package com.didyouunpause.gui;

import com.didyouunpause.DidYouUnpause;
import com.didyouunpause.DidYouUnpauseConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import static com.didyouunpause.DidYouUnpause.GRAY;
import static com.didyouunpause.DidYouUnpause.LIGHT_PURPLE;
import static com.didyouunpause.DidYouUnpause.styled;

/**
 * A small, self-contained settings screen in the vanilla options style (no external
 * config-screen library needed) - opened via the keybind or through Mod Menu.
 */
public final class DidYouUnpauseSettingsScreen extends Screen {
	private static final int ROW_WIDTH = 220;

	private final Screen parent;
	private final DidYouUnpauseConfig config;

	private Button enabledButton;
	private Button breakButton;
	private Button placeButton;
	private Button moveButton;
	private Button lookButton;
	private Button intervalButton;
	private Button snoozeButton;
	private Button autoUnpauseButton;

	public DidYouUnpauseSettingsScreen(Screen parent) {
		super(title());
		this.parent = parent;
		this.config = DidYouUnpause.CONFIG;
	}

	private static Component title() {
		return styled("Did You Unpause? - Settings", LIGHT_PURPLE, true);
	}

	@Override
	protected void init() {
		super.init();

		GridLayout gridLayout = new GridLayout();
		gridLayout.defaultCellSetting().padding(4, 4, 4, 0);
		GridLayout.RowHelper rowHelper = gridLayout.createRowHelper(1);

		rowHelper.addChild(new StringWidget(ROW_WIDTH, 20, title(), this.font));

		this.enabledButton = Button.builder(Component.empty(), b -> {
			this.config.enabled = !this.config.enabled;
			this.refreshLabels();
		}).width(ROW_WIDTH).build();
		rowHelper.addChild(this.enabledButton);

		this.breakButton = Button.builder(Component.empty(), b -> {
			this.config.nagOnBreak = !this.config.nagOnBreak;
			this.refreshLabels();
		}).width(ROW_WIDTH).build();
		rowHelper.addChild(this.breakButton);

		this.placeButton = Button.builder(Component.empty(), b -> {
			this.config.nagOnPlace = !this.config.nagOnPlace;
			this.refreshLabels();
		}).width(ROW_WIDTH).build();
		rowHelper.addChild(this.placeButton);

		this.moveButton = Button.builder(Component.empty(), b -> {
			this.config.nagOnMove = !this.config.nagOnMove;
			this.refreshLabels();
		}).width(ROW_WIDTH).build();
		rowHelper.addChild(this.moveButton);

		this.lookButton = Button.builder(Component.empty(), b -> {
			this.config.nagOnLook = !this.config.nagOnLook;
			this.refreshLabels();
		}).width(ROW_WIDTH).build();
		rowHelper.addChild(this.lookButton);

		this.intervalButton = Button.builder(Component.empty(), b -> {
			this.config.cycleInterval();
			this.refreshLabels();
		}).width(ROW_WIDTH).build();
		rowHelper.addChild(this.intervalButton);

		this.snoozeButton = Button.builder(Component.empty(), b -> {
			this.config.cycleSnooze();
			this.refreshLabels();
		}).width(ROW_WIDTH).build();
		rowHelper.addChild(this.snoozeButton);

		this.autoUnpauseButton = Button.builder(Component.empty(), b -> {
			this.config.autoUnpauseOnActivity = !this.config.autoUnpauseOnActivity;
			this.refreshLabels();
		}).width(ROW_WIDTH).build();
		rowHelper.addChild(this.autoUnpauseButton);

		rowHelper.addChild(Button.builder(Component.translatable("gui.done"), b -> this.onClose())
			.width(ROW_WIDTH).build());

		this.refreshLabels();

		gridLayout.arrangeElements();
		FrameLayout.alignInRectangle(gridLayout, 0, 0, this.width, this.height, 0.5f, 0.5f);
		gridLayout.visitWidgets(this::addRenderableWidget);
	}

	private void refreshLabels() {
		this.enabledButton.setMessage(toggleLabel("Mod Enabled", this.config.enabled));
		this.breakButton.setMessage(toggleLabel("Nag On Block Break", this.config.nagOnBreak));
		this.placeButton.setMessage(toggleLabel("Nag On Block Place", this.config.nagOnPlace));
		this.moveButton.setMessage(toggleLabel("Nag On Movement", this.config.nagOnMove));
		this.lookButton.setMessage(toggleLabel("Nag On Camera Look", this.config.nagOnLook));
		this.intervalButton.setMessage(styled("Nag Every: " + this.config.nagIntervalSeconds + "s", GRAY, false));
		this.snoozeButton.setMessage(styled("Snooze Duration: " + this.config.snoozeMinutes + "m", GRAY, false));
		this.autoUnpauseButton.setMessage(toggleLabel("Auto-Unpause Instead Of Nagging", this.config.autoUnpauseOnActivity));
	}

	private static MutableComponent toggleLabel(String label, boolean value) {
		int color = value ? LIGHT_PURPLE : GRAY;
		String suffix = value ? " : ON" : " : OFF";
		return styled(label + suffix, color, value);
	}

	@Override
	public void onClose() {
		this.config.save();
		Minecraft.getInstance().setScreen(this.parent);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
