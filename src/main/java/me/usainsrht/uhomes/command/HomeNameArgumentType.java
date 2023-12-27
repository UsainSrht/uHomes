package me.usainsrht.uhomes.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import me.usainsrht.uhomes.Home;
import me.usainsrht.uhomes.HomeManager;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class HomeNameArgumentType implements ArgumentType<String> {

    private final HomeManager homeManager;

    public HomeNameArgumentType(HomeManager homeManager) {
        this.homeManager = homeManager;
    }

    @Override
    public String parse(StringReader reader) throws CommandSyntaxException {
        return reader.readString();
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        if (context.getSource() instanceof Player player) {
            return homeManager.getHomes(player.getUniqueId())
                    .thenApply(homes -> {
                        homes.stream()
                                .map(Home::getName)
                                .filter(Objects::nonNull) // Exclude null names
                                .forEach(builder::suggest);
                        return builder.build();
                    });
        }
        return Suggestions.empty();
    }

    public static HomeNameArgumentType homeName(HomeManager homeManager) {
        return new HomeNameArgumentType(homeManager);
    }
}

