package com.natoboram.switcheroo;

import static net.fabricmc.api.EnvType.CLIENT;

import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

		final String itemName = item.getName().getString();
		final int slot = inventory.getSlotWithStack(item);
		if (slot == -1) {
			LOGGER.warn("Item {} not found in inventory", itemName);
			return;
		}

		if (config.debug) LOGGER.info("Switching for {}", itemName);

		if (PlayerInventory.isValidHotbarIndex(slot)) {
			// Select the item from the hotbar
			if (config.debug) LOGGER.info("Selecting slot {}", slot);
			inventory.selectedSlot = slot;
		} else {
			// Pick the item from the inventory
			final int nextSlot = Switch.findEmptyOrCurrentHotbarSlot(inventory);

			if (config.debug) LOGGER.info("Switching from slot {} to {}", slot, nextSlot);

			inventory.selectedSlot = nextSlot;
			CLIENT.interactionManager.clickSlot(
				player.playerScreenHandler.syncId,
				slot,
				nextSlot,
				SlotActionType.SWAP,
				player
			);
		}
	}

	/**
	 * Finds the index of the first empty slot in the player's hotbar. If no empty
	 * slot is found, returns the index of the currently selected hotbar slot.
	 *
	 * @param inventory The player's inventory.
	 * @return The index of an empty hotbar slot. If there's none, fallbacks to the
	 *         currently selected slot.
	 */
	private static int findEmptyOrCurrentHotbarSlot(final PlayerInventory inventory) {
		if (inventory.getStack(inventory.selectedSlot).isEmpty()) return inventory.selectedSlot;

		for (int i = 0; i < PlayerInventory.getHotbarSize(); i++) if (inventory.getStack(i).isEmpty()) return i;

		return inventory.selectedSlot;
	}
}
