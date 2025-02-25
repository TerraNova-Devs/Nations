package de.terranova.nations.command;

import de.mcterranova.terranovaLib.commands.AbstractCommand;
import de.mcterranova.terranovaLib.commands.CachedSupplier;
import de.mcterranova.terranovaLib.commands.CommandAnnotation;
import de.mcterranova.terranovaLib.commands.PlayerAwarePlaceholder;
import de.mcterranova.terranovaLib.utils.Chat;
import de.terranova.nations.NationsPlugin;
import de.terranova.nations.nations.Nation;
import de.terranova.nations.nations.NationManager;
import de.terranova.nations.regions.RegionManager;
import de.terranova.nations.regions.access.TownAccess;
import de.terranova.nations.regions.access.TownAccessLevel;
import de.terranova.nations.regions.base.Region;
import de.terranova.nations.regions.grid.SettleRegion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

import static de.terranova.nations.NationsPlugin.nationManager;

public class NationCommands extends AbstractCommand  {

    public NationCommands() {

        addPlaceholder("$NATION_NAMES", () ->
                nationManager.getNations().values().stream().map(Nation::getName).collect(Collectors.toList()));
        addPlaceholder("$SETTLEMENTS", () ->
                RegionManager.retrieveAllCachedRegions("settle").values().stream().map(Region::getName).collect(Collectors.toList()));

        registerSubCommand(this,"create");

        setupHelpCommand();
        initialize();
    }

    @CommandAnnotation(
            domain = "create.$0",
            permission = "nations.nation.create",
            description = "Creates a new nation",
            usage = "/nation create <name>"
    )
    public boolean createNation(Player p, String[] args) {
        if (args.length < 2) {
            p.sendMessage(Chat.errorFade("Bitte gebe den Namen der Nation an."));
            return false;
        }

        String nationName = args[1].toLowerCase();
        if (nationManager.getNationByName(nationName) != null) {
            p.sendMessage(Chat.errorFade("Der Name ist bereits vergeben."));
            return false;
        }

        Optional<SettleRegion> osettle = RegionManager.retrievePlayersSettlement(p.getUniqueId());
        if (osettle.isEmpty()) {
            p.sendMessage(Chat.errorFade("Du musst erst eine Stadt besitzen."));
            return false;
        }

        SettleRegion settle = osettle.get();

        if(!TownAccess.hasAccess(settle.getAccess().getAccessLevel(p.getUniqueId()), TownAccessLevel.MAJOR)) {
            p.sendMessage(Chat.errorFade("Du musst Bürgermeister deiner Stadt sein."));
            return false;
        }

        // Check if settle is already in a nation
        for (Nation nation : nationManager.getNations().values()) {
            if (nation.getSettlements().contains(settle.getId())) {
                p.sendMessage(Chat.errorFade("Die Stadt ist bereits in einer Nation."));
                return false;
            }
        }

        Nation nation = new Nation(nationName, settle.getId());
        nationManager.addNation(nation, settle.getId());

        p.sendMessage(Chat.greenFade("Die Nation " + nationName + " wurde erfolgreich gegründet."));
        return true;
    }
}
