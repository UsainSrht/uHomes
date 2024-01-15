package me.usainsrht.uhomes.claim_interfaces;

import me.angeschossen.lands.api.LandsIntegration;
import me.angeschossen.lands.api.land.Area;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class Lands implements ClaimAPI {

    private LandsIntegration landsIntegration;

    public Lands(LandsIntegration landsIntegration) {
        this.landsIntegration = landsIntegration;
    }

    @Override
    public boolean canEnter(Player player, Location location) {
        Area area = landsIntegration.getArea(location);
        if (area == null) return true;
        return (area.isTrusted(player.getUniqueId()));
    }
}
