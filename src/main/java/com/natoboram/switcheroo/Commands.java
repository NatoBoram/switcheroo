package com.natoboram.switcheroo;

import java.util.ArrayList;
import java.util.Arrays;

import com.mojang.brigadier.context.CommandContext;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.command.argument.BlockStateArgument;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

final public class Commands {

	static private final ConfigHolder<SwitcherooConfig> CONFIG_HOLDER = AutoConfig
			.getConfigHolder(SwitcherooConfig.class);

	/**
	 * <pre>
	 * /switcheroo blacklist block add minecraft:grass_block
	 * </pre>
	 */
	static public int blacklistBlocksAdd(final CommandContext<FabricClientCommandSource> command) {
		final Identifier id = Registry.BLOCK
				.getId(command.getArgument("block", BlockStateArgument.class).getBlockState().getBlock());
		CONFIG_HOLDER.getConfig().blacklist.blocks += " " + id;
		CONFIG_HOLDER.save();
		return 0;
	};

	/**
	 * <pre>
	 * /switcheroo blacklist block remove minecraft:grass_block
	 * </pre>
	 */
	static public int blacklistBlocksRemove(final CommandContext<FabricClientCommandSource> command) {
		final Identifier id = Registry.BLOCK
				.getId(command.getArgument("block", BlockStateArgument.class).getBlockState().getBlock());

		final ArrayList<String> blacklist = new ArrayList<String>(
				Arrays.asList(CONFIG_HOLDER.getConfig().blacklist.blocks.split(" ")));

		blacklist.removeIf(blacklisted -> {
			switch (blacklisted.split(":").length) {
				case 1:
					if (id.toString().equals("minecraft:" + blacklisted))
						return true;
					break;
				case 2:
				default:
					if (id.toString().equals(blacklisted))
						return true;
					break;
			}
			return false;
		});

		CONFIG_HOLDER.getConfig().blacklist.blocks = String.join(" ", blacklist);
		CONFIG_HOLDER.save();
		return 0;
	};

}
