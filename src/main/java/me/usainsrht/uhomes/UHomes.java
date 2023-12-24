package me.usainsrht.uhomes;

import me.lucko.commodore.Commodore;
import me.lucko.commodore.CommodoreProvider;
import me.usainsrht.uhomes.command.HomeCommand;
import me.usainsrht.uhomes.config.MainConfig;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class UHomes extends JavaPlugin {

    private static UHomes instance;
    private static final int pluginID = 20539;
    private Metrics metrics;
    private HomeManager homeManager;
    private Commodore commodore;
    public File HOMES_FOLDER;

    @Override
    public void onEnable() {
        instance = this;

        this.metrics = new Metrics(this, pluginID);

        this.homeManager = new HomeManager(this);

        loadConfig();

        commodore = CommodoreProvider.getCommodore(this);
        registerCommands();
    }

    @Override
    public void onDisable() {

    }

    public void reload() {

    }

    public void loadConfig() {
        saveDefaultConfig();

        HOMES_FOLDER = new File(getDataFolder(), "homes");
        HOMES_FOLDER.mkdirs();
    }

    public void registerCommands() {
        HomeCommand homeCommand = new HomeCommand(MainConfig.getHomeCommand());
        //CommandHandler.register("uhomes", homeCommand);
        commodore.register(homeCommand, homeCommand.getCommodoreCommand());
    }

    public Metrics getMetrics() {
        return metrics;
    }

    public HomeManager getHomeManager() {
        return homeManager;
    }

    public static UHomes getInstance() {
        return instance;
    }
}
