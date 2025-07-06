package com.natoboram.switcheroo;

import static com.natoboram.switcheroo.ItemStackUtil.getAttackDamage;
import static com.natoboram.switcheroo.ItemStackUtil.getDps;
import static com.natoboram.switcheroo.ItemStackUtil.getMaxAttackDamage;
import static com.natoboram.switcheroo.ItemStackUtil.getMaxDps;
import static com.natoboram.switcheroo.ItemStackUtil.keepMostAttackDamage;
import static com.natoboram.switcheroo.ItemStackUtil.keepMostDamagedItems;
import static com.natoboram.switcheroo.ItemStackUtil.keepMostDps;
import static com.natoboram.switcheroo.ItemStackUtil.removeDamagedEnchantedItems;
import static com.natoboram.switcheroo.ItemStackUtil.round;
import static net.fabricmc.api.EnvType.CLIENT;
import static net.minecraft.entity.attribute.EntityAttributes.ATTACK_DAMAGE;

import java.util.ArrayList;
import me.shedaniel.autoconfig.ConfigHolder;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.AirBlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.registry.Registries;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

/** Execute a switcheroo action when attacking an entity. */
@Environment(value = CLIENT)
public class EntitySwitch implements AttackEntityCallback {

	private static final Logger LOGGER = LogManager.getLogger(Main.MOD_ID);
	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
	private final ConfigHolder<SwitcherooConfig> CONFIG_HOLDER;

	EntitySwitch(final ConfigHolder<SwitcherooConfig> holder) {
		this.CONFIG_HOLDER = holder;
	}

	@Override
	public ActionResult interact(
		final PlayerEntity player,
		final World world,
		final Hand hand,
		final Entity entity,
		@Nullable final EntityHitResult hitResult
	) {
		final SwitcherooConfig config = CONFIG_HOLDER.getConfig();
		if (
			player.isSpectator()
				|| player.isSneaking()
				|| !entity.isLiving()
				|| !entity.isAlive()
				|| entity.isInvulnerable()
				|| !config.enabled
		) {
			if (config.debug)
				LOGGER.info("Skipping interaction with entity {}", entity.getName().getString());

			return ActionResult.PASS;
		}

		final LivingEntity livingEntity = (LivingEntity) entity;

		if (isBlacklisted(livingEntity, config)) {
			if (config.debug)
				LOGGER.info("Entity {} is blacklisted", livingEntity.getName().getString());
			return ActionResult.PASS;
		}

		final ArrayList<ItemStack> weapons = new ArrayList<ItemStack>();
		final PlayerInventory inventory = player.getInventory();

		// Get all potential weapons
		for (final ItemStack stack : inventory.getMainStacks()) {
			final Item item = stack.getItem();
			if (item instanceof AirBlockItem || item instanceof RangedWeaponItem)
				continue;

			// A potential weapon is any stack that has more attack damage than the player's attack damage. The calculation
			// for attack damage includes the player's base attack damage.
			final double ad = getAttackDamage(stack, entity, world, config);
			if (ad > CLIENT.player.getAttributeValue(ATTACK_DAMAGE)) {
				if (config.debug) {
					final double dps = getDps(stack, entity, world, config);

					LOGGER.info(
						"Found potential weapon {} with {} attack damage and {} damage per seconds",
						stack.getName().getString(),
						ad,
						round(dps)
					);
				}

				weapons.add(stack);
			}
		}

		// Filters enchanted items with low durability
		removeDamagedEnchantedItems(weapons, config);

		// Safety before launching streams
		if (weapons.isEmpty()) {
			if (config.debug)
				LOGGER.info("No weapons found");

			return ActionResult.PASS;
		}

		// Use max AD on players and max DPS on mobs
		if (entity instanceof PlayerEntity) {
			// Stop if there's already a max ad weapon in hand
			final double maxAd = getMaxAttackDamage(weapons, entity, world, config);
			final double currentAd = getAttackDamage(CLIENT.player.getMainHandStack(), entity, world, config);
			if (currentAd >= maxAd || weapons.isEmpty()) {
				if (config.debug)
					LOGGER.info("Current AD is already maxed at {}/{}", currentAd, maxAd);
				return ActionResult.PASS;
			}

			keepMostAttackDamage(weapons, entity, maxAd, world, config);
		} else {
			// Stop if there's already a max dps weapon in hand
			final double maxDps = getMaxDps(weapons, entity, world, config);
			final double currentDps = getDps(CLIENT.player.getMainHandStack(), entity, world, config);
			if (currentDps >= maxDps || weapons.isEmpty()) {
				if (config.debug)
					LOGGER.info("Current DPS is already maxed at {}/{}", round(currentDps), round(maxDps));

				return ActionResult.PASS;
			}

			keepMostDps(weapons, entity, maxDps, world, config);
		}

		keepMostDamagedItems(weapons);

		if (!weapons.isEmpty())
			Switch.switcheroo(player, weapons.get(0), config);

		return ActionResult.PASS;
	}

	private boolean isBlacklisted(final LivingEntity livingEntity, final SwitcherooConfig config) {
		final Identifier id = Registries.ENTITY_TYPE.getId(livingEntity.getType());
		final String[] blacklist = config.blacklist.mobs.split(" ");

		for (final String blacklisted : blacklist) {
			switch (blacklisted.split(":").length) {
				case 1:
					if (id.toString().equals("minecraft:" + blacklisted))
						return true;

				default:
				case 2:
					if (id.toString().equals(blacklisted))
						return true;
			}
		}
		return false;
	}
}
