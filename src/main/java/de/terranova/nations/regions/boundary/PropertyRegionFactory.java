package de.terranova.nations.regions.boundary;

import de.terranova.nations.regions.base.Region;
import de.terranova.nations.regions.base.RegionContext;
import de.terranova.nations.regions.base.RegionFactory;
import de.terranova.nations.regions.grid.SettleRegion;
import de.terranova.nations.regions.grid.SettleRegionFactory;
import de.terranova.nations.utils.Chat;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;
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

        // Perform all necessary validations before creation
        if (!isValidName(name, p)) {
            p.sendMessage(Chat.errorFade("Invalid name for settlement." + name));
            return null;  // Return null to indicate creation failure.
        }

        return new PropertyRegion(ctx.extra.get("name"), UUID.randomUUID(),ctx.player.getUniqueId());
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
