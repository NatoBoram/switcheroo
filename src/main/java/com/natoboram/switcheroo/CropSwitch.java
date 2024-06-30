package com.natoboram.switcheroo;

import static net.fabricmc.api.EnvType.CLIENT;

import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import me.shedaniel.autoconfig.ConfigHolder;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

/**
 * Execute a switcheroo action when attacking crops. The original plan was to
 * auto-replant, but I'll need to wait for a block break event to do this. For
 * now, this section of code is disabled.
 */
@Environment(value = CLIENT)
public class CropSwitch implements AttackBlockCallback {

	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
	private static final Logger LOGGER = LogManager.getLogger(Main.MOD_ID);
	private final ConfigHolder<SwitcherooConfig> CONFIG_HOLDER;

	CropSwitch(final ConfigHolder<SwitcherooConfig> holder) {
		this.CONFIG_HOLDER = holder;
	}

	@Override
	public ActionResult interact(final PlayerEntity player, final World world, final Hand hand, final BlockPos pos,
			final Direction direction) {
		final SwitcherooConfig config = CONFIG_HOLDER.getConfig();
		final PlayerInventory inventory = player.getInventory();

		final BlockState blockState = world.getBlockState(pos);
		final Block block = blockState.getBlock();

		// Check if it's a crop.
		if (!(block instanceof CropBlock)) {
			if (config.debug)
				LOGGER.info("Skipping interaction with block " + blockState.getBlock().getName().getString());
			return ActionResult.PASS;
		}

		// Check if we already have the appropriate item in hand
		final Item seedItem = block.asItem();
		final ItemStack mainHandStack = inventory.getMainHandStack();
		if (mainHandStack.getItem().equals(seedItem)) {
			if (config.debug)
				LOGGER.info("Already holding " + seedItem.getName().getString());
			return ActionResult.PASS;
		}

		// Get all the appropriate seeds
		final ArrayList<ItemStack> seeds = new ArrayList<ItemStack>();
		for (final ItemStack stack : inventory.main) {
			if (stack.getItem().equals(seedItem))
				seeds.add(stack);
		}

		if (seeds.isEmpty()) {
			if (config.debug)
				LOGGER.info("No seeds found for " + seedItem.getName().getString());
			return ActionResult.PASS;
		}
		ItemStackUtil.keepLowestStacks(seeds);

		if (seeds.isEmpty()) {
			if (config.debug)
				LOGGER.warn("No seeds found for " + seedItem.getName().getString());
			return ActionResult.PASS;
		}
		final ItemStack seed = seeds.get(0);
		Switch.switcheroo(player, seed, CONFIG_HOLDER.getConfig());

		// Plant the seed!
		final BlockHitResult blockHitResult = (BlockHitResult) CLIENT.crosshairTarget;
		final ActionResult placeSeedResult = CLIENT.interactionManager.interactBlock(CLIENT.player, hand,
				blockHitResult);
		if (placeSeedResult.isAccepted() && placeSeedResult.shouldSwingHand())
			CLIENT.player.swingHand(hand);

		return ActionResult.PASS;
	}

}
