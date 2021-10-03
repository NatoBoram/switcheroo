package com.natoboram.switcheroo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;

@Environment(EnvType.CLIENT)
public class Switch {

	private static final Logger LOGGER = LogManager.getLogger(Main.MOD_ID);
	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();

	/**
	 * Perform the actual switcheroo.
	 *
	 * @param player PlayerEntity that's about to execute the switcheroo.
	 * @param item   Item that should be put in its hand.
	 */
	public static void switcheroo(final PlayerEntity player, final ItemStack item, final SwitcherooConfig config) {
		final PlayerInventory inventory = player.getInventory();

		final int slot = inventory.getSlotWithStack(item);
		if (slot == -1)
			return;

		if (config.debug)
			LOGGER.info("Switching for " + item.getItem());

		if (PlayerInventory.isValidHotbarIndex(slot)) {
			// Select the item from the hotbar
			inventory.selectedSlot = slot;
		} else {
			// Pick the item from the inventory
			CLIENT.interactionManager.pickFromInventory(slot);
		}
	}

}
