package de.terranova.nations.commands;

import de.mcterranova.terranovaLib.utils.Chat;
import de.terranova.nations.NationsPlugin;
import de.terranova.nations.regions.grid.SettleRegionType;
import org.bukkit.entity.Player;

import java.util.Optional;

class SelectCommands {

    @CommandAnnotation(
            domain = "terra.select",
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
            domain = "terra.select.$ARGUMENT",
            permission = "nations.select.region",
            description = "Select a region",
            usage = "/t select <name>",
            tabCompletion = {"$REGION_NAMES"}
    )
    public static boolean selectRegion(Player p, String[] args) {
        if (args.length == 1) {
            p.sendMessage(Chat.errorFade("Bitte gebe den Namen einer Stadt an."));
            return false;
        }

        String regionName = args[1].toLowerCase();
        p.sendMessage(regionName);
        if (!NationsPlugin.settleManager.isNameCached(regionName)) {
            p.sendMessage(Chat.errorFade("Die angegebene Stadt " + regionName + " gibt es leider nicht."));
            return false;
        }

        Optional<SettleRegionType> osettle = NationsPlugin.settleManager.getSettleByName(regionName);
        if (osettle.isEmpty()) {
            p.sendMessage(Chat.errorFade("Die angegebene Stadt " + regionName + " gibt es leider nicht."));
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