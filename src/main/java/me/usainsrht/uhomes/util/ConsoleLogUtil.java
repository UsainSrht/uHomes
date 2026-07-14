package me.usainsrht.uhomes.util;

import me.usainsrht.uhomes.Home;
import me.usainsrht.uhomes.UHomes;
import me.usainsrht.uhomes.config.MainConfig;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public final class ConsoleLogUtil {

    private ConsoleLogUtil() {
    }

    public static void logHomeCreated(Player player, Home home) {
        if (!MainConfig.isConsoleLogging()) return;
        UHomes.getInstance().getLogger().info(String.format("%s created a home named %s at %s",
                player.getName(), homeName(home), formatLocation(home.getLocation())));
    }

    public static void logHomeTeleport(Entity entity, Home home) {
        if (!MainConfig.isConsoleLogging()) return;
        UHomes.getInstance().getLogger().info(String.format("%s teleported to a home named %s at %s",
                entityName(entity), homeName(home), formatLocation(home.getLocation())));
    }

    public static void logHomeRenamed(Player player, String newName) {
        if (!MainConfig.isConsoleLogging()) return;
        UHomes.getInstance().getLogger().info(String.format("%s renamed home to %s",
                player.getName(), newName));
    }

    public static void logHomeRelocated(Player player, Home home, Location from) {
        if (!MainConfig.isConsoleLogging()) return;
        UHomes.getInstance().getLogger().info(String.format("%s relocated home named %s from %s to %s",
                player.getName(), homeName(home), formatLocation(from), formatLocation(home.getLocation())));
    }

    public static void logHomeDeleted(Player player, Home home) {
        if (!MainConfig.isConsoleLogging()) return;
        UHomes.getInstance().getLogger().info(String.format("%s deleted its home named %s at %s",
                player.getName(), homeName(home), formatLocation(home.getLocation())));
    }

    private static String entityName(Entity entity) {
        return entity instanceof Player player ? player.getName() : entity.getName();
    }

    private static String homeName(Home home) {
        return home.getName() != null ? home.getName() : "unnamed";
    }

    private static String formatLocation(Location location) {
        return location.getBlockX() + " " + location.getBlockY() + " " + location.getBlockZ()
                + " " + location.getWorld().getName();
    }
}
