package de.terranova.nations.commands;

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import de.terranova.nations.NationsPlugin;
import de.terranova.nations.settlements.settlement;
import de.terranova.nations.utils.ChatUtils;
import de.terranova.nations.worldguard.claim;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class settle implements BasicCommand, TabCompleter {

  NationsPlugin plugin;

  public settle(NationsPlugin plugin) {
    this.plugin = plugin;
  }

    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String[] args) {

    if (!(stack.getSender() instanceof Player p)) {
      stack.getSender().sendMessage("Du musst für diesen Command ein Spieler sein!");
      return;
    }

    if (args.length == 0) {
      ChatUtils.sendMessage(p, "Nations Plugin by gerryxn. Version 1.0.0 as of 13.07.2024 | Copyright Pixel Party.");
      return;
    }

    if (!p.hasPermission("admin")) {
      return;
    }

    if (args[0].equalsIgnoreCase("create")) {
      if (!(args.length == 2)) {
        p.sendMessage(ChatUtils.returnRedFade(ChatUtils.chatPrefix + "Syntax: /settle create <name>"));
        return;
      }
      if (args[1].length() > 20) {
        p.sendMessage(ChatUtils.returnRedFade(ChatUtils.chatPrefix + "Der Name darf nicht laenger als 20 zeichen sein."));
        return;
      }
      String name = args[1];
      if (!plugin.settlementManager.isNameAvaible(name)) {
        p.sendMessage(ChatUtils.returnRedFade(ChatUtils.chatPrefix + "Der Name ist bereits vergeben!"));
        return;
      }
      if (claim.checkAreaForSettles(p)) {
        p.sendMessage(ChatUtils.returnRedFade(ChatUtils.chatPrefix + "Der Claim ist bereits vergeben!"));
        return;
      }

      if (plugin.settlementManager.canSettle(p.getUniqueId())) {

        LocalPlayer lp = WorldGuardPlugin.inst().wrapPlayer(p);
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regions = container.get(lp.getWorld());
        regions.getRegions().forEach((k, v) -> p.sendMessage(ChatUtils.returnGreenFade(" " + k + v)));
        settlement newsettle = new settlement(p.getUniqueId(), p.getLocation(), name);
        plugin.settlementManager.addSettlement(p.getUniqueId(), newsettle);
        claim.createClaim(name, p);
      }
    }
    if (args[0].equalsIgnoreCase("tphere")) {
      Optional<settlement> settlement = plugin.settlementManager.checkIfPlayerIsInsideHisClaim(p);
      if (settlement.isPresent()) {
        settlement.get().tpNPC(p.getLocation());
      } else {
        p.sendMessage(ChatUtils.returnRedFade(ChatUtils.chatPrefix + "Zum teleportieren bitte innerhalb deines Claims stehen!"));
      }

      return;
    }


    if (args[0].equalsIgnoreCase("claim")) {
      if (plugin.settlementManager.howManySettlements(p.getUniqueId()) >= 1) {
        Optional<ProtectedRegion> t = claim.checkSurrAreaForSettles(p);
        t.ifPresent(protectedRegion -> claim.addToExistingClaim(p, protectedRegion));


      }


    }

      return;
  }

  @Override
  public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
    return List.of();
  }
}
