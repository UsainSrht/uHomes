package me.usainsrht.uhomes.command;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import me.usainsrht.uhomes.Home;
import me.usainsrht.uhomes.manager.HomeManager;
import me.usainsrht.uhomes.UHomes;
import me.usainsrht.uhomes.config.MainConfig;
import me.usainsrht.uhomes.gui.HomesGUI;
import me.usainsrht.uhomes.util.MessageUtil;
import me.usainsrht.uhomes.util.SoundUtil;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.*;
import java.util.concurrent.CompletableFuture;

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
                /*.then(RequiredArgumentBuilder.argument("player", StringArgumentType.word())
                        .then(RequiredArgumentBuilder.argument("home-name", StringArgumentType.greedyString())))*/
                .build();
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
        if (args.length == 1) {
            if (sender instanceof Player player) {
                List<String> homeNames = new ArrayList<>();
                CompletableFuture<List<Home>> homesFuture = UHomes.getInstance().getHomeManager().getHomes(player.getUniqueId());
                if (homesFuture.isDone())
                    try {
                        //todo add unnamed homes to tab complete
                        homesFuture.get().stream().map(Home::getName)
                                .filter(Objects::nonNull)
                                .forEach(homeNames::add);
                    } catch (Exception ignore) {}
                if (sender.hasPermission("uhomes.reload")) homeNames.add("reload");
                return StringUtil.copyPartialMatches(args[0], homeNames, new ArrayList<>());
            } else return ImmutableList.of("reload");
        }
        return ImmutableList.of();
    }


    @Override
    public boolean execute(CommandSender sender, String command, String[] args) {
        if (!sender.hasPermission(permission)) {
            Component permMsg = MiniMessage.miniMessage().deserialize(permissionMessage,
                    Placeholder.unparsed("permission", permission));
            sender.sendMessage(permMsg);
            SoundUtil.play(sender, permissionSounds);
            return false;
        }
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("reload") && sender.hasPermission("uhomes.reload")) {
                UHomes.getInstance().reload();
                MessageUtil.send(sender, MainConfig.getMessage("reload"));
                SoundUtil.play(sender, MainConfig.getSound("reload"));
            } else {
                if (sender instanceof Player player) {
                    String name = String.join(" ", args);
                    HomeManager homeManager = UHomes.getInstance().getHomeManager();
                    CompletableFuture<List<Home>> homesFuture = homeManager.getHomes(player.getUniqueId());
                    homesFuture.thenAccept(homes -> {
                        for (Home home : homes) {
                            if (home.getName() != null && home.getName().equalsIgnoreCase(name)) {
                                homeManager.teleport(player, home);
                                return;
                            }
                        }
                        MessageUtil.send(sender, MainConfig.getMessage("no_home_with_that_name"), Placeholder.unparsed("home_name", name));
                        SoundUtil.play(sender, MainConfig.getSound("no_home_with_that_name"));
                    });
                }

            }
        } else {
            if (sender instanceof Player player) {
                HomesGUI.open(player.getUniqueId(), player);
            }
        }
        return true;
    }

}
