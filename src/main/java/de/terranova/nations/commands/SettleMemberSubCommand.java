package de.terranova.nations.commands;

import de.mcterranova.terranovaLib.utils.Chat;
import de.terranova.nations.NationsPlugin;
import de.terranova.nations.settlements.AccessLevelEnum;
import de.terranova.nations.settlements.Settle;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class SettleMemberSubCommand extends SubCommand implements BasicCommand {
    SettleMemberSubCommand(String permission) {
        super(permission);
    }

    @Override
    public void execute(@NotNull CommandSourceStack commandSourceStack, @NotNull String[] args) {
        Player p = isPlayer(commandSourceStack);
        if(p == null) return;
        if(!hasPermission(p, permission)) return;

        if (args[1].equalsIgnoreCase("promote")) {
            if (!hasPermission(p, "nations.member.add")) return;
            Optional<Player> target = isPlayer(args[2], p);
            if (target.isEmpty()) return;
            Optional<Settle> settle = NationsPlugin.settleManager.getSettle(p.getLocation());
            if (settle.isEmpty()) return;
            Optional<AccessLevelEnum> access = NationsPlugin.settleManager.getAccessLevel(p, settle.get().id);
            if (access.isEmpty()) return;
            if (!hasAccess(access.get(), List.of(AccessLevelEnum.MAJOR, AccessLevelEnum.VICE))) return;
            Optional<AccessLevelEnum> newAccess;
            try {
                newAccess = settle.get().promoteOrAdd(target.get(), p);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            if (newAccess.isEmpty()) return;
            p.sendMessage(Chat.greenFade(String.format("Der Spieler %s wurde zum Rang %s befördert.", PlainTextComponentSerializer.plainText().serialize(target.get().displayName()), newAccess.get())));
        }

        if (args[1].equalsIgnoreCase("demote")) {
            if (!hasPermission(p, "nations.member.remove")) return;
            Optional<Player> target = isPlayer(args[2], p);
            if (target.isEmpty()) return;
            Optional<Settle> settle = NationsPlugin.settleManager.getSettle(p.getLocation());
            if (settle.isEmpty()) return;
            Optional<AccessLevelEnum> access = NationsPlugin.settleManager.getAccessLevel(p, settle.get().id);
            if (access.isEmpty()) return;
            if (!hasAccess(access.get(), List.of(AccessLevelEnum.MAJOR, AccessLevelEnum.VICE))) return;
            Optional<AccessLevelEnum> newAccess;
            try {
                newAccess = settle.get().demoteOrRemove(target.get(), p);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            if (newAccess.isEmpty() || newAccess.get().equals(AccessLevelEnum.REMOVE)) return;
            p.sendMessage(Chat.greenFade(String.format("Der Spieler %s wurde zum Rang %s degradiert.", PlainTextComponentSerializer.plainText().serialize(target.get().displayName()), newAccess.get())));
        }
    }
}
