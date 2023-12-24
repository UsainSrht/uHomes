package me.usainsrht.uhomes.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.SimpleCommandMap;

import java.lang.reflect.Field;
import java.util.*;

public class CommandHandler {

    public static CommandMap getCommandMap() {
        CommandMap commandMap = null;
        try {
            Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            commandMap = (CommandMap) commandMapField.get(Bukkit.getServer());
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return commandMap;
    }

    public static HashMap<String, Command> getKnownCommands(CommandMap commandMap) {
        HashMap<String, Command> knownCommands = null;
        try {
            Field knownCommandsField = ((SimpleCommandMap)commandMap).getClass().getDeclaredField("knownCommands");
            knownCommandsField.setAccessible(true);
            knownCommands = (HashMap<String, Command>) knownCommandsField.get(commandMap);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return knownCommands;
    }

    public static void register(String registrar, Command... cmds) {
        CommandMap commandMap = getCommandMap();
        for (Command cmd : cmds) {
            commandMap.register(registrar, cmd);
        }
    }

    public static void register(String registrar, List<Command> commands) {
        getCommandMap().registerAll(registrar, commands);
    }

    public static void unregister(Collection<Command> commands, boolean removeOtherPlugins, boolean removeAliases) {
        CommandMap commandMap = getCommandMap();
        HashMap<String, Command> knownCommands = getKnownCommands(commandMap);
        commands.forEach(command -> {
            command.unregister(commandMap);
            if (removeOtherPlugins) {
                Set<String> toBeRemovedKeys = new HashSet<>();
                for (String key : knownCommands.keySet()) {
                    if (key.endsWith(":" + command.getLabel())) {
                        toBeRemovedKeys.add(key);
                    }
                }
                for (String key : toBeRemovedKeys) {
                    knownCommands.remove(key);
                }
            }
            if (removeAliases) {
                if (removeOtherPlugins) {
                    Set<String> toBeRemovedKeys = new HashSet<>();
                    for (String key : knownCommands.keySet()) {
                        if (command.getAliases().stream().anyMatch(alias -> key.endsWith(":" + alias))) {
                            toBeRemovedKeys.add(key);
                        }
                    }
                    for (String key : toBeRemovedKeys) {
                        knownCommands.remove(key);
                    }
                }

                for (String alias : command.getAliases()) {
                    knownCommands.remove(alias);
                }
            }
            knownCommands.remove(command.getLabel());
        });
    }

}
