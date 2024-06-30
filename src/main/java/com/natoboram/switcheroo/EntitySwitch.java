package com.natoboram.switcheroo;

import static com.natoboram.switcheroo.ItemStackUtil.getAttackDamage;
import static com.natoboram.switcheroo.ItemStackUtil.getDps;
import static com.natoboram.switcheroo.ItemStackUtil.getMaxAttackDamage;
import static com.natoboram.switcheroo.ItemStackUtil.getMaxDps;
import static com.natoboram.switcheroo.ItemStackUtil.keepMostAttackDamage;
import static com.natoboram.switcheroo.ItemStackUtil.keepMostDamagedItems;
import static com.natoboram.switcheroo.ItemStackUtil.keepMostDps;
import static com.natoboram.switcheroo.ItemStackUtil.removeDamagedEnchantedItems;

import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import me.shedaniel.autoconfig.ConfigHolder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MiningToolItem;
import net.minecraft.item.SwordItem;
import net.minecraft.item.TridentItem;
import net.minecraft.registry.Registries;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;

/** Execute a switcheroo action when attacking an entity. */
@Environment(EnvType.CLIENT)
public class EntitySwitch implements AttackEntityCallback {

	private static final Logger LOGGER = LogManager.getLogger(Main.MOD_ID);
	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
	private final ConfigHolder<SwitcherooConfig> CONFIG_HOLDER;

	EntitySwitch(final ConfigHolder<SwitcherooConfig> holder) {
		this.CONFIG_HOLDER = holder;
	}

	@Override
	public ActionResult interact(final PlayerEntity player, final World world, final Hand hand, final Entity entity,
			@Nullable final EntityHitResult hitResult) {
		final SwitcherooConfig config = CONFIG_HOLDER.getConfig();
		if (player.isSpectator() || player.isSneaking() || !entity.isLiving() || !entity.isAlive()
				|| entity.isInvulnerable() || !config.enabled)
			return ActionResult.PASS;

		final LivingEntity livingEntity = (LivingEntity) entity;

		if (isBlacklisted(livingEntity, config)) {
			if (config.debug)
				LOGGER.info("Entity " + livingEntity + " is blacklisted");
			return ActionResult.PASS;
		}

		final ArrayList<ItemStack> weapons = new ArrayList<ItemStack>();
		final PlayerInventory inventory = player.getInventory();

		// Get all potential weapons
		for (final ItemStack stack : inventory.main) {
			final Item item = stack.getItem();

			if (item instanceof SwordItem || item instanceof TridentItem || item instanceof MiningToolItem)
				weapons.add(stack);
		}

		// Filters enchanted items with low durability
		removeDamagedEnchantedItems(weapons, config);

		// Safety before launching streams
		if (weapons.isEmpty())
			return ActionResult.PASS;

		// Use max AD on players and max DPS on mobs
		if (entity instanceof PlayerEntity) {

			// Stop if there's already a max ad weapon in hand
			final double maxAd = getMaxAttackDamage(weapons, entity, world);
			final double currentAd = getAttackDamage(CLIENT.player.getMainHandStack(), entity, world);
			if (currentAd >= maxAd || weapons.isEmpty())
				return ActionResult.PASS;

			keepMostAttackDamage(weapons, entity, maxAd, world);
		} else {

			// Stop if there's already a max dps weapon in hand
			final double maxDps = getMaxDps(weapons, entity, world);
			final double currentDps = getDps(CLIENT.player.getMainHandStack(), entity, world);
			if (currentDps >= maxDps || weapons.isEmpty())
				return ActionResult.PASS;

			keepMostDps(weapons, entity, maxDps, world);
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
