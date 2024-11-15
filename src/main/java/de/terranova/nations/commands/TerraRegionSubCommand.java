package de.terranova.nations.commands;

import de.mcterranova.terranovaLib.utils.Chat;
import de.terranova.nations.database.SettleDBstuff;
import de.terranova.nations.settlements.RegionType;
import de.terranova.nations.settlements.RegionTypes.OutpostRegionType;
import de.terranova.nations.settlements.RegionTypes.SettleRegionType;
import de.terranova.nations.worldguard.math.Vectore2;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class TerraRegionSubCommand extends SubCommand implements BasicCommand {
    TerraRegionSubCommand(String permission) {super(permission);}

    @Override
    public void execute(@NotNull CommandSourceStack commandSourceStack, @NotNull String[] args) {
        Player p = isPlayer(commandSourceStack);


        if(args[0].equalsIgnoreCase("create")) {
            if(args.length <= 2) {
                p.sendMessage(Chat.errorFade(String.format("Bitte benutze nur folgende Regionstypen:", RegionType.regionTypes)));
                return;
            }
            String name = MiniMessage.miniMessage().stripTags(String.join("_", Arrays.copyOfRange(args, 1, args.length)));
            switch (args[1].toLowerCase()) {
                case "settle":
                    hasPermission(p, permission + ".settle");
                    SettleRegionType.conditionCheck(p,name);
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
                    p.sendMessage(Chat.errorFade(""));
            }
        }
        if(args[0].equalsIgnoreCase("remove")) {
        }
    }
}
