package com.natoboram.fabric_switcheroo;

import java.util.ArrayList;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.tools.FabricToolTags;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

public class Main implements ModInitializer {
	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		System.out.println("Loaded Switcheroo!");

		AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {

			// Cancel if Spectator and on Shift
			if (cancelSwitcheroo(player)) {
				return ActionResult.PASS;
			}

			BlockState block = world.getBlockState(pos);
			ArrayList<ItemStack> tools = new ArrayList<ItemStack>();

			// Get all effective tools from the inventory
			player.inventory.main.forEach((item) -> {
				if (item.isEffectiveOn(block)) {
					tools.add(item);
				}
			});

			// Filters enchanted items with 1 durability
			preserveEnchantedItems(tools);

			// Get best or worst tool
			if (MinecraftClient.getInstance().options.keySprint.isPressed()) {
				float miningSpeed = 0;
				tools.forEach((item) -> {
					miningSpeed = Math.max(miningSpeed, item.getMiningSpeed(block));
				});
				tools.removeIf((item) -> {
					return miningSpeed > item.getMiningSpeed(block);
				});
			} else {
				float miningSpeed = Float.MAX_VALUE;
				tools.forEach((item) -> {
					miningSpeed = Math.min(miningSpeed, item.getMiningSpeed(block));
				});
				tools.removeIf((item) -> {
					return miningSpeed < item.getMiningSpeed(block);
				});
			}

			// Get most damaged item
			mostDamagedItems(tools);

			if (tools.isEmpty()) {
				return ActionResult.PASS;
			}

			switcheroo(player, tools.get(0));

			return ActionResult.PASS;
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

			// Get the most damaging or least damaging
			if (MinecraftClient.getInstance().options.keySprint.isPressed()) {
				float attackDamage = 0;
				swords.forEach((item) -> {
					attackDamage = Math.max(attackDamage, EnchantmentHelper.getAttackDamage(item, entityGroup));
				});
				swords.removeIf((item) -> {
					return attackDamage > EnchantmentHelper.getAttackDamage(item, entityGroup);
				});
			} else {
				float attackDamage = Float.MAX_VALUE;
				swords.forEach((item) -> {
					attackDamage = Math.min(attackDamage, EnchantmentHelper.getAttackDamage(item, entityGroup));
				});
				swords.removeIf((item) -> {
					return attackDamage < EnchantmentHelper.getAttackDamage(item, entityGroup);
				});
			}

			// Get most damaged items
			mostDamagedItems(swords);

			if (swords.isEmpty()) {
				return ActionResult.PASS;
			}

			switcheroo(player, swords.get(0));

			return ActionResult.PASS;
		});

	}

	private boolean cancelSwitcheroo(PlayerEntity player) {
		return player.isSpectator() || player.isSneaking();
	}

	private void preserveEnchantedItems(ArrayList<ItemStack> items) {
		items.removeIf((item) -> {
			return !item.getEnchantments().isEmpty() && item.getMaxDamage() - item.getDamage() <= 1;
		});
	}

	private void mostDamagedItems(ArrayList<ItemStack> items) {
		float damage = 0;
		items.forEach((item) -> {
			damage = Math.max(damage, item.getDamage());
		});
		items.removeIf((item) -> {
			return damage > item.getDamage();
		});
	}

	private void switcheroo(PlayerEntity player, ItemStack item) {
		ItemStack held = player.getStackInHand(Hand.MAIN_HAND).copy();
		int index = player.inventory.main.indexOf(item);

		player.setStackInHand(Hand.MAIN_HAND, item);
		player.inventory.main.set(index, held);
	}
}
