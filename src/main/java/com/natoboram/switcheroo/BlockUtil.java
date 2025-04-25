package com.natoboram.switcheroo;

import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class BlockUtil {

	public static boolean isInList(final Block block, final String[] blocks) {
		final Identifier id = Registries.BLOCK.getId(block);

		for (final String blockId : blocks) {
			switch (blockId.split(":").length) {
				case 1:
					if (id.toString().equals("minecraft:" + blockId)) return true;
					break;
				case 2:
				default:
					if (id.toString().equals(blockId)) return true;
					break;
			}
		}

		return false;
	}
}
