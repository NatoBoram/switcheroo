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
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
final public class Commands {

	static private final ConfigHolder<SwitcherooConfig> CONFIG_HOLDER = AutoConfig
			.getConfigHolder(SwitcherooConfig.class);

	static public int enable(final CommandContext<FabricClientCommandSource> command) {
		CONFIG_HOLDER.getConfig().enabled = true;
		CONFIG_HOLDER.save();
		command.getSource().sendFeedback(Text.of("Switcheroo is now §aenabled§f."));
		return Command.SINGLE_SUCCESS;
	}

	static public int disable(final CommandContext<FabricClientCommandSource> command) {
		CONFIG_HOLDER.getConfig().enabled = false;
		CONFIG_HOLDER.save();
		command.getSource().sendFeedback(Text.of("Switcheroo is now §7disabled§f."));
		return Command.SINGLE_SUCCESS;
	}

	/**
	 * Shows the list of blacklisted blocks.
	 *
	 * <pre>
	 * /switcheroo blacklist blocks
	 * </pre>
	 */
	static public int blacklistBlocks(final CommandContext<FabricClientCommandSource> command) {
		final String blocks = CONFIG_HOLDER.getConfig().blacklist.blocks;
		command.getSource().sendFeedback(Text.of("Blacklist: §e" + (blocks.isEmpty() ? "[]" : blocks)));
		return Command.SINGLE_SUCCESS;
	}

	/**
	 * Add a block to the blacklist.
	 *
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
	 * Remove a block from the blacklist.
	 *
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
	 * Shows the list of blacklisted mobs.
	 *
	 * <pre>
	 * /switcheroo blacklist mobs
	 * </pre>
	 */
	static public int blacklistMobs(final CommandContext<FabricClientCommandSource> command) {
		final String mobs = CONFIG_HOLDER.getConfig().blacklist.mobs;
		command.getSource().sendFeedback(Text.of("§fBlacklist: §e" + (mobs.isEmpty() ? "[]" : mobs)));
		return Command.SINGLE_SUCCESS;
	}

	/**
	 * Add a mob to the blacklist.
	 *
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
	 * Remove a mob from the blacklist.
	 *
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

	/**
	 * Shows the configuration for <code>alwaysFastest</code>.
	 *
	 * <pre>
	 * /switcheroo alwaysFastest
	 * </pre>
	 */
	static public int alwaysFastest(final CommandContext<FabricClientCommandSource> command) {
		command.getSource().sendFeedback(Text.of("alwaysFastest: §e" + CONFIG_HOLDER.getConfig().alwaysFastest));
		return Command.SINGLE_SUCCESS;
	}

	/**
	 * Sets the configuration for <code>alwaysFastest</code>.
	 *
	 * <pre>
	 * /switcheroo alwaysFastest true
	 * </pre>
	 */
	static public int alwaysFastestToggle(final CommandContext<FabricClientCommandSource> command) {
		final Boolean input = command.getArgument("boolean", Boolean.class);
		final SwitcherooConfig config = CONFIG_HOLDER.getConfig();
		config.alwaysFastest = input == null ? !config.alwaysFastest : input;
		CONFIG_HOLDER.save();
		return alwaysFastest(command);
	}

	/**
	 * Shows the configuration for <code>minDurability</code>.
	 *
	 * <pre>
	 * /switcheroo minDurability
	 * </pre>
	 */
	static public int minDurability(final CommandContext<FabricClientCommandSource> command) {
		command.getSource().sendFeedback(Text.of("minDurability: §e" + CONFIG_HOLDER.getConfig().minDurability));
		return Command.SINGLE_SUCCESS;
	}

	/**
	 * Sets the configuration for <code>minDurability</code>.
	 *
	 * <pre>
	 * /switcheroo minDurability 5
	 * </pre>
	 */
	static public int minDurabilitySet(final CommandContext<FabricClientCommandSource> command) {
		final Integer input = command.getArgument("integer", Integer.class);
		final SwitcherooConfig config = CONFIG_HOLDER.getConfig();
		config.minDurability = input == null ? 5 : input;
		CONFIG_HOLDER.save();
		return minDurability(command);
	}

	/**
	 * Shows the list of blocks to prefer Silk Touch on.
	 *
	 * <pre>
	 * /switcheroo prefer silk_touch
	 * </pre>
	 */
	static public int preferSilkTouch(final CommandContext<FabricClientCommandSource> command) {
		final String silkTouch = CONFIG_HOLDER.getConfig().prefer.silk_touch;
		command.getSource().sendFeedback(Text.of("Silk Touch: §e" + (silkTouch.isEmpty() ? "[]" : silkTouch)));
		return Command.SINGLE_SUCCESS;
	}

	/**
	 * Add a block to prefer Silk Touch on.
	 *
	 * <pre>
	 * /switcheroo prefer silk_touch add minecraft:grass_block
	 * </pre>
	 */
	static public int preferSilkTouchAdd(final CommandContext<FabricClientCommandSource> command)
			throws CommandSyntaxException {
		final Identifier id = command.getArgument("block", Identifier.class);
		CONFIG_HOLDER.getConfig().prefer.silk_touch += " " + id;
		CONFIG_HOLDER.save();
		command.getSource()
				.sendFeedback(Text.of("§fAdded §e" + id + "§f to the list of blocks to prefer Silk Touch on."));
		return preferSilkTouch(command);
	}

	/**
	 * Remove a block to prefer Silk Touch on.
	 *
	 * <pre>
	 * /switcheroo prefer silk_touch remove minecraft:grass_block
	 * </pre>
	 */
	static public int preferSilkTouchRemove(final CommandContext<FabricClientCommandSource> command)
			throws CommandSyntaxException {
		final Identifier id = command.getArgument("block", Identifier.class);

		final ArrayList<String> prefer = new ArrayList<String>(
				Arrays.asList(CONFIG_HOLDER.getConfig().prefer.silk_touch.split(" ")));

		prefer.removeIf(preferred -> {
			switch (preferred.split(":").length) {
				case 1:
					if (id.toString().equals("minecraft:" + preferred))
						return true;
					break;
				case 2:
				default:
					if (id.toString().equals(preferred))
						return true;
					break;
			}
			return false;
		});

		CONFIG_HOLDER.getConfig().prefer.silk_touch = String.join(" ", prefer);
		CONFIG_HOLDER.save();
		command.getSource()
				.sendFeedback(Text.of("§fRemoved §e" + id + "§f from the list of blocks to prefer Silk Touch on."));

		return preferSilkTouch(command);
	}
}
