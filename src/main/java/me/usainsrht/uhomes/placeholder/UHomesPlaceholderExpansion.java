package me.usainsrht.uhomes.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.usainsrht.uhomes.UHomes;
import me.usainsrht.uhomes.Home;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class UHomesPlaceholderExpansion extends PlaceholderExpansion {

    private final UHomes plugin;

    public UHomesPlaceholderExpansion(UHomes plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "uhomes";
    }

    @Override
    public @NotNull String getAuthor() {
        return String.join(", ", plugin.getDescription().getAuthors());
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) {
            return null;
        }

        if (params.equalsIgnoreCase("home_count") || params.equalsIgnoreCase("homes_count") || params.equalsIgnoreCase("homes")) {
            List<Home> homes = plugin.getHomeManager().getLoadedHomes(player.getUniqueId());
            if (homes != null) {
                return String.valueOf(homes.size());
            } else {
                plugin.getHomeManager().getHomes(player.getUniqueId());
                return "0";
            }
        }

        if (params.equalsIgnoreCase("home_limit") || params.equalsIgnoreCase("homes_limit") || params.equalsIgnoreCase("limit")) {
            return String.valueOf(plugin.getHomeManager().getHomeLimit(player.getUniqueId()));
        }

        return null;
    }
}
