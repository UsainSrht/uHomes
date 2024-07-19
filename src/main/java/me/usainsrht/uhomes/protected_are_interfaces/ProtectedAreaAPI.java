package me.usainsrht.uhomes.protected_are_interfaces;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface ProtectedAreaAPI {

    boolean canEnter(Player player, Location location);

}
