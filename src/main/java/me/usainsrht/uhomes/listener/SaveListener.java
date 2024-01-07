package me.usainsrht.uhomes.listener;

import me.usainsrht.uhomes.manager.HomeManager;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldSaveEvent;

public class SaveListener implements Listener {

    private HomeManager homeManager;

    public SaveListener(HomeManager homeManager) {
        this.homeManager = homeManager;
    }

    @EventHandler
    public void onSave(WorldSaveEvent e) {
        if (e.getWorld() != Bukkit.getWorlds().get(0)) return;
        homeManager.save();
    }


}
