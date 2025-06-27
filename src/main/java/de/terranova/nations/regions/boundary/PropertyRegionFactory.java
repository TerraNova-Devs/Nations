package de.terranova.nations.regions.boundary;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import de.terranova.nations.regions.RegionManager;
import de.terranova.nations.regions.base.RegionFactoryBase;
import de.terranova.nations.regions.modules.access.TownAccess;
import de.terranova.nations.regions.modules.access.TownAccessLevel;
import de.terranova.nations.regions.base.Region;
import de.terranova.nations.regions.base.RegionContext;
import de.terranova.nations.regions.grid.SettleRegion;
import de.terranova.nations.utils.Chat;
import de.terranova.nations.worldguard.BoundaryClaimFunctions;
import org.bukkit.entity.Player;

import java.util.*;

public class PropertyRegionFactory implements RegionFactoryBase {

    @Override
    public Class<? extends Region> getRegionClass() {
        return PropertyRegion.class;
    }

    @Override
    public Region createWithContext(RegionContext ctx) {

        Player p = ctx.player;
        String name = ctx.name;

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


        com.sk89q.worldedit.entity.Player wePlayer = BukkitAdapter.adapt(p);
        LocalSession session = WorldEdit.getInstance().getSessionManager().get(wePlayer);
        RegionSelector selector = session.getRegionSelector(BukkitAdapter.adapt(p.getWorld()));


        try {
            com.sk89q.worldedit.regions.Region region = selector.getRegion();
            ProtectedRegion tempRegion = BoundaryClaimFunctions.asProtectedRegion(region,UUID.randomUUID().toString());

            if (!validate(ctx,name,tempRegion, settle)){
                return null;
            }
        } catch (IncompleteRegionException e) {
            p.sendMessage(Chat.errorFade("Deine Auswahl ist unvollständig."));
            return null;
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

    public static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
