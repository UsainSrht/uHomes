package me.usainsrht.uhomes.protected_area_interfaces;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface ProtectedAreaAPI {

    boolean canEnter(Player player, Location location);

}
