package me.usainsrht.uhomes.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemUtil {

    public static ItemStack getItemFromYaml(ConfigurationSection config, TagResolver... placeholders) {
        Material material = Material.matchMaterial(config.getString("material"));
        ItemStack itemStack = new ItemStack(material);
        ItemMeta itemMeta = itemStack.getItemMeta();

        if (config.isSet("name")) {
            String name = config.getString("name");
            Component parsedName = MiniMessage.miniMessage().deserialize(name, placeholders);
            itemMeta.displayName(parsedName);
        }

        if (config.isSet("lore")) {
            List<String> lore = config.getStringList("lore");
            List<Component> parsedLore = new ArrayList<>();
            for (String line : lore) {
                parsedLore.add(MiniMessage.miniMessage().deserialize(line, placeholders));
            }
            itemMeta.lore(parsedLore);
        }

        itemStack.setAmount(config.getInt("amount", 1));

        if (config.isSet("enchantments")) {
            ConfigurationSection enchs = config.getConfigurationSection("enchantments");
            for (String enchName : enchs.getKeys(false)) {
                NamespacedKey key = NamespacedKey.fromString(enchName);
                Enchantment ench = Registry.ENCHANTMENT.get(key);
                itemMeta.addEnchant(ench, enchs.getInt(enchName), true);
            }
        }

        if (config.isSet("item_flags")) {
            config.getStringList("item_flags").forEach(flag -> {
                itemMeta.addItemFlags(ItemFlag.valueOf(flag));
            });
        }

        //todo unbreakable, lether dye, skull, nbt, potion

        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public static String replace(String string, HashMap<String, String> placeholders) {
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            string = string.replace(entry.getKey(), entry.getValue());
        }
        return string;
    }

    public static List<String> replace(List<String> strings, HashMap<String, String> placeholders) {
        List<String> copy = new ArrayList<>();
        for (String string : strings) {
            copy.add(replace(string, placeholders));
        }
        return copy;
    }


}
