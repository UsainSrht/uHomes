package me.usainsrht.uhomes.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import me.usainsrht.uhomes.Home;
import me.usainsrht.uhomes.UHomes;
import me.usainsrht.uhomes.config.MainConfig;
import me.usainsrht.uhomes.gui.HomesGUI;
import me.usainsrht.uhomes.manager.HomeManager;
import me.usainsrht.uhomes.util.MessageUtil;
import me.usainsrht.uhomes.util.PlayerUtil;
import me.usainsrht.uhomes.util.SchedulerUtil;
import me.usainsrht.uhomes.util.SoundUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class HomeCommand {

    private HomeCommand() {
    }

    public static LiteralCommandNode<CommandSourceStack> buildNode(YamlCommand cmd) {
        return Commands.literal(cmd.getName())
                .executes(ctx -> openGui(ctx.getSource(), cmd))
                .then(Commands.literal("reload")
                        .requires(source -> source.getSender().hasPermission("uhomes.reload"))
                        .executes(ctx -> reload(ctx.getSource())))
                .then(Commands.literal("see")
                        .requires(source -> source.getSender().hasPermission(MainConfig.getHomeSeePermission()))
                        .then(Commands.argument("player", StringArgumentType.greedyString())
                                .suggests((ctx, builder) -> {
                                    Bukkit.getOnlinePlayers().stream()
                                            .map(Player::getName)
                                            .filter(name -> name.toLowerCase().startsWith(builder.getRemainingLowerCase()))
                                            .forEach(builder::suggest);
                                    return builder.buildFuture();
                                })
                                .executes(ctx -> seeOthersHomes(
                                        ctx.getSource(),
                                        cmd,
                                        ctx.getArgument("player", String.class)))))
                .then(Commands.argument("home-name", StringArgumentType.greedyString())
                        .suggests((ctx, builder) -> {
                            if (ctx.getSource().getExecutor() instanceof Player player) {
                                CompletableFuture<List<Home>> homesFuture = UHomes.getInstance().getHomeManager()
                                        .getHomes(player.getUniqueId());
                                if (homesFuture.isDone()) {
                                    try {
                                        homesFuture.get().stream()
                                                .map(Home::getName)
                                                .filter(Objects::nonNull)
                                                .filter(name -> name.toLowerCase().startsWith(builder.getRemainingLowerCase()))
                                                .forEach(builder::suggest);
                                    } catch (Exception ignore) {
                                    }
                                }
                            }
                            return builder.buildFuture();
                        })
                        .executes(ctx -> teleportToHome(
                                ctx.getSource(),
                                cmd,
                                ctx.getArgument("home-name", String.class))))
                .build();
    }

    private static int openGui(CommandSourceStack source, YamlCommand cmd) {
        if (!checkPermission(source, cmd)) {
            return 0;
        }
        if (!(source.getSender() instanceof Player player)) {
            MessageUtil.send(source.getSender(), MainConfig.getMessage("player_only_command"));
            return 0;
        }
        HomesGUI.open(player.getUniqueId(), player);
        return Command.SINGLE_SUCCESS;
    }

    private static int reload(CommandSourceStack source) {
        CommandSender sender = source.getSender();
        UHomes.getInstance().reload();
        MessageUtil.send(sender, MainConfig.getMessage("reload"));
        SoundUtil.play(sender, MainConfig.getSound("reload"));
        return Command.SINGLE_SUCCESS;
    }

    private static int seeOthersHomes(CommandSourceStack source, YamlCommand cmd, String targetName) {
        if (!checkPermission(source, cmd)) {
            return 0;
        }
        if (!(source.getSender() instanceof Player player)) {
            MessageUtil.send(source.getSender(), MainConfig.getMessage("player_only_command"));
            return 0;
        }
        if (!source.getSender().hasPermission(MainConfig.getHomeSeePermission())) {
            denyPermission(source, cmd.getPermissionMessage(), MainConfig.getHomeSeePermission(), cmd.getPermissionSounds());
            return 0;
        }
        seeOthersHomes(player, targetName);
        return Command.SINGLE_SUCCESS;
    }

    private static int teleportToHome(CommandSourceStack source, YamlCommand cmd, String name) {
        if (!checkPermission(source, cmd)) {
            return 0;
        }
        if (!(source.getSender() instanceof Player player)) {
            MessageUtil.send(source.getSender(), MainConfig.getMessage("player_only_command"));
            return 0;
        }
        HomeManager homeManager = UHomes.getInstance().getHomeManager();
        CompletableFuture<List<Home>> homesFuture = homeManager.getHomes(player.getUniqueId());
        homesFuture.thenAccept(homes -> {
            for (Home home : homes) {
                if (home.getName() != null && home.getName().equalsIgnoreCase(name)) {
                    homeManager.teleport(player, home);
                    return;
                }
            }
            MessageUtil.send(source.getSender(), MainConfig.getMessage("no_home_with_that_name"),
                    Placeholder.unparsed("home_name", name));
            SoundUtil.play(source.getSender(), MainConfig.getSound("no_home_with_that_name"));
        });
        return Command.SINGLE_SUCCESS;
    }

    private static boolean checkPermission(CommandSourceStack source, YamlCommand cmd) {
        if (source.getSender().hasPermission(cmd.getPermission())) {
            return true;
        }
        denyPermission(source, cmd.getPermissionMessage(), cmd.getPermission(), cmd.getPermissionSounds());
        return false;
    }

    private static void denyPermission(CommandSourceStack source, String permissionMessage, String permission,
                                       java.util.Collection<net.kyori.adventure.sound.Sound> permissionSounds) {
        Component permMsg = MiniMessage.miniMessage().deserialize(permissionMessage,
                Placeholder.unparsed("permission", permission));
        source.getSender().sendMessage(permMsg);
        SoundUtil.play(source.getSender(), permissionSounds);
    }

    private static void seeOthersHomes(Player player, String targetName) {
        SchedulerUtil.runAsync(UHomes.getInstance(), () -> {
            UUID targetUuid = PlayerUtil.resolvePlayerUuid(targetName);
            String resolvedName = targetUuid != null ? PlayerUtil.getPlayerName(targetUuid) : null;
            SchedulerUtil.runNextTick(UHomes.getInstance(), () -> {
                if (targetUuid == null || resolvedName == null) {
                    MessageUtil.send(player, MainConfig.getMessage("player_not_found"),
                            Placeholder.unparsed("player", targetName));
                    SoundUtil.play(player, MainConfig.getSound("player_not_found"));
                    return;
                }
                MessageUtil.send(player, MainConfig.getMessage("see_open"),
                        Placeholder.unparsed("player", resolvedName));
                SoundUtil.play(player, MainConfig.getSound("see_open"));
                HomesGUI.open(targetUuid, player);
            });
        });
    }

}
