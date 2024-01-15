package me.usainsrht.uhomes.claim_interfaces;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface ClaimAPI {

    boolean canEnter(Player player, Location location);

}
