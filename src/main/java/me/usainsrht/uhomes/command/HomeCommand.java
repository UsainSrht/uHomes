package me.usainsrht.uhomes.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import me.usainsrht.uhomes.UHomes;
import me.usainsrht.uhomes.config.MainConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class HomeCommand extends Command {

    public HomeCommand(String name, String description, String usageMessage, List<String> aliases) {
        super(name, description, usageMessage, aliases);
    }

    public LiteralCommandNode<?> getCommodoreCommand() {
        return LiteralArgumentBuilder.literal(super.getName())
                .then(LiteralArgumentBuilder.literal("reset")
                        .then(RequiredArgumentBuilder.argument("player", StringArgumentType.word())))
                .then(RequiredArgumentBuilder.argument("player", StringArgumentType.word()))
                .build();
    }

    @Override
    public boolean execute(CommandSender sender, String command, String[] args) {
        YamlCommand cmd = MainConfig.getHomeCommand();
        if (!sender.hasPermission(cmd.getPermission())) {
            Component permMsg = MiniMessage.miniMessage().deserialize(MainConfig.getHomeCommand().getPermissionMessage(),
                    Placeholder.unparsed("permission", cmd.getPermission()));
            sender.sendMessage(permMsg);
            sender.pla
            MainConfig.getSound()
            return false;
        }
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("reload")) {
                UHomes.getInstance().reload();

            } else if (args[0].equalsIgnoreCase("debug")) {

            } else if (args[0].equalsIgnoreCase("async")) {

        } else {

            return false;
        }
        return true;
    }

}
