package me.usainsrht.uhomes.config;

import me.usainsrht.uhomes.command.YamlCommand;
import me.usainsrht.uhomes.util.ItemUtil;
import me.usainsrht.uhomes.util.SoundUtil;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class MainConfig {

    private static String prefix;

    private static HashMap<String, Collection<String>> messages;
    private static HashMap<String, Collection<Sound>> sounds;

    private static String homeLimitPermission;
    private static boolean sumHomeLimits;
    private static int homeNameCharLimit;
    private static boolean askForNameBeforeSave;

    private static String homesGuiTitle;
    private static boolean homesGuiIndexAmount;
    private static ConfigurationSection homesGuiFillItem;
    private static ConfigurationSection defaultHomeItem;
    private static ConfigurationSection noHomeItem;
    private static ConfigurationSection setHomeItem;

    private static HashMap<String, String> worldNames;

    private static YamlCommand homeCommand;
    private static YamlCommand setHomeCommand;
    //todo delhome

    public static void create(ConfigurationSection config) {

        prefix = config.getString("prefix");

        messages = new HashMap<>();
        config.getConfigurationSection("messages").getKeys(false).forEach(key -> {
            List<String> msgCollection = new ArrayList<>();
            if (config.isString("messages." + key)) msgCollection.add(config.getString("messages." + key));
            else if (config.isList("messages." + key)) msgCollection.addAll(config.getStringList("messages." + key));
            messages.put(key, msgCollection);
        });

        sounds = new HashMap<>();
        config.getConfigurationSection("sounds").getKeys(false).forEach(key -> {
            List<Sound> soundList = new ArrayList<>();
            if (config.isString("sounds." + key)) soundList.add(SoundUtil.getSound(config.getString("sounds." + key)));
            else if (config.isList("sounds." + key)) config.getStringList("sounds." + key).forEach(string -> soundList.add(SoundUtil.getSound(string)));
            sounds.put(key, soundList);
        });

        homeLimitPermission = config.getString("home_limit_permission");
        sumHomeLimits = config.getBoolean("sum_limit_permissions");
        homeNameCharLimit = config.getInt("home_name_character_limit");
        askForNameBeforeSave = config.getBoolean("ask_for_name_before_save");

        homesGuiTitle = config.getString("gui.title");
        homesGuiIndexAmount = config.getBoolean("gui.index_amount");
        // needs to be parsed to item with different placeholders everytime
        homesGuiFillItem = config.getConfigurationSection("gui.fill");
        defaultHomeItem = config.getConfigurationSection("gui.default_home_icon");
        noHomeItem = config.getConfigurationSection("gui.no_home");
        setHomeItem = config.getConfigurationSection("gui.sethome");

        worldNames = new HashMap<>();
        config.getConfigurationSection("world_names").getKeys(false).forEach(key -> {
            worldNames.put(key, config.getString("world_names."+key));
            //todo save component instead of string
        });

        homeCommand = new YamlCommand(
                config.getString("commands.home.name"),
                config.getString("commands.home.description"),
                config.getString("commands.home.usage"),
                config.getStringList("commands.home.aliases"),
                config.getString("commands.home.permission"),
                config.getString("commands.home.permission_message"),
                SoundUtil.getSounds(config.get("commands.home.permission_sounds")));
        setHomeCommand = new YamlCommand(
                config.getString("commands.sethome.name"),
                config.getString("commands.sethome.description"),
                config.getString("commands.sethome.usage"),
                config.getStringList("commands.sethome.aliases"),
                config.getString("commands.sethome.permission"),
                config.getString("commands.sethome.permission_message"),
                SoundUtil.getSounds(config.get("commands.sethome.permission_sounds")));

    }

    public static String getPrefix() {
        return prefix;
    }

    public static Collection<String> getMessage(String message) {
        return messages.getOrDefault(message, List.of(message));
    }

    public static Collection<Sound> getSound(String sound) {
        return sounds.getOrDefault(sound, Collections.emptyList());
    }

    public static String getWorldName(String name) {
        return worldNames.getOrDefault(name, name);
    }

    public static boolean isAskForNameBeforeSave() {
        return askForNameBeforeSave;
    }

    public static boolean isHomesGuiIndexAmount() {
        return homesGuiIndexAmount;
    }

    public static boolean isSumHomeLimits() {
        return sumHomeLimits;
    }

    public static int getHomeNameCharLimit() {
        return homeNameCharLimit;
    }

    public static ConfigurationSection getDefaultHomeItem() {
        return defaultHomeItem;
    }

    public static ConfigurationSection getHomesGuiFillItem() {
        return homesGuiFillItem;
    }

    public static ConfigurationSection getNoHomeItem() {
        return noHomeItem;
    }

    public static ConfigurationSection getSetHomeItem() {
        return setHomeItem;
    }

    public static String getHomeLimitPermission() {
        return homeLimitPermission;
    }

    public static String getHomesGuiTitle() {
        return homesGuiTitle;
    }

    public static YamlCommand getHomeCommand() {
        return homeCommand;
    }

    public static YamlCommand getSetHomeCommand() {
        return setHomeCommand;
    }
}
