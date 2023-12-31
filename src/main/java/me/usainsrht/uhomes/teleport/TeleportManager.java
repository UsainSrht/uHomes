package me.usainsrht.uhomes.teleport;

import me.usainsrht.uhomes.UHomes;
import me.usainsrht.uhomes.config.MainConfig;

import java.util.HashMap;
import java.util.UUID;

public class TeleportManager {

    private UHomes plugin;
    private HashMap<UUID, TimedTeleport> tpMap;

    public TeleportManager(UHomes plugin) {
        this.plugin = plugin;
        this.tpMap = new HashMap<>();
    }

    public void start(TimedTeleport timedTeleport) {
        UUID uuid = timedTeleport.getEntity().getUniqueId();
        if (tpMap.containsKey(uuid)) {
            if (!tpMap.get(uuid).isDone()) tpMap.get(uuid).cancel();
            tpMap.remove(uuid);
        }
        tpMap.put(uuid, timedTeleport);
        timedTeleport.start(MainConfig.isLoadChunkBeforeTp());
    }

    public UHomes getPlugin() {
        return plugin;
    }
}
