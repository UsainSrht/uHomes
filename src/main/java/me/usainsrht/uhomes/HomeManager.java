package me.usainsrht.uhomes;

import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTCompoundList;
import de.tr7zw.changeme.nbtapi.NBTFile;
import de.tr7zw.changeme.nbtapi.NBTListCompound;
import me.usainsrht.uhomes.util.NBTUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitTask;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
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
        }
        NBTFile nbtFile = getNBTFile(uuid);
        NBTListCompound compound = nbtFile.getCompoundList("Homes").addCompound();
        compound.setString("Name", home.getName());
        compound.setLong("Created", home.getCreated());
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

    public UHomes getPlugin() {
        return plugin;
    }
}
