package me.usainsrht.uhomes.gui;

import me.usainsrht.uhomes.Home;
import me.usainsrht.uhomes.HomeManager;
import me.usainsrht.uhomes.UHomes;
import me.usainsrht.uhomes.config.MainConfig;
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

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class HomesGUI {

    public static void open(UUID uuid, Player player) {
        Component title = MiniMessage.miniMessage().deserialize(MainConfig.getHomesGuiTitle());
        Inventory inventory = Bukkit.createInventory(null, 3*9, title);

        HomeManager homeManager = UHomes.getInstance().getHomeManager();
        CompletableFuture<List<Home>> future = homeManager.getHomes(uuid);
        future.thenAccept(homes -> {
            int rows = 2 + Math.min((int)Math.ceil(homes.size() / 7f), 4);
            open(player, homes, rows*9);
        });
        player.openInventory(inventory);

    }

    public static void open(Player player, List<Home> homes, int size) {
        Component title = MiniMessage.miniMessage().deserialize(MainConfig.getHomesGuiTitle());
        Inventory inventory = Bukkit.createInventory(null, size, title);

        int i = 0;
        for (Home home : homes) {
            Location location = home.getLocation();
            ConfigurationSection defaultIcon = MainConfig.getDefaultHomeItem();
            TagResolver[] placeholders = new TagResolver[]{};
            placeholders[0] = Formatter.number("x", location.getX());
            placeholders[1] = Formatter.number("y", location.getY());
            placeholders[2] = Formatter.number("z", location.getZ());
            placeholders[3] = Placeholder.parsed("world", MainConfig.getWorldName(location.getWorld().getName()));
            placeholders[4] = Formatter.date("created", Instant.ofEpochMilli(home.getCreated()));
            // date formatter with fallback because of LastTeleport can be -1 (haven't teleported yet)
            placeholders[5] = MMUtil.date("lastteleport", Instant.ofEpochMilli(home.getCreated()));
            placeholders[6] = Placeholder.parsed("index", String.valueOf(i));
            placeholders[7] = Placeholder.parsed("name", home.getName());

            ItemStack icon = home.getIcon() != null ? home.getIcon() : ItemUtil.getItemFromYaml(defaultIcon);
            ItemMeta iconMeta = icon.getItemMeta();

            Component displayName = MiniMessage.miniMessage().deserialize(defaultIcon.getString("name"), placeholders);
            iconMeta.displayName(displayName);

            List<Component> lore = new ArrayList<>();
            for (String line : defaultIcon.getStringList("lore")) {
                lore.add(MiniMessage.miniMessage().deserialize(line, placeholders));
            }
            iconMeta.lore(lore);
            inventory.setItem(getSlot(i), icon);

            i++;
        }
        player.openInventory(inventory);
    }

    public static int getSlot(int index) {
        return index < 8 ? 9+index : (index < 15 ? 11+index : (index < 22 ? 13+index : 15+index));
    }

}
