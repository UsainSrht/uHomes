package me.usainsrht.uhomes.manager;

import me.angeschossen.lands.api.LandsIntegration;
import me.angeschossen.lands.api.land.Area;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.ClaimPermission;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.usainsrht.uhomes.UHomes;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class ClaimManager {

    private UHomes plugin;
    private Object claimAPI;

    public ClaimManager(UHomes plugin, Object claimAPI) {
        this.plugin = plugin;
        this.claimAPI = claimAPI;
    }

    public UHomes getPlugin() {
        return plugin;
    }

    public Object getClaimAPI() {
        return claimAPI;
    }

    public boolean canEnter(Player player, Location location) {
        if (claimAPI == null) return true;
        //todo stops code silently when class is undefined
        else if (claimAPI instanceof LandsIntegration api) {
            Area area = api.getArea(location);
            if (area == null) return true;
            return (area.isTrusted(player.getUniqueId()));
        } else if (claimAPI instanceof GriefPrevention api) {
            Claim claim = api.dataStore.getClaimAt(location, true, null);
            if (claim == null) return true;
            return claim.hasExplicitPermission(player, ClaimPermission.Manage);
        }
        return true;
    }

}
