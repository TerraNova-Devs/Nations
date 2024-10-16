package de.terranova.nations.commands;

import de.mcterranova.terranovaLib.utils.Chat;
import de.terranova.nations.NationsPlugin;
import de.terranova.nations.settlements.AccessLevelEnum;
import de.terranova.nations.settlements.Settle;
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
            if (!hasPermission(p, "nations.remove")) return;
            Optional<Settle> settle = NationsPlugin.settleManager.getSettle(p.getLocation());
            if (settle.isEmpty()) return;
            Optional<AccessLevelEnum> access = NationsPlugin.settleManager.getAccessLevel(p, settle.get().id);
            if (access.isEmpty()) return;
            if (!access.get().equals(AccessLevelEnum.MAJOR)) return;
            NationsPlugin.settleManager.removeSettlement(settle.get().id);
            p.sendMessage(Chat.greenFade("Die Stadt " + settle.get().name + " wurde erfolgreich entfernt."));
        }

        if (args[0].equalsIgnoreCase("forceremove")) {
            if (!hasPermission(p, "nations.admin.remove")) return;
            Optional<Settle> settle = NationsPlugin.settleManager.getSettle(p.getLocation());
            if (settle.isEmpty()) return;
            NationsPlugin.settleManager.removeSettlement(settle.get().id);
            p.sendMessage(Chat.greenFade("Die Stadt " + settle.get().name + " wurde erfolgreich entfernt."));
        }
    }
}
