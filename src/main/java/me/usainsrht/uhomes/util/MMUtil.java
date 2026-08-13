package me.usainsrht.uhomes.util;

import io.github.miniplaceholders.api.MiniPlaceholders;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.TagPattern;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import static net.kyori.adventure.text.format.TextDecoration.ITALIC;
import static net.kyori.adventure.text.format.TextDecoration.State.FALSE;

public class MMUtil {

    public static Component PARSE_ERROR = Component.text("! PARSE ERROR !");

    private static boolean miniPlaceholdersEnabled;

    private MMUtil() {
    }

    public static void init(JavaPlugin plugin) {
        miniPlaceholdersEnabled = Bukkit.getPluginManager().getPlugin("MiniPlaceholders") != null;
        if (miniPlaceholdersEnabled) {
            plugin.getLogger().info("MiniPlaceholders integration enabled.");
        }
    }

    public static boolean isMiniPlaceholdersEnabled() {
        return miniPlaceholdersEnabled;
    }

    public static Component deserialize(String text, TagResolver... placeholders) {
        if (text == null || text.isEmpty()) {
            return Component.empty();
        }
        TagResolver resolver = combineResolvers(globalResolver(), placeholders);
        if (resolver != null) {
            return MiniMessage.miniMessage().deserialize(text, resolver);
        }
        return MiniMessage.miniMessage().deserialize(text, placeholders);
    }

    public static Component deserialize(Audience audience, String text, TagResolver... placeholders) {
        if (text == null || text.isEmpty()) {
            return Component.empty();
        }
        TagResolver resolver = combineResolvers(audienceResolver(audience), placeholders);
        if (resolver != null) {
            return MiniMessage.miniMessage().deserialize(text, audience, resolver);
        }
        if (placeholders.length > 0) {
            return MiniMessage.miniMessage().deserialize(text, audience, placeholders);
        }
        return MiniMessage.miniMessage().deserialize(text, audience);
    }

    public static Component deserialize(Player player, String text, TagResolver... placeholders) {
        return deserialize((Audience) player, text, placeholders);
    }

    public static Component deserializeItem(String text, TagResolver... placeholders) {
        return deserialize(text, placeholders).decorationIfAbsent(ITALIC, FALSE);
    }

    public static Component deserializeItem(Player player, String text, TagResolver... placeholders) {
        return deserialize(player, text, placeholders).decorationIfAbsent(ITALIC, FALSE);
    }

    public static void send(CommandSender sender, String text, TagResolver... placeholders) {
        if (text == null || text.isEmpty()) {
            return;
        }
        if (sender instanceof Player player) {
            sender.sendMessage(deserialize(player, text, placeholders));
        } else {
            sender.sendMessage(deserialize(text, placeholders));
        }
    }

    public static @NotNull TagResolver date(@TagPattern final @NotNull String key, final @NotNull LocalDateTime time) {
        return TagResolver.resolver(key, (argumentQueue, context) -> {
            String format = argumentQueue.popOr("Format expected.").value();
            String fallback = argumentQueue.hasNext() ? argumentQueue.pop().value() : null;
            return Tag.inserting(context.deserialize(
                    (time.atZone(ZoneId.systemDefault()).toInstant().getEpochSecond() != -1 || fallback == null)
                            ? DateTimeFormatter.ofPattern(format).format(time)
                            : fallback));
        });
    }

    public static String mmStringToJson(String minimessage, TagResolver... placeholders) {
        return GsonComponentSerializer.gson().serialize(deserialize(minimessage, placeholders));
    }

    @Nullable
    private static TagResolver globalResolver() {
        if (!miniPlaceholdersEnabled) {
            return null;
        }
        return MiniPlaceholders.globalPlaceholders();
    }

    @Nullable
    private static TagResolver audienceResolver(Audience audience) {
        if (!miniPlaceholdersEnabled) {
            return null;
        }
        return audience instanceof Player
                ? MiniPlaceholders.audiencePlaceholders()
                : MiniPlaceholders.globalPlaceholders();
    }

    @Nullable
    private static TagResolver combineResolvers(@Nullable TagResolver base, TagResolver... placeholders) {
        if (base == null && placeholders.length == 0) {
            return null;
        }
        if (base == null) {
            return TagResolver.resolver(placeholders);
        }
        if (placeholders.length == 0) {
            return base;
        }
        TagResolver[] combined = new TagResolver[placeholders.length + 1];
        combined[0] = base;
        System.arraycopy(placeholders, 0, combined, 1, placeholders.length);
        return TagResolver.resolver(combined);
    }
}
