package de.terranova.nations.commands;

import de.mcterranova.terranovaLib.utils.Chat;
import de.terranova.nations.NationsPlugin;
import de.terranova.nations.database.SettleDBstuff;
import de.terranova.nations.settlements.AccessLevel;
import de.terranova.nations.settlements.RegionTypes.SettleRegionType;
import de.terranova.nations.worldguard.math.Vectore2;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class SettleCreateSubCommand extends SubCommand implements BasicCommand {

    SettleCreateSubCommand(String permission) {
        super(permission);
    }

    @Override
    public void execute(@NotNull CommandSourceStack commandSourceStack, @NotNull String[] args) {

        Player p = isPlayer(commandSourceStack);
        if(p == null) return;
        if(!hasPermission(p, permission)) return;

        if (args[0].equalsIgnoreCase("create")) {
            //if(!SettlementRegionType.conditionCheck(p,args)) return;

            //String name = MiniMessage.miniMessage().stripTags(String.join("_", Arrays.copyOfRange(args, 1, args.length)));

            //SettlementRegionType newsettle = new SettlementRegionType(name, p);
            SettleRegionType newsettle = SettleRegionType.conditionCheck(p,args);
            NationsPlugin.settleManager.addSettlement(newsettle.id, newsettle);
            SettleDBstuff.addSettlement(newsettle.id, newsettle.name, new Vectore2(p.getLocation()), p.getUniqueId());
            p.sendMessage(Chat.greenFade("Deine Stadt " + newsettle.name + " wurde erfolgreich gegründet."));
            NationsPlugin.settleManager.addSettlementToPl3xmap(newsettle);

        }

        if (args[0].equalsIgnoreCase("rename")) {
            if (!hasPermission(p, "nations.rename")) return;
            if (!(args.length >= 2)) {
                p.sendMessage(Chat.errorFade("Syntax: /settle rename <name>"));
                return;
            }
            String name = MiniMessage.miniMessage().stripTags(String.join("_", Arrays.copyOfRange(args, 1, args.length)));
            if (!name.matches("^[a-zA-Z0-9_]{1,20}$")) {
                p.sendMessage(Chat.errorFade("Bitte verwende keine Sonderzeichen im Stadtnamen. Statt Leerzeichen _ verwenden. Nicht weniger als 3 oder mehr als 20 Zeichen verwenden."));
                return;
            }
            if (!NationsPlugin.settleManager.isNameAvaible(name)) {
                p.sendMessage(Chat.errorFade("Der Name ist leider bereits vergeben."));
                return;
            }
            Optional<SettleRegionType> settle = NationsPlugin.settleManager.getSettle(p.getLocation());
            if (settle.isPresent()) {
                Optional<AccessLevel> access = NationsPlugin.settleManager.getAccessLevel(p, settle.get().id);
                if (access.isEmpty()) return;
                if (access.get().equals(AccessLevel.MAJOR)) {
                    settle.get().rename(name);
                    p.sendMessage(name);
                } else {
                    p.sendMessage(Chat.errorFade("Du hast nicht genuegend Berechtigung um diese Stadt umzubenennen."));
                }
            } else {
                p.sendMessage(Chat.errorFade("Zum umbenennen bitte innerhalb einer Stadt stehen."));
            }
        }
        if (args[0].equalsIgnoreCase("tphere")) {
            if (!hasPermission(p, "nations.tphere")) return;
            Optional<SettleRegionType> settle = NationsPlugin.settleManager.getSettle(p.getLocation());

            if (settle.isPresent()) {
                Optional<AccessLevel> access = NationsPlugin.settleManager.getAccessLevel(p, settle.get().id);
                if (access.isEmpty()) return;
                if (access.get().equals(AccessLevel.MAJOR) || access.get().equals(AccessLevel.VICE)) {
                    settle.get().tpNPC(p.getLocation());
                }
            } else {
                p.sendMessage(Chat.errorFade("Zum teleportieren bitte innerhalb deines Claims stehen."));
            }
            return;
        }

        if (args[0].equalsIgnoreCase("unshow")) {
            if (!hasPermission(p, "worldedit.analysis.sel")) return;
            Bukkit.getServer().dispatchCommand(p, "/sel");
        }

        if (args[0].equalsIgnoreCase("show")) {
            if (!hasPermission(p, "worldguard.region.select.*")) return;
            Bukkit.getServer().dispatchCommand(p, "rg sel");
        }
    }

}
