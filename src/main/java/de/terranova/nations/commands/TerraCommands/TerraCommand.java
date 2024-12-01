package de.terranova.nations.commands.TerraCommands;

import de.mcterranova.terranovaLib.utils.Chat;
import de.terranova.nations.NationsPlugin;
import de.terranova.nations.commands.CommandAnnotation;
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
        registerCommands(AccessCommands.class, "access");
    }

    private static void registerCommands(Class<?> clazz, String groupName) {
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(CommandAnnotation.class)) {
                CommandAnnotation commandAnnotation = method.getAnnotation(CommandAnnotation.class);
                commandMethods.put(commandAnnotation.domain(), method);
            }
        }
        commandGroups.add(groupName);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage("This command can only be run by a player.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("Usage: /terra <" + String.join("|", commandGroups) + "> <subcommand>");
            return true;
        }

        StringBuilder subCommandKeyBuilder = new StringBuilder("terra." + args[0].toLowerCase());
        Method commandMethod = findCommandMethod(subCommandKeyBuilder, args);

        if (commandMethod == null) {
            p.sendMessage(Chat.errorFade("Unknown subcommand. Usage: /terra <" + String.join("|", commandGroups) + "> <subcommand>"));
            return true;
        }

        CommandAnnotation annotation = commandMethod.getAnnotation(CommandAnnotation.class);
        if (!annotation.permission().isEmpty() && !sender.hasPermission(annotation.permission())) {
            sender.sendMessage("You do not have permission (" + annotation.permission() + ") to execute this command.");
            return true;
        }

        try {
            // Execute the command with all arguments, as found by findCommandMethod
            return (boolean) commandMethod.invoke(null, p, args);
        } catch (Exception e) {
            sender.sendMessage("An error occurred while executing the command. Please try again.");
            e.printStackTrace();
            return true;
        }
    }

    private Method findCommandMethod(StringBuilder subCommandKeyBuilder, String[] args) {
        // Step 1: Find the initial exact match (e.g., "terra.select")
        Method commandMethod = commandMethods.get(subCommandKeyBuilder.toString());
        Method fallbackMethod = commandMethod; // Save this in case no $ARGUMENT method is found
        int index = 1;

        // Step 2: If arguments are present, try to find a more specific match with $ARGUMENT
        while (index < args.length) {
            subCommandKeyBuilder.append(".").append(args[index].toLowerCase());
            commandMethod = commandMethods.get(subCommandKeyBuilder.toString());

            if (commandMethod != null) {
                // Found a more specific command method, update fallbackMethod
                fallbackMethod = commandMethod;
            } else {
                // If no exact match is found, look for a method with $ARGUMENT placeholder
                String argumentCommandKey = subCommandKeyBuilder.substring(0, subCommandKeyBuilder.lastIndexOf(".")) + ".$ARGUMENT";
                Method argumentMethod = commandMethods.get(argumentCommandKey);
                if (argumentMethod != null) {
                    fallbackMethod = argumentMethod;
                }
            }
            index++;
        }

        // Step 3: Return the most appropriate method found (either a specific or the initial one)
        return fallbackMethod;
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

        StringBuilder baseCommandBuilder = new StringBuilder("terra." + args[0].toLowerCase());
        for (int i = 1; i < args.length - 1; i++) {
            baseCommandBuilder.append(".").append(args[i].toLowerCase());
        }

        String baseCommand = baseCommandBuilder.toString();
        for (Map.Entry<String, Method> entry : commandMethods.entrySet()) {
            String commandName = entry.getKey();

            if (!commandName.startsWith(baseCommand)) {
                continue;
            }

            CommandAnnotation annotation = entry.getValue().getAnnotation(CommandAnnotation.class);
            String[] annotationTabCompletion = annotation.tabCompletion();
            String[] commandParts = commandName.split("\\.");

            if (args.length == commandParts.length && commandParts[commandParts.length - 1].toLowerCase().startsWith(args[args.length - 1].toLowerCase())) {
                completions.add(commandParts[commandParts.length - 1]);
            } else if (args.length == commandParts.length + 1) {
                for (String suggestion : annotationTabCompletion) {
                    if (suggestion.toLowerCase().startsWith(args[args.length - 1].toLowerCase())) {
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
        } else if (placeholder.equalsIgnoreCase("$REGION_NAMES")) {
            // Example: Replace with logic to get available regions

            return String.join(",", NationsPlugin.settleManager.getNameCache());
        }
        return placeholder;
    }
}

