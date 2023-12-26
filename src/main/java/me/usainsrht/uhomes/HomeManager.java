package me.usainsrht.uhomes;

import de.tr7zw.changeme.nbtapi.NBTCompoundList;
import de.tr7zw.changeme.nbtapi.NBTFile;
import de.tr7zw.changeme.nbtapi.NBTList;
import de.tr7zw.changeme.nbtapi.NBTListCompound;
import me.usainsrht.uhomes.config.MainConfig;
import me.usainsrht.uhomes.util.NBTUtil;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.PermissionAttachmentInfo;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class HomeManager {

    private UHomes plugin;
    private HashMap<UUID, List<Home>> loadedHomes;

    public HomeManager(UHomes plugin) {
        this.plugin = plugin;
        this.loadedHomes = new HashMap<>();
    }

    public CompletableFuture<List<Home>> getHomes(UUID uuid) {
        CompletableFuture<List<Home>> future = new CompletableFuture<>();

        if (loadedHomes.containsKey(uuid)) {
            future.complete(loadedHomes.get(uuid));
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                NBTFile nbtFile = getNBTFile(uuid);
                NBTCompoundList compoundList = nbtFile.getCompoundList("Homes");
                List<Home> homeList = new ArrayList<>();
                compoundList.forEach(compound -> {
                    Location location = NBTUtil.getLocation(compound);
                    Home home = new Home(uuid, location);
                    home.setCreated(compound.getLong("Created"));
                    if (compound.hasTag("LastTeleport")) home.setLastTeleport(compound.getLong("LastTeleport"));
                    if (compound.hasTag("Icon")) home.setIcon(compound.getItemStack("Icon"));
                    if (compound.hasTag("Name")) home.setIcon(compound.getItemStack("Name"));
                    homeList.add(home);
                });
                loadedHomes.put(uuid, homeList);
                future.complete(homeList);
            });
        }

        return future;
    }

    public void addHome(UUID uuid, Home home) {
        if (loadedHomes.containsKey(uuid)) {
            loadedHomes.get(uuid).add(home);
        } else {
            List<Home> homeList = new ArrayList<>();
            homeList.add(home);
            loadedHomes.put(uuid, homeList);
        }
        saveHome(home);
    }

    public void saveHome(Home home) {
        //todo save players' all homes at once
        UUID uuid = home.getOwner();
        NBTFile nbtFile = getNBTFile(uuid);
        NBTCompoundList compoundList = nbtFile.getCompoundList("Homes");
        NBTListCompound compound = null;
        for (int i = 0; i < compoundList.size(); i++) {
            NBTListCompound current = compoundList.get(i);
            // check for existing home to update
            if (home.getCreated() == current.getLong("Created")) compound = current;
        }
        // add new home to the list
        if (compound == null) compound = nbtFile.getCompoundList("Homes").addCompound();

        compound.setLong("Created", home.getCreated());
        Location location = home.getLocation();
        compound.setString("World", location.getWorld().getName());
        NBTList<Double> pos = compound.getDoubleList("Pos");
        pos.add(location.getX());
        pos.add(location.getY());
        pos.add(location.getZ());
        NBTList<Float> rotation = compound.getFloatList("Rotation");
        rotation.add(location.getYaw());
        rotation.add(location.getPitch());

        if (home.getName() != null) compound.setString("Name", home.getName());
        if (home.getIcon() != null) compound.setItemStack("Icon", home.getIcon());
        if (home.getLastTeleport() != -1) compound.setLong("LastTeleport", home.getLastTeleport());

        try {
            nbtFile.save();
        } catch (IOException e) {
            plugin.getLogger().severe("An error occurred while saving home of "+Bukkit.getOfflinePlayer(uuid).getName()+" ("+uuid+")");
            e.printStackTrace();
        }
    }

    @Nullable
    public NBTFile getNBTFile(UUID uuid) {
        NBTFile nbtFile = null;
        try {
            File file = new File(plugin.HOMES_FOLDER, uuid.toString()+".nbt");
            nbtFile = new NBTFile(file);
        } catch (IOException e) {
            plugin.getLogger().severe("An error occurred while loading homes of "+Bukkit.getOfflinePlayer(uuid).getName()+" ("+uuid+")");
            e.printStackTrace();
        }
        return nbtFile;
    }

    public int getHomeLimit(UUID uuid) {
        return getHomeLimit(Bukkit.getEntity(uuid));
    }

    public int getHomeLimit(Permissible permissible) {
        if (MainConfig.isSumHomeLimits()) {
            int total = 0;
            for (PermissionAttachmentInfo permInfo : permissible.getEffectivePermissions()) {
                String perm = permInfo.getPermission();
                if (!perm.startsWith(MainConfig.getHomeLimitPermission())) continue;
                String substr = perm.substring(MainConfig.getHomeLimitPermission().length());
                try {
                    int number = Integer.parseInt(substr);
                    total += number;
                } catch (NumberFormatException ignore) {}
            }
            return total;
        } else {
            int highest = 0;
            for (PermissionAttachmentInfo permInfo : permissible.getEffectivePermissions()) {
                String perm = permInfo.getPermission();
                if (!perm.startsWith(MainConfig.getHomeLimitPermission())) continue;
                String substr = perm.substring(MainConfig.getHomeLimitPermission().length());
                try {
                    int number = Integer.parseInt(substr);
                    if (number > highest) highest = number;
                } catch (NumberFormatException ignore) {}
            }
            return highest;
        }
    }

    public CompletableFuture<Boolean> canRegisterHome(UUID uuid) {
        CompletableFuture<List<Home>> future = getHomes(uuid);
        CompletableFuture<Boolean> canRegisterFuture = new CompletableFuture<>();
        future.thenAccept(homes -> canRegisterFuture.complete(homes.size() < getHomeLimit(uuid)));
        return canRegisterFuture;
    }



    public UHomes getPlugin() {
        return plugin;
    }
}
