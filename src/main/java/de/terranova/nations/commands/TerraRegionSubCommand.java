package de.terranova.nations.commands;

import de.mcterranova.terranovaLib.utils.Chat;
import de.terranova.nations.NationsPlugin;
import de.terranova.nations.database.SettleDBstuff;
import de.terranova.nations.settlements.RegionTypes.OutpostRegionType;
import de.terranova.nations.settlements.RegionTypes.SettleRegionType;
import de.terranova.nations.worldguard.math.Vectore2;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TerraRegionSubCommand extends SubCommand implements BasicCommand {
    TerraRegionSubCommand(String permission) {super(permission);}

    @Override
    public void execute(@NotNull CommandSourceStack commandSourceStack, @NotNull String[] args) {
        Player p = isPlayer(commandSourceStack);

        if(args[0].equalsIgnoreCase("create")) {
            switch (args[1].toLowerCase()) {
                case "settle":
                    hasPermission(p, permission + ".settle");
                    SettleRegionType settle = SettleRegionType.conditionCheck(p,args);
                    if (settle == null) return;
                    NationsPlugin.settleManager.addSettlement(settle.id, settle);
                    SettleDBstuff.addSettlement(settle.id, settle.name, new Vectore2(p.getLocation()), p.getUniqueId());
                    p.sendMessage(Chat.greenFade("Deine Stadt " + settle.name + " wurde erfolgreich gegründet."));
                    NationsPlugin.settleManager.addSettlementToPl3xmap(settle);
                case "outpost":
                    hasPermission(p, permission + ".outpost");
                    TerraSelectCache cache = hasSelect(p);
                    if(cache == null) return;
                    OutpostRegionType outpost = OutpostRegionType.conditionCheck(p,args);
                    if (outpost == null) return;
                    //NationsPlugin.settleManager.addSettlement(outpost.id, outpost);
                    SettleDBstuff.addSettlement(outpost.id, outpost.name, new Vectore2(p.getLocation()), p.getUniqueId());
                    p.sendMessage(Chat.greenFade("Deine Stadt " + outpost.name + " wurde erfolgreich gegründet."));
                    //NationsPlugin.settleManager.addSettlementToPl3xmap(outpost);
                default:
            }
        }
        if(args[0].equalsIgnoreCase("remove")) {
        }
    }
}
