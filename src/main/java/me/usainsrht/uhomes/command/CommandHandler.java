package me.usainsrht.uhomes.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;

import java.util.*;

public class CommandHandler {
    public static void register(String registrar, Command... cmds) {
        CommandMap commandMap = Bukkit.getCommandMap();
        for (Command cmd : cmds) {
            commandMap.register(registrar, cmd);
        }
    }

    public static void register(String registrar, List<Command> commands) {
        Bukkit.getCommandMap().registerAll(registrar, commands);
    }

    public static void unregister(Collection<Command> commands, boolean removeOtherPlugins, boolean removeAliases) {
        CommandMap commandMap = Bukkit.getCommandMap();
        Map<String, Command> knownCommands = commandMap.getKnownCommands();
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
