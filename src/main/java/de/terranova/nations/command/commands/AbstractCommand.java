package de.terranova.nations.command.commands;

import de.terranova.nations.utils.Chat;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AbstractCommand implements CommandExecutor, TabCompleter {

    // Old: protected Map<String, Supplier<List<String>>> commandTabPlaceholders = registerPlaceholders();
    // New: store placeholders as PlayerAwarePlaceholder
    protected final Map<String, PlayerAwarePlaceholder> commandTabPlaceholders = registerPlaceholders();

    protected final Map<String, Method> commandMethods = new HashMap<>();
    protected final Map<String, Object> commandClassInstances = new HashMap<>();
    protected final Map<String, Class<?>> commandClasses = new HashMap<>();

    DomainCommandResolver commandResolver;
    DomainTabCompletionResolver tabResolver;

    /**
     * Here we now return a Map<String, PlayerAwarePlaceholder> instead of
     * Map<String, Supplier<List<String>>>
     */
    Map<String, PlayerAwarePlaceholder> registerPlaceholders() {
        return new HashMap<>();
    }

    /**
     * Overload: Add a placeholder that does NOT depend on the player
     */
    public void addPlaceholder(String key, Supplier<List<String>> replacements) {
        this.commandTabPlaceholders.put(key, PlayerAwarePlaceholder.ofStatic(replacements));
    }

    /**
     * Overload: Add a placeholder that DOES depend on the player's UUID
     */
    public void addPlaceholder(String key, Function<UUID, List<String>> replacements) {
        this.commandTabPlaceholders.put(key, PlayerAwarePlaceholder.ofPlayerFunction(replacements));
    }

    // NEW method for directly passing a PlayerAwarePlaceholder
    public void addPlaceholder(String key, PlayerAwarePlaceholder placeholder) {
        this.commandTabPlaceholders.put(key, placeholder);
    }

    protected void setupHelpCommand() {
        if (!commandMethods.containsKey("help")) {
            try {
                Map<String, Method> newEntries = new HashMap<>();
                for (String group : commandClassInstances.keySet()) {
                    newEntries.put("help." + group, AbstractCommand.class
                            .getDeclaredMethod("help", Player.class, String[].class, String.class, Map.class));
                }
                for (String group : commandClasses.keySet()) {
                    newEntries.put("help." + group, AbstractCommand.class
                            .getDeclaredMethod("help", Player.class, String[].class, String.class, Map.class));
                }
                commandMethods.putAll(newEntries);
            } catch (NoSuchMethodException e) {
                System.out.println("ERROR: " + e.getMessage());
            }
        }
    }

    protected void initialize() {
        setupHelpCommand();
        // Pass our placeholders map to the DomainTabCompletionResolver
        tabResolver = new DomainTabCompletionResolver(new ArrayList<>(commandMethods.keySet()), commandTabPlaceholders);
        commandResolver = new DomainCommandResolver(commandMethods);
    }

    protected void registerSubCommand(Object instance, String groupName) {
        for (Method method : instance.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(CommandAnnotation.class)) {
                if(Modifier.isStatic(method.getModifiers())) continue;
                CommandAnnotation annotation = method.getAnnotation(CommandAnnotation.class);
                commandMethods.put(annotation.domain(), method);
            }
        }
        commandClassInstances.put(groupName, instance);
    }

    protected void registerSubCommand(Class<?> clazz, String groupName) {
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(CommandAnnotation.class)) {
                if(!Modifier.isStatic(method.getModifiers())) continue;
                CommandAnnotation annotation = method.getAnnotation(CommandAnnotation.class);
                commandMethods.put(annotation.domain(), method);
            }
        }
        commandClasses.put(groupName, clazz);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be run by a player.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("Usage: /" + command.getName() + " help <" +
                    String.join("|", commandClassInstances.keySet()) + ">");
            return true;
        }

        Method commandMethod = commandResolver.matchCommands(args, player);
        if (commandMethod == null) return true;

        CommandAnnotation annotation = commandMethod.getAnnotation(CommandAnnotation.class);
        if (annotation != null && !annotation.permission().isEmpty() && !player.hasPermission(annotation.permission())) {
            player.sendMessage("You do not have permission (" + annotation.permission() + ") to execute this command.");
            return true;
        }

        try {
            if(commandMethod.getName().equals("help")) {
                return help(player, args, command.getName(),
                        Stream.concat(commandClassInstances.entrySet().stream()
                                                .map(entry -> Map.entry(entry.getKey(), entry.getValue().getClass())),
                                        commandClasses.entrySet().stream())
                                .collect(Collectors.toMap(Map.Entry::getKey,
                                        Map.Entry::getValue,
                                        (value1, value2) -> value1)));
            } else if (Modifier.isStatic(commandMethod.getModifiers())) {
                return (boolean) commandMethod.invoke(null, player, args);
            } else if(commandClassInstances.containsKey(args[0])) {
                return (boolean) commandMethod.invoke(commandClassInstances.get(args[0]), player, args);
            } else {
                throw new RuntimeException("Error: Command was found but no instance was available to invoke it.");
            }
        } catch (Exception e) {
            player.sendMessage("An error occurred while executing the command. Please try again.");
            e.printStackTrace();
            return true;
        }
    }

    public boolean help(Player p, String[] args, String commandPrefix, Map<String, Class<?>> commandClasses) {
        if (!p.hasPermission(commandPrefix + ".help." + args[1])) {
            p.sendMessage("You do not have permission (" + commandPrefix + ".help." + args[1] + ") to execute this command.");
            return true;
        }
        p.sendMessage(Chat.greenFade("---------" +
                        commandClasses.get(args[1]).getSimpleName() +
                        " Help ---------")
                .decorate(TextDecoration.BOLD));
        for (Method method : commandClasses.get(args[1]).getDeclaredMethods()) {
            if (method.isAnnotationPresent(CommandAnnotation.class)) {
                CommandAnnotation commandAnnotation = method.getAnnotation(CommandAnnotation.class);
                if (!commandAnnotation.description().isEmpty() && !commandAnnotation.usage().isEmpty()) {
                    p.sendMessage(Chat.cottonCandy("\u2B24 " + commandAnnotation.description()));
                    p.sendMessage(Chat.blueFade("\u2192" + commandAnnotation.usage()));
                }
            }
        }
        return true;
    }

    /**
     * Refactored tab complete method: we pass the CommandSender to the resolver
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return tabResolver.getNextElements(sender, args);
    }
}



