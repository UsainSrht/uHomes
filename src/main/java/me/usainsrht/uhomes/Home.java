package me.usainsrht.uhomes;

import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBT;
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBTList;
import de.tr7zw.changeme.nbtapi.iface.ReadableNBT;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.UUID;

public class Home {

    private UUID owner;
    private String name;
    private ItemStack icon;
    private Location location;
    private long created;
    private long lastTeleport;

    public Home(UUID owner, Location location) {
        this.created = System.currentTimeMillis();

        this.owner = owner;
        this.location = location;

        this.name = null;
        this.icon = null;
        this.lastTeleport = -1;
    }

    public ReadableNBT getCompound() {
        ReadWriteNBT nbt = NBT.createNBTObject();
        nbt.setUUID("Owner", owner);
        nbt.setLong("Created", created);

        ReadWriteNBTList<Double> pos = nbt.getDoubleList("Pos");
        pos.add(location.getX());
        pos.add(location.getY());
        pos.add(location.getZ());

        ReadWriteNBTList<Float> rotation = nbt.getFloatList("Rotation");
        rotation.add(location.getYaw());
        rotation.add(location.getPitch());

        if (name != null) nbt.setString("Name", name);
        if (icon != null) nbt.setItemStack("Icon", icon);
        if (lastTeleport != -1) nbt.setLong("LastTeleport", lastTeleport);
        return nbt;
    }

    @Nullable
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Location getLocation() {
        return location.clone();
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    @Nullable
    public ItemStack getIcon() {
        return icon != null ? icon.clone() : null;
    }

    public long getCreated() {
        return created;
    }

    @Nullable
    public long getLastTeleport() {
        return lastTeleport;
    }

    public UUID getOwner() {
        return owner;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public void setIcon(ItemStack icon) {
        this.icon = icon;
    }

    public void setLastTeleport(long lastTeleport) {
        this.lastTeleport = lastTeleport;
    }

    public void setOwner(UUID owner) {
        throw new UnsupportedOperationException("Cannot change the owner of a home.");
    }
}
