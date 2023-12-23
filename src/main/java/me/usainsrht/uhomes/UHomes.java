package me.usainsrht.uhomes;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class UHomes extends JavaPlugin {

    private static UHomes instance;
    private static final int pluginID = 20539;
    private Metrics metrics;
    public File HOMES_FOLDER;

    @Override
    public void onEnable() {
        instance = this;

        this.metrics = new Metrics(this, pluginID);

        loadConfig();
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

    public Metrics getMetrics() {
        return metrics;
    }

    public static UHomes getInstance() {
        return instance;
    }
}
