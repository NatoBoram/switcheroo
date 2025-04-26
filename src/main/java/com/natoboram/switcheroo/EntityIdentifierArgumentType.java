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
import net.minecraft.command.argument.BlockStateArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * Argument type for obtaining an entity {@link Identifier}.
 *
 * @see BlockStateArgumentType
 * @see EntityArgumentType
 */
public class EntityIdentifierArgumentType implements ArgumentType<Identifier> {

	private static final Collection<String> EXAMPLES = Arrays.asList("minecraft:pig", "cow");

	public static final DynamicCommandExceptionType NOT_FOUND_EXCEPTION = new DynamicCommandExceptionType(id -> {
		return Text.translatable("switcheroo.error.mobNotFound", new Object[] { id });
	});

	public static EntityIdentifierArgumentType entityIdentifier() {
		return new EntityIdentifierArgumentType();
	}

	private static Identifier validate(final Identifier id) throws CommandSyntaxException {
		final RegistryKey<EntityType<?>> key = RegistryKey.of(RegistryKeys.ENTITY_TYPE, id);
		Registries.ENTITY_TYPE.getOptional(key).orElseThrow(() -> NOT_FOUND_EXCEPTION.create(id));
		return id;
	}

	public Identifier getEntityIdentifier(final CommandContext<FabricClientCommandSource> context, final String name)
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

		Registries.ENTITY_TYPE.getIds()
			.forEach(id -> {
				if (id.toString().startsWith(remaining) || id.getPath().startsWith(remaining))
					builder.suggest(id.toString());
			});

		return builder.buildFuture();
	}

	@Override
	public Identifier parse(final StringReader stringReader) throws CommandSyntaxException {
		return validate(Identifier.fromCommandInput(stringReader));
	}
}
