package me.usainsrht.uhomes.util;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class InventoryUtil {

    public static void fillInventory(Inventory inv, ItemStack item) {
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, item);
        }
    }

}
