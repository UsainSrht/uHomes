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
import me.usainsrht.uhomes.util.DialogImpl;
import me.usainsrht.uhomes.util.DialogUtil;
import me.usainsrht.uhomes.util.ItemUtil;
import me.usainsrht.uhomes.util.MMUtil;
import me.usainsrht.uhomes.util.MessageUtil;
import me.usainsrht.uhomes.util.SoundUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class SetHomeCommand {

    private SetHomeCommand() {
    }

    public static LiteralCommandNode<CommandSourceStack> buildNode(YamlCommand cmd) {
        return Commands.literal(cmd.getName())
                .executes(ctx -> execute(ctx.getSource(), cmd, null))
                .then(Commands.argument("home-name", StringArgumentType.greedyString())
                        .executes(ctx -> execute(
                                ctx.getSource(),
                                cmd,
                                ctx.getArgument("home-name", String.class))))
                .build();
    }

    private static int execute(CommandSourceStack source, YamlCommand cmd, @Nullable String name) {
        if (!source.getSender().hasPermission(cmd.getPermission())) {
            Component permMsg = MiniMessage.miniMessage().deserialize(cmd.getPermissionMessage(),
                    Placeholder.unparsed("permission", cmd.getPermission()));
            source.getSender().sendMessage(permMsg);
            SoundUtil.play(source.getSender(), cmd.getPermissionSounds());
            return 0;
        }
        if (!(source.getSender() instanceof Player player)) {
            MessageUtil.send(source.getSender(), MainConfig.getMessage("player_only_command"));
            return 0;
        }
        Location location = player.getLocation().clone();
        setHome(player, location, name);
        return Command.SINGLE_SUCCESS;
    }

    public static void setHome(Player player, Location location, @Nullable String name) {
        setHome(player, player.getUniqueId(), location, name);
    }

    public static void setHome(Player player, UUID targetUuid, Location location, @Nullable String name) {
        //check permissions because of call from gui
        YamlCommand cmd = MainConfig.getSetHomeCommand();
        boolean managingOthers = MainConfig.isManagingOthersHomes(player, targetUuid);
        if (managingOthers) {
            if (!player.hasPermission(MainConfig.getHomeSeePermission())) {
                Component permMsg = MiniMessage.miniMessage().deserialize(cmd.getPermissionMessage(),
                        Placeholder.unparsed("permission", MainConfig.getHomeSeePermission()));
                player.sendMessage(permMsg);
                SoundUtil.play(player, cmd.getPermissionSounds());
                return;
            }
        } else if (!player.hasPermission(cmd.getPermission())) {
            Component permMsg = MiniMessage.miniMessage().deserialize(cmd.getPermissionMessage(),
                    Placeholder.unparsed("permission", cmd.getPermission()));
            player.sendMessage(permMsg);
            SoundUtil.play(player, cmd.getPermissionSounds());
            return;
        }
        if (!managingOthers && location.getWorld() != null
                && MainConfig.isWorldBlacklisted(location.getWorld().getName())) {
            MessageUtil.send(player, MainConfig.getMessage("blacklisted_world"),
                    Placeholder.parsed("world", MainConfig.getWorldName(location.getWorld().getName())));
            SoundUtil.play(player, MainConfig.getSound("blacklisted_world"));
            return;
        }
        if (MainConfig.isSethomeClaimCheck() && !managingOthers) {
            if (!UHomes.getInstance().getClaimManager().canEnter(player, location)) {
                MessageUtil.send(player, MainConfig.getMessage("not_allowed_to_sethome"));
                SoundUtil.play(player, MainConfig.getSound("not_allowed_to_sethome"));
                return;
            }
        }
        if (name != null) {
            name = name.trim();
            if (!MainConfig.getHomeNameCharLimit().isInBetween(name.length())) {
                MessageUtil.send(player, MainConfig.getMessage("home_name_limit"),
                        Formatter.number("min", MainConfig.getHomeNameCharLimit().getMin()),
                        Formatter.number("max", MainConfig.getHomeNameCharLimit().getMax()),
                        Formatter.number("home_name_char_size", name.length()),
                        Placeholder.unparsed("home_name", name));
                SoundUtil.play(player, MainConfig.getSound("home_name_limit"));
                return;
            }
            if (!name.matches(MainConfig.getHomeNameValidChars())) {
                MessageUtil.send(player, MainConfig.getMessage("home_name_not_valid"),
                        Placeholder.unparsed("home_name", name),
                        Placeholder.unparsed("invalid_characters", String.join(" ", name.split(MainConfig.getHomeNameValidChars()))));
                SoundUtil.play(player, MainConfig.getSound("home_name_not_valid"));
                return;
            }
            registerHome(player, targetUuid, location, name, managingOthers);
        } else if (MainConfig.isAskForNameBeforeSave()) {
            renameHome(player, null, location, targetUuid, managingOthers);
        } else {
            registerHome(player, targetUuid, location, null, managingOthers);
        }
    }

    public static void renameHome(Player player, @Nullable Home home, @Nullable Location location) {
        UUID targetUuid = home != null ? home.getOwner() : player.getUniqueId();
        renameHome(player, home, location, targetUuid, MainConfig.isManagingOthersHomes(player, targetUuid));
    }

    /*
    * pass home null to create home
    * pass location null to rename home
    * */
    public static void renameHome(Player player, @Nullable Home home, @Nullable Location location,
                                  UUID targetUuid, boolean managingOthers) {
        if (MainConfig.isDialogEnabled() && DialogUtil.isSupported()) {
            DialogImpl.sendHomeForm(player, home, location, (name) -> {
                if (home == null) {
                    registerHome(player, targetUuid, location, name, managingOthers);
                    HomesGUI.open(targetUuid, player);
                }
                else {
                    home.setName(name);
                    HomesGUI.open(home.getOwner(), player);
                }
            });
            return;
        }

        if (!MainConfig.isAnvilGuiEnabled()) {
            // Fallback to chat or just do nothing since anvil is disabled and dialog is unsupported
            return;
        }

        AnvilGUI.Builder builder = new AnvilGUI.Builder()
                .plugin(UHomes.getInstance())
                .jsonTitle(MMUtil.mmStringToJson(MainConfig.getSetHomeGuiTitle()));

        MainConfig.getSetHomeGuiSlots().forEach((slot, item) -> {
            if (slot == 0) {
                if (home == null) item.set("name", MainConfig.getSetHomeGuiText());
                else if (home.getName() != null) item.set("name", home.getName());
                builder.itemLeft(ItemUtil.getItemFromYaml(item));
            }
            else if (slot == 1) builder.itemRight(ItemUtil.getItemFromYaml(item));
            else if (slot == 2) builder.itemOutput(ItemUtil.getItemFromYaml(item));
        });

        builder.onClick((slot, stateSnapshot) -> {
            if (slot != AnvilGUI.Slot.OUTPUT) {
                return Collections.emptyList();
            }

            String name = stateSnapshot.getText().trim();
            if (!MainConfig.getHomeNameCharLimit().isInBetween(name.length())) {
                return Arrays.asList(
                        AnvilGUI.ResponseAction.updateJsonTitle(MMUtil.mmStringToJson(MainConfig.getSetHomeGuiTitleCharLimit(),
                                Formatter.number("min", MainConfig.getHomeNameCharLimit().getMin()),
                                Formatter.number("max", MainConfig.getHomeNameCharLimit().getMax()),
                                Formatter.number("home_name_char_size", name.length()),
                                Placeholder.unparsed("home_name", name)
                        ), true),
                        AnvilGUI.ResponseAction.run(() -> SoundUtil.play(player, MainConfig.getSound("home_name_limit")))
                );
            }
            if (!name.matches(MainConfig.getHomeNameValidChars())) {
                return Arrays.asList(
                        AnvilGUI.ResponseAction.updateJsonTitle(MMUtil.mmStringToJson(MainConfig.getSetHomeGuiTitleNotValid(),
                                Placeholder.unparsed("home_name", name),
                                Placeholder.unparsed("invalid_characters", String.join(" ", name.split(MainConfig.getHomeNameValidChars())))
                        ), true),
                        AnvilGUI.ResponseAction.run(() -> SoundUtil.play(player, MainConfig.getSound("home_name_not_valid")))
                );
            }

            return Arrays.asList(
                    AnvilGUI.ResponseAction.close(),
                    AnvilGUI.ResponseAction.run(() -> {
                        if (home == null) {
                            registerHome(player, targetUuid, location, name, managingOthers);
                            HomesGUI.open(targetUuid, player);
                        }
                        else {
                            home.setName(name);
                            HomesGUI.open(home.getOwner(), player);
                        }
                    })
            );
        });

        builder.open(player);
    }

    private static void registerHome(Player player, UUID targetUuid, Location location, @Nullable String name,
                                     boolean bypassLimit) {
        HomeManager homeManager = UHomes.getInstance().getHomeManager();
        CompletableFuture<List<Home>> homesFuture = homeManager.getHomes(targetUuid);
        homesFuture.thenAccept(homes -> {
            if (!bypassLimit) {
                int homeLimit = homeManager.getHomeLimit(targetUuid);
                if (homes.size() >= homeLimit) {
                    MessageUtil.send(player, MainConfig.getMessage("home_limit"), Formatter.number("home_limit", homeLimit));
                    SoundUtil.play(player, MainConfig.getSound("home_limit"));
                    return;
                }
            }

            if (homes.stream().anyMatch(home -> home.getName() != null && home.getName().equalsIgnoreCase(name))) {
                MessageUtil.send(player, MainConfig.getMessage("home_name_already_in_use"), Placeholder.unparsed("home_name", name));
                SoundUtil.play(player, MainConfig.getSound("home_name_already_in_use"));
                return;
            }
            Home home = new Home(targetUuid, location);
            if (name != null) home.setName(name);
            homeManager.addHome(targetUuid, home);
            MessageUtil.send(player, MainConfig.getMessage("sethome"), Placeholder.unparsed("home_name", name == null ? "" : name));
            SoundUtil.play(player, MainConfig.getSound("sethome"));
        });
    }

}
