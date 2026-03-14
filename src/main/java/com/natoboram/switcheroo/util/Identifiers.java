package com.natoboram.switcheroo.util;

import java.util.Arrays;
import java.util.Objects;
import net.minecraft.util.Identifier;

public class Identifiers {

	/** Check if the given identifier is present in a space-separated string list. */
	public static boolean anyMatch(final Identifier id, final String list) {
		return Arrays.stream(list.split(" "))
			.map(Identifier::tryParse)
			.filter(Objects::nonNull)
			.anyMatch(id::equals);
	}
}
