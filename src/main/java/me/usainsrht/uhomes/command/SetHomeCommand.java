package me.usainsrht.uhomes.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import me.usainsrht.uhomes.Home;
import me.usainsrht.uhomes.manager.HomeManager;
import me.usainsrht.uhomes.UHomes;
import me.usainsrht.uhomes.config.MainConfig;
import me.usainsrht.uhomes.gui.HomesGUI;
import me.usainsrht.uhomes.util.ItemUtil;
import me.usainsrht.uhomes.util.MMUtil;
import me.usainsrht.uhomes.util.MessageUtil;
import me.usainsrht.uhomes.util.SoundUtil;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class SetHomeCommand extends Command {

    private String permission;
    private String permissionMessage;
    private Collection<Sound> permissionSounds;

    public SetHomeCommand(YamlCommand cmd) {
        super(cmd.getName(), cmd.getDescription(), cmd.getUsage(), cmd.getAliases());
        this.permission = cmd.getPermission();
        this.permissionMessage = cmd.getPermissionMessage();
        this.permissionSounds = cmd.getPermissionSounds();
    }

    public LiteralCommandNode<?> getCommodoreCommand() {
        return LiteralArgumentBuilder.literal(super.getName())
                .then(RequiredArgumentBuilder.argument("home-name", StringArgumentType.greedyString()))
                .build();
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
        if (!(sender instanceof Player)) {
            MessageUtil.send(sender, MainConfig.getMessage("player_only_command"));
            return false;
        }
        Player player = (Player) sender;
        Location location = player.getLocation().clone();
        setHome(player, location, (args.length > 0) ? String.join(" ", args) : null);
        return true;
    }

    public static void setHome(Player player, Location location, @Nullable String name) {
        //check permissions because of call from gui
        YamlCommand cmd = MainConfig.getSetHomeCommand();
        if (!player.hasPermission(cmd.getPermission())) {
            Component permMsg = MiniMessage.miniMessage().deserialize(cmd.getPermissionMessage(),
                    Placeholder.unparsed("permission", cmd.getPermission()));
            player.sendMessage(permMsg);
            SoundUtil.play(player, cmd.getPermissionSounds());
            return;
        }
        if (MainConfig.isSethomeClaimCheck()) {
            if (!UHomes.getInstance().getClaimManager().getClaimAPI().canEnter(player, location)) {
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
            registerHome(player, location, name);
        } else if (MainConfig.isAskForNameBeforeSave()) {
            renameHome(player, null, location);
        } else {
            registerHome(player, location, null);
        }
    }

    /*
    * pass home null to create home
    * pass location null to rename home
    * */
    public static void renameHome(Player player, @Nullable Home home, @Nullable Location location) {
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
                            registerHome(player, location, name);
                            HomesGUI.open(player.getUniqueId(), player);
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

    private static void registerHome(Player player, Location location, @Nullable String name) {
        UUID uuid = player.getUniqueId();
        HomeManager homeManager = UHomes.getInstance().getHomeManager();
        CompletableFuture<List<Home>> homesFuture = homeManager.getHomes(uuid);
        homesFuture.thenAccept(homes -> {
            int homeLimit = homeManager.getHomeLimit(uuid);
            if (homes.size() >= homeLimit) {
                MessageUtil.send(player, MainConfig.getMessage("home_limit"), Formatter.number("home_limit", homeLimit));
                SoundUtil.play(player, MainConfig.getSound("home_limit"));
                return;
            }

            if (homes.stream().anyMatch(home -> home.getName() != null && home.getName().equalsIgnoreCase(name))) {
                MessageUtil.send(player, MainConfig.getMessage("home_name_already_in_use"), Placeholder.unparsed("home_name", name));
                SoundUtil.play(player, MainConfig.getSound("home_name_already_in_use"));
                return;
            }
            Home home = new Home(uuid, location);
            if (name != null) home.setName(name);
            homeManager.addHome(uuid, home);
            MessageUtil.send(player, MainConfig.getMessage("sethome"), Placeholder.unparsed("home_name", name == null ? "" : name));
            SoundUtil.play(player, MainConfig.getSound("sethome"));
        });
    }

}
