package me.usainsrht.uhomes;

import me.angeschossen.lands.api.LandsIntegration;
import me.lucko.commodore.Commodore;
import me.lucko.commodore.CommodoreProvider;
import me.usainsrht.uhomes.claim_interfaces.ClaimAPI;
import me.usainsrht.uhomes.claim_interfaces.GriefPrevention;
import me.usainsrht.uhomes.claim_interfaces.Lands;
import me.usainsrht.uhomes.command.CommandHandler;
import me.usainsrht.uhomes.command.HomeCommand;
import me.usainsrht.uhomes.command.SetHomeCommand;
import me.usainsrht.uhomes.config.MainConfig;
import me.usainsrht.uhomes.listener.InventoryClickListener;
import me.usainsrht.uhomes.listener.JoinListener;
import me.usainsrht.uhomes.listener.SaveListener;
import me.usainsrht.uhomes.manager.ClaimManager;
import me.usainsrht.uhomes.manager.HomeManager;
import me.usainsrht.uhomes.teleport.TeleportManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bstats.bukkit.Metrics;

import java.io.File;

public final class UHomes extends JavaPlugin {

    private static UHomes instance;
    private static final int pluginID = 20539;
    private Metrics metrics;
    private HomeManager homeManager;
    private ClaimManager claimManager;
    private TeleportManager teleportManager;
    private Commodore commodore;
    public File HOMES_FOLDER;

    @Override
    public void onEnable() {
        instance = this;

        this.metrics = new Metrics(this, pluginID);

        this.homeManager = new HomeManager(this);
        this.teleportManager = new TeleportManager(this);
        ClaimAPI claimAPI = null;
        if (getServer().getPluginManager().isPluginEnabled("Lands"))
            claimAPI = new Lands(LandsIntegration.of(this));
        else if (getServer().getPluginManager().isPluginEnabled("GriefPrevention"))
            claimAPI = new GriefPrevention(me.ryanhamshire.GriefPrevention.GriefPrevention.instance);
        this.claimManager = new ClaimManager(this, claimAPI);

        loadConfig();

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

    public Metrics getMetrics() {
        return metrics;
    }

    public HomeManager getHomeManager() {
        return homeManager;
    }

    public ClaimManager getClaimManager() {
        return claimManager;
    }

    public TeleportManager getTeleportManager() {
        return teleportManager;
    }

    public static UHomes getInstance() {
        return instance;
    }
}
