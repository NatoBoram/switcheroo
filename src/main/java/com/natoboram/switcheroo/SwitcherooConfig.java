package com.natoboram.switcheroo;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = Main.MOD_ID)
public class SwitcherooConfig implements ConfigData {

	@ConfigEntry.Gui.Excluded()
	public boolean enableCrop = false;

	public boolean alwaysFastest = false;

	@ConfigEntry.Gui.Tooltip()
	public int minDurability = 5;

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

	@ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
	public Prefer prefer = new Prefer();

	public static class Prefer {
		@ConfigEntry.Gui.Tooltip()
		public String silk_touch = "glass_pane";

		@ConfigEntry.Gui.Excluded()
		@ConfigEntry.Gui.Tooltip()
		public String fortune = "";
	}
}
