package com.natoboram.switcheroo;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;

public class RegisterCommands implements ClientCommandRegistrationCallback {

	private final String MOD_ID;

	RegisterCommands(final String MOD_ID) {
		this.MOD_ID = MOD_ID;
	}

	@Override
	public void register(
		final CommandDispatcher<FabricClientCommandSource> dispatcher,
		final CommandRegistryAccess registryAccess) {
		final var enable = literal("enable").executes(Commands::enable);
		final var disable = literal("disable").executes(Commands::disable);

		final var blocks = literal("blocks")
			.executes(Commands::blacklistBlocks)
			.then(
				literal("add").then(
					argument("block", BlockIdentifierArgumentType.blockIdentifier()).executes(Commands::blacklistBlocksAdd)))
			.then(
				literal("remove").then(
					argument("block", BlockIdentifierArgumentType.blockIdentifier()).executes(Commands::blacklistBlocksRemove)));

		final var mobs = literal("mobs")
			.executes(Commands::blacklistMobs)
			.then(
				literal("add").then(
					argument("mob", EntityIdentifierArgumentType.entityIdentifier()).executes(Commands::blacklistMobsAdd)))
			.then(
				literal("remove").then(
					argument("mob", EntityIdentifierArgumentType.entityIdentifier()).executes(Commands::blacklistMobsRemove)));

		final var blacklist = literal("blacklist").then(blocks).then(mobs);

		final var alwaysFastest = literal("alwaysFastest")
			.executes(Commands::alwaysFastest)
			.then(argument("boolean", BoolArgumentType.bool()).executes(Commands::alwaysFastestToggle));

		final var minDurability = literal("minDurability")
			.executes(Commands::minDurability)
			.then(argument("integer", IntegerArgumentType.integer()).executes(Commands::minDurabilitySet));

		final var prefer = literal("prefer").then(
			literal("silk_touch")
				.executes(Commands::preferSilkTouch)
				.then(
					literal("add").then(
						argument("block", BlockIdentifierArgumentType.blockIdentifier()).executes(Commands::preferSilkTouchAdd)))
				.then(
					literal("remove").then(
						argument("block", BlockIdentifierArgumentType.blockIdentifier())
							.executes(Commands::preferSilkTouchRemove))));

		final var switcheroo = literal(MOD_ID)
			.then(enable)
			.then(disable)
			.then(alwaysFastest)
			.then(blacklist)
			.then(minDurability)
			.then(prefer);

		dispatcher.register(switcheroo);
	}
}
