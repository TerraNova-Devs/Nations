package de.terranova.nations.command.commands;

import de.mcterranova.terranovaLib.utils.Chat;
import de.terranova.nations.NationsPlugin;
import de.terranova.nations.command.CommandAnnotation;
import de.terranova.nations.command.DomainCommandResolver;
import de.terranova.nations.command.DomainTabResolver;
import de.terranova.nations.regions.access.AccessCommands;
import de.terranova.nations.regions.bank.BankCommands;
import de.terranova.nations.regions.base.RegionCommands;
import de.terranova.nations.regions.base.SelectCommands;
import de.terranova.nations.regions.npc.NPCCommands;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//TODO Abstraction TabCompletion
public class TerraCommand implements CommandExecutor, TabCompleter {

    private static final Map<String, Method> commandMethods = new HashMap<>();
    private static final Map<String, Class<?>> commandClasses = new HashMap<>();
    private static final Map<String, String[]> commandTabReplacements = new HashMap<>();

    static {
        registerCommands(RegionCommands.class, "region");
        registerCommands(BankCommands.class, "bank");
        registerCommands(SelectCommands.class, "select");
        registerCommands(AccessCommands.class, "access");
        registerCommands(NPCCommands.class, "npc");

        List<String> strings = DomainCommandResolver.resolvePlaceholder(commandTabReplacements);
        if (NationsPlugin.debug) {
            System.out.println(commandTabReplacements.keySet());
            System.out.println(strings);
        }
    }

    private static void registerCommands(Class<?> clazz, String groupName) {
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(CommandAnnotation.class)) {
                CommandAnnotation commandAnnotation = method.getAnnotation(CommandAnnotation.class);
                commandMethods.put(commandAnnotation.domain(), method);
                commandTabReplacements.put(method.getAnnotation(CommandAnnotation.class).domain(), method.getAnnotation(CommandAnnotation.class).tabCompletion());
            }
        }
        commandClasses.put(groupName, clazz);
        if (!commandMethods.containsKey("help")) {
            try {
                // Create a new map for additional entries
                Map<String, Method> newEntries = new HashMap<>();

                for (String string : commandClasses.keySet()) {
                    newEntries.put("help." + string, TerraCommand.class.getDeclaredMethod("help", Player.class, String[].class));
                }

                // Add all new entries to the original map
                commandMethods.putAll(newEntries);
                for (String string : newEntries.keySet()) {
                    commandTabReplacements.put(string, null);
                }

            } catch (NoSuchMethodException e) {
                System.out.println("ERROR: " + e.getMessage());
            }
        }
    }

    public static boolean help(Player p, String[] args) {
        if (!p.hasPermission("terra.help." + args[1])) {
            p.sendMessage("You do not have permission (" + "terra.help." + args[1] + ") to execute this command.");
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
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage("This command can only be run by a player.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(Chat.greenFade("Usage: /" + command.getName() + " <" + String.join("|", commandClasses.keySet()) + ">"));
            return true;
        }

        DomainCommandResolver resolver = new DomainCommandResolver(commandMethods);
        //Method commandMethod = CommandUtil.matchCommands(CommandUtil.replacePlaceholder(commandMethods,args),args);
        Method commandMethod = resolver.matchCommands(args, p);

        if (commandMethod == null) return true;

        CommandAnnotation annotation = commandMethod.getAnnotation(CommandAnnotation.class);
        if (annotation != null) {
            if (!annotation.permission().isEmpty() && !p.hasPermission(annotation.permission())) {
                p.sendMessage("You do not have permission (" + annotation.permission() + ") to execute this command.");
                return true;
            }
        }


        try {
            // Execute the command with all arguments, as found by findCommandMethod
            System.out.println("DEBUG4");
            return (boolean) commandMethod.invoke(null, p, args);
        } catch (Exception e) {
            p.sendMessage("An error occurred while executing the command. Please try again.");
            e.printStackTrace();
            return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> keys = new ArrayList<>(commandTabReplacements.keySet());
        DomainTabResolver resolver = new DomainTabResolver(keys);
        return resolver.getNextElements(args);
    }

}

