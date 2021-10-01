package com.natoboram.switcheroo;

import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import me.shedaniel.autoconfig.ConfigHolder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.PlantBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.AxeItem;
import net.minecraft.item.HoeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShearsItem;
import net.minecraft.item.SwordItem;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

/**
 * Execute a switcheroo action when attacking a block.
 */
@Environment(EnvType.CLIENT)
public class BlockSwitch implements AttackBlockCallback {

	private final ConfigHolder<SwitcherooConfig> CONFIG_HOLDER;
	private final CropSwitch CROP_SWITCH;
	private final static Logger LOGGER = LogManager.getLogger(Main.MOD_ID);
	static private final MinecraftClient CLIENT = MinecraftClient.getInstance();

	BlockSwitch(final ConfigHolder<SwitcherooConfig> holder) {
		this.CONFIG_HOLDER = holder;
		this.CROP_SWITCH = new CropSwitch();
	}

	@Override
	public ActionResult interact(final PlayerEntity player, final World world, final Hand hand, final BlockPos pos,
			final Direction direction) {
		if (player.isCreative() || player.isSpectator() || player.isSneaking())
			return ActionResult.PASS;

		final BlockState blockState = world.getBlockState(pos);
		final Block block = blockState.getBlock();
		final SwitcherooConfig config = CONFIG_HOLDER.getConfig();

		// Blacklist some blocks
		if (isBlacklisted(block, config)) {
			if (config.debug)
				LOGGER.debug(block.toString() + " is blacklisted");
			return ActionResult.PASS;
		}

		// Use CROP_SWITCH to handle crops
		if (world.getBlockState(pos).getBlock() instanceof CropBlock && config.enableCrop) {
			return CROP_SWITCH.interact(player, world, hand, pos, direction);
		}

		final ArrayList<ItemStack> tools = new ArrayList<ItemStack>();
		final PlayerInventory inventory = player.getInventory();

		// Use hoe on crops
		if (block instanceof CropBlock) {
			for (final ItemStack stack : inventory.main)
				if (stack.getItem() instanceof HoeItem)
					tools.add(stack);
		} else {

			// Use shears on leaves and plants
			if (block instanceof LeavesBlock || block instanceof PlantBlock)
				for (final ItemStack stack : inventory.main)
					if (stack.getItem() instanceof ShearsItem)
						tools.add(stack);

			// Get all effective tools from the inventory but ignore swords.
			if (tools.isEmpty())
				for (final ItemStack stack : inventory.main)
					if (stack.isSuitableFor(blockState) && !(stack.getItem() instanceof SwordItem)
							&& ignoreAxeOnPlants(block, stack.getItem()))
						tools.add(stack);

			// If there's no effective tools, check for the mining speed but ignore swords.
			if (tools.isEmpty())
				for (final ItemStack stack : inventory.main)
					if (stack.getMiningSpeedMultiplier(blockState) > 1.0F && !(stack.getItem() instanceof SwordItem)
							&& ignoreAxeOnPlants(block, stack.getItem()))
						tools.add(stack);
		}

		// Filters enchanted items with 1 durability
		ItemStackUtil.removeDamagedEnchantedItems(tools);

		// Safety before launching streams
		if (tools.isEmpty())
			return ActionResult.PASS;

		// Get best or worst tool
		if (CLIENT.options.keySprint.isPressed()) {
			ItemStackUtil.keepFastestTools(tools, blockState);
		} else {
			ItemStackUtil.keepSlowestTools(tools, blockState);
		}

		// Stop if there's already a valid item in hand
		if (tools.stream().anyMatch(stack -> stack.getItem().equals(CLIENT.player.getMainHandStack().getItem())))
			return ActionResult.PASS;

		// Get most damaged item
		ItemStackUtil.keepMostDamagedItems(tools);

		if (!tools.isEmpty())
			Switch.switcheroo(player, tools.get(0));
		return ActionResult.PASS;
	}

	private boolean ignoreAxeOnPlants(final Block block, final Item item) {
		return block instanceof PlantBlock ^ item instanceof AxeItem
				|| !(block instanceof PlantBlock) && !(item instanceof AxeItem);
	}

	private boolean isBlacklisted(final Block block, final SwitcherooConfig config) {
		final Identifier id = Registry.BLOCK.getId(block);
		final String[] blacklist = config.blacklist.blocks.split(" ");

		for (final String blacklisted : blacklist) {
			switch (blacklisted.split(":").length) {
			case 1:
				if (id.toString().equals("minecraft:" + blacklisted))
					return true;
				break;
			case 2:
			default:
				if (id.toString().equals(blacklisted))
					return true;
				break;
			}
		}

		return false;
	}
}
