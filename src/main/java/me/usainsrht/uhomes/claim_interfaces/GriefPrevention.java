package me.usainsrht.uhomes.claim_interfaces;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.ClaimPermission;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class GriefPrevention implements ClaimAPI {

    private me.ryanhamshire.GriefPrevention.GriefPrevention instance;

    public GriefPrevention(me.ryanhamshire.GriefPrevention.GriefPrevention instance) {
        this.instance = instance;
    }

    @Override
    public boolean canEnter(Player player, Location location) {
        Claim claim = instance.dataStore.getClaimAt(location, true, null);
        if (claim == null) return true;
        return claim.hasExplicitPermission(player, ClaimPermission.Manage);
    }
}
