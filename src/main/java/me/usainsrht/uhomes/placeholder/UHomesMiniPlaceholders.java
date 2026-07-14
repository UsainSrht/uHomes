package me.usainsrht.uhomes.placeholder;

import io.github.miniplaceholders.api.Expansion;
import me.usainsrht.uhomes.Home;
import me.usainsrht.uhomes.UHomes;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import org.bukkit.entity.Player;

import java.util.List;

public final class UHomesMiniPlaceholders {

    private UHomesMiniPlaceholders() {
    }

    public static Expansion build(UHomes plugin) {
        Expansion.Builder builder = Expansion.builder("uhomes")
                .author("UsainSrht")
                .version(plugin.getPluginMeta().getVersion());

        builder.audiencePlaceholder(Player.class, "home_count", (player, queue, ctx) ->
                tagFor(plugin, player, "home_count"));
        builder.audiencePlaceholder(Player.class, "homes_count", (player, queue, ctx) ->
                tagFor(plugin, player, "homes_count"));
        builder.audiencePlaceholder(Player.class, "homes", (player, queue, ctx) ->
                tagFor(plugin, player, "homes"));
        builder.audiencePlaceholder(Player.class, "home_limit", (player, queue, ctx) ->
                tagFor(plugin, player, "home_limit"));
        builder.audiencePlaceholder(Player.class, "homes_limit", (player, queue, ctx) ->
                tagFor(plugin, player, "homes_limit"));
        builder.audiencePlaceholder(Player.class, "limit", (player, queue, ctx) ->
                tagFor(plugin, player, "limit"));

        return builder.build();
    }

    private static Tag tagFor(UHomes plugin, Player player, String params) {
        String value = resolve(plugin, player, params);
        return Tag.selfClosingInserting(Component.text(value == null ? "" : value));
    }

    private static String resolve(UHomes plugin, Player player, String params) {
        if (params.equalsIgnoreCase("home_count")
                || params.equalsIgnoreCase("homes_count")
                || params.equalsIgnoreCase("homes")) {
            List<Home> homes = plugin.getHomeManager().getLoadedHomes(player.getUniqueId());
            if (homes != null) {
                return String.valueOf(homes.size());
            }
            plugin.getHomeManager().getHomes(player.getUniqueId());
            return "0";
        }

        if (params.equalsIgnoreCase("home_limit")
                || params.equalsIgnoreCase("homes_limit")
                || params.equalsIgnoreCase("limit")) {
            return String.valueOf(plugin.getHomeManager().getHomeLimit(player.getUniqueId()));
        }

        return null;
    }
}
