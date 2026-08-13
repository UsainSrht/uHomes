package me.usainsrht.uhomes.gui;

import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBT;
import me.usainsrht.uhomes.Home;
import me.usainsrht.uhomes.UHomes;
import me.usainsrht.uhomes.manager.HomeManager;
import me.usainsrht.uhomes.config.MainConfig;
import me.usainsrht.uhomes.util.InventoryUtil;
import me.usainsrht.uhomes.util.ItemUtil;
import me.usainsrht.uhomes.util.MMUtil;
import me.usainsrht.uhomes.util.PlayerUtil;
import me.usainsrht.uhomes.util.SchedulerUtil;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;

import javax.annotation.Nullable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class HomesGUI {

    public static void open(UUID uuid, Player player) {
        if (!player.isOnline()) {
            return;
        }
        HomeManager homeManager = UHomes.getInstance().getHomeManager();
        homeManager.getHomes(uuid).thenAccept(homes -> SchedulerUtil.runNextTick(UHomes.getInstance(), () -> {
            if (!player.isOnline()) {
                return;
            }
            // maximum 4 rows of homes // get 1 even if 0 homes
            int rows = 2 + Math.min(Math.max((int) Math.ceil(homes.size() / 7f), 1), 4);
            open(player, uuid, homes, rows * 9, homeManager.getHomeLimit(uuid));
        }));
    }

    public static void open(Player player, List<Home> homes, int size, int maxHomes) {
        open(player, player.getUniqueId(), homes, size, maxHomes);
    }

    public static void open(Player player, UUID ownerUuid, List<Home> homes, int size, int maxHomes) {
        homes = HomeManager.sortHomes(homes);
        boolean viewingOthers = !ownerUuid.equals(player.getUniqueId());
        String titleString = viewingOthers
                ? MainConfig.getHomesGuiTitleOthers()
                : MainConfig.getHomesGuiTitle();
        if (viewingOthers) {
            String ownerName = PlayerUtil.getPlayerName(ownerUuid);
            if (ownerName != null) {
                titleString = titleString.replace("<player>", ownerName);
            }
        }
        Component title = MMUtil.deserialize(player, titleString);
        Inventory inventory = Bukkit.createInventory(null, size, title);

        InventoryUtil.fillInventory(inventory, ItemUtil.getItemFromYaml(MainConfig.getHomesGuiFillItem()));

        if (homes.size() == 0) {
            ItemStack noHome = ItemUtil.getItemFromYaml(MainConfig.getNoHomeItem());
            NBT.modify(noHome, nbt -> { nbt.setBoolean("sethome", true); });
            inventory.setItem(13, noHome);
        }

        ItemStack setHome = ItemUtil.getItemFromYaml(MainConfig.getSetHomeItem(),
                Formatter.number("home_size", homes.size()),
                Formatter.number("max_home_size", maxHomes));
        NBT.modify(setHome, nbt -> { nbt.setBoolean("sethome", true); });
        inventory.setItem(size-5, setHome);

        if (MainConfig.isHomesGuiInfoEnabled()) {
            ItemStack infoItem = ItemUtil.getItemFromYaml(MainConfig.getHomesGuiInfoItem(), MainConfig.getGuiActionPlaceholders());
            inventory.setItem(MainConfig.getHomesGuiInfoSlot(), infoItem);
        }

        int i = 0;
        for (Home home : homes) {
            ItemStack homeButton = getButton(home, i);
            inventory.setItem(getSlot(i), homeButton);
            i++;
        }
        player.openInventory(inventory);
        player.setMetadata("HomesGUI", new FixedMetadataValue(UHomes.getInstance(), inventory));
        player.setMetadata("HomesGUIOwner", new FixedMetadataValue(UHomes.getInstance(), ownerUuid));
    }

    public static int getSlot(int index) {
        return index < 7 ? 10+index : (index < 14 ? 12+index : (index < 21 ? 14+index : 16+index));
    }

    public static ItemStack getButton(Home home, int index) {
        Location location = home.getLocation();
        ConfigurationSection defaultIcon = MainConfig.getDefaultHomeItem();
        TagResolver[] placeholders = new TagResolver[8];
        placeholders[0] = Formatter.number("x", location.getX());
        placeholders[1] = Formatter.number("y", location.getY());
        placeholders[2] = Formatter.number("z", location.getZ());
        placeholders[3] = Placeholder.parsed("world", MainConfig.getWorldName(location.getWorld().getName()));
        placeholders[4] = Formatter.date("created", LocalDateTime.ofInstant(Instant.ofEpochMilli(home.getCreated()), ZoneId.systemDefault()));
        // date formatter with fallback because of LastTeleport can be -1 (haven't teleported yet)
        placeholders[5] = MMUtil.date("last_teleport", LocalDateTime.ofInstant(Instant.ofEpochMilli(home.getLastTeleport()), ZoneId.systemDefault()));
        placeholders[6] = Placeholder.parsed("index", String.valueOf(index));
        placeholders[7] = Placeholder.parsed("name", getHomeName(home, index));

        ItemStack icon;
        if (home.getIcon() != null) {
            icon = home.getIcon();
            ItemMeta iconMeta = icon.getItemMeta();

            Component displayName = MMUtil.deserializeItem(defaultIcon.getString("name"), placeholders);
            iconMeta.displayName(displayName);

            List<Component> lore = new ArrayList<>();
            for (String line : defaultIcon.getStringList("lore")) {
                lore.add(MMUtil.deserializeItem(line, placeholders));
            }
            iconMeta.lore(lore);

            if (defaultIcon.isSet("hide-tooltip")) {
                iconMeta.setHideTooltip(defaultIcon.getBoolean("hide-tooltip"));
            }

            icon.setItemMeta(iconMeta);
        } else {
            icon = ItemUtil.getItemFromYaml(defaultIcon, placeholders);
        }

        NBT.modify(icon, nbt -> {
            ReadWriteNBT homeCompound = nbt.getOrCreateCompound("Home");
            homeCompound.mergeCompound(home.getCompound());
            homeCompound.setInteger("Index", index);
        });

        if (MainConfig.isHomesGuiIndexAmount()) icon.setAmount(index + 1);

        return icon;
    }

    public static List<DialogBody> getDialogBodies(Home home, int index, Component message) {
        return getDialogBodies(home, index, message, null);
    }

    public static List<DialogBody> getDialogBodies(Home home, int index, Component message, @Nullable Component error) {
        List<DialogBody> bodies = new ArrayList<>();
        if (home != null && index >= 0) {
            bodies.add(DialogBody.item(getButton(home, index))
                    .showTooltip(true)
                    .showDecorations(true)
                    .build());
        }
        if (error != null) {
            bodies.add(DialogBody.plainMessage(error));
        }
        bodies.add(DialogBody.plainMessage(message));
        return bodies;
    }

    public static ItemStack getAnvilLeftItem(Home home, int index, @Nullable String displayNameOverride,
                                             TagResolver... placeholders) {
        ItemStack item = getButton(home, index).clone();
        if (displayNameOverride != null) {
            ItemMeta meta = item.getItemMeta();
            meta.displayName(MMUtil.deserializeItem(displayNameOverride, placeholders));
            item.setItemMeta(meta);
        }
        return item;
    }

    public static String getHomeName(Home home, int index) {
        return home.getName() != null ? home.getName() : MainConfig.getUnnamedHomeName().replace("<index>", String.valueOf(index));
    }

}
