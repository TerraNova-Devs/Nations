package org.nations.commands;

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nations.Nations;
import org.nations.settlements.settlement;
import org.nations.utils.ChatUtils;
import org.nations.worldguard.claim;

import java.util.List;

public class settle implements CommandExecutor, TabCompleter {

    Nations plugin;
    settlement settlement;

    public settle(Nations plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player p)) {
            sender.sendMessage("Du musst für diesen Command ein Spieler sein!");
            return false;
        }

        if (args.length == 0) {
            ChatUtils.sendMessage(p, "Nations Plugin by gerryxn. Version 1.0.0 as of 13.07.2024 | Copyright Pixel Party.");
            return false;
        }

        if (!p.hasPermission("admin")) {
            return false;
        }

        if (args[0].equalsIgnoreCase("test")) {
            if (plugin.settlementManager.canSettle(p.getUniqueId())) {


                LocalPlayer lp = WorldGuardPlugin.inst().wrapPlayer(p);
                RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
                RegionManager regions = container.get(lp.getWorld());
                regions.getRegions().forEach((k, v) -> p.sendMessage(ChatUtils.returnGreenFade(" " + k + v)));


                settlement newsettle = new settlement(p.getUniqueId(), p.getLocation(), "test");
                this.settlement = newsettle;
                plugin.settlementManager.addSettlement(p.getUniqueId(), newsettle);

            }
        }
        if (args[0].equalsIgnoreCase("tphere")) {
            this.settlement.tpNPC(p.getLocation());
            return false;
        }


        if (args[0].equalsIgnoreCase("claim")) {
            claim.createClaim(p.getLocation().getBlockX(),p.getLocation().getBlockZ(),"BastiisnKloos", p);
        }

        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return List.of();
    }
}
