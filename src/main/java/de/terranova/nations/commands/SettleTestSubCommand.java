package de.terranova.nations.commands;

import de.mcterranova.terranovaLib.utils.Chat;
import de.terranova.nations.NationsPlugin;
import de.terranova.nations.settlements.PropertyTypeClasses.SettlementPropertyType;
import de.terranova.nations.worldguard.math.Vectore2;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("UnstableApiUsage")
public class SettleTestSubCommand extends SubCommand implements BasicCommand {


    SettleTestSubCommand(String permission) {
        super(permission);
    }

    @Override
    public void execute(@NotNull CommandSourceStack commandSourceStack, @NotNull String[] args) {

        Player p = isPlayer(commandSourceStack);
        if(p == null) return;
        if(!hasPermission(p, permission)) return;

        if (args[0].equalsIgnoreCase("testt")) {
            if (!hasPermission(p, "nations.admin.testt")) return;
            double x = (double) Integer.parseInt(args[1]) /100;
            double y = x < 0.5 ? 2 * x * x : 1 - Math.pow(-2 * x + 2, 2) / 2;
            p.sendMessage(Chat.stringToComponent("" + y));
        }

        if (args[0].equalsIgnoreCase("test")) {
            if (!hasPermission(p, "nations.admin.test")) return;
            SettlementPropertyType settle = NationsPlugin.settleManager.getSettle(p.getLocation()).get();
            p.sendMessage("" + settle.location.asString() + ": " + settle.name);
            for (Vectore2 loc : NationsPlugin.settleManager.locations) {
                p.sendMessage(loc.asString());
            }
        }
    }
}
