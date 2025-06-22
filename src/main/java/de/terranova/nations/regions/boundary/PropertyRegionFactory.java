package de.terranova.nations.regions.boundary;

import de.terranova.nations.regions.RegionManager;
import de.terranova.nations.regions.access.TownAccess;
import de.terranova.nations.regions.access.TownAccessLevel;
import de.terranova.nations.regions.base.Region;
import de.terranova.nations.regions.base.RegionContext;
import de.terranova.nations.regions.base.RegionFactory;
import de.terranova.nations.regions.grid.SettleRegion;
import de.terranova.nations.utils.Chat;
import de.terranova.nations.worldguard.BoundaryClaimFunctions;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class PropertyRegionFactory implements RegionFactory {

    @Override
    public String getType() {
        return PropertyRegion.REGION_TYPE;
    }

    @Override
    public Region createWithContext(RegionContext ctx) {

        Player p = ctx.player;
        String name = ctx.extra.get("name");

        Optional<SettleRegion> settleOpt = RegionManager.retrievePlayersSettlement(p.getUniqueId());
        if (settleOpt.isEmpty()) {
            p.sendMessage(Chat.errorFade("Du bist in keiner Stadt."));
            return null;
        }
        SettleRegion settle = settleOpt.get();
        TownAccess access = settle.getAccess();
        if (!TownAccess.hasAccess(access.getAccessLevel(p.getUniqueId()), TownAccessLevel.VICE)) {
            p.sendMessage(Chat.errorFade("Du hast nicht die Berechtigung, um Grundstücke zu erstellen."));
            return null;
        }

        if(!BoundaryClaimFunctions.isValidSelection(p)) {
            p.sendMessage(Chat.errorFade("Deine Auswahl befindet sich ausserhalb der Stadtgrenzen"));
            return null;
        }

        if (!isValidName(name, p)) {
            p.sendMessage(Chat.errorFade("Invalid name for Property." + name));
            return null;  // Return null to indicate creation failure.
        }

        return new PropertyRegion(
                ctx.extra.get("name").toLowerCase() + "_" + BoundaryClaimFunctions.getNextFreeRegionNumber(ctx.extra.get("name").toLowerCase()),
                UUID.randomUUID(),
                ctx.player.getUniqueId()
        );
    }

    @Override
    public Region createFromArgs(List<String> args) {
        return new PropertyRegion(
                args.getFirst(),
                UUID.fromString(args.get(1)),
                UUID.fromString(args.get(2))
        );
    }

    private static boolean isValidName(String name, Player p) {
        if (name.matches("^(?!.*__)(?!_)(?!.*_$)(?!.*(.)\\1{3,})[a-zA-Z_*]{3,20}$")) {
            return true;
        }
        p.sendMessage(Chat.errorFade("Bitte verwende keine Sonderzeichen im Grundstücksnamen. Statt Leerzeichen _ verwenden. Nicht weniger als 3 oder mehr als 20 Zeichen verwenden."));
        return false;

    }
    public static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
