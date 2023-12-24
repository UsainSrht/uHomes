package me.usainsrht.uhomes.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import me.usainsrht.uhomes.UHomes;
import me.usainsrht.uhomes.config.MainConfig;
import me.usainsrht.uhomes.gui.HomesGUI;
import me.usainsrht.uhomes.util.MessageUtil;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;

public class HomeCommand extends Command {

    private String permission;
    private String permissionMessage;
    private Collection<Sound> permissionSounds;

    public HomeCommand(YamlCommand cmd) {
        super(cmd.getName(), cmd.getDescription(), cmd.getUsage(), cmd.getAliases());
        this.permission = cmd.getPermission();
        this.permissionMessage = cmd.getPermissionMessage();
        this.permissionSounds = cmd.getPermissionSounds();
    }

    public LiteralCommandNode<?> getCommodoreCommand() {
        return LiteralArgumentBuilder.literal(super.getName())
                .then(RequiredArgumentBuilder.argument("home-name", StringArgumentType.greedyString()))
                .then(RequiredArgumentBuilder.argument("player", StringArgumentType.word()))
                .build();
    }

    @Override
    public boolean execute(CommandSender sender, String command, String[] args) {
        if (!sender.hasPermission(permission)) {
            Component permMsg = MiniMessage.miniMessage().deserialize(permissionMessage,
                    Placeholder.unparsed("permission", permission));
            sender.sendMessage(permMsg);
            MessageUtil.play(sender, permissionSounds);
            return false;
        }
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("reload") && sender.hasPermission("uhomes.reload")) {
                UHomes.getInstance().reload();
                MessageUtil.send(sender, MainConfig.getMessage("reload"));
                MessageUtil.play(sender, MainConfig.getSound("reload"));
            } else {

            }
        } else {
            if (sender instanceof Player player) {
                HomesGUI.open(player.getUniqueId(), player);
            }
        }
        return true;
    }

}
