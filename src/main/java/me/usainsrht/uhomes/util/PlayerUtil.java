package me.usainsrht.uhomes.util;

import me.usainsrht.uhomes.UHomes;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.io.File;
import java.util.UUID;

public class PlayerUtil {

    @Nullable
    public static UUID resolvePlayerUuid(String name) {
        Player online = Bukkit.getPlayerExact(name);
        if (online != null) return online.getUniqueId();

        OfflinePlayer cached = Bukkit.getOfflinePlayerIfCached(name);
        if (cached != null && cached.getName() != null && cached.getName().equalsIgnoreCase(name))
            return cached.getUniqueId();

        File homesFolder = UHomes.getInstance().HOMES_FOLDER;
        File[] files = homesFolder.listFiles((dir, fileName) -> fileName.endsWith(".nbt"));
        if (files != null) {
            for (File file : files) {
                try {
                    UUID uuid = UUID.fromString(file.getName().replace(".nbt", ""));
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
                    if (offlinePlayer.getName() != null && offlinePlayer.getName().equalsIgnoreCase(name))
                        return uuid;
                } catch (IllegalArgumentException ignore) {
                }
            }
        }

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(name);
        if (offlinePlayer.hasPlayedBefore() && offlinePlayer.getName() != null
                && offlinePlayer.getName().equalsIgnoreCase(name))
            return offlinePlayer.getUniqueId();

        return null;
    }

    @Nullable
    public static String getPlayerName(UUID uuid) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
        return offlinePlayer.getName();
    }

}
