package me.usainsrht.uhomes.listener;

import me.usainsrht.uhomes.manager.HomeManager;
import me.usainsrht.uhomes.util.SchedulerUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {

    private HomeManager homeManager;

    public JoinListener(HomeManager homeManager) {
        this.homeManager = homeManager;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        SchedulerUtil.runLater(homeManager.getPlugin(), () -> {
            if (!player.isOnline()) return;
            homeManager.getHomes(player.getUniqueId());
        }, 1L);
    }

}
