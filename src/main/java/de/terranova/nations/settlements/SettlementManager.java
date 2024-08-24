package de.terranova.nations.settlements;

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import de.terranova.nations.pl3xmap.Pl3xMapSettlementLayer;
import de.terranova.nations.worldguard.math.Vectore2;
import de.terranova.nations.worldguard.SettlementFlag;
import net.pl3x.map.core.Pl3xMap;
import net.pl3x.map.core.markers.layer.Layer;
import net.pl3x.map.core.registry.Registry;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class SettlementManager {

    public HashMap<UUID, Settlement> settlements;
    public List<Vectore2> locations;

    private Registry<@NotNull Layer> layerRegistry;

    public SettlementManager() {
        this.settlements = new HashMap<>();
        this.locations = new ArrayList<>();
        this.layerRegistry = Objects.requireNonNull(Pl3xMap.api().getWorldRegistry().get("world")).getLayerRegistry();
    }


    public void setSettlements(HashMap<UUID, Settlement> settlements) {
        this.settlements = settlements;
    }

    public void addSettlement(UUID uuid, Settlement settlement) {
        settlements.put(uuid, settlement);
    }

    public boolean isNameAvaible(String name) {

        for (Settlement settlements : settlements.values()) {

                if (Objects.equals(settlements.name.toLowerCase(), name.toLowerCase())) {
                    return false;
                }
        }
        return true;
    }

    public Optional<AccessLevelEnum> getAccessLevel(Player p, UUID settlementUUID) {
        if(p.hasPermission("nations.admin.bypass")) return Optional.of(AccessLevelEnum.MAJOR);
        AccessLevelEnum access = settlements.get(settlementUUID).membersAccess.get(p.getUniqueId());
        if(access == null) return Optional.empty();
        return Optional.of(access);
    }

    public void addSettlementsToPl3xmap() {
        layerRegistry.register("settlement-layer",new Pl3xMapSettlementLayer(Objects.requireNonNull(Pl3xMap.api().getWorldRegistry().get("world"))));
        /*
        World world = Bukkit.getWorld("world");
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        assert world != null;
        RegionManager regions = container.get(BukkitAdapter.adapt(world));
        assert regions != null;
        for (ProtectedRegion region :regions.getRegions().values()){
            for (settlement settle : settlements.values()) {
                if(region.getFlag(settlementFlag.SETTLEMENT_UUID_FLAG) == null) continue;
                UUID settlementUUID = UUID.fromString(Objects.requireNonNull(region.getFlag(settlementFlag.SETTLEMENT_UUID_FLAG)));
                if(settle.id.equals(settlementUUID)) {
                    layerRegistry.register(settle.name.toLowerCase()+"-smarker",new createPl3xMapSettlementLayer(Objects.requireNonNull(Pl3xMap.api().getWorldRegistry().get("world")), Vectore2.fromBlockVectorList(region.getPoints()),settle));
                }
            }
        }

         */
    }

    public Settlement getSettlement(UUID settlementUUID){
        return settlements.get(settlementUUID);
    }

    public void addSettlementToPl3xmap(Settlement settle) {
        addSettlementsToPl3xmap();
        /*
        World world = Bukkit.getWorld("world");
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        assert world != null;
        RegionManager regions = container.get(BukkitAdapter.adapt(world));


        for (ProtectedRegion region :regions.getRegions().values()){
            if(!Objects.equals(region.getFlag(settlementFlag.SETTLEMENT_UUID_FLAG), settle.id.toString()))continue;
            layerRegistry.register("settlement-layer",new createPl3xMapSettlementLayer(Objects.requireNonNull(Pl3xMap.api().getWorldRegistry().get("world")), Vectore2.fromBlockVectorList(region.getPoints()),settle));
        }

         */
    }

    public Optional<Settlement> checkIfPlayerIsWithinClaim(Player player) {
        LocalPlayer lp = WorldGuardPlugin.inst().wrapPlayer(player);
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regions = container.get(lp.getWorld());
        assert regions != null;
        RegionQuery query = container.createQuery();
        ApplicableRegionSet set = query.getApplicableRegions(lp.getLocation());
        for (ProtectedRegion region : set) {
            for (Settlement settle : settlements.values()) {
                UUID settlementUUID = UUID.fromString(Objects.requireNonNull(region.getFlag(SettlementFlag.SETTLEMENT_UUID_FLAG)));
                if(settle.id.equals(settlementUUID)) {
                    return Optional.of(settlements.get(settlementUUID));
                }
            }
        }
        return Optional.empty();
    }

    public boolean canSettle(Player p) {
        for (Settlement settlement : settlements.values()) {
            for (AccessLevelEnum acess : settlement.membersAccess.values()) {
                if(acess.equals(AccessLevelEnum.MAJOR)) {
                    return false;
                }
            }
        }
        return true;
    }

    public void changeSkin(Player p, Settlement settlement) {
        //new TownAdmSkinGUI(p,settlement.level);
    }

}