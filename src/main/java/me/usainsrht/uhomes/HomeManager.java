package me.usainsrht.uhomes;

import de.tr7zw.changeme.nbtapi.NBTCompoundList;
import de.tr7zw.changeme.nbtapi.NBTFile;
import de.tr7zw.changeme.nbtapi.NBTList;
import de.tr7zw.changeme.nbtapi.NBTListCompound;
import me.usainsrht.uhomes.command.SetHomeCommand;
import me.usainsrht.uhomes.config.MainConfig;
import me.usainsrht.uhomes.teleport.TimedTeleport;
import me.usainsrht.uhomes.util.MessageUtil;
import me.usainsrht.uhomes.util.NBTUtil;
import me.usainsrht.uhomes.util.SoundUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
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
                    if (compound.hasTag("Name")) home.setName(compound.getString("Name"));
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
        pos.clear();
        pos.add(location.getX());
        pos.add(location.getY());
        pos.add(location.getZ());
        NBTList<Float> rotation = compound.getFloatList("Rotation");
        rotation.clear();
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

    public void save() {
        long start = System.currentTimeMillis();
        int saved = 0;
        int removedFromCache = 0;
        //plugin.getLogger().info("Saving homes...");
        Iterator<Map.Entry<UUID, List<Home>>> iterator = loadedHomes.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, List<Home>> entry = iterator.next();
            entry.getValue().forEach(this::saveHome);
            saved += entry.getValue().size();
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(entry.getKey());
            if (offlinePlayer.isOnline()) continue;
            iterator.remove();
            removedFromCache++;
        }
        /*plugin.getLogger().info("Saved " + saved + " homes in " + (System.currentTimeMillis()-start)
                + "ms (removed " + removedFromCache + " players from cache)");*/
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

    public int getHomeTeleportTime(UUID uuid) {
        return getHomeTeleportTime(Bukkit.getEntity(uuid));
    }

    public int getHomeTeleportTime(Permissible permissible) {
        if (permissible.hasPermission(MainConfig.getHomeTeleportTimePerm()+"bypass")) return 0;
        int lowest = 60; //default 3 seconds
        for (PermissionAttachmentInfo permInfo : permissible.getEffectivePermissions()) {
            String perm = permInfo.getPermission();
            if (!perm.startsWith(MainConfig.getHomeTeleportTimePerm())) continue;
            String substr = perm.substring(MainConfig.getHomeTeleportTimePerm().length());
            try {
                int number = Integer.parseInt(substr);
                if (number < lowest) lowest = number;
            } catch (NumberFormatException ignore) {}
        }
        return lowest;
    }

    public CompletableFuture<Boolean> canRegisterHome(UUID uuid) {
        CompletableFuture<List<Home>> future = getHomes(uuid);
        CompletableFuture<Boolean> canRegisterFuture = new CompletableFuture<>();
        future.thenAccept(homes -> canRegisterFuture.complete(homes.size() < getHomeLimit(uuid)));
        return canRegisterFuture;
    }

    public void teleport(Entity entity, Home home) {
        if (entity.getWorld() != home.getLocation().getWorld() && !entity.hasPermission(MainConfig.getTpBetweenWorldsPerm())) {
            MessageUtil.send(entity, MainConfig.getMessage("teleport_between_worlds_permission"),
                    Placeholder.unparsed("permission", MainConfig.getTpBetweenWorldsPerm()));
            SoundUtil.play(entity, MainConfig.getSound("teleport_between_worlds_permission"));
            return;
        }
        TimedTeleport timedTeleport = new TimedTeleport()
                .entity(entity)
                .location(home.getLocation())
                .ticksToRunOnTick(20)
                .ticks(getHomeTeleportTime(entity))
                .onFinish(tt -> {
                    home.setLastTeleport(System.currentTimeMillis());
                    entity.teleport(home.getLocation());
                    //todo make unnamed homes seen in chat instead of ""
                    MessageUtil.send(entity, MainConfig.getMessage("teleport"),
                            Placeholder.unparsed("home_name", home.getName() == null ? "" : home.getName()));
                    SoundUtil.play(entity, MainConfig.getSound("teleport"));

                    SoundUtil.stop(entity, MainConfig.getSound("teleport_start"));
                })
                .onCancel(tt -> {
                    MessageUtil.send(entity, MainConfig.getMessage("teleport_cancel"));
                    SoundUtil.play(entity, MainConfig.getSound("teleport_cancel"));

                    SoundUtil.stop(entity, MainConfig.getSound("teleport_start"));
                })
                .onTick(tt -> {
                    MessageUtil.send(entity, MainConfig.getMessage("teleport_tick"),
                            Formatter.number("ticks_passed", tt.getTicksPassed()),
                            Formatter.number("ticks_total", tt.getTicksTotal()));
                    SoundUtil.play(entity, MainConfig.getSound("teleport_tick"));
                })
                .onStart(tt -> {
                    int seconds = (int)Math.ceil(tt.getTicksTotal() / 20d);
                    if (seconds > 0) {
                        MessageUtil.send(entity, MainConfig.getMessage("teleport_start"),
                                Placeholder.unparsed("home_name", home.getName() == null ? "" : home.getName()),
                                Formatter.number("seconds", seconds));
                    }
                    SoundUtil.play(entity, MainConfig.getSound("teleport_start"));
                })
                ;

        plugin.getTeleportManager().start(timedTeleport);
    }

    public void relocate(Player player, Home home) {
        //todo confirmation
        home.setLocation(player.getLocation().clone());
    }

    public void delete(Player player, Home home) {
        //todo confirmation
        UUID uuid = home.getOwner();
        if (loadedHomes.containsKey(uuid)) {
            List<Home> homes = loadedHomes.get(uuid);
            homes.remove(home);
        }
        NBTFile nbtFile = getNBTFile(uuid);
        NBTCompoundList compoundList = nbtFile.getCompoundList("Homes");
        for (int i = 0; i < compoundList.size(); i++) {
            NBTListCompound current = compoundList.get(i);
            if (home.getCreated() == current.getLong("Created")) {
                compoundList.remove(i);
                try {
                    nbtFile.save();
                } catch (IOException e) {
                    plugin.getLogger().severe("An error occurred while deleting home of "+Bukkit.getOfflinePlayer(uuid).getName()+" ("+uuid+")");
                    e.printStackTrace();
                }
                return;
            }
        }

    }

    public void rename(Player player, Home home) {
        SetHomeCommand.renameHome(player, home, null);
    }

    public UHomes getPlugin() {
        return plugin;
    }
}
