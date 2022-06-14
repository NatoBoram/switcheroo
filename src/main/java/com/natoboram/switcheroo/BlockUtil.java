package com.natoboram.switcheroo;

import net.minecraft.block.Block;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class BlockUtil {
	public static boolean isInList(final Block block, final String[] blocks) {
		final Identifier id = Registry.BLOCK.getId(block);

		for (final String blockId : blocks) {
			switch (blockId.split(":").length) {
			case 1:
				if (id.toString().equals("minecraft:" + blockId))
					return true;
				break;
			case 2:
			default:
				if (id.toString().equals(blockId))
					return true;
				break;
			}
		}

		return false;
	}
}
