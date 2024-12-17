package de.terranova.nations.regions.base;

import de.mcterranova.terranovaLib.commands.CommandAnnotation;
import de.mcterranova.terranovaLib.utils.Chat;
import de.terranova.nations.regions.grid.SettleRegionType;
import de.terranova.nations.regions.RegionManager;
import org.bukkit.entity.Player;

import java.util.Optional;

public class SelectCommands {

    @CommandAnnotation(
            domain = "select",
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
            domain = "select.$REGION_NAMES",
            permission = "nations.select",
            description = "Select a region",
            usage = "/t select <name>"
    )
    public static boolean selectRegion(Player p, String[] args) {
        if (args.length == 1) {
            p.sendMessage(Chat.errorFade("Bitte gebe den Namen einer Stadt an."));
            return false;
        }

        String regionName = args[1].toLowerCase();
        p.sendMessage(regionName);
        if (!RegionType.isNameCached(regionName)) {
            p.sendMessage(Chat.errorFade("Die angegebene Stadt " + regionName + " gibt es leider nicht.(x)"));
            return false;
        }


        Optional<SettleRegionType> osettle = RegionManager.retrieveRegion("settle", regionName);
        if (osettle.isEmpty()) {
            p.sendMessage(Chat.errorFade("Die angegebene Stadt " + regionName + " gibt es leider nicht.(z)"));
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