package de.terranova.nations.commands;

import de.mcterranova.terranovaLib.utils.Chat;
import de.terranova.nations.NationsPlugin;
import de.terranova.nations.regions.access.AccessCommands;
import de.terranova.nations.regions.bank.BankCommands;
import de.terranova.nations.regions.base.RegionCommands;
import de.terranova.nations.regions.base.SelectCommands;
import de.terranova.nations.regions.npc.NPCCommands;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.*;
//TODO Abstraction help
public class TerraCommand implements CommandExecutor, TabCompleter {

    private static final Map<String, Method> commandMethods = new HashMap<>();
    private static final List<String> commandGroups = new ArrayList<>();
    private static final Map<String, String[]> commandTabReplacements = new HashMap<>();
    static {
        registerCommands(RegionCommands.class, "region");
        registerCommands(BankCommands.class, "bank");
        registerCommands(SelectCommands.class, "select");
        registerCommands(AccessCommands.class, "access");
        registerCommands(NPCCommands.class, "npc");

        List<String> strings = DomainCommandResolver.resolvePlaceholder(commandTabReplacements);
        if(NationsPlugin.debug){
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
        commandGroups.add(groupName);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage("This command can only be run by a player.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(Chat.greenFade("Usage: /" + command.getName() + " <" + String.join("|", commandGroups) + ">"));
            return true;
        }

        DomainCommandResolver resolver = new DomainCommandResolver(commandMethods);
        //Method commandMethod = CommandUtil.matchCommands(CommandUtil.replacePlaceholder(commandMethods,args),args);
        Method commandMethod = resolver.matchCommands(args, p);

        if (commandMethod == null) return true;

        CommandAnnotation annotation = commandMethod.getAnnotation(CommandAnnotation.class);
        if (!annotation.permission().isEmpty() && !sender.hasPermission(annotation.permission())) {
            sender.sendMessage("You do not have permission (" + annotation.permission() + ") to execute this command.");
            return true;
        }

        try {
            // Execute the command with all arguments, as found by findCommandMethod
            System.out.println("DEBUG4");
            return (boolean) commandMethod.invoke(null, p, args);
        } catch (Exception e) {
            sender.sendMessage("An error occurred while executing the command. Please try again.");
            e.printStackTrace();
            return true;
        }
    }


    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> keys = new ArrayList<>(commandTabReplacements.keySet());
        //return CommandUtil.getNextElements(keys,args);
        DomainTabResolver resolver = new DomainTabResolver(keys);
        return resolver.getNextElements(args);
    }



}

