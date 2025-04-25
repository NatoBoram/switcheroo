package com.natoboram.switcheroo;

import static net.fabricmc.api.EnvType.CLIENT;
import static net.minecraft.component.DataComponentTypes.ATTRIBUTE_MODIFIERS;
import static net.minecraft.component.DataComponentTypes.ENCHANTMENTS;
import static net.minecraft.enchantment.Enchantments.EFFICIENCY;
import static net.minecraft.entity.attribute.EntityAttributes.ATTACK_DAMAGE;
import static net.minecraft.entity.attribute.EntityAttributes.ATTACK_SPEED;
import static net.minecraft.registry.RegistryKeys.ENCHANTMENT;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.effect.EnchantmentEffectEntry;
import net.minecraft.enchantment.effect.EnchantmentValueEffect;
import net.minecraft.enchantment.effect.value.AddEnchantmentEffect;
import net.minecraft.enchantment.effect.value.MultiplyEnchantmentEffect;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.EntityPropertiesLootCondition;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.EntityTypePredicate;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.World;

@Environment(value = CLIENT)
public class ItemStackUtil {

	private static final Logger LOGGER = LogManager.getLogger(Main.MOD_ID);
	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();

	/**
	 * Calculates the attack damage of the current player towards a specific entity
	 * using a specific item stack.
	 */
	public static double getAttackDamage(final ItemStack stack, final Entity entity, final World world,
			final SwitcherooConfig config) {
		if (config.debug)
			LOGGER.info("Calculating the damage of {}", stack.getItem().getName().getString());
		double damage = 0;

		// Player damage
		final double player = CLIENT.player.getAttributeBaseValue(ATTACK_DAMAGE);
		if (config.debug)
			LOGGER.info("Player damage: {}", player);
		damage += player;

		// Stack damage
		final double weapon = stack.getOrDefault(ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.DEFAULT).modifiers()
				.stream().filter((entry) -> entry.attribute().equals(ATTACK_DAMAGE))
				.mapToDouble((entry) -> entry.modifier().value()).sum();
		if (config.debug)
			LOGGER.info("Weapon damage: {}", weapon);
		damage += weapon;

		// Enchantment damage
		final double enchantments = getEnchantmentDamage(stack, entity, damage, config);
		if (config.debug)
			LOGGER.info("Enchantment damage: {}", enchantments);
		damage += enchantments;

		if (config.debug)
			LOGGER.info("Total damage: {}", damage);
		return damage;
	}

	/**
	 * Gets the maximum attack damage the current player can deal to a specific
	 * entity using a list of weapons.
	 */
	public static double getMaxAttackDamage(final ArrayList<ItemStack> weapons, final Entity entity, final World world,
			final SwitcherooConfig config) {
		return getAttackDamage(
				weapons.stream().max(Comparator.comparing(item -> getAttackDamage(item, entity, world, config))).get(),
				entity, world, config);
	}

	/**
	 * Removes weapons that deal less damage than the maximum attack damage the
	 * player can deal to a specific entity.
	 */
	public static boolean keepMostAttackDamage(final ArrayList<ItemStack> weapons, final Entity entity,
			@Nullable final Double maxAd, final World world, final SwitcherooConfig config) {
		final double max = maxAd == null ? getMaxAttackDamage(weapons, entity, world, config) : maxAd.doubleValue();
		return weapons.removeIf(stack -> max > getAttackDamage(stack, entity, world, config));
	}

	/**
	 * Calculates the attack speed of a specific item stack.
	 */
	public static double getAttackSpeed(final ItemStack stack, final SwitcherooConfig config) {
		double speed = 0F;

		final double player = CLIENT.player.getAttributeBaseValue(ATTACK_SPEED);
		if (config.debug)
			LOGGER.info("Player speed: {}", round(player));
		speed += player;

		final double weapon = stack.getOrDefault(ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.DEFAULT).modifiers()
				.stream().filter((entry) -> entry.attribute().equals(ATTACK_SPEED))
				.mapToDouble((entry) -> entry.modifier().value()).sum();
		if (config.debug)
			LOGGER.info("Weapon speed: {}", round(weapon));
		speed += weapon;

		if (config.debug)
			LOGGER.info("Total speed: {}", round(speed));
		return speed;
	}

	public static double round(final double speed) {
		return Math.round(speed * 10.0) / 10.0;
	}

	/**
	 * Calculates the damage per seconds a player can deal using specific item stack
	 * towards a specific entity.
	 */
	public static double getDps(final ItemStack stack, final Entity entity, final World world,
			final SwitcherooConfig config) {
		return getAttackDamage(stack, entity, world, config) * getAttackSpeed(stack, config);
	}

	/**
	 * Gets the maximum damage per seconds a player can deal to a specific entity
	 */
	public static double getMaxDps(final ArrayList<ItemStack> weapons, final Entity entity, final World world,
			final SwitcherooConfig config) {
		return getDps(weapons.stream().max(Comparator.comparing(item -> getDps(item, entity, world, config))).get(), entity,
				world, config);
	}

	/**
	 * Removes weapons that deal less damage per seconds than the maximum damage per
	 * seconds the player can deal to a specific entity.
	 */
	public static boolean keepMostDps(final ArrayList<ItemStack> weapons, final Entity entityGroup,
			@Nullable final Double maxDps, final World world, final SwitcherooConfig config) {
		final double max = maxDps == null ? getMaxDps(weapons, entityGroup, world, config) : maxDps.doubleValue();
		return weapons.removeIf(stack -> max > getDps(stack, entityGroup, world, config));
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
		final Registry<Enchantment> enchantments = manager.getOrThrow(ENCHANTMENT);
		final RegistryEntry<Enchantment> efficiency = enchantments.getOptional(EFFICIENCY).get();

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

	/** Calculates the enchantment damage done by a weapon to an entity. */
	static double getEnchantmentDamage(final ItemStack stack, final Entity entity, final double damage,
			final SwitcherooConfig config) {
		final ItemEnchantmentsComponent component = stack.getOrDefault(ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT);
		final var entries = component.getEnchantmentEntries();

		double bonus = 0;
		for (final var entry : entries) {
			final Enchantment enchantment = entry.getKey().value();
			final var effects = enchantment.getEffect(EnchantmentEffectComponentTypes.DAMAGE);

			// Enchantments have effects and conditions. An effect is an operator applied to
			// a value. A condition is a predicate that must be satisfied for the effect to
			// be applied.
			for (final EnchantmentEffectEntry<EnchantmentValueEffect> effect : effects) {
				final EnchantmentValueEffect operator = effect.effect();

				// Test the effect before applying it
				final Optional<LootCondition> requirements = effect.requirements();
				if (requirements.isPresent()) {
					final LootCondition condition = requirements.get();

					if (condition instanceof EntityPropertiesLootCondition) {
						// Here, we have an entity condition. Entity conditions apply to an entity and
						// put conditions on it.
						final EntityPropertiesLootCondition property = (EntityPropertiesLootCondition) condition;

						if (property.entity().equals(LootContext.EntityTarget.THIS)) {
							// The "entity" is the target of the enchantment. Now we need to check
							// conditions on it.
							if (property.predicate().isPresent()) {
								final EntityPredicate predicate = property.predicate().get();
								if (predicate.type().isPresent()) {
									final EntityTypePredicate type = predicate.type().get();
									final boolean matches = type.matches(entity.getType());

									final String description = enchantment.description().getString();
									final String name = entity.getName().getString();

									if (matches) {
										if (config.debug)
											LOGGER.info("Enchantment {} applies to {}", description, name);
									} else {
										if (config.debug)
											LOGGER.info("Enchantment {} does not apply to {}", description, name);
										continue;
									}
								}
							}
						}
					}
				}

				final int level = entry.getIntValue();
				if (operator instanceof AddEnchantmentEffect) {
					final AddEnchantmentEffect add = (AddEnchantmentEffect) operator;
					final float added = add.value().getValue(level);
					if (config.debug)
						LOGGER.info("Added: {}", round(added));
					bonus += added;
				} else if (operator instanceof MultiplyEnchantmentEffect) {
					final MultiplyEnchantmentEffect multiply = (MultiplyEnchantmentEffect) operator;
					final float multiplied = multiply.factor().getValue(level);
					if (config.debug)
						LOGGER.info("Multiplied: {}", round(multiplied));
					bonus *= multiplied;
				} else {
					LOGGER.warn("Unknown operator: {}", operator);
				}
			}
		}

		return bonus;
	}
}
