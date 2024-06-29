package com.natoboram.switcheroo;

import static net.fabricmc.api.EnvType.CLIENT;
import static net.minecraft.component.DataComponentTypes.ATTRIBUTE_MODIFIERS;
import static net.minecraft.enchantment.Enchantments.EFFICIENCY;
import static net.minecraft.entity.attribute.EntityAttributes.GENERIC_ATTACK_DAMAGE;
import static net.minecraft.item.Item.BASE_ATTACK_DAMAGE_MODIFIER_ID;
import static net.minecraft.item.Item.BASE_ATTACK_SPEED_MODIFIER_ID;
import static net.minecraft.registry.RegistryKeys.ENCHANTMENT;

import java.util.ArrayList;
import java.util.Comparator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.World;

@Environment(CLIENT)
public class ItemStackUtil {

	private static final Logger LOGGER = LogManager.getLogger(Main.MOD_ID);
	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();

	/**
	 * Calculates the attack damage of the current player towards a specific entity
	 * using a specific item stack.
	 */
	public static double getAttackDamage(final ItemStack stack, final Entity entity, final World world) {

		LOGGER.info("Item stack: " + stack.getName());

		// Player damage
		double damage = CLIENT.player.getAttributeValue(GENERIC_ATTACK_DAMAGE);
		LOGGER.info("Player damage: " + damage);

		// Stack damage
		final Item item = stack.getItem();
		final AttributeModifiersComponent component = item.getComponents().get(ATTRIBUTE_MODIFIERS);

		for (final AttributeModifiersComponent.Entry entry : component.modifiers()) {
			final EntityAttributeModifier modifier = entry.modifier();
			LOGGER.info("Modifier: " + modifier.id() + " " + modifier.value());

			if (modifier.idMatches(BASE_ATTACK_DAMAGE_MODIFIER_ID))
				damage += modifier.value();
		}

		// Enchantment damage
		// damage += EnchantmentHelper.getDamage(world, stack, entity, damageSource,
		// (float) damage);
		// damage += EnchantmentHelper.getAttackDamage(item, entity);

		LOGGER.info("Total damage: " + damage);

		return damage;
	}

	/**
	 * Gets the maximum attack damage the current player can deal to a specific
	 * entity using a list of weapons.
	 */
	public static double getMaxAttackDamage(final ArrayList<ItemStack> weapons, final Entity entity, final World world) {
		return getAttackDamage(
				weapons.stream().max(Comparator.comparing(item -> getAttackDamage(item, entity, world))).get(),
				entity, world);
	}

	/**
	 * Removes weapons that deal less damage than the maximum attack damage the
	 * player can deal to a specific entity.
	 */
	public static boolean keepMostAttackDamage(final ArrayList<ItemStack> weapons, final Entity entity,
			@Nullable final Double maxAd, final World world) {
		final double max = maxAd == null ? getMaxAttackDamage(weapons, entity, world) : maxAd.doubleValue();
		return weapons.removeIf(stack -> max > getAttackDamage(stack, entity, world));
	}

	/**
	 * Calculates the attack speed of a specific item stack.
	 */
	public static double getAttackSpeed(final ItemStack stack) {

		double speed = 0F;

		final Item item = stack.getItem();
		final AttributeModifiersComponent component = item.getComponents().get(ATTRIBUTE_MODIFIERS);

		for (final AttributeModifiersComponent.Entry entry : component.modifiers()) {
			final EntityAttributeModifier modifier = entry.modifier();

			if (modifier.idMatches(BASE_ATTACK_SPEED_MODIFIER_ID))
				speed += modifier.value();
		}

		return speed;
	}

	/**
	 * Calculates the damage per seconds a player can deal using specific item stack
	 * towards a specific entity.
	 */
	public static double getDps(final ItemStack stack, final Entity entity, final World world) {
		return getAttackDamage(stack, entity, world) * getAttackSpeed(stack);
	}

	/**
	 * Gets the maximum damage per seconds a player can deal to a specific entity
	 */
	public static double getMaxDps(final ArrayList<ItemStack> weapons, final Entity entity, final World world) {
		return getDps(weapons.stream().max(Comparator.comparing(item -> getDps(item, entity, world))).get(), entity, world);
	}

	/**
	 * Removes weapons that deal less damage per seconds than the maximum damage per
	 * seconds the player can deal to a specific entity.
	 */
	public static boolean keepMostDps(final ArrayList<ItemStack> weapons, final Entity entityGroup,
			@Nullable final Double maxDps, final World world) {
		final double max = maxDps == null ? getMaxDps(weapons, entityGroup, world) : maxDps.doubleValue();
		return weapons.removeIf(stack -> max > getDps(stack, entityGroup, world));
	}

	/**
	 * Removes tools that have a lower mining speed than the fastest tool in the
	 * inventory.
	 */
	public static boolean keepFastestTools(final ArrayList<ItemStack> tools, final BlockState blockState,
			final World world) {
		final double max = getMiningSpeedMultiplier(
				tools.stream().max(Comparator.comparing(item -> getMiningSpeedMultiplier(item, blockState, world))).get(),
				blockState, world);

		return tools.removeIf(item -> max > getMiningSpeedMultiplier(item, blockState, world));
	}

	/**
	 * Removes tools that have a higher mining speed than the slowest tool in the
	 * inventory.
	 */
	public static boolean keepSlowestTools(final ArrayList<ItemStack> tools, final BlockState blockState,
			final World world) {
		final double min = getMiningSpeedMultiplier(
				tools.stream().min(Comparator.comparing(item -> getMiningSpeedMultiplier(item, blockState, world))).get(),
				blockState, world);

		return tools.removeIf(item -> min < getMiningSpeedMultiplier(item, blockState, world));
	}

	/**
	 * Wrapper for {@link ItemStack#getMiningSpeedMultiplier} that takes into
	 * account efficiency levels.
	 *
	 * @see <a href= "https://minecraft.fandom.com/wiki/Efficiency">Efficiency</a>
	 */
	public static double getMiningSpeedMultiplier(final ItemStack tool, final BlockState blockState, final World world) {
		final DynamicRegistryManager manager = world.getRegistryManager();
		final Registry<Enchantment> enchantments = manager.get(ENCHANTMENT);
		final RegistryEntry<Enchantment> efficiency = enchantments.getEntry(EFFICIENCY).get();

		final float multiplier = tool.getMiningSpeedMultiplier(blockState);
		if (tool.isSuitableFor(blockState)) {
			final int level = EnchantmentHelper.getLevel(efficiency, tool);
			return multiplier + (level == 0 ? 0 : 1 + Math.pow(level, 2));
		}

		return multiplier;
	}

	/** Removes enchanted items that have only 5 durability left. */
	public static boolean removeDamagedEnchantedItems(final ArrayList<ItemStack> items, final SwitcherooConfig config) {
		return items.removeIf(
				item -> item.hasEnchantments() && item.getMaxDamage() - item.getDamage() <= config.minDurability);
	}

	/** Removes items that have less durability than the most damaged item. */
	public static boolean keepMostDamagedItems(final ArrayList<ItemStack> items) {
		final float max = items.stream().max(Comparator.comparing(item -> item.getDamage())).get().getDamage();
		return items.removeIf(item -> max > item.getDamage());
	}

	/** Removes items that have a smaller stack size than the lowest stack size. */
	public static boolean keepLowestStacks(final ArrayList<ItemStack> items) {
		final float min = items.stream().min(Comparator.comparing(item -> item.getCount())).get().getCount();
		return items.removeIf(item -> min < item.getCount());
	}
}
