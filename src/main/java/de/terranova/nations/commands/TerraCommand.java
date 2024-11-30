package de.terranova.nations.commands;

import de.terranova.nations.NationsPlugin;
import de.terranova.nations.regions.base.RegionType;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.*;

public class TerraCommand implements CommandExecutor, TabCompleter {

    private static final Map<String, Method> commandMethods = new HashMap<>();
    private static final List<String> commandGroups = new ArrayList<>();

    static {
        registerCommands(RegionCommands.class, "region");
        registerCommands(BankCommands.class, "bank");
        registerCommands(SelectCommands.class, "select");
    }

    private static void registerCommands(Class<?> clazz, String groupName) {
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(CommandAnnotation.class)) {
                CommandAnnotation commandAnnotation = method.getAnnotation(CommandAnnotation.class);
                commandMethods.put(commandAnnotation.name(), method);
            }
        }
        commandGroups.add(groupName);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("terra") && !command.getName().equalsIgnoreCase("t")) {
            return false;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be run by a player.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("Usage: /terra <" + String.join("|", commandGroups) + "> <subcommand>");
            return true;
        }

        String groupName = args[0].toLowerCase();
        String subCommand = args.length > 1 ? args[1].toLowerCase() : "";
        String commandKey = "terra." + groupName + (subCommand.isEmpty() ? "" : "." + subCommand);
        Method commandMethod = commandMethods.get(commandKey);

        if (commandMethod == null) {
            sender.sendMessage("Unknown subcommand. Usage: /terra <" + String.join("|", commandGroups) + "> <subcommand>");
            return true;
        }

        CommandAnnotation annotation = commandMethod.getAnnotation(CommandAnnotation.class);
        if (!annotation.permission().isEmpty() && !sender.hasPermission(annotation.permission())) {
            sender.sendMessage("You do not have permission to execute this command.");
            return true;
        }

        if (args.length < annotation.tabCompletion().length + 2) {
            sender.sendMessage("Usage: " + annotation.usage());
            return true;
        }

        try {
            return (boolean) commandMethod.invoke(null, player, Arrays.stream(args).skip(1).toArray(String[]::new));
        } catch (Exception e) {
            sender.sendMessage("An error occurred while executing the command. Please try again.");
            e.printStackTrace();
            return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (!command.getName().equalsIgnoreCase("terra") && !command.getName().equalsIgnoreCase("t")) {
            return completions;
        }

        if (args.length == 1) {
            for (String group : commandGroups) {
                if (group.toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(group);
                }
            }
            return completions;
        }

        String baseCommand = "terra." + args[0].toLowerCase();
        for (Map.Entry<String, Method> entry : commandMethods.entrySet()) {
            String commandName = entry.getKey();

            if (!commandName.startsWith(baseCommand)) {
                continue;
            }

            CommandAnnotation annotation = entry.getValue().getAnnotation(CommandAnnotation.class);
            String[] annotationTabCompletion = annotation.tabCompletion();
            String[] commandParts = commandName.split("\\.");

            if (args.length == 2 && commandParts.length == 3 && commandParts[1].equalsIgnoreCase(args[0])) {
                if (args[1].isEmpty() || commandParts[2].toLowerCase().startsWith(args[1].toLowerCase())) {
                    completions.add(commandParts[2]);
                }
            } else if (args.length == 3 && commandParts.length > 2 && args[1].equalsIgnoreCase(commandParts[2])) {
                for (String suggestion : annotationTabCompletion) {
                    if (!suggestion.equalsIgnoreCase(commandParts[2]) && suggestion.toLowerCase().startsWith(args[2].toLowerCase())) {
                        completions.add(resolvePlaceholder(suggestion, sender));
                    }
                }
            }
        }

        return completions;
    }

    private String resolvePlaceholder(String placeholder, CommandSender sender) {
        if (placeholder.equalsIgnoreCase("$PLAYER")) {
            List<String> playerNames = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                playerNames.add(player.getName());
            }
            return String.join(",", playerNames);
        } else if (placeholder.equalsIgnoreCase("$REGISTERED_REGION_TYPES")) {
            // Example: Replace with logic to get available regions

            return String.join(",", RegionType.registry.keySet());
        }else if (placeholder.equalsIgnoreCase("$REGION_NAMES")) {
            // Example: Replace with logic to get available regions

            return String.join(",", NationsPlugin.settleManager.nameCache);
        }
        return placeholder;
    }
}

