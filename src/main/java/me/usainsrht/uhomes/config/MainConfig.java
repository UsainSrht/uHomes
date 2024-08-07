package me.usainsrht.uhomes.config;

import me.usainsrht.uhomes.IntArray;
import me.usainsrht.uhomes.command.YamlCommand;
import me.usainsrht.uhomes.gui.HomeButtonAction;
import me.usainsrht.uhomes.util.SoundUtil;
import net.kyori.adventure.sound.Sound;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

public class MainConfig {

    private static String prefix;

    private static HashMap<String, Collection<String>> messages;
    private static HashMap<String, Collection<Sound>> sounds;

    private static String homeLimitPermission;
    private static boolean sumHomeLimits;
    private static boolean lpHomeLimit;
    private static String lpHomeLimitName;
    private static IntArray homeNameCharLimit;
    private static String homeNameValidChars;
    private static boolean askForNameBeforeSave;
    private static String tpBetweenWorldsPerm;
    private static String unnamedHomeName;
    private static boolean loadChunkBeforeTp;
    private static String homeTeleportTimePerm;
    private static boolean sethomeClaimCheck;
    private static boolean teleportClaimCheck;

    private static String homesGuiTitle;
    private static boolean homesGuiIndexAmount;
    private static ConfigurationSection homesGuiFillItem;
    private static ConfigurationSection defaultHomeItem;
    private static ConfigurationSection noHomeItem;
    private static ConfigurationSection setHomeItem;

    private static HomeButtonAction homeButtonLeftClick;
    private static HomeButtonAction homeButtonRightClick;
    private static HomeButtonAction homeButtonLeftClickWithShift;
    private static HomeButtonAction homeButtonRightClickWithShift;

    private static String setHomeGuiTitle;
    private static String setHomeGuiTitleNotValid;
    private static String setHomeGuiTitleCharLimit;
    private static String setHomeGuiText;
    private static HashMap<Integer, ConfigurationSection> setHomeGuiSlots;

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
        lpHomeLimit = config.getBoolean("luckperms_meta_limit");
        lpHomeLimitName = config.getString("luckperms_meta_name");
        homeNameCharLimit = new IntArray(config.getString("home_name_character_limit"));
        homeNameValidChars = config.getString("home_name_valid_characters");
        askForNameBeforeSave = config.getBoolean("ask_for_name_before_save");
        tpBetweenWorldsPerm = config.getString("teleport_between_worlds_permission");
        unnamedHomeName = config.getString("unnamed_home");
        loadChunkBeforeTp = config.getBoolean("load_chunk_before_tp");
        homeTeleportTimePerm = config.getString("home_teleport_time_permission");
        sethomeClaimCheck = config.getBoolean("sethome_claim_check");
        teleportClaimCheck = config.getBoolean("teleport_claim_check");

        homesGuiTitle = config.getString("gui.title");
        homesGuiIndexAmount = config.getBoolean("gui.index_amount");
        // needs to be parsed to item with different placeholders everytime
        homesGuiFillItem = config.getConfigurationSection("gui.fill");
        defaultHomeItem = config.getConfigurationSection("gui.default_home_icon");
        noHomeItem = config.getConfigurationSection("gui.no_home");
        setHomeItem = config.getConfigurationSection("gui.sethome");

        homeButtonLeftClick = HomeButtonAction.valueOf(config.getString("gui.left_click"));
        homeButtonRightClick = HomeButtonAction.valueOf(config.getString("gui.right_click"));
        homeButtonLeftClickWithShift = HomeButtonAction.valueOf(config.getString("gui.left_click_with_shift"));
        homeButtonRightClickWithShift = HomeButtonAction.valueOf(config.getString("gui.right_click_with_shift"));

        setHomeGuiTitle = config.getString("anvil_gui.sethome.title");
        setHomeGuiTitleNotValid = config.getString("anvil_gui.sethome.home_name_not_valid");
        setHomeGuiTitleCharLimit = config.getString("anvil_gui.sethome.home_name_limit");
        setHomeGuiText = config.getString("anvil_gui.sethome.text");
        setHomeGuiSlots = new HashMap<>();
        config.getConfigurationSection("anvil_gui.sethome.slots").getKeys(false).forEach(keyString -> {
            setHomeGuiSlots.put(Integer.parseInt(keyString), config.getConfigurationSection("anvil_gui.sethome.slots."+keyString));
        });

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

    public static HomeButtonAction getHomeButtonLeftClick() {
        return homeButtonLeftClick;
    }

    public static HomeButtonAction getHomeButtonLeftClickWithShift() {
        return homeButtonLeftClickWithShift;
    }

    public static HomeButtonAction getHomeButtonRightClick() {
        return homeButtonRightClick;
    }

    public static HomeButtonAction getHomeButtonRightClickWithShift() {
        return homeButtonRightClickWithShift;
    }

    public static String getWorldName(String name) {
        return worldNames.getOrDefault(name, name);
    }

    public static String getTpBetweenWorldsPerm() {
        return tpBetweenWorldsPerm;
    }

    public static String getUnnamedHomeName() {
        return unnamedHomeName;
    }

    public static boolean isAskForNameBeforeSave() {
        return askForNameBeforeSave;
    }

    public static boolean isHomesGuiIndexAmount() {
        return homesGuiIndexAmount;
    }

    public static boolean isLoadChunkBeforeTp() {
        return loadChunkBeforeTp;
    }

    public static String getHomeTeleportTimePerm() {
        return homeTeleportTimePerm;
    }

    public static boolean isSumHomeLimits() {
        return sumHomeLimits;
    }

    public static boolean isLpHomeLimit() {
        return lpHomeLimit;
    }

    public static String getLpHomeLimitName() {
        return lpHomeLimitName;
    }

    public static IntArray getHomeNameCharLimit() {
        return homeNameCharLimit;
    }

    public static String getHomeNameValidChars() {
        return homeNameValidChars;
    }

    public static boolean isSethomeClaimCheck() {
        return sethomeClaimCheck;
    }

    public static boolean isTeleportClaimCheck() {
        return teleportClaimCheck;
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

    public static HashMap<Integer, ConfigurationSection> getSetHomeGuiSlots() {
        return setHomeGuiSlots;
    }

    public static String getSetHomeGuiText() {
        return setHomeGuiText;
    }

    public static String getSetHomeGuiTitle() {
        return setHomeGuiTitle;
    }

    public static String getSetHomeGuiTitleCharLimit() {
        return setHomeGuiTitleCharLimit;
    }

    public static String getSetHomeGuiTitleNotValid() {
        return setHomeGuiTitleNotValid;
    }

    public static YamlCommand getHomeCommand() {
        return homeCommand;
    }

    public static YamlCommand getSetHomeCommand() {
        return setHomeCommand;
    }
}
