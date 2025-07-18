package com.natoboram.switcheroo;

import static net.fabricmc.api.EnvType.CLIENT;

import java.util.ArrayList;
import me.shedaniel.autoconfig.ConfigHolder;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.minecraft.block.BambooBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BrushableBlock;
import net.minecraft.block.CaveVinesBodyBlock;
import net.minecraft.block.CaveVinesHeadBlock;
import net.minecraft.block.CobwebBlock;
import net.minecraft.block.CropBlock;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.PlantBlock;
import net.minecraft.block.SugarCaneBlock;
import net.minecraft.block.VineBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.AxeItem;
import net.minecraft.item.BrushItem;
import net.minecraft.item.HoeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShearsItem;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Execute a switcheroo action when attacking a block. */
@Environment(value = CLIENT)
public class BlockSwitch implements AttackBlockCallback {

	private static final Logger LOGGER = LogManager.getLogger(Main.MOD_ID);
	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
	private final ConfigHolder<SwitcherooConfig> CONFIG_HOLDER;
	private final CropSwitch CROP_SWITCH;

	BlockSwitch(final ConfigHolder<SwitcherooConfig> holder) {
		this.CONFIG_HOLDER = holder;
		this.CROP_SWITCH = new CropSwitch(CONFIG_HOLDER);
	}

	@Override
	public ActionResult interact(
		final PlayerEntity player,
		final World world,
		final Hand hand,
		final BlockPos pos,
		final Direction direction
	) {
		final SwitcherooConfig config = CONFIG_HOLDER.getConfig();

		final BlockState blockState = world.getBlockState(pos);
		final Block block = blockState.getBlock();

		if (player.isCreative() || player.isSpectator() || player.isSneaking() || !config.enabled) {
			if (config.debug)
				LOGGER.info("Skipping interaction with block {}", block.getName().getString());

			return ActionResult.PASS;
		}

		// Blacklist some blocks
		if (isBlacklisted(block, config)) {
			if (config.debug)
				LOGGER.info("{} is blacklisted", block.getName().getString());

			return ActionResult.PASS;
		}

		// Cache enchantments
		final DynamicRegistryManager manager = world.getRegistryManager();
		final Registry<Enchantment> enchantments = manager.getOrThrow(RegistryKeys.ENCHANTMENT);
		final RegistryEntry<Enchantment> silkTouchEntry = enchantments.getEntry(Enchantments.SILK_TOUCH.getValue()).get();

		// Use CROP_SWITCH to handle crops
		if (world.getBlockState(pos).getBlock() instanceof CropBlock && config.enableCrop)
			return CROP_SWITCH.interact(player, world, hand, pos, direction);

		final ArrayList<ItemStack> tools = new ArrayList<ItemStack>();
		final PlayerInventory inventory = player.getInventory();

		if (block instanceof BrushableBlock) {
			// Use brush on suspicious blocks
			for (final ItemStack stack : inventory.getMainStacks())
				if (stack.getItem() instanceof BrushItem)
					tools.add(stack);
		} else if (block instanceof CropBlock) {
			// Use hoe on crops
			for (final ItemStack stack : inventory.getMainStacks())
				if (stack.getItem() instanceof HoeItem)
					tools.add(stack);
		} else {
			// Use shears on glow berries, cobwebs, leaves, plants and vines
			if (
				block instanceof CaveVinesBodyBlock
					|| block instanceof CaveVinesHeadBlock
					|| block instanceof CobwebBlock
					|| block instanceof LeavesBlock
					|| block instanceof PlantBlock
					|| block instanceof VineBlock
			)
				for (final ItemStack stack : inventory.getMainStacks())
					if (stack.getItem() instanceof ShearsItem)
						tools.add(stack);

			// Use sword on cobwebs and bamboo
			if (tools.isEmpty() && (block instanceof BambooBlock || block instanceof CobwebBlock))
				for (final ItemStack stack : inventory.getMainStacks())
					if (stack.isIn(ItemTags.SWORDS))
						tools.add(stack);

			// Get all effective tools from the inventory
			if (tools.isEmpty())
				for (final ItemStack stack : inventory.getMainStacks())
					if (stack.isSuitableFor(blockState) && !(stack.isIn(ItemTags.SWORDS)) && axeFilter(block, stack.getItem()))
						tools.add(stack);

			// If there's no effective tools, check for the mining speed
			if (tools.isEmpty())
				for (final ItemStack stack : inventory.getMainStacks())
					if (
						ItemStackUtil.getMiningSpeedMultiplier(stack, blockState, world) > 1.0F && !(stack.isIn(ItemTags.SWORDS))
							&& axeFilter(block, stack.getItem())
					)
						tools.add(stack);

			// Add Silk Touch
			if (tools.isEmpty() && preferSilkTouch(block, config))
				for (final ItemStack stack : inventory.getMainStacks())
					if (EnchantmentHelper.getLevel(silkTouchEntry, stack) > 0)
						tools.add(stack);
		}

		// Keep Silk Touch
		if (
			preferSilkTouch(block, config)
				&& tools.stream().anyMatch(tool -> EnchantmentHelper.getLevel(silkTouchEntry, tool) > 0)
		)
			tools.removeIf(tool -> EnchantmentHelper.getLevel(silkTouchEntry, tool) <= 0);

		// Filters enchanted items with low durability
		ItemStackUtil.removeDamagedEnchantedItems(tools, config);

		// Safety before launching streams
		if (tools.isEmpty()) {
			if (config.debug)
				LOGGER.info("No tools found");

			return ActionResult.PASS;
		}

		// Get best or worst tool
		if (CLIENT.options.sprintKey.isPressed() || config.alwaysFastest)
			ItemStackUtil.keepFastestTools(tools, blockState, world);
		else
			ItemStackUtil.keepSlowestTools(tools, blockState, world);

		final ItemStack mainHand = player.getMainHandStack();
		final double mainHandSpeed = ItemStackUtil.getMiningSpeedMultiplier(mainHand, blockState, world);

		// Stop if there's already a valid item in hand
		if (
			tools.stream()
				.anyMatch(
					stack -> mainHandSpeed == ItemStackUtil.getMiningSpeedMultiplier(stack, blockState, world)
						&& ItemStack.areItemsEqual(stack, mainHand)
				)
		) {
			if (config.debug)
				LOGGER.info("There's already a {} in hand", mainHand.getItem().getName().getString());

			return ActionResult.PASS;
		}

		// Get most damaged item
		ItemStackUtil.keepMostDamagedItems(tools);

		if (!tools.isEmpty())
			Switch.switcheroo(player, tools.get(0), config);

		return ActionResult.PASS;
	}

	/** Axes shouldn't be used on tall grass, sugar cane nor vines. */
	private boolean axeFilter(final Block block, final Item item) {
		return !((block instanceof PlantBlock || block instanceof SugarCaneBlock || block instanceof VineBlock)
			&& item instanceof AxeItem);
	}

	private boolean isBlacklisted(final Block block, final SwitcherooConfig config) {
		final Identifier id = Registries.BLOCK.getId(block);
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

	private boolean preferSilkTouch(final Block block, final SwitcherooConfig config) {
		final Identifier id = Registries.BLOCK.getId(block);
		final String[] blocks = config.prefer.silk_touch.split(" ");

		for (final String blockId : blocks) {
			switch (blockId.split(":").length) {
				case 1:
					if (id.toString().equals("minecraft:" + blockId))
						return true;
					break;

				case 2:
				default:
					if (id.toString().equals(blockId))
						return true;
					break;
			}
		}

		return false;
	}
}
