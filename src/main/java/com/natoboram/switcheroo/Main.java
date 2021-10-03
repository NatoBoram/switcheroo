package com.natoboram.switcheroo;

import static net.fabricmc.fabric.api.client.command.v1.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v1.ClientCommandManager.literal;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;

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

@Environment(EnvType.CLIENT)
public class Main implements ClientModInitializer {

	public static final String MOD_ID = "switcheroo";

	/**
	 * This logger is used to write text to the console and the log file. It is
	 * considered best practice to use your mod id as the logger's name. That way,
	 * it's clear which mod wrote info, warnings, and errors.
	 * <p>
	 * Note: {@link Logger#debug} does not write to the console.
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
		ClientCommandManager.DISPATCHER.register(literal(MOD_ID)
				.then(literal("blacklist").then(literal("blocks").executes(Commands::blacklistBlocks)
						.then(literal("add").then(argument("block", BlockIdentifierArgumentType.blockIdentifier())
								.executes(Commands::blacklistBlocksAdd)))
						.then(literal("remove")
								.then(argument("block", BlockIdentifierArgumentType.blockIdentifier())
										.executes(Commands::blacklistBlocksRemove))))
						.then(literal("mobs").executes(Commands::blacklistMobs)
								.then(literal("add")
										.then(argument("mob", EntityIdentifierArgumentType.entityIdentifier())
												.executes(Commands::blacklistMobsAdd)))
								.then(literal("remove")
										.then(argument("mob", EntityIdentifierArgumentType.entityIdentifier())
												.executes(Commands::blacklistMobsRemove)))))
				.then(literal("alwaysFastest").executes(Commands::alwaysFastest)
						.then(argument("boolean", BoolArgumentType.bool()).executes(Commands::alwaysFastestToggle)))
				.then(literal("minDurability").executes(Commands::minDurability).then(
						argument("integer", IntegerArgumentType.integer()).executes(Commands::minDurabilitySet))));

		LOGGER.info("Loaded Switcheroo!");
	}

}
