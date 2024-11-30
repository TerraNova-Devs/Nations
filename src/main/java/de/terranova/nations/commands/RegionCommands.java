package de.terranova.nations.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class RegionCommands {

    @CommandAnnotation(name = "terra.region.create", permission = "nations.region.create", description = "Creates a new region", usage = "/terra region create <name>", tabCompletion = {"create", "delete"})
    public static boolean createRegion(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be run by a player.");
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage("Usage: /terra region create <name>");
            return true;
        }

        String regionName = args[2];

        // Here you would add your logic for creating a region
        player.sendMessage("Region " + regionName + " created successfully!");
        return true;
    }

    @CommandAnnotation(name = "terra.region.delete", permission = "nations.region.delete", description = "Deletes an existing region", usage = "/terra region delete <name>", tabCompletion = {"delete"})
    public static boolean deleteRegion(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be run by a player.");
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage("Usage: /terra region delete <name>");
            return true;
        }

        String regionName = args[2];

        // Here you would add your logic for deleting a region
        player.sendMessage("Region " + regionName + " deleted successfully!");
        return true;
    }
}
