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
		public String blocks = "budding_amethyst farmland";

		@ConfigEntry.Gui.Tooltip()
		public String mobs = "axolotl bat cat donkey fox horse mule ocelot parrot skeleton_horse snow_golem strider villager wandering_trader";
	}

	@ConfigEntry.Gui.Tooltip()
	public boolean debug = false;

	@ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
	public Prefer prefer = new Prefer();

	public static class Prefer {
		@ConfigEntry.Gui.Tooltip()
		public String silk_touch = "bee_nest beehive black_stained_glass black_stained_glass_pane blue_ice blue_stained_glass blue_stained_glass_pane bookshelf brain_coral brain_coral_block brown_mushroom_block brown_stained_glass brown_stained_glass_pane bubble_coral bubble_coral_block campfire crimson_nylium cyan_stained_glass cyan_stained_glass_pane dirt_path ender_chest fire_coral fire_coral_block glass glass_pane gray_stained_glass gray_stained_glass_pane green_stained_glass green_stained_glass_pane horn_coral horn_coral_block ice infested_chiseled_stone_bricks infested_cobblestone infested_cracked_stone_bricks infested_deepslate infested_mossy_stone_bricks infested_stone infested_stone_bricks large_amethyst_bud light_blue_stained_glass light_blue_stained_glass_pane light_gray_stained_glass light_gray_stained_glass_pane lime_stained_glass lime_stained_glass_pane magenta_stained_glass magenta_stained_glass_pane medium_amethyst_bud mushroom_stem mycelium orange_stained_glass orange_stained_glass_pane packed_ice pink_stained_glass pink_stained_glass_pane purple_stained_glass purple_stained_glass_pane red_mushroom_block red_stained_glass red_stained_glass_pane sculk sculk_catalyst sculk_sensor sculk_shrieker sculk_vein sea_lantern small_amethyst_bud snow snow_block soul_campfire tube_coral tube_coral_block turtle_egg twisting_vines twisting_vines_plant warped_nylium weeping_vines weeping_vines_plant white_stained_glass white_stained_glass_pane yellow_stained_glass yellow_stained_glass_pane";

		@ConfigEntry.Gui.Excluded()
		@ConfigEntry.Gui.Tooltip()
		public String fortune = "";
	}
}
