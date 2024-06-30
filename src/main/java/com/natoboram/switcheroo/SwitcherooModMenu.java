package com.natoboram.switcheroo;

import static net.fabricmc.api.EnvType.CLIENT;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.api.Environment;

@Environment(value = CLIENT)
public class SwitcherooModMenu implements ModMenuApi {

	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return parent -> AutoConfig.getConfigScreen(SwitcherooConfig.class, parent).get();
	}
}
