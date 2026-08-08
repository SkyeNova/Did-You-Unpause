package com.didyouunpause.compat;

import com.didyouunpause.gui.DidYouUnpauseSettingsScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public final class ModMenuIntegration implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return DidYouUnpauseSettingsScreen::new;
	}
}
