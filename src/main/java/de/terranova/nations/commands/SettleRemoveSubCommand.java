package de.terranova.nations.commands;

import de.mcterranova.terranovaLib.utils.Chat;
import de.terranova.nations.NationsPlugin;
import de.terranova.nations.settlements.AccessLevel;
import de.terranova.nations.settlements.RegionTypes.SettleRegionType;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class SettleRemoveSubCommand extends SubCommand implements BasicCommand {
    SettleRemoveSubCommand(String permission) {
        super(permission);
    }

    @Override
    public void execute(@NotNull CommandSourceStack commandSourceStack, @NotNull String[] args) {

        Player p = isPlayer(commandSourceStack);
        if(p == null) return;
        if(!hasPermission(p, permission)) return;

        if (args[0].equalsIgnoreCase("remove")) {

        }

        if (args[0].equalsIgnoreCase("forceremove")) {
            if (!hasPermission(p, "nations.admin.remove")) return;
            Optional<SettleRegionType> settle = NationsPlugin.settleManager.getSettle(p.getLocation());
            if (settle.isEmpty()) return;
            NationsPlugin.settleManager.removeSettlement(settle.get().id);
            p.sendMessage(Chat.greenFade("Die Stadt " + settle.get().name + " wurde erfolgreich entfernt."));
        }
    }
}
