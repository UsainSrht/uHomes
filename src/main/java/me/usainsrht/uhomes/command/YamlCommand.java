package me.usainsrht.uhomes.command;

import net.kyori.adventure.sound.Sound;

import java.util.Collection;
import java.util.List;

public class YamlCommand {

    private String name;
    private String description;
    private String usage;
    private List<String> aliases;
    private String permission;
    private String permissionMessage;
    private Collection<Sound> permissionSounds;

    public YamlCommand(String name, String description, String usage, List<String> aliases, String permission, String permissionMessage, Collection<Sound> permissionSounds) {
        this.name = name;
        this.description = description;
        this.usage = usage;
        this.aliases = aliases;
        this.permission = permission;
        this.permissionMessage = permissionMessage;
        this.permissionSounds = permissionSounds;
    }

    public String getName() {
        return name;
    }

    public List<String> getAliases() {
        return aliases;
    }

    public String getUsage() {
        return usage;
    }

    public String getPermission() {
        return permission;
    }

    public String getDescription() {
        return description;
    }

    public String getPermissionMessage() {
        return permissionMessage;
    }

    public Collection<Sound> getPermissionSounds() {
        return permissionSounds;
    }
}
