package com.natoboram.switcheroo;

import java.util.ArrayList;
import java.util.Comparator;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;

@Environment(EnvType.CLIENT)
public class ItemStackUtil {

	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();

	public static double getAttackDamage(final ItemStack stack, final EntityGroup entityGroup) {

		// Player damage
		double damage = CLIENT.player.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);

		// Stack enchantments
		damage += EnchantmentHelper.getAttackDamage(stack, entityGroup);

		// Stack attack damage
		damage += stack.getAttributeModifiers(EquipmentSlot.MAINHAND).get(EntityAttributes.GENERIC_ATTACK_DAMAGE)
				.stream().mapToDouble(EntityAttributeModifier::getValue).sum();

		return damage;
	}

	public static double getAttackSpeed(final ItemStack stack) {
		return 4 + stack.getAttributeModifiers(EquipmentSlot.MAINHAND).get(EntityAttributes.GENERIC_ATTACK_SPEED)
				.stream().mapToDouble(EntityAttributeModifier::getValue).sum();
	}

	public static double getDps(final ItemStack stack, final EntityGroup entityGroup) {
		return getAttackDamage(stack, entityGroup) * getAttackSpeed(stack);
	}

	public static double getMaxDps(final ArrayList<ItemStack> weapons, final EntityGroup entityGroup) {
		return ItemStackUtil.getDps(
				weapons.stream().max(Comparator.comparing(item -> ItemStackUtil.getDps(item, entityGroup))).get(),
				entityGroup);
	}

	public static void keepMostDps(final ArrayList<ItemStack> weapons, final EntityGroup entityGroup,
			@Nullable final Double maxDps) {
		final double dps = maxDps == null ? getMaxDps(weapons, entityGroup) : maxDps.doubleValue();
		weapons.removeIf(stack -> dps > ItemStackUtil.getDps(stack, entityGroup));
	}

	public static void keepFastestTools(final ArrayList<ItemStack> tools, final BlockState blockState) {
		final float max = tools.stream().max(Comparator.comparing(item -> item.getMiningSpeedMultiplier(blockState)))
				.get().getMiningSpeedMultiplier(blockState);
		tools.removeIf(item -> max > item.getMiningSpeedMultiplier(blockState));
	}

	public static void keepSlowestTools(final ArrayList<ItemStack> tools, final BlockState blockState) {
		final float min = tools.stream().min(Comparator.comparing(item -> item.getMiningSpeedMultiplier(blockState)))
				.get().getMiningSpeedMultiplier(blockState);
		tools.removeIf(item -> min < item.getMiningSpeedMultiplier(blockState));
	}

	/**
	 * Removes enchanted items that have only one durability left.
	 */
	public static void removeDamagedEnchantedItems(final ArrayList<ItemStack> items) {
		items.removeIf(item -> {
			return !item.getEnchantments().isEmpty() && item.getMaxDamage() - item.getDamage() <= 1;
		});
	}

	public static void keepMostDamagedItems(final ArrayList<ItemStack> items) {
		final float max = items.stream().max(Comparator.comparing(item -> item.getDamage())).get().getDamage();
		items.removeIf(item -> max > item.getDamage());
	}

	public static void keepLowestStacks(final ArrayList<ItemStack> items) {
		final float min = items.stream().min(Comparator.comparing(item -> item.getCount())).get().getCount();
		items.removeIf(item -> min < item.getCount());
	}

}
