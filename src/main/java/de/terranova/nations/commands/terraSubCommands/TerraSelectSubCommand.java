package de.terranova.nations.commands.terraSubCommands;

import de.mcterranova.terranovaLib.utils.Chat;
import de.terranova.nations.NationsPlugin;
import de.terranova.nations.commands.SubCommand;
import de.terranova.nations.commands.TerraSelectCache;
import de.terranova.nations.settlements.RegionTypes.SettleRegionType;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class TerraSelectSubCommand extends SubCommand implements BasicCommand {
    public static Component nonSelectError = Chat.errorFade("Bitte wähle zuerst mit '/terra select <Stadt_Name>' eine Stadt aus.");

    public TerraSelectSubCommand(String permission) {
        super(permission);
    }

    @Override
    public void execute(@NotNull CommandSourceStack commandSourceStack, @NotNull String[] args) {

        Player p = isPlayer(commandSourceStack);
        hasPermission(p, permission + ".select");

        if (args.length == 1 && TerraSelectCache.selectCache.containsKey(p.getUniqueId())) {
            p.sendMessage(Chat.blueFade(String.format("Du hast die Stadt %s ausgewählt, dein Rang lautet %s", TerraSelectCache.selectCache.get(p.getUniqueId()).getRegion().name, TerraSelectCache.selectCache.get(p.getUniqueId()).getAccess().name())));
            return;
        } else if (args.length == 1) {
            p.sendMessage(nonSelectError);
            return;
        }

        Optional<SettleRegionType> osettle = NationsPlugin.settleManager.getSettleByName(args[1]);
        if (osettle.isEmpty()) {
            p.sendMessage(Chat.errorFade("Die angegebene Stadt %s gibt es leider nicht."));
            return;
        }
        SettleRegionType settle = osettle.get();

        TerraSelectCache cache = new TerraSelectCache(settle, p.getUniqueId());

        if (!TerraSelectCache.selectCache.containsKey(p.getUniqueId()))
            TerraSelectCache.selectCache.put(p.getUniqueId(), cache);
        else TerraSelectCache.selectCache.replace(p.getUniqueId(), cache);

        p.sendMessage(Chat.blueFade(String.format("Du hast die Stadt %s ausgewählt, dein Rang lautet %s", cache.getRegion().name, cache.getAccess().name())));
    }


}
