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
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class Main implements ModInitializer {

	private MinecraftClient client = MinecraftClient.getInstance();

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		System.out.println("Loaded Switcheroo!");

		AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {

			if (cancelSwitcheroo(player)) {
				return ActionResult.PASS;
			}

			if (world.getBlockState(pos).getBlock() instanceof CropBlock) {
				return onAttackCrop(player, world, hand, pos, direction);
			} else {
				return onAttackBlock(player, world, hand, pos, direction);
			}
		});

		AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {

			// Cancel if Spectator and on Shift
			if (player.isSpectator() || player.isSneaking()) {
				return ActionResult.PASS;
			}

			// Entity checks
			if (!entity.isLiving() || !entity.isAlive() || entity.isInvulnerable()) {
				return ActionResult.PASS;
			}

			return onAttackEntity(player, world, hand, entity, hitResult);

		});

	}

	/**
	 * Execute a switcheroo action when attacking a block.
	 */
	private ActionResult onAttackBlock(final PlayerEntity player, final World world, final Hand hand,
			final BlockPos pos, final Direction direction) {

		final BlockState block = world.getBlockState(pos);
		final ArrayList<ItemStack> tools = new ArrayList<ItemStack>();

		// Get all effective tools from the inventory
		player.inventory.main.forEach((item) -> {
			if (item.isEffectiveOn(block)) {
				tools.add(item);
			}
		});

		// If there's no effective tools, check for the mining speed.
		if (tools.isEmpty()) {
			player.inventory.main.forEach((item) -> {
				if (item.getMiningSpeedMultiplier(block) > 1.0F) {
					tools.add(item);
				}
			});
		}

		// Filters enchanted items with 1 durability
		removeDamagedEnchantedItems(tools);

		// Safety before launching streams
		if (tools.isEmpty()) {
			return ActionResult.PASS;
		}

		// Get best or worst tool
		if (this.client.options.keySprint.isPressed()) {
			final float max = tools.stream().max(Comparator.comparing(item -> item.getMiningSpeedMultiplier(block)))
					.get().getMiningSpeedMultiplier(block);
			tools.removeIf((item) -> max > item.getMiningSpeedMultiplier(block));
		} else {
			final float min = tools.stream().min(Comparator.comparing(item -> item.getMiningSpeedMultiplier(block)))
					.get().getMiningSpeedMultiplier(block);
			tools.removeIf((item) -> min < item.getMiningSpeedMultiplier(block));
		}

		// Get most damaged item
		keepMostDamagedItems(tools);

		if (tools.isEmpty()) {
			return ActionResult.PASS;
		}

		switcheroo(player, tools.get(0));
		return ActionResult.PASS;
	}

	/**
	 * Execute a switcheroo action when attacking crops.
	 */
	private ActionResult onAttackCrop(final PlayerEntity player, final World world, final Hand hand, final BlockPos pos,
			final Direction direction) {

		final BlockState blockState = world.getBlockState(pos);
		final Block block = blockState.getBlock();

		// Check if it's a crop.
		if (!(block instanceof CropBlock)) {
			return ActionResult.PASS;
		}

		// Check if we already have the appropriate item in hand
		final CropBlock cropBlock = (CropBlock) block;
		final Item seedItem = Item.fromBlock(cropBlock);
		final ItemStack mainHandStack = player.inventory.getMainHandStack();
		if (mainHandStack.getItem().equals(seedItem)) {
			return ActionResult.PASS;
		}

		// Get all the appropriate seeds
		final ArrayList<ItemStack> seeds = new ArrayList<ItemStack>();
		player.inventory.main.forEach((item) -> {
			if (item.getItem().equals(seedItem)) {
				seeds.add(item);
			}
		});

		if (seeds.isEmpty()) {
			return ActionResult.PASS;
		}
		keepLowestStacks(seeds);

		if (seeds.isEmpty()) {
			return ActionResult.PASS;
		}
		switcheroo(player, seeds.get(0));

		// Plant the seed!
		final MinecraftClient minecraftClient = this.client;
		final HitResult hitResult = minecraftClient.crosshairTarget;
		final Vec3d vec3d = new Vec3d(hitResult.getPos().x, hitResult.getPos().y - 1, hitResult.getPos().z);
		final BlockHitResult bhr = new BlockHitResult(vec3d, Direction.UP, pos.down(), true);
		minecraftClient.interactionManager.interactBlock(minecraftClient.player, minecraftClient.world, hand, bhr);

		return ActionResult.PASS;
	}

	/**
	 * Execute a switcheroo action when attacking an entity.
	 */
	private ActionResult onAttackEntity(final PlayerEntity player, final World world, final Hand hand,
			final Entity entity, /* Nullable */ final EntityHitResult hitResult) {

		final ArrayList<ItemStack> swords = new ArrayList<ItemStack>();

		// Get all swords
		player.inventory.main.forEach((item) -> {
			if (FabricToolTags.SWORDS.contains(item.getItem())) {
				swords.add(item);
			}
		});

		// Filters enchanted items with 1 durability
		removeDamagedEnchantedItems(swords);

		final LivingEntity livingEntity = (LivingEntity) entity;
		final EntityGroup entityGroup = livingEntity.getGroup();

		// Safety before launching streams
		if (swords.isEmpty()) {
			return ActionResult.PASS;
		}

		// Get the most damaging or least damaging
		if (this.client.options.keySprint.isPressed()) {
			final float max = getDamage(
					swords.stream().max(Comparator.comparing(item -> getDamage(item, entityGroup))).get(), entityGroup);
			swords.removeIf((item) -> max > getDamage(item, entityGroup));
		} else {
			final float min = getDamage(
					swords.stream().min(Comparator.comparing(item -> getDamage(item, entityGroup))).get(), entityGroup);
			swords.removeIf((item) -> min < getDamage(item, entityGroup));
		}

		// Get most damaged items
		keepMostDamagedItems(swords);

		if (swords.isEmpty()) {
			return ActionResult.PASS;
		}

		switcheroo(player, swords.get(0));
		return ActionResult.PASS;
	}

	/**
	 * Checks if the switcheroo action should be cancelled.
	 *
	 * @param player Player that's about to execute a switcheroo.
	 */
	private boolean cancelSwitcheroo(final PlayerEntity player) {
		return player.isSpectator() || player.isSneaking();
	}

	/**
	 * Gets the damage that would be done by an ItemStack to an EntityGroup.
	 */
	private float getDamage(final ItemStack stack, final EntityGroup entityGroup) {
		float damage = 0;

		// Stack Enchantments
		damage += EnchantmentHelper.getAttackDamage(stack, entityGroup);

		// Item Modifiers
		final Item item = stack.getItem();
		item.getAttributeModifiers(EquipmentSlot.MAINHAND).get(EntityAttributes.GENERIC_ATTACK_DAMAGE).stream()
				.mapToDouble(EntityAttributeModifier::getValue).sum();

		// Sword Attack Damage
		if (item instanceof SwordItem) {
			final SwordItem sword = (SwordItem) item;
			damage += sword.getAttackDamage();
		}

		return damage;
	}

	/**
	 * Removes enchanted items that have only one durability left from the
	 * switcheroo.
	 */
	private void removeDamagedEnchantedItems(final ArrayList<ItemStack> items) {
		items.removeIf((item) -> {
			return !item.getEnchantments().isEmpty() && item.getMaxDamage() - item.getDamage() <= 1;
		});
	}

	/**
	 * Only keep the most damaged items.
	 */
	private void keepMostDamagedItems(final ArrayList<ItemStack> items) {
		final float max = items.stream().max(Comparator.comparing(item -> item.getDamage())).get().getDamage();
		items.removeIf((item) -> max > item.getDamage());
	}

	/**
	 * Only keep the lowest stacks.
	 */
	private void keepLowestStacks(final ArrayList<ItemStack> items) {
		final float min = items.stream().min(Comparator.comparing(item -> item.getCount())).get().getDamage();
		items.removeIf((item) -> min < item.getCount());
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
		if (player.inventory.getMainHandStack().isItemEqualIgnoreDamage(item)) {
			return;
		}

		this.client.interactionManager.pickFromInventory(player.inventory.getSlotWithStack(item));
	}

}
