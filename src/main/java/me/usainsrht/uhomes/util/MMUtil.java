package me.usainsrht.uhomes.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.TagPattern;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class MMUtil {

    public static Component PARSE_ERROR = Component.text("! PARSE ERROR !");

    public static @NotNull TagResolver date(@TagPattern final @NotNull String key, final @NotNull LocalDateTime time) {
        return TagResolver.resolver(key, (argumentQueue, context) -> {
            String format = argumentQueue.popOr("Format expected.").value();
            String fallback = argumentQueue.hasNext() ? argumentQueue.pop().value() : null;
            return Tag.inserting(context.deserialize(
                    (time.toEpochSecond(ZoneOffset.UTC) != -1 || fallback == null)
                    ? DateTimeFormatter.ofPattern(format).format(time)
                    : fallback));
        });
    }

    public static String mmStringToJson(String minimessage, TagResolver... placeholders) {
        return GsonComponentSerializer.gson().serialize(MiniMessage.miniMessage().deserialize(minimessage, placeholders));
    }


}
