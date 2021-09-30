package com.natoboram.switcheroo;

import java.util.ArrayList;

import net.fabricmc.api.EnvType;
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
@Environment(EnvType.CLIENT)
public class CropSwitch implements AttackBlockCallback {

	static private final MinecraftClient CLIENT = MinecraftClient.getInstance();

	@Override
	public ActionResult interact(final PlayerEntity player, final World world, final Hand hand, final BlockPos pos,
			final Direction direction) {
		final PlayerInventory inventory = player.getInventory();

		final BlockState blockState = world.getBlockState(pos);
		final Block block = blockState.getBlock();

		// Check if it's a crop.
		if (!(block instanceof CropBlock))
			return ActionResult.PASS;

		// Check if we already have the appropriate item in hand
		final CropBlock cropBlock = (CropBlock) block;
		final Item seedItem = Item.fromBlock(cropBlock);
		final ItemStack mainHandStack = inventory.getMainHandStack();
		if (mainHandStack.getItem().equals(seedItem))
			return ActionResult.PASS;

		// Get all the appropriate seeds
		final ArrayList<ItemStack> seeds = new ArrayList<ItemStack>();
		for (final ItemStack stack : inventory.main) {
			if (stack.getItem().equals(seedItem))
				seeds.add(stack);
		}

		if (seeds.isEmpty())
			return ActionResult.PASS;
		ItemStackUtil.keepLowestStacks(seeds);

		if (seeds.isEmpty())
			return ActionResult.PASS;
		final ItemStack seed = seeds.get(0);
		Switch.switcheroo(player, seed);

		// Plant the seed!
		final BlockHitResult blockHitResult = (BlockHitResult) CLIENT.crosshairTarget;
		final ActionResult placeSeedResult = CLIENT.interactionManager.interactBlock(CLIENT.player, CLIENT.world, hand,
				blockHitResult);
		if (placeSeedResult.isAccepted() && placeSeedResult.shouldSwingHand())
			CLIENT.player.swingHand(hand);

		return ActionResult.PASS;
	}

}
