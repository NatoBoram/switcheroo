package com.natoboram.switcheroo;

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
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MiningToolItem;
import net.minecraft.item.SwordItem;
import net.minecraft.item.TridentItem;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

/** Execute a switcheroo action when attacking an entity. */
@Environment(EnvType.CLIENT)
public class EntitySwitch implements AttackEntityCallback {

	private final ConfigHolder<SwitcherooConfig> CONFIG_HOLDER;
	private static final Logger LOGGER = LogManager.getLogger(Main.MOD_ID);
	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();

	EntitySwitch(final ConfigHolder<SwitcherooConfig> holder) {
		this.CONFIG_HOLDER = holder;
	}

	@Override
	public ActionResult interact(final PlayerEntity player, final World world, final Hand hand, final Entity entity,
			@Nullable final EntityHitResult hitResult) {
		if (player.isSpectator() || player.isSneaking() || !entity.isLiving() || !entity.isAlive()
				|| entity.isInvulnerable())
			return ActionResult.PASS;

		final LivingEntity livingEntity = (LivingEntity) entity;
		final SwitcherooConfig config = CONFIG_HOLDER.getConfig();

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
		ItemStackUtil.removeDamagedEnchantedItems(weapons, config);

		// Safety before launching streams
		if (weapons.isEmpty())
			return ActionResult.PASS;

		final EntityGroup entityGroup = livingEntity.getGroup();

		// Stop if there's already a max dps weapon in hand
		final double maxDps = ItemStackUtil.getMaxDps(weapons, entityGroup);
		final double currentDps = ItemStackUtil.getDps(CLIENT.player.getMainHandStack(), entityGroup);
		if (currentDps >= maxDps || weapons.isEmpty())
			return ActionResult.PASS;

		ItemStackUtil.keepMostDps(weapons, entityGroup, maxDps);
		ItemStackUtil.keepMostDamagedItems(weapons);

		if (!weapons.isEmpty())
			Switch.switcheroo(player, weapons.get(0), config);
		return ActionResult.PASS;
	}

	private boolean isBlacklisted(final LivingEntity livingEntity, final SwitcherooConfig config) {
		final Identifier id = Registry.ENTITY_TYPE.getId(livingEntity.getType());
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
