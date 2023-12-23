package me.usainsrht.uhomes.gui;

import me.usainsrht.uhomes.Home;
import me.usainsrht.uhomes.HomeManager;
import me.usainsrht.uhomes.UHomes;
import me.usainsrht.uhomes.config.MainConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class HomesGUI {

    public static void open(UUID uuid, Player player) {
        Component title = MiniMessage.miniMessage().deserialize(MainConfig.getHomesGuiTitle());
        Inventory inventory = Bukkit.createInventory(null, 3*9, title);

        HomeManager homeManager = UHomes.getInstance().getHomeManager();
        CompletableFuture<List<Home>> future = homeManager.getHomes(uuid);
        future.thenAccept(new Consumer<>() {
            private int i = 0;

            @Override
            public void accept(List<Home> homes) {
                int rows = 2 + (int) Math.ceil(homes.size() / 7f);
                if (rows*9 != inventory.getSize()) {
                    open(uuid, player, homes);
                    return;
                }
                Home home = homes.get(i);
                ItemStack icon = home.getIcon() != null ? home.getIcon() : MainConfig.getDefaultHomeItem();

                i++;
            }
        });

        player.openInventory(inventory);

    }

    public static void open(UUID uuid, Player player, List<Home> homes) {

    }

}
