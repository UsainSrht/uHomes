package me.usainsrht.uhomes.command;

import java.util.List;

public class YamlCommand {

    private String name;
    private String description;
    private String usage;
    private List<String> aliases;
    private String permission;
    private String permissionMessage;

    public YamlCommand(String name, String description, String usage, List<String> aliases, String permission, String permissionMessage) {
        this.name = name;
        this.description = description;
        this.usage = usage;
        this.aliases = aliases;
        this.permission = permission;
        this.permissionMessage = permissionMessage;
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
}
