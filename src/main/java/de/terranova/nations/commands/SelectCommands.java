package de.terranova.nations.commands;

import de.mcterranova.terranovaLib.utils.Chat;
import de.terranova.nations.NationsPlugin;
import de.terranova.nations.regions.grid.SettleRegionType;
import org.bukkit.entity.Player;

import java.util.Optional;

import static de.terranova.nations.commands.terraSubCommands.TerraSelectSubCommand.nonSelectError;

class SelectCommands {

    @CommandAnnotation(
            name = "terra.select",
            permission = "nations.select",
            description = "Print out selected region",
            usage = "/t select"
    )
    public static boolean select(Player p, String[] args) {
        if (TerraSelectCache.selectCache.containsKey(p.getUniqueId())) {
            p.sendMessage(Chat.blueFade(String.format("Du hast die Stadt %s ausgewählt, dein Rang lautet %s", TerraSelectCache.selectCache.get(p.getUniqueId()).getRegion().getName(), TerraSelectCache.selectCache.get(p.getUniqueId()).getAccess().name())));
            return true;
        } else {
            p.sendMessage(Chat.errorFade("Bitte wähle zuerst mit '/terra select <Stadt_Name>' eine Stadt aus."));
            return false;
        }
    }

    @CommandAnnotation(
            name = "terra.select",
            permission = "nations.select.region",
            description = "Select a region",
            usage = "/t select <name>",
            tabCompletion = {"$REGION_NAMES"}
    )
    public static boolean selectRegion(Player p, String[] args) {
        p.sendMessage(args[0].toLowerCase());
        if(!NationsPlugin.settleManager.isNameCached(args[0].toLowerCase())){
            p.sendMessage(Chat.errorFade("Die angegebene Stadt" + args[0].toLowerCase() + "gibt es leider nicht."));
            return false;
        }

        Optional<SettleRegionType> osettle = NationsPlugin.settleManager.getSettleByName(args[0]);
        if (osettle.isEmpty()) {
            p.sendMessage(Chat.errorFade("Die angegebene Stadt %s gibt es leider nicht."));
            return false;
        }

        SettleRegionType settle = osettle.get();

        TerraSelectCache cache = new TerraSelectCache(settle, p.getUniqueId());

        if (!TerraSelectCache.selectCache.containsKey(p.getUniqueId()))
            TerraSelectCache.selectCache.put(p.getUniqueId(), cache);
        else TerraSelectCache.selectCache.replace(p.getUniqueId(), cache);

        p.sendMessage(Chat.blueFade(String.format("Du hast die Stadt %s ausgewählt, dein Rang lautet %s", cache.getRegion().getName(), cache.getAccess().name())));

        return true;
    }
}
