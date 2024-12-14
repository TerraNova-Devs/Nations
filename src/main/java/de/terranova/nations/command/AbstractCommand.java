package de.terranova.nations.command;

import de.mcterranova.terranovaLib.utils.Chat;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Supplier;

public abstract class AbstractCommand implements CommandExecutor, TabCompleter {

    protected final Map<String, Method> commandMethods = new HashMap<>();
    protected final Map<String, Class<?>> commandClasses = new HashMap<>();
    protected final Map<String, Supplier<List<String>>> commandTabPlaceholders = new HashMap<>();

    public AbstractCommand() {
        registerSubCommands();
        setupHelpCommand();
    }

    protected void addPlaceholder(String key, Supplier<List<String>> replacements) {
        this.commandTabPlaceholders.put(key, replacements);
    }

    protected abstract void registerSubCommands();

    private void setupHelpCommand() {
        if (!commandMethods.containsKey("help")) {
            try {
                Map<String, Method> newEntries = new HashMap<>();
                for (String group : commandClasses.keySet()) {
                    newEntries.put("help." + group, AbstractCommand.class.getDeclaredMethod("help", Player.class, String[].class, String.class, Map.class));
                }
                commandMethods.putAll(newEntries);
            } catch (NoSuchMethodException e) {
                System.out.println("ERROR: " + e.getMessage());
            }
        }
    }

    protected void registerSubCommand(Class<?> clazz, String groupName) {
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(CommandAnnotation.class)) {
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
            sender.sendMessage("Usage: /" + command.getName() + " help <" + String.join("|", commandClasses.keySet()) + ">");
            return true;
        }

        DomainCommandResolver resolver = new DomainCommandResolver(commandMethods);
        Method commandMethod = resolver.matchCommands(args, player);

        if (commandMethod == null) return true;

        CommandAnnotation annotation = commandMethod.getAnnotation(CommandAnnotation.class);
        if (annotation != null && !annotation.permission().isEmpty() && !player.hasPermission(annotation.permission())) {
            player.sendMessage("You do not have permission (" + annotation.permission() + ") to execute this command.");
            return true;
        }

        try {
            if(commandMethod.getName().equals("help")) {
                return help(player,args,command.getName(),commandClasses);
            }
            return (boolean) commandMethod.invoke(null, player, args);
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
        p.sendMessage(Chat.greenFade("---------" + commandClasses.get(args[1]).getSimpleName() + " Help ---------").decorate(TextDecoration.BOLD));
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

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        DomainTabResolver resolver = new DomainTabResolver(new ArrayList<>(commandMethods.keySet()), commandTabPlaceholders);
        List<String> results = resolver.getNextElements(args);
        return results;
    }
}



