package com.natoboram.switcheroo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;

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
		// AttackEntityCallback.EVENT.register(new EntitySwitch(holder));

		// Register commands
		ClientCommandRegistrationCallback.EVENT.register(new RegisterCommands(MOD_ID));

		LOGGER.info("Loaded Switcheroo!");
	}

}
