package com.natoboram.fabric_switcheroo;

import java.util.ArrayList;
import java.util.Comparator;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.tools.FabricToolTags;
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
	 * Checks if the switcheroo action should be cancelled.
	 * 
	 * @param player Player that's about to execute a switcheroo.
	 */
	private boolean cancelSwitcheroo(PlayerEntity player) {
		return player.isSpectator() || player.isSneaking();
	}

	/**
	 * Removes enchanted items that have only one durability left from the
	 * switcheroo.
	 */
	private void preserveEnchantedItems(ArrayList<ItemStack> items) {
		items.removeIf((item) -> {
			return !item.getEnchantments().isEmpty() && item.getMaxDamage() - item.getDamage() <= 1;
		});
	}

	/**
	 * Only keep the most damaged items.
	 */
	private void mostDamagedItems(ArrayList<ItemStack> items) {
		float max = items.stream().max(Comparator.comparing(item -> item.getDamage())).get().getDamage();
		items.removeIf((item) -> max > item.getDamage());
	}

	/**
	 * Perform the actual switcheroo.
	 * 
	 * @param player PlayerEntity that's about to execute the switcheroo.
	 * @param item   Item that should be but in its hand.
	 */
	private void switcheroo(PlayerEntity player, ItemStack item) {

		// Only works in single player because it actually edits the world.
		// Collections.swap(player.inventory.main, player.inventory.main.indexOf(item),
		// player.inventory.main.indexOf(player.getStackInHand(Hand.MAIN_HAND)));

		// Don't send useless packets
		if (player.inventory.getMainHandStack().isItemEqualIgnoreDamage(item)) {
			return;
		}

		MinecraftClient.getInstance().interactionManager.pickFromInventory(player.inventory.getSlotWithStack(item));
	}

	/**
	 * Gets the damage that would be done by an ItemStack to an EntityGroup.
	 */
	private float getDamage(ItemStack stack, EntityGroup entityGroup) {
		float damage = 0;

		// Stack Enchantments
		damage += EnchantmentHelper.getAttackDamage(stack, entityGroup);

		// Item Modifiers
		Item item = stack.getItem();
		item.getModifiers(EquipmentSlot.MAINHAND).get(EntityAttributes.ATTACK_DAMAGE.getId()).stream()
				.mapToDouble(EntityAttributeModifier::getAmount).sum();

		// Sword Attack Damage
		if (item instanceof SwordItem) {
			SwordItem sword = (SwordItem) item;
			damage += sword.getAttackDamage();
		}

		return damage;
	}

	/**
	 * Only keep the lowest stacks.
	 */
	private void keepLowestStacks(ArrayList<ItemStack> items) {
		float min = items.stream().min(Comparator.comparing(item -> item.getCount())).get().getDamage();
		items.removeIf((item) -> min < item.getCount());
	}

	/**
	 * Execute a switcheroo action when attacking crops.
	 */
	private ActionResult onAttackCrop(PlayerEntity player, World world, Hand hand, BlockPos pos, Direction direction) {

		BlockState blockState = world.getBlockState(pos);
		Block block = blockState.getBlock();

		// Check if it's a crop.
		if (!(block instanceof CropBlock)) {
			return ActionResult.PASS;
		}

		// Check if we already have the appropriate item in hand
		CropBlock cropBlock = (CropBlock) block;
		ItemStack newSeedStack = cropBlock.getPickStack(world, pos, blockState);
		ItemStack mainHandStack = player.inventory.getMainHandStack();
		if (mainHandStack.isItemEqualIgnoreDamage(newSeedStack)) {
			return ActionResult.PASS;
		}

		// Get all the appropriate seeds
		ArrayList<ItemStack> seeds = new ArrayList<ItemStack>();
		player.inventory.main.forEach((item) -> {
			if (item.getItem() == newSeedStack.getItem()) {
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
		switcheroo(player, newSeedStack);

		// Plant the seed!
		MinecraftClient minecraftClient = MinecraftClient.getInstance();
		HitResult hitResult = minecraftClient.hitResult;
		Vec3d vec3d = new Vec3d(hitResult.getPos().x, hitResult.getPos().y - 1, hitResult.getPos().z);
		BlockHitResult bhr = new BlockHitResult(vec3d, Direction.UP, pos.down(), true);
		minecraftClient.interactionManager.interactBlock(minecraftClient.player, minecraftClient.world, hand, bhr);

		return ActionResult.PASS;
	}

	/**
	 * Execute a switcheroo action when attacking a block.
	 */
	private ActionResult onAttackBlock(PlayerEntity player, World world, Hand hand, BlockPos pos, Direction direction) {

		BlockState block = world.getBlockState(pos);
		ArrayList<ItemStack> tools = new ArrayList<ItemStack>();

		// Get all effective tools from the inventory
		player.inventory.main.forEach((item) -> {
			if (item.isEffectiveOn(block)) {
				tools.add(item);
			}
		});

		// If there's no effective tools, check for the mining speed.
		if (tools.isEmpty()) {
			player.inventory.main.forEach((item) -> {
				if (item.getMiningSpeed(block) > 1.0F) {
					tools.add(item);
				}
			});
		}

		// Filters enchanted items with 1 durability
		preserveEnchantedItems(tools);

		// Safety before launching streams
		if (tools.isEmpty()) {
			return ActionResult.PASS;
		}

		// Get best or worst tool
		if (MinecraftClient.getInstance().options.keySprint.isPressed()) {
			float max = tools.stream().max(Comparator.comparing(item -> item.getMiningSpeed(block))).get()
					.getMiningSpeed(block);
			tools.removeIf((item) -> max > item.getMiningSpeed(block));
		} else {
			float min = tools.stream().min(Comparator.comparing(item -> item.getMiningSpeed(block))).get()
					.getMiningSpeed(block);
			tools.removeIf((item) -> min < item.getMiningSpeed(block));
		}

		// Get most damaged item
		mostDamagedItems(tools);

		if (tools.isEmpty()) {
			return ActionResult.PASS;
		}

		switcheroo(player, tools.get(0));
		return ActionResult.PASS;
	}

	/**
	 * Execute a switcheroo action when attacking an entity.
	 */
	private ActionResult onAttackEntity(PlayerEntity player, World world, Hand hand, Entity entity,
			/* Nullable */ EntityHitResult hitResult) {

		ArrayList<ItemStack> swords = new ArrayList<ItemStack>();

		// Get all swords
		player.inventory.main.forEach((item) -> {
			if (FabricToolTags.SWORDS.contains(item.getItem())) {
				swords.add(item);
			}
		});

		// Filters enchanted items with 1 durability
		preserveEnchantedItems(swords);

		LivingEntity livingEntity = (LivingEntity) entity;
		EntityGroup entityGroup = livingEntity.getGroup();

		// Safety before launching streams
		if (swords.isEmpty()) {
			return ActionResult.PASS;
		}

		// Get the most damaging or least damaging
		if (MinecraftClient.getInstance().options.keySprint.isPressed()) {
			float max = getDamage(swords.stream().max(Comparator.comparing(item -> getDamage(item, entityGroup))).get(),
					entityGroup);
			swords.removeIf((item) -> max > getDamage(item, entityGroup));
		} else {
			float min = getDamage(swords.stream().min(Comparator.comparing(item -> getDamage(item, entityGroup))).get(),
					entityGroup);
			swords.removeIf((item) -> min < getDamage(item, entityGroup));
		}

		// Get most damaged items
		mostDamagedItems(swords);

		if (swords.isEmpty()) {
			return ActionResult.PASS;
		}

		switcheroo(player, swords.get(0));
		return ActionResult.PASS;
	}
}
