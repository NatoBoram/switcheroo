package com.natoboram.switcheroo;

import java.util.ArrayList;
import java.util.Comparator;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
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

	public static double getMaxAttackDamage(final ArrayList<ItemStack> weapons, final EntityGroup entityGroup) {
		return getAttackDamage(
				weapons.stream().max(Comparator.comparing(item -> getAttackDamage(item, entityGroup))).get(),
				entityGroup);
	}

	public static boolean keepMostAttackDamage(final ArrayList<ItemStack> weapons, final EntityGroup entityGroup,
			@Nullable final Double maxAd) {
		final double max = maxAd == null ? getMaxAttackDamage(weapons, entityGroup) : maxAd.doubleValue();
		return weapons.removeIf(stack -> max > getAttackDamage(stack, entityGroup));
	}

	public static double getAttackSpeed(final ItemStack stack) {
		return 4 + stack.getAttributeModifiers(EquipmentSlot.MAINHAND).get(EntityAttributes.GENERIC_ATTACK_SPEED)
				.stream().mapToDouble(EntityAttributeModifier::getValue).sum();
	}

	public static double getDps(final ItemStack stack, final EntityGroup entityGroup) {
		return getAttackDamage(stack, entityGroup) * getAttackSpeed(stack);
	}

	public static double getMaxDps(final ArrayList<ItemStack> weapons, final EntityGroup entityGroup) {
		return getDps(weapons.stream().max(Comparator.comparing(item -> getDps(item, entityGroup))).get(), entityGroup);
	}

	public static boolean keepMostDps(final ArrayList<ItemStack> weapons, final EntityGroup entityGroup,
			@Nullable final Double maxDps) {
		final double max = maxDps == null ? getMaxDps(weapons, entityGroup) : maxDps.doubleValue();
		return weapons.removeIf(stack -> max > getDps(stack, entityGroup));
	}

	public static boolean keepFastestTools(final ArrayList<ItemStack> tools, final BlockState blockState) {
		final double max = getMiningSpeedMultiplier(
				tools.stream().max(Comparator.comparing(item -> getMiningSpeedMultiplier(item, blockState))).get(),
				blockState);

		return tools.removeIf(item -> max > getMiningSpeedMultiplier(item, blockState));
	}

	public static boolean keepSlowestTools(final ArrayList<ItemStack> tools, final BlockState blockState) {
		final double min = getMiningSpeedMultiplier(
				tools.stream().min(Comparator.comparing(item -> getMiningSpeedMultiplier(item, blockState))).get(),
				blockState);

		return tools.removeIf(item -> min < getMiningSpeedMultiplier(item, blockState));
	}

	/**
	 * Wrapper for {@link ItemStack#getMiningSpeedMultiplier} that takes into
	 * account efficiency levels.
	 *
	 * @see <a href= "https://minecraft.fandom.com/wiki/Efficiency">Efficiency</a>
	 */
	public static double getMiningSpeedMultiplier(final ItemStack tool, final BlockState blockState) {
		final float multiplier = tool.getMiningSpeedMultiplier(blockState);
		if (tool.isSuitableFor(blockState)) {
			final int level = EnchantmentHelper.getLevel(Enchantments.EFFICIENCY, tool);
			return multiplier + (level == 0 ? 0 : 1 + Math.pow(level, 2));
		}
		return multiplier;
	}

	/** Removes enchanted items that have only 5 durability left. */
	public static boolean removeDamagedEnchantedItems(final ArrayList<ItemStack> items, final SwitcherooConfig config) {
		return items.removeIf(
				item -> item.hasEnchantments() && item.getMaxDamage() - item.getDamage() <= config.minDurability);
	}

	public static boolean keepMostDamagedItems(final ArrayList<ItemStack> items) {
		final float max = items.stream().max(Comparator.comparing(item -> item.getDamage())).get().getDamage();
		return items.removeIf(item -> max > item.getDamage());
	}

	public static boolean keepLowestStacks(final ArrayList<ItemStack> items) {
		final float min = items.stream().min(Comparator.comparing(item -> item.getCount())).get().getCount();
		return items.removeIf(item -> min < item.getCount());
	}
}
