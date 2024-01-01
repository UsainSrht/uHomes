package me.usainsrht.uhomes.listener;

import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.iface.ReadableNBT;
import me.usainsrht.uhomes.Home;
import me.usainsrht.uhomes.HomeManager;
import me.usainsrht.uhomes.command.SetHomeCommand;
import me.usainsrht.uhomes.gui.HomeButtonAction;
import me.usainsrht.uhomes.gui.HomesGUI;
import me.usainsrht.uhomes.util.ItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public class InventoryClickListener implements Listener {

    private HomeManager homeManager;

    public InventoryClickListener(HomeManager homeManager) {
        this.homeManager = homeManager;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        if (!player.hasMetadata("HomesGUI") || player.getMetadata("HomesGUI").isEmpty()) return;
        Inventory homesGUI = (Inventory) player.getMetadata("HomesGUI").get(0).value();

        Inventory inventory = e.getClickedInventory();
        if (inventory == null || inventory != homesGUI) return;
        e.setCancelled(true);

        ItemStack item = e.getCurrentItem();
        if (item == null || item.isEmpty()) return;
        NBT.get(item, nbt -> {
            if (nbt.hasTag("Home")) {
                player.closeInventory();
                ReadableNBT homeCompound = nbt.getCompound("Home");
                homeManager.getHomes(homeCompound.getUUID("Owner")).thenAccept(homes -> {
                    Optional<Home> optionalHome = homes.stream()
                            .filter(home -> home.getCreated() == homeCompound.getLong("Created"))
                            .findFirst();
                    optionalHome.ifPresent(home -> {
                        ItemStack cursor = e.getCursor();
                        if (!cursor.getType().isAir()) {
                            home.setIcon(cursor.clone());
                            HomesGUI.open(home.getOwner(), player);
                            return;
                        }
                        HomeButtonAction action = HomeButtonAction.getFromClick(e.getClick());
                        if (action == null) return;
                        switch (action) {
                            case TELEPORT -> homeManager.teleport(player, home);
                            case RELOCATE -> {
                                homeManager.relocate(player, home);
                                HomesGUI.open(home.getOwner(), player);
                            }
                            case RENAME -> homeManager.rename(player, home);
                            case DELETE -> {
                                homeManager.delete(player, home);
                                HomesGUI.open(home.getOwner(), player);
                            }
                        }

                    });
                });
            } else if (nbt.hasTag("sethome")) {
                player.closeInventory();
                SetHomeCommand.setHome(player, player.getLocation(), null);
            }
        });
    }

}