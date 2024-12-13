package me.usainsrht.uhomes.manager;

import me.usainsrht.uhomes.UHomes;
import me.usainsrht.uhomes.protected_area_interfaces.ProtectedAreaAPI;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;

public class ProtectedAreManager implements ProtectedAreaAPI {

    private UHomes plugin;
    private List<ProtectedAreaAPI> protectedAreaAPIs;

    public ProtectedAreManager(UHomes plugin, List<ProtectedAreaAPI> protectedAreaAPIs) {
        this.plugin = plugin;
        this.protectedAreaAPIs = protectedAreaAPIs;
    }

    public UHomes getPlugin() {
        return plugin;
    }

    public List<ProtectedAreaAPI> getProtectedAreAPIs() {
        return protectedAreaAPIs;
    }


    @Override
    public boolean canEnter(Player player, Location location) {
        for (ProtectedAreaAPI protectedAreaAPI : protectedAreaAPIs) {
            if (!protectedAreaAPI.canEnter(player, location)) return false;
        }
        return true;
    }
}
