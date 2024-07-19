package me.usainsrht.uhomes;

import me.angeschossen.lands.api.LandsIntegration;
import me.lucko.commodore.Commodore;
import me.lucko.commodore.CommodoreProvider;
import me.usainsrht.uhomes.protected_are_interfaces.ProtectedAreaAPI;
import me.usainsrht.uhomes.protected_are_interfaces.GriefPrevention;
import me.usainsrht.uhomes.protected_are_interfaces.Lands;
import me.usainsrht.uhomes.command.CommandHandler;
import me.usainsrht.uhomes.command.HomeCommand;
import me.usainsrht.uhomes.command.SetHomeCommand;
import me.usainsrht.uhomes.config.MainConfig;
import me.usainsrht.uhomes.listener.InventoryClickListener;
import me.usainsrht.uhomes.listener.JoinListener;
import me.usainsrht.uhomes.listener.SaveListener;
import me.usainsrht.uhomes.manager.ProtectedAreManager;
import me.usainsrht.uhomes.manager.HomeManager;
import me.usainsrht.uhomes.teleport.TeleportManager;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bstats.bukkit.Metrics;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class UHomes extends JavaPlugin {

    private static UHomes instance;
    private static final int pluginID = 20539;
    private Metrics metrics;
    private HomeManager homeManager;
    private ProtectedAreManager protectedAreManager;
    private TeleportManager teleportManager;
    private Commodore commodore;
    private LuckPerms luckPerms;
    public File HOMES_FOLDER;

    @Override
    public void onEnable() {
        instance = this;

        this.metrics = new Metrics(this, pluginID);

        this.homeManager = new HomeManager(this);
        this.teleportManager = new TeleportManager(this);
        List<ProtectedAreaAPI> protectedAreaAPIs = new ArrayList<>();
        if (getServer().getPluginManager().isPluginEnabled("Lands"))
            protectedAreaAPIs.add(new Lands(LandsIntegration.of(this)));
        else if (getServer().getPluginManager().isPluginEnabled("GriefPrevention"))
            protectedAreaAPIs.add(new GriefPrevention(me.ryanhamshire.GriefPrevention.GriefPrevention.instance));
        this.protectedAreManager = new ProtectedAreManager(this, protectedAreaAPIs);

        loadConfig();

        try {
            luckPerms = LuckPermsProvider.get();
        } catch (Exception e) {
            getLogger().info("couldn't hook into luckperms " + e.getClass().getName());
        }

        commodore = CommodoreProvider.getCommodore(this);
        registerCommands();

        registerListeners();
    }

    @Override
    public void onDisable() {

    }

    public void reload() {
        reloadConfig();
        loadConfig();
    }

    public void loadConfig() {
        saveDefaultConfig();

        MainConfig.create(getConfig());

        HOMES_FOLDER = new File(getDataFolder(), "homes");
        HOMES_FOLDER.mkdirs();
    }

    public void registerCommands() {
        HomeCommand homeCommand = new HomeCommand(MainConfig.getHomeCommand());
        CommandHandler.register("uhomes", homeCommand);
        commodore.register(homeCommand, homeCommand.getCommodoreCommand());

        SetHomeCommand setHomeCommand = new SetHomeCommand(MainConfig.getSetHomeCommand());
        CommandHandler.register("uhomes", setHomeCommand);
        commodore.register(setHomeCommand, setHomeCommand.getCommodoreCommand());
    }

    public void registerListeners() {
        Bukkit.getPluginManager().registerEvents(new SaveListener(homeManager), this);
        Bukkit.getPluginManager().registerEvents(new JoinListener(homeManager), this);
        Bukkit.getPluginManager().registerEvents(new InventoryClickListener(homeManager), this);
    }

    public LuckPerms getLuckPerms() {
        return luckPerms;
    }

    public Metrics getMetrics() {
        return metrics;
    }

    public HomeManager getHomeManager() {
        return homeManager;
    }

    public ProtectedAreManager getClaimManager() {
        return protectedAreManager;
    }

    public TeleportManager getTeleportManager() {
        return teleportManager;
    }

    public static UHomes getInstance() {
        return instance;
    }
}
