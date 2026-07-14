package me.usainsrht.uhomes.util;

import me.usainsrht.uhomes.config.MainConfig;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;

public class MessageUtil {

    public static void send(CommandSender sender, Collection<String> messages, TagResolver... placeholders) {
        messages.forEach(message -> {
            if (message.isEmpty()) return;
            if (!MainConfig.getPrefix().isEmpty()) message = MainConfig.getPrefix() + " " + message;
            if (sender instanceof Player player) {
                sender.sendMessage(MMUtil.deserialize(player, message, placeholders));
            } else {
                sender.sendMessage(MMUtil.deserialize(message, placeholders));
            }
        });
    }

}
