package com.natoboram.switcheroo;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.command.argument.BlockStateArgumentType;
import net.minecraft.command.argument.EntitySummonArgumentType;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

/**
 * Argument type for obtaining a block <code>Identifier</code>.
 *
 * @see BlockStateArgumentType
 * @see EntitySummonArgumentType
 */
public class BlockIdentifierArgumentType implements ArgumentType<Identifier> {
	private static final Collection<String> EXAMPLES = Arrays.asList("stone", "minecraft:stone");

	public static final DynamicCommandExceptionType NOT_FOUND_EXCEPTION = new DynamicCommandExceptionType((id) -> {
		return new TranslatableText("switcheroo.error.blockNotFound", new Object[] { id });
	});

	public static BlockIdentifierArgumentType blockIdentifier() {
		return new BlockIdentifierArgumentType();
	}

	@Override
	public Identifier parse(final StringReader reader) throws CommandSyntaxException {
		return validate(Identifier.fromCommandInput(reader));
	}

	public static Identifier getBlockIdentifier(final String name,
			final CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
		return validate((Identifier) context.getArgument(name, Identifier.class));
	}

	public Collection<String> getExamples() {
		return EXAMPLES;
	}

	private static Identifier validate(final Identifier id) throws CommandSyntaxException {
		Registry.BLOCK.getOrEmpty(id).orElseThrow(() -> NOT_FOUND_EXCEPTION.create(id));
		return id;
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context,
			final SuggestionsBuilder builder) {
		final String remaining = builder.getRemaining();

		Registry.BLOCK.getIds().forEach(id -> {
			if (id.toString().startsWith(remaining) || id.getPath().startsWith(remaining))
				builder.suggest(id.toString());
		});

		return builder.buildFuture();
	}
}
