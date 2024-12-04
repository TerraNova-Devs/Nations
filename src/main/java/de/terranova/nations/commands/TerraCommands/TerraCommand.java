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

import static de.terranova.nations.commands.CommandUtil.*;

public class TerraCommand implements CommandExecutor, TabCompleter {

    private static final Map<String, Method> commandMethods = new HashMap<>();
    private static final List<String> commandGroups = new ArrayList<>();
    private static final Map<String, String[]> commandTabReplacements = new HashMap<>();
    protected static Map<String,Object> domainTree = new HashMap<>();

    static {
        registerCommands(RegionCommands.class, "region");
        registerCommands(BankCommands.class, "bank");
        registerCommands(SelectCommands.class, "select");
        registerCommands(AccessCommands.class, "access");

        List<String> strings = CommandUtil.resolvePlaceholder(commandTabReplacements);
        domainTree = buildDomainTree(strings);
        if(NationsPlugin.debug){
            System.out.println(commandTabReplacements.keySet());
            System.out.println(strings);
            printTree(domainTree, "[Debug] ");
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
            sender.sendMessage("Usage: /terra <" + String.join("|", commandGroups) + "> <subcommand>");
            return true;
        }

        Method commandMethod = CommandUtil.matchCommands(CommandUtil.replacePlaceholder(commandMethods,args),args);

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
        List<String> keys = new ArrayList<>(commandTabReplacements.keySet());
        return CommandUtil.getNextElements(keys,args);
    }



}

