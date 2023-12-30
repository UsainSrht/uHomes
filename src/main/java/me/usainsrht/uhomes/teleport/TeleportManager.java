package me.usainsrht.uhomes.teleport;

import me.usainsrht.uhomes.UHomes;

import java.util.HashMap;
import java.util.UUID;

public class TeleportManager {

    private UHomes plugin;
    private HashMap<UUID, TimedTeleport> tpMap;

    public TeleportManager(UHomes plugin) {
        this.plugin = plugin;
    }



    public UHomes getPlugin() {
        return plugin;
    }
}
