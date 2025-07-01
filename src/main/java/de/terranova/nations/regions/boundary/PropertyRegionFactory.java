package de.terranova.nations.regions.boundary;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import de.terranova.nations.regions.RegionManager;
import de.terranova.nations.regions.base.Region;
import de.terranova.nations.regions.base.RegionContext;
import de.terranova.nations.regions.base.RegionFactoryBase;
import de.terranova.nations.regions.grid.SettleRegion;
import de.terranova.nations.utils.Chat;
import de.terranova.nations.worldguard.BoundaryClaimFunctions;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class PropertyRegionFactory implements RegionFactoryBase {


    @Override
    public String getType() {
        return PropertyRegion.REGION_TYPE;
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

        com.sk89q.worldedit.entity.Player wePlayer = BukkitAdapter.adapt(p);
        LocalSession session = WorldEdit.getInstance().getSessionManager().get(wePlayer);
        RegionSelector selector = session.getRegionSelector(BukkitAdapter.adapt(p.getWorld()));
        try {
            com.sk89q.worldedit.regions.Region region = selector.getRegion();
            ProtectedRegion tempRegion = BoundaryClaimFunctions.asProtectedRegion(region, UUID.randomUUID().toString());

            if (!validate(ctx, name, tempRegion, settle)) {
                return null;
            }
        } catch (IncompleteRegionException e) {
            p.sendMessage(Chat.errorFade("Deine Worldeditauswahl ist unvollständig."));
            return null;
        }

        return new PropertyRegion(
                ctx.name.toLowerCase() + "_" + BoundaryClaimFunctions.getNextFreeRegionNumber(ctx.name),
                UUID.randomUUID(),
                settle
        );
    }

    @Override
    public Region createFromArgs(List<String> args) {
        return new PropertyRegion(
                args.getFirst(),
                UUID.fromString(args.get(1)),
                (SettleRegion) RegionManager.retrieveRegion("settle", UUID.fromString(args.get(2))).get()
        );
    }

}
