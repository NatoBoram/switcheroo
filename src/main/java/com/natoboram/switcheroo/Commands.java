package com.natoboram.switcheroo;

import java.util.ArrayList;
import java.util.Arrays;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
final public class Commands {

	static private final ConfigHolder<SwitcherooConfig> CONFIG_HOLDER = AutoConfig
			.getConfigHolder(SwitcherooConfig.class);

	/**
	 * <pre>
	 * /switcheroo blacklist blocks
	 * </pre>
	 */
	static public int blacklistBlocks(final CommandContext<FabricClientCommandSource> command) {
		command.getSource().sendFeedback(Text.of("Blacklist: §e" + CONFIG_HOLDER.getConfig().blacklist.blocks));
		return Command.SINGLE_SUCCESS;
	}

	/**
	 * <pre>
	 * /switcheroo blacklist blocks add minecraft:grass_block
	 * </pre>
	 */
	static public int blacklistBlocksAdd(final CommandContext<FabricClientCommandSource> command) {
		final Identifier id = command.getArgument("block", Identifier.class);
		CONFIG_HOLDER.getConfig().blacklist.blocks += " " + id;
		CONFIG_HOLDER.save();
		command.getSource().sendFeedback(Text.of("§fAdded §e" + id + "§f to the blacklist."));
		return blacklistBlocks(command);
	};

	/**
	 * <pre>
	 * /switcheroo blacklist blocks remove minecraft:grass_block
	 * </pre>
	 */
	static public int blacklistBlocksRemove(final CommandContext<FabricClientCommandSource> command) {
		final Identifier id = command.getArgument("block", Identifier.class);

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
		command.getSource().sendFeedback(Text.of("§fRemoved §e" + id + "§f from the blacklist."));
		return blacklistBlocks(command);
	};

	/**
	 * <pre>
	 * /switcheroo blacklist mobs
	 * </pre>
	 */
	static public int blacklistMobs(final CommandContext<FabricClientCommandSource> command) {
		command.getSource().sendFeedback(Text.of("§fBlacklist: §e" + CONFIG_HOLDER.getConfig().blacklist.mobs));
		return Command.SINGLE_SUCCESS;
	}

	/**
	 * <pre>
	 * /switcheroo blacklist mobs add minecraft:cow
	 * </pre>
	 *
	 * @throws CommandSyntaxException
	 */
	static public int blacklistMobsAdd(final CommandContext<FabricClientCommandSource> command)
			throws CommandSyntaxException {
		final Identifier id = command.getArgument("mob", Identifier.class);
		CONFIG_HOLDER.getConfig().blacklist.mobs += " " + id;
		CONFIG_HOLDER.save();
		command.getSource().sendFeedback(Text.of("§fAdded §e" + id + "§f to the blacklist."));
		return blacklistMobs(command);
	};

	/**
	 * <pre>
	 * /switcheroo blacklist mobs remove minecraft:cow
	 * </pre>
	 *
	 * @throws CommandSyntaxException
	 */
	static public int blacklistMobsRemove(final CommandContext<FabricClientCommandSource> command)
			throws CommandSyntaxException {
		final Identifier id = command.getArgument("mob", Identifier.class);

		final ArrayList<String> blacklist = new ArrayList<String>(
				Arrays.asList(CONFIG_HOLDER.getConfig().blacklist.mobs.split(" ")));

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

		CONFIG_HOLDER.getConfig().blacklist.mobs = String.join(" ", blacklist);
		CONFIG_HOLDER.save();
		command.getSource().sendFeedback(Text.of("§fRemoved §e" + id + "§f from the blacklist."));
		return blacklistMobs(command);
	};

}
