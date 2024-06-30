package com.natoboram.switcheroo;

import static net.fabricmc.api.EnvType.CLIENT;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;

@Environment(value = CLIENT)
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
		if (slot == -1) {
			LOGGER.warn("Item " + item.getName().getString() + " not found in inventory");
			return;
		}

		if (config.debug)
			LOGGER.info("Switching for " + item.getName().getString());

		if (PlayerInventory.isValidHotbarIndex(slot)) {
			// Select the item from the hotbar
			if (config.debug)
				LOGGER.info("Selecting slot " + slot);
			inventory.selectedSlot = slot;
		} else {
			// Pick the item from the inventory
			if (config.debug)
				LOGGER.info("Picking from slot " + slot);
			CLIENT.interactionManager.pickFromInventory(slot);
		}
	}

}
