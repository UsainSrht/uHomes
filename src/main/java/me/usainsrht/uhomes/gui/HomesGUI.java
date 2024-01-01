package me.usainsrht.uhomes.gui;

import de.tr7zw.changeme.nbtapi.NBT;
import me.usainsrht.uhomes.Home;
import me.usainsrht.uhomes.HomeManager;
import me.usainsrht.uhomes.UHomes;
import me.usainsrht.uhomes.config.MainConfig;
import me.usainsrht.uhomes.util.InventoryUtil;
import me.usainsrht.uhomes.util.ItemUtil;
import me.usainsrht.uhomes.util.MMUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
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

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static net.kyori.adventure.text.format.TextDecoration.ITALIC;
import static net.kyori.adventure.text.format.TextDecoration.State.FALSE;

public class HomesGUI {

    public static void open(UUID uuid, Player player) {
        Component title = MiniMessage.miniMessage().deserialize(MainConfig.getHomesGuiTitle());
        Inventory inventory = Bukkit.createInventory(null, 3*9, title);

        InventoryUtil.fillInventory(inventory, ItemUtil.getItemFromYaml(MainConfig.getHomesGuiFillItem()));

        player.openInventory(inventory);
        player.setMetadata("HomesGUI", new FixedMetadataValue(UHomes.getInstance(), inventory));

        HomeManager homeManager = UHomes.getInstance().getHomeManager();
        CompletableFuture<List<Home>> future = homeManager.getHomes(uuid);
        future.thenAccept(homes -> {
            int rows = 2 + Math.min((int)Math.ceil(homes.size() / 7f), 4);
            open(player, homes, rows*9, homeManager.getHomeLimit(uuid));
        });

    }

    public static void open(Player player, List<Home> homes, int size, int maxHomes) {
        Component title = MiniMessage.miniMessage().deserialize(MainConfig.getHomesGuiTitle());
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

        int i = 0;
        for (Home home : homes) {
            ItemStack homeButton = getButton(home, i);
            NBT.modify(homeButton, nbt -> { nbt.getOrCreateCompound("Home").mergeCompound(home.getCompound()); });
            inventory.setItem(getSlot(i), homeButton);
            i++;
        }
        player.openInventory(inventory);
        player.setMetadata("HomesGUI", new FixedMetadataValue(UHomes.getInstance(), inventory));
    }

    public static int getSlot(int index) {
        return index < 8 ? 10+index : (index < 15 ? 12+index : (index < 22 ? 14+index : 16+index));
    }

    public static ItemStack getButton(Home home, int index) {
        Location location = home.getLocation();
        ConfigurationSection defaultIcon = MainConfig.getDefaultHomeItem();
        TagResolver[] placeholders = new TagResolver[8];
        placeholders[0] = Formatter.number("x", location.getX());
        placeholders[1] = Formatter.number("y", location.getY());
        placeholders[2] = Formatter.number("z", location.getZ());
        placeholders[3] = Placeholder.parsed("world", MainConfig.getWorldName(location.getWorld().getName()));
        placeholders[4] = Formatter.date("created", LocalDateTime.ofInstant(Instant.ofEpochMilli(home.getCreated()), ZoneOffset.UTC));
        // date formatter with fallback because of LastTeleport can be -1 (haven't teleported yet)
        placeholders[5] = MMUtil.date("last_teleport", LocalDateTime.ofInstant(Instant.ofEpochMilli(home.getLastTeleport()), ZoneOffset.UTC));
        placeholders[6] = Placeholder.parsed("index", String.valueOf(index));
        placeholders[7] = Placeholder.parsed("name",
                home.getName() == null ? MainConfig.getUnnamedHomeName().replace("<index>", String.valueOf(index)) : home.getName());

        ItemStack icon;
        if (home.getIcon() != null) {
            icon = home.getIcon();
            ItemMeta iconMeta = icon.getItemMeta();

            Component displayName = MiniMessage.miniMessage().deserialize(defaultIcon.getString("name"), placeholders).decorationIfAbsent(ITALIC, FALSE);
            iconMeta.displayName(displayName);

            List<Component> lore = new ArrayList<>();
            for (String line : defaultIcon.getStringList("lore")) {
                lore.add(MiniMessage.miniMessage().deserialize(line, placeholders).decorationIfAbsent(ITALIC, FALSE));
            }
            iconMeta.lore(lore);
        } else {
            icon = ItemUtil.getItemFromYaml(defaultIcon, placeholders);
        }

        if (MainConfig.isHomesGuiIndexAmount()) icon.setAmount(index + 1);

        return icon;
    }

}
