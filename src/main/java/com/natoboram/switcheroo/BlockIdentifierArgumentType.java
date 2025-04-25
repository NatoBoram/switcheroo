package com.natoboram.switcheroo;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.block.Block;
import net.minecraft.command.argument.BlockStateArgumentType;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * Argument type for obtaining a block {@link Identifier}.
 *
 * @see BlockStateArgumentType
 */
public class BlockIdentifierArgumentType implements ArgumentType<Identifier> {

	private static final Collection<String> EXAMPLES = Arrays.asList("stone", "minecraft:stone");

	public static final DynamicCommandExceptionType NOT_FOUND_EXCEPTION = new DynamicCommandExceptionType(id -> {
		return Text.translatable("switcheroo.error.blockNotFound", new Object[] { id });
	});

	public static BlockIdentifierArgumentType blockIdentifier() {
		return new BlockIdentifierArgumentType();
	}

	public Identifier getBlockIdentifier(final String name, final CommandContext<FabricClientCommandSource> context)
		throws CommandSyntaxException {
		return validate((Identifier) context.getArgument(name, Identifier.class));
	}

	@Override
	public Collection<String> getExamples() {
		return EXAMPLES;
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(
		final CommandContext<S> context,
		final SuggestionsBuilder builder
	) {
		final String remaining = builder.getRemaining();

		Registries.BLOCK.getIds()
			.forEach(id -> {
				if (id.toString().startsWith(remaining) || id.getPath().startsWith(remaining)) builder.suggest(id.toString());
			});

		return builder.buildFuture();
	}

	@Override
	public Identifier parse(final StringReader reader) throws CommandSyntaxException {
		return validate(Identifier.fromCommandInput(reader));
	}

	private Identifier validate(final Identifier id) throws CommandSyntaxException {
		final Block block = Registries.BLOCK.get(id);

		if (block == null) throw NOT_FOUND_EXCEPTION.create(id);

		return id;
	}
}
