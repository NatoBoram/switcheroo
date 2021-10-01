package com.natoboram.switcheroo;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = Main.MOD_ID)
public class SwitcherooConfig implements ConfigData {

	@ConfigEntry.Gui.Excluded()
	public boolean enableCrop = false;

	@ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
	public Blacklist blacklist = new Blacklist();

	public static class Blacklist {
		@ConfigEntry.Gui.Tooltip()
		public String blocks = "farmland glass_pane";

		@ConfigEntry.Gui.Tooltip()
		public String mobs = "axolotl bat cat donkey fox horse mule ocelot parrot skeleton_horse snow_golem strider villager wandering_trader";
	}

	@ConfigEntry.Gui.Tooltip()
	public boolean debug = false;
}
