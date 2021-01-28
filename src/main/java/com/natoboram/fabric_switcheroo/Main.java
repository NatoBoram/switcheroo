package com.natoboram.fabric_switcheroo;

import java.util.ArrayList;
import java.util.Comparator;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MiningToolItem;
import net.minecraft.item.SwordItem;
import net.minecraft.item.TridentItem;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class Main implements ModInitializer {

	private final Boolean enableOnAttackCrop = false;
	private final MinecraftClient client = MinecraftClient.getInstance();

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		System.out.println("Loaded Switcheroo!");

		AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
			if (player.isCreative() || player.isSpectator() || player.isSneaking())
				return ActionResult.PASS;

			if (world.getBlockState(pos).getBlock() instanceof CropBlock && enableOnAttackCrop) {
				return onAttackCrop.interact(player, world, hand, pos, direction);
			} else {
				return onAttackBlock.interact(player, world, hand, pos, direction);
			}
		});

		AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
			if (player.isSpectator() || player.isSneaking() || !entity.isLiving() || !entity.isAlive()
					|| entity.isInvulnerable())
				return ActionResult.PASS;

			return onAttackEntity.interact(player, world, hand, entity, hitResult);
		});
	}

	/**
	 * Execute a switcheroo action when attacking a block.
	 */
	private final AttackBlockCallback onAttackBlock = (final PlayerEntity player, final World world, final Hand hand,
			final BlockPos pos, final Direction direction) -> {

		final BlockState block = world.getBlockState(pos);
		final ArrayList<ItemStack> tools = new ArrayList<ItemStack>();

		// Get all effective tools from the inventory
		player.inventory.main.forEach(item -> {
			if (item.isEffectiveOn(block))
				tools.add(item);
		});

		// If there's no effective tools, check for the mining speed but ignore swords.
		if (tools.isEmpty()) {
			player.inventory.main.forEach(item -> {
				if (item.getMiningSpeedMultiplier(block) > 1.0F && !item.getItem().isIn(FabricToolTags.SWORDS))
					tools.add(item);
			});
		}

		// Filters enchanted items with 1 durability
		removeDamagedEnchantedItems(tools);

		// Safety before launching streams
		if (tools.isEmpty())
			return ActionResult.PASS;

		// Get best or worst tool
		if (this.client.options.keySprint.isPressed()) {
			final float max = tools.stream().max(Comparator.comparing(item -> item.getMiningSpeedMultiplier(block)))
					.get().getMiningSpeedMultiplier(block);
			tools.removeIf(item -> max > item.getMiningSpeedMultiplier(block));
		} else {
			final float min = tools.stream().min(Comparator.comparing(item -> item.getMiningSpeedMultiplier(block)))
					.get().getMiningSpeedMultiplier(block);
			tools.removeIf(item -> min < item.getMiningSpeedMultiplier(block));
		}

		// Get most damaged item
		keepMostDamagedItems(tools);

		if (tools.isEmpty())
			return ActionResult.PASS;

		switcheroo(player, tools.get(0));
		return ActionResult.PASS;
	};

	/**
	 * Execute a switcheroo action when attacking crops.
	 */
	private final AttackBlockCallback onAttackCrop = (final PlayerEntity player, final World world, final Hand hand,
			final BlockPos pos, final Direction direction) -> {

		if (!enableOnAttackCrop)
			return ActionResult.PASS;

		final BlockState blockState = world.getBlockState(pos);
		final Block block = blockState.getBlock();

		// Check if it's a crop.
		if (!(block instanceof CropBlock))
			return ActionResult.PASS;

		// Check if we already have the appropriate item in hand
		final CropBlock cropBlock = (CropBlock) block;
		final Item seedItem = Item.fromBlock(cropBlock);
		final ItemStack mainHandStack = player.inventory.getMainHandStack();
		if (mainHandStack.getItem().equals(seedItem))
			return ActionResult.PASS;

		// Get all the appropriate seeds
		final ArrayList<ItemStack> seeds = new ArrayList<ItemStack>();
		player.inventory.main.forEach(item -> {
			if (item.getItem().equals(seedItem))
				seeds.add(item);
		});

		if (seeds.isEmpty())
			return ActionResult.PASS;
		keepLowestStacks(seeds);

		if (seeds.isEmpty())
			return ActionResult.PASS;
		final ItemStack seed = seeds.get(0);
		switcheroo(player, seed);

		// Plant the seed!
		BlockHitResult blockHitResult = (BlockHitResult) client.crosshairTarget;
		ActionResult placeSeedResult = this.client.interactionManager.interactBlock(client.player, client.world, hand,
				blockHitResult);
		if (placeSeedResult.isAccepted() && placeSeedResult.shouldSwingHand())
			client.player.swingHand(hand);

		return ActionResult.PASS;
	};

	/**
	 * Execute a switcheroo action when attacking an entity.
	 */
	private final AttackEntityCallback onAttackEntity = (final PlayerEntity player, final World world, final Hand hand,
			final Entity entity, /* Nullable */ final EntityHitResult hitResult) -> {

		final ArrayList<ItemStack> weapons = new ArrayList<ItemStack>();

		// Get all weapons
		player.inventory.main.forEach(stack -> {
			final Item item = stack.getItem();

			if (item instanceof SwordItem || item instanceof AxeItem || item instanceof TridentItem)
				weapons.add(stack);
		});

		// In desperate situations, use tools
		final Boolean useTools = weapons.isEmpty();
		if (useTools)
			player.inventory.main.forEach(stack -> {
				final Item item = stack.getItem();

				if (item instanceof MiningToolItem)
					weapons.add(stack);
			});

		// Filters enchanted items with 1 durability
		removeDamagedEnchantedItems(weapons);

		final LivingEntity livingEntity = (LivingEntity) entity;
		final EntityGroup entityGroup = livingEntity.getGroup();

		// Safety before launching streams
		if (weapons.isEmpty())
			return ActionResult.PASS;

		// Get the most damaging or least damaging
		if (this.client.options.keySprint.isPressed() || useTools) {
			final double max = getDamage(
					weapons.stream().max(Comparator.comparing(item -> getDamage(item, entityGroup))).get(),
					entityGroup);
			weapons.removeIf(item -> max > getDamage(item, entityGroup));
		} else {
			final double min = getDamage(
					weapons.stream().min(Comparator.comparing(item -> getDamage(item, entityGroup))).get(),
					entityGroup);
			weapons.removeIf(item -> min < getDamage(item, entityGroup));
		}

		// Get most damaged items
		keepMostDamagedItems(weapons);

		if (weapons.isEmpty())
			return ActionResult.PASS;

		switcheroo(player, weapons.get(0));
		return ActionResult.PASS;
	};

	/**
	 * Gets the damage that would be done by an ItemStack to an EntityGroup.
	 */
	private double getDamage(final ItemStack stack, final EntityGroup entityGroup) {

		// Player damage
		double damage = client.player.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);

		// Stack enchantments
		damage += EnchantmentHelper.getAttackDamage(stack, entityGroup);

		// Item modifiers
		final Item item = stack.getItem();
		damage += item.getAttributeModifiers(EquipmentSlot.MAINHAND).get(EntityAttributes.GENERIC_ATTACK_DAMAGE)
				.stream().mapToDouble(EntityAttributeModifier::getValue).sum();

		return damage;
	}

	/**
	 * Removes enchanted items that have only one durability left from the
	 * switcheroo.
	 */
	private void removeDamagedEnchantedItems(final ArrayList<ItemStack> items) {
		items.removeIf(item -> {
			return !item.getEnchantments().isEmpty() && item.getMaxDamage() - item.getDamage() <= 1;
		});
	}

	/**
	 * Only keep the most damaged items.
	 */
	private void keepMostDamagedItems(final ArrayList<ItemStack> items) {
		final float max = items.stream().max(Comparator.comparing(item -> item.getDamage())).get().getDamage();
		items.removeIf(item -> max > item.getDamage());
	}

	/**
	 * Only keep the lowest stacks.
	 */
	private void keepLowestStacks(final ArrayList<ItemStack> items) {
		final float min = items.stream().min(Comparator.comparing(item -> item.getCount())).get().getCount();
		items.removeIf(item -> min < item.getCount());
	}

	/**
	 * Perform the actual switcheroo.
	 *
	 * @param player PlayerEntity that's about to execute the switcheroo.
	 * @param item   Item that should be but in its hand.
	 */
	private void switcheroo(final PlayerEntity player, final ItemStack item) {

		// Only works in single player because it actually edits the world.
		// Collections.swap(player.inventory.main, player.inventory.main.indexOf(item),
		// player.inventory.main.indexOf(player.getStackInHand(Hand.MAIN_HAND)));

		// Don't send useless packets
		if (player.inventory.getMainHandStack().isItemEqualIgnoreDamage(item))
			return;

		this.client.interactionManager.pickFromInventory(player.inventory.getSlotWithStack(item));
	}
}
