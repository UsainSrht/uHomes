package me.usainsrht.uhomes.util;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;

import java.util.Collection;

public class MessageUtil {

    public static void send(CommandSender sender, Collection<String> messages, TagResolver... placeholders) {
        //todo add prefix
        messages.forEach(message -> sender.sendMessage(MiniMessage.miniMessage().deserialize(message, placeholders)));
    }

}
