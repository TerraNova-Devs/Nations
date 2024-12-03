package de.terranova.nations.commands.TerraCommands;

import de.mcterranova.terranovaLib.utils.Chat;
import de.terranova.nations.NationsPlugin;
import de.terranova.nations.commands.CommandAnnotation;
import de.terranova.nations.commands.CommandUtil;
import de.terranova.nations.regions.base.RegionType;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

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

        Method commandMethod = CommandUtil.findExactCommand(CommandUtil.findCommands(commandMethods,args),args);

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


    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        String commandLabel = command.getName().toLowerCase(); // Should be "terra" or "t"

        int argsLength = args.length;

        for (Map.Entry<String, Method> entry : commandMethods.entrySet()) {
            String domain = entry.getKey();
            Method method = entry.getValue();
            CommandAnnotation annotation = method.getAnnotation(CommandAnnotation.class);
            if (annotation == null) continue;

            String[] domainParts = domain.split("\\.");

            if (!domainParts[0].equalsIgnoreCase(commandLabel)) continue;

            int domainIndex = 1; // Start after "terra"
            int argsIndex = 0; // Start from first arg in args

            boolean match = true;
            while (domainIndex < domainParts.length && argsIndex < argsLength) {
                String domainPart = domainParts[domainIndex];
                String arg = args[argsIndex];

                if (domainPart.equals("$ARGUMENT")) {
                    // Any arg matches
                } else if (!domainPart.equalsIgnoreCase(arg)) {
                    match = false;
                    break;
                }

                domainIndex++;
                argsIndex++;
            }

            if (!match) continue;

            // Handle the case where the last argument is empty (user typed a space and is expecting suggestions)
            boolean isExpectingArgument = argsLength == domainIndex && (argsLength == 0 || args[argsLength - 1].isEmpty());

            if (isExpectingArgument) {
                if (domainIndex < domainParts.length) {
                    String nextDomainPart = domainParts[domainIndex];

                    if (nextDomainPart.equals("$ARGUMENT")) {
                        int argPositionIndex = 0;
                        for (int i = 1; i <= domainIndex; i++) {
                            if (domainParts[i].equals("$ARGUMENT")) {
                                argPositionIndex++;
                            }
                        }
                        argPositionIndex--; // Adjust to zero-based index

                        String[] tabCompletions = annotation.tabCompletion();
                        if (argPositionIndex < tabCompletions.length) {
                            String tabCompletion = tabCompletions[argPositionIndex];
                            // Process tabCompletion
                            if (tabCompletion.startsWith("$")) {
                                String replacement = resolvePlaceholder(tabCompletion, sender);
                                if (replacement != null && !replacement.isEmpty()) {
                                    String[] options = replacement.split(",");
                                    completions.addAll(Arrays.asList(options));
                                }
                            } else {
                                completions.add(tabCompletion);
                            }
                        }
                    } else {
                        completions.add(nextDomainPart);
                    }
                }
            }
        }

        // Remove duplicates
        completions = new ArrayList<>(new HashSet<>(completions));

        // Filter completions based on current input
        if (argsLength > 0) {
            String currentArg = args[argsLength - 1].toLowerCase();
            if (!currentArg.isEmpty()) {
                completions.removeIf(s -> !s.toLowerCase().startsWith(currentArg));
            }
        }

        return completions;
    }

    private String resolvePlaceholder(String placeholder, CommandSender sender) {
        if (placeholder.equalsIgnoreCase("$PLAYER")) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .collect(Collectors.joining(","));
        } else if (placeholder.equalsIgnoreCase("$REGISTERED_REGION_TYPES")) {
            return String.join(",", RegionType.registry.keySet());
        } else if (placeholder.equalsIgnoreCase("$REGION_NAMES")) {
            return String.join(",", NationsPlugin.settleManager.getNameCache());
        }
        return placeholder;
    }

}

