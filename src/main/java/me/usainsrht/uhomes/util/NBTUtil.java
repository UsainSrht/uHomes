package me.usainsrht.uhomes.util;

import de.tr7zw.changeme.nbtapi.iface.ReadableNBT;
import de.tr7zw.changeme.nbtapi.iface.ReadableNBTList;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class NBTUtil {

    public static Location getLocation(ReadableNBT compound) {
        World world = Bukkit.getWorld(compound.getString("World"));
        ReadableNBTList<Double> pos = compound.getDoubleList("Pos");
        ReadableNBTList<Float> rotation = compound.getFloatList("Rotation");
        return new Location(world, pos.get(0), pos.get(1), pos.get(2), rotation.get(0), rotation.get(1));
    }

}
