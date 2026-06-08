package me.usainsrht.uhomes.util;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.scheduler.BukkitTask;

public class SchedulerUtil {
    private static final boolean IS_FOLIA = isFolia();

    private static boolean isFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.scheduler.AsyncScheduler");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static void runAsync(Plugin plugin, Runnable runnable) {
        if (IS_FOLIA) {
            Bukkit.getAsyncScheduler().runNow(plugin, task -> runnable.run());
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable);
        }
    }

    public static void runLater(Plugin plugin, Runnable runnable, long delayTicks) {
        if (IS_FOLIA) {
            Bukkit.getGlobalRegionScheduler().runDelayed(plugin, task -> runnable.run(), Math.max(1L, delayTicks));
        } else {
            Bukkit.getScheduler().runTaskLater(plugin, runnable, delayTicks);
        }
    }

    public static void runNextTick(Plugin plugin, Runnable runnable) {
        if (IS_FOLIA) {
            Bukkit.getGlobalRegionScheduler().runDelayed(plugin, task -> runnable.run(), 1L);
        } else {
            Bukkit.getScheduler().runTask(plugin, runnable);
        }
    }

    public static Object runTimer(Plugin plugin, Runnable runnable, long delayTicks, long periodTicks) {
        if (IS_FOLIA) {
            return Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, task -> runnable.run(), Math.max(1L, delayTicks), periodTicks);
        } else {
            return Bukkit.getScheduler().runTaskTimer(plugin, runnable, delayTicks, periodTicks);
        }
    }

    public static void cancelTask(Object task) {
        if (task == null) return;
        if (IS_FOLIA) {
            if (task instanceof ScheduledTask) {
                ((ScheduledTask) task).cancel();
            }
        } else {
            if (task instanceof BukkitTask) {
                ((BukkitTask) task).cancel();
            } else if (task instanceof Integer) {
                Bukkit.getScheduler().cancelTask((Integer) task);
            }
        }
    }
}
