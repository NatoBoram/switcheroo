package com.natoboram.switcheroo;

import static net.fabricmc.fabric.api.client.command.v1.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v1.ClientCommandManager.literal;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.command.argument.BlockStateArgumentType;

@Environment(EnvType.CLIENT)
public class Main implements ClientModInitializer {

	public static final String MOD_ID = "switcheroo";

	/**
	 * This logger is used to write text to the console and the log file. It is
	 * considered best practice to use your mod id as the logger's name. That way,
	 * it's clear which mod wrote info, warnings, and errors.
	 */
	private final static Logger LOGGER = LogManager.getLogger(MOD_ID);

	@Override
	public void onInitializeClient() {

		// Register config
		AutoConfig.register(SwitcherooConfig.class, GsonConfigSerializer::new);
		final ConfigHolder<SwitcherooConfig> holder = AutoConfig.getConfigHolder(SwitcherooConfig.class);

		// Register switcheroos
		AttackBlockCallback.EVENT.register(new BlockSwitch(holder));
		AttackEntityCallback.EVENT.register(new EntitySwitch(holder));

		// Register commands
		ClientCommandManager.DISPATCHER.register(literal(MOD_ID).then(literal("blacklist").then(literal("block")
				.then(literal("add").then(
						argument("block", BlockStateArgumentType.blockState()).executes(Commands::blacklistBlocksAdd)))
				.then(literal("remove").then(argument("block", BlockStateArgumentType.blockState())
						.executes(Commands::blacklistBlocksRemove))))));

		LOGGER.info("Loaded Switcheroo!");
	}

}
