package me.usainsrht.uhomes.manager;

import de.tr7zw.changeme.nbtapi.NBTCompoundList;
import de.tr7zw.changeme.nbtapi.NBTFile;
import de.tr7zw.changeme.nbtapi.NBTList;
import de.tr7zw.changeme.nbtapi.NBTListCompound;
import me.usainsrht.uhomes.Home;
import me.usainsrht.uhomes.UHomes;
import me.usainsrht.uhomes.command.SetHomeCommand;
import me.usainsrht.uhomes.config.MainConfig;
import me.usainsrht.uhomes.teleport.TimedTeleport;
import me.usainsrht.uhomes.gui.HomeSort;
import me.usainsrht.uhomes.gui.HomesGUI;
import me.usainsrht.uhomes.util.ConfirmationInput;
import me.usainsrht.uhomes.util.ConsoleLogUtil;
import me.usainsrht.uhomes.util.MessageUtil;
import me.usainsrht.uhomes.util.NBTUtil;
import me.usainsrht.limitapi.api.PermUtil;
import me.usainsrht.uhomes.util.SoundUtil;
import me.usainsrht.uhomes.util.SchedulerUtil;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class HomeManager {

    private UHomes plugin;
    private ConcurrentHashMap<UUID, List<Home>> loadedHomes;

    public HomeManager(UHomes plugin) {
        this.plugin = plugin;
        this.loadedHomes = new ConcurrentHashMap<>();
    }

    public CompletableFuture<List<Home>> getHomes(UUID uuid) {
        CompletableFuture<List<Home>> future = new CompletableFuture<>();

        if (loadedHomes.containsKey(uuid)) {
            future.complete(sortHomes(loadedHomes.get(uuid)));
        } else {
            SchedulerUtil.runAsync(plugin, () -> {
                NBTFile nbtFile = getNBTFile(uuid);
                NBTCompoundList compoundList = nbtFile.getCompoundList("Homes");
                List<Home> homeList = Collections.synchronizedList(new ArrayList<>());
                compoundList.forEach(compound -> {
                    Location location = NBTUtil.getLocation(compound);
                    Home home = new Home(uuid, location);
                    home.setCreated(compound.getLong("Created"));
                    if (compound.hasTag("LastTeleport"))
                        home.setLastTeleport(compound.getLong("LastTeleport"));
                    if (compound.hasTag("Icon"))
                        home.setIcon(compound.getItemStack("Icon"));
                    if (compound.hasTag("Name"))
                        home.setName(compound.getString("Name"));
                    if (compound.hasTag("Priority"))
                        home.setPriority(compound.getInteger("Priority"));
                    homeList.add(home);
                });
                loadedHomes.put(uuid, homeList);
                future.complete(sortHomes(homeList));
            });
        }

        return future;
    }

    public void addHome(UUID uuid, Home home) {
        if (loadedHomes.containsKey(uuid)) {
            loadedHomes.get(uuid).add(home);
        } else {
            List<Home> homeList = Collections.synchronizedList(new ArrayList<>());
            homeList.add(home);
            loadedHomes.put(uuid, homeList);
        }
        saveHome(home);
    }

    public List<Home> getLoadedHomes(UUID uuid) {
        return loadedHomes.get(uuid);
    }

    public void saveHome(Home home) {
        UUID uuid = home.getOwner();
        NBTFile nbtFile = getNBTFile(uuid);
        NBTCompoundList compoundList = nbtFile.getCompoundList("Homes");
        NBTListCompound compound = null;
        for (int i = 0; i < compoundList.size(); i++) {
            NBTListCompound current = compoundList.get(i);
            // check for existing home to update
            if (home.getCreated() == current.getLong("Created"))
                compound = current;
        }
        // add new home to the list
        if (compound == null)
            compound = nbtFile.getCompoundList("Homes").addCompound();

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

        if (home.getName() != null)
            compound.setString("Name", home.getName());
        if (home.getIcon() != null)
            compound.setItemStack("Icon", home.getIcon());
        if (home.getLastTeleport() != -1)
            compound.setLong("LastTeleport", home.getLastTeleport());
        if (home.getPriority() != 0)
            compound.setInteger("Priority", home.getPriority());

        try {
            nbtFile.save();
        } catch (IOException e) {
            plugin.getLogger().severe("An error occurred while saving home of "
                    + Bukkit.getOfflinePlayer(uuid).getName() + " (" + uuid + ")");
            e.printStackTrace();
        }
    }

    public void save() {
        long start = System.currentTimeMillis();
        int saved = 0;
        int removedFromCache = 0;
        // plugin.getLogger().info("Saving homes...");
        Iterator<Map.Entry<UUID, List<Home>>> iterator = loadedHomes.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, List<Home>> entry = iterator.next();
            entry.getValue().forEach(this::saveHome);
            saved += entry.getValue().size();
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(entry.getKey());
            if (offlinePlayer.isOnline())
                continue;
            iterator.remove();
            removedFromCache++;
        }
        /*
         * plugin.getLogger().info("Saved " + saved + " homes in " +
         * (System.currentTimeMillis()-start)
         * + "ms (removed " + removedFromCache + " players from cache)");
         */
    }

    @Nullable
    public NBTFile getNBTFile(UUID uuid) {
        NBTFile nbtFile = null;
        try {
            File file = new File(plugin.HOMES_FOLDER, uuid.toString() + ".nbt");
            nbtFile = new NBTFile(file);
        } catch (IOException e) {
            plugin.getLogger().severe("An error occurred while loading homes of "
                    + Bukkit.getOfflinePlayer(uuid).getName() + " (" + uuid + ")");
            e.printStackTrace();
        }
        return nbtFile;
    }

    public int getHomeLimit(UUID uuid) {
        if (MainConfig.isLpHomeLimit()) {
            var lp = PermUtil.getLuckPerms();
            if (lp != null) {
                var user = lp.getUserManager().getUser(uuid);
                if (user != null) {
                    return PermUtil.LuckPerms.getMetaLimit(MainConfig.getLpHomeLimitName(), user);
                }
            }
            return 0;
        }
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
            if (MainConfig.isSumHomeLimits()) {
                return PermUtil.Bukkit.getSummedLimit(MainConfig.getHomeLimitPermission(), player);
            }
            return PermUtil.Bukkit.getHighestLimit(MainConfig.getHomeLimitPermission(), player);
        }
        return 0;
    }

    public int getHomeTeleportTime(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player != null && player.hasPermission(MainConfig.getHomeTeleportTimeBypassPerm())) {
            return 0;
        }

        var lp = PermUtil.getLuckPerms();
        if (lp != null) {
            var user = lp.getUserManager().getUser(uuid);
            if (user != null) {
                int meta = PermUtil.LuckPerms.getMetaLimit(MainConfig.getHomeTeleportTimePerm(), user);
                return meta > 0 ? meta : 60;
            }
        }
        return 60;
    }

    public CompletableFuture<Boolean> canRegisterHome(UUID uuid) {
        CompletableFuture<List<Home>> future = getHomes(uuid);
        CompletableFuture<Boolean> canRegisterFuture = new CompletableFuture<>();
        future.thenAccept(homes -> canRegisterFuture.complete(homes.size() < getHomeLimit(uuid)));
        return canRegisterFuture;
    }

    public void teleport(Entity entity, Home home) {
        boolean managingOthers = entity instanceof Player player
                && MainConfig.isManagingOthersHomes(player, home.getOwner());
        if (entity.getWorld() != home.getLocation().getWorld()
                && !entity.hasPermission(MainConfig.getTpBetweenWorldsPerm())
                && !managingOthers) {
            MessageUtil.send(entity, MainConfig.getMessage("teleport_between_worlds_permission"),
                    Placeholder.unparsed("permission", MainConfig.getTpBetweenWorldsPerm()));
            SoundUtil.play(entity, MainConfig.getSound("teleport_between_worlds_permission"));
            return;
        }
        if (MainConfig.isTeleportClaimCheck() && entity instanceof Player player && !managingOthers) {
            if (!plugin.getClaimManager().canEnter(player, home.getLocation())) {
                MessageUtil.send(entity, MainConfig.getMessage("not_allowed_to_teleport"));
                SoundUtil.play(entity, MainConfig.getSound("not_allowed_to_teleport"));
                return;
            }
        }
        TimedTeleport timedTeleport = new TimedTeleport()
                .entity(entity)
                .location(home.getLocation())
                .ticksToRunOnTick(20)
                .ticks(getHomeTeleportTime(entity.getUniqueId()))
                .onFinish(tt -> {
                    home.setLastTeleport(System.currentTimeMillis());
                    entity.teleportAsync(home.getLocation());
                    ConsoleLogUtil.logHomeTeleport(entity, home);
                    // todo make unnamed homes seen in chat instead of ""
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
                    int seconds = (int) Math.ceil(tt.getTicksTotal() / 20d);
                    if (seconds > 0) {
                        MessageUtil.send(entity, MainConfig.getMessage("teleport_start"),
                                Placeholder.unparsed("home_name", home.getName() == null ? "" : home.getName()),
                                Formatter.number("seconds", seconds));
                    }
                    SoundUtil.play(entity, MainConfig.getSound("teleport_start"));
                });
        plugin.getTeleportManager().start(timedTeleport);
    }

    public void relocate(Player player, Home home, int index) {
        ConfirmationInput.open(player, ConfirmationInput.Type.RELOCATE, home, index, () -> {
            performRelocate(player, home);
            HomesGUI.open(home.getOwner(), player);
        });
    }

    private void performRelocate(Player player, Home home) {
        Location oldLocation = home.getLocation();
        Location location = player.getLocation().clone();
        boolean managingOthers = MainConfig.isManagingOthersHomes(player, home.getOwner());
        if (!managingOthers && location.getWorld() != null
                && MainConfig.isWorldBlacklisted(location.getWorld().getName())) {
            MessageUtil.send(player, MainConfig.getMessage("blacklisted_world"),
                    Placeholder.parsed("world", MainConfig.getWorldName(location.getWorld().getName())));
            SoundUtil.play(player, MainConfig.getSound("blacklisted_world"));
            return;
        }
        if (MainConfig.isSethomeClaimCheck() && !managingOthers) {
            if (!UHomes.getInstance().getClaimManager().canEnter(player, location)) {
                MessageUtil.send(player, MainConfig.getMessage("not_allowed_to_sethome"));
                SoundUtil.play(player, MainConfig.getSound("not_allowed_to_sethome"));
                return;
            }
        }
        home.setLocation(location);
        saveHome(home);
        ConsoleLogUtil.logHomeRelocated(player, home, oldLocation);
        MessageUtil.send(player, MainConfig.getMessage("relocatehome"),
                Placeholder.unparsed("home_name", home.getName() == null ? "" : home.getName()));
        SoundUtil.play(player, MainConfig.getSound("relocatehome"));
    }

    public void delete(Player player, Home home, int index) {
        ConfirmationInput.open(player, ConfirmationInput.Type.REMOVE, home, index, () -> {
            performDelete(player, home);
            HomesGUI.open(home.getOwner(), player);
        });
    }

    private void performDelete(Player player, Home home) {
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
                    plugin.getLogger().severe("An error occurred while deleting home of "
                            + Bukkit.getOfflinePlayer(uuid).getName() + " (" + uuid + ")");
                    e.printStackTrace();
                }
                MessageUtil.send(player, MainConfig.getMessage("deletehome"),
                        Placeholder.unparsed("home_name", home.getName() == null ? "" : home.getName()));
                ConsoleLogUtil.logHomeDeleted(player, home);
                SoundUtil.play(player, MainConfig.getSound("deletehome"));
                return;
            }
        }
    }

    public void rename(Player player, Home home, int index) {
        SetHomeCommand.renameHome(player, home, null, index);
    }

    public void reorder(Player player, Home home) {
        UUID owner = home.getOwner();
        List<Home> homes = loadedHomes.get(owner);
        if (homes == null) return;

        List<Home> sorted = sortHomes(new ArrayList<>(homes));
        int index = -1;
        for (int i = 0; i < sorted.size(); i++) {
            if (sorted.get(i).getCreated() == home.getCreated()) {
                index = i;
                break;
            }
        }
        if (index <= 0) {
            SchedulerUtil.runNextTick(plugin, () -> HomesGUI.open(owner, player));
            return;
        }

        Collections.swap(sorted, index, index - 1);

        int size = sorted.size();
        for (int i = 0; i < size; i++) {
            Home h = sorted.get(i);
            int newPriority = size - i;
            if (h.getPriority() != newPriority) {
                h.setPriority(newPriority);
                saveHome(h);
            }
        }

        SchedulerUtil.runNextTick(plugin, () -> HomesGUI.open(owner, player));
    }

    public static List<Home> sortHomes(List<Home> homes) {
        List<Home> sorted = new ArrayList<>(homes);
        HomeSort sort = MainConfig.getDefaultSorting();
        Comparator<Home> comparator = Comparator.comparingInt(Home::getPriority).reversed();
        comparator = comparator.thenComparing(switch (sort) {
            case CREATE -> Comparator.comparingLong(Home::getCreated);
            case CREATE_REVERSED -> Comparator.comparingLong(Home::getCreated).reversed();
            case LASTTP -> Comparator.<Home>comparingLong(h ->
                    h.getLastTeleport() == -1 ? Long.MIN_VALUE : h.getLastTeleport()).reversed();
        });
        sorted.sort(comparator);
        return sorted;
    }

    public UHomes getPlugin() {
        return plugin;
    }
}
