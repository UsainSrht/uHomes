package me.usainsrht.uhomes.util;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SoundUtil {

    public static void play(CommandSender sender, Collection<Sound> sounds) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            sounds.forEach(player::playSound);
        }
    }

    public static void stop(CommandSender sender, Collection<Sound> sounds) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            sounds.forEach(player::stopSound);
        }
    }

    public static Sound getSound(String string) {
        Sound.Builder sound = Sound.sound();
        String[] splitted = string.split(",");
        Key name = Key.key(splitted[0]);
        sound.type(name);
        if (splitted.length > 1) {
            float volume = Float.parseFloat(splitted[1]);
            sound.volume(volume);
            if (splitted.length > 2) {
                float pitch = Float.parseFloat(splitted[2]);
                sound.pitch(pitch);
                if (splitted.length > 3) {
                    Sound.Source source = Sound.Source.valueOf(splitted[3]);
                    sound.source(source);
                    if (splitted.length > 4) {
                        long seed = Long.parseLong(splitted[4]);
                        sound.seed(seed);
                    }
                }
            }
        }
        return sound.build();
    }

    public static Collection<Sound> getSounds(Object obj) {
        List<Sound> soundList = new ArrayList<>();
        if (obj instanceof String string) soundList.add(SoundUtil.getSound(string));
        else if (obj instanceof Collection<?>) ((List<String>)obj).forEach(string -> soundList.add(SoundUtil.getSound(string)));
        return soundList;
    }

}
