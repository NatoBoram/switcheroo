package com.natoboram.switcheroo.util;

import java.util.Arrays;
import net.minecraft.util.Identifier;

public class Identifiers {

	/** Check if the given identifier is present in a space-separated string list. */
	public static boolean anyMatch(final Identifier id, final String list) {
		return Arrays.stream(list.split(" "))
			.filter(entry -> !entry.isBlank())
			.map(Identifier::of)
			.anyMatch(id::equals);
	}
}
