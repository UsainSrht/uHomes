package me.usainsrht.uhomes.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.TagPattern;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

public class MMUtil {

    public static Component PARSE_ERROR = Component.text("! PARSE ERROR !");

    public static @NotNull TagResolver date(@TagPattern final @NotNull String key, final @NotNull TemporalAccessor time) {
        return TagResolver.resolver(key, (argumentQueue, context) -> {
            String format = argumentQueue.popOr("Format expected.").value();
            String fallback = argumentQueue.hasNext() ? argumentQueue.pop().value() : null;
            return Tag.inserting(
                    context.deserialize(
                            (Instant.from(time).toEpochMilli() != -1 || fallback == null)
                                    ? DateTimeFormatter.ofPattern(format).format(time)
                                    : fallback
                    )
            );
        });
    }


}
