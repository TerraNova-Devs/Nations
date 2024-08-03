package de.terranova.nations.settlements;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import de.terranova.nations.database.SettleDBstuff;
import de.terranova.nations.pl3xmap.createPl3xMapSettlementLayer;
import de.terranova.nations.worldguard.math.Vectore2;
import de.terranova.nations.worldguard.settlementFlag;
import net.pl3x.map.core.Pl3xMap;
import net.pl3x.map.core.markers.layer.Layer;
import net.pl3x.map.core.registry.Registry;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class settlementManager {

    public HashMap<UUID, settlement> settlements;
    public List<Vectore2> locations;

    private Registry<@NotNull Layer> layerRegistry;

    public settlementManager() {
        this.settlements = new HashMap<>();
        this.locations = new ArrayList<>();
        this.layerRegistry = Objects.requireNonNull(Pl3xMap.api().getWorldRegistry().get("world")).getLayerRegistry();
    }


    public void setSettlements(HashMap<UUID, settlement> settlements) {
        this.settlements = settlements;
    }

    public void addSettlement(UUID uuid, settlement settlement) {
        settlements.put(uuid, settlement);
    }

    public boolean isNameAvaible(String name) {

        for (settlement settlements : settlements.values()) {

                if (Objects.equals(settlements.name.toLowerCase(), name.toLowerCase())) {
                    return false;
                }
        }
        return true;
    }

    public AccessLevelEnum getAcessLevel(Player p, UUID settlementUUID) {
        return settlements.get(settlementUUID).members.get(p.getUniqueId());
    }

    public void addSettlementsToPl3xmap() {

        World world = Bukkit.getWorld("world");
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        assert world != null;
        RegionManager regions = container.get(BukkitAdapter.adapt(world));

        assert regions != null;
        for (ProtectedRegion region :regions.getRegions().values()){
            for (settlement settle : settlements.values()) {
                if(region.getFlag(settlementFlag.SETTLEMENT_UUID_FLAG)== null)continue;
                UUID settlementUUID = UUID.fromString(Objects.requireNonNull(region.getFlag(settlementFlag.SETTLEMENT_UUID_FLAG)));
                if(settle.id.equals(settlementUUID)) {
                    layerRegistry.register(settle.name.toLowerCase()+"-smarker",new createPl3xMapSettlementLayer(Objects.requireNonNull(Pl3xMap.api().getWorldRegistry().get("world")), Vectore2.fromBlockVectorList(region.getPoints()),settle));
                }
            }
        }
    }

    public settlement getSettlement(UUID settlementUUID){
        return settlements.get(settlementUUID);
    }

    public void addSettlementToPl3xmap(settlement settle) {

        World world = Bukkit.getWorld("world");
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        assert world != null;
        RegionManager regions = container.get(BukkitAdapter.adapt(world));


        for (ProtectedRegion region :regions.getRegions().values()){
            if(!Objects.equals(region.getFlag(settlementFlag.SETTLEMENT_UUID_FLAG), settle.id.toString()))continue;
            layerRegistry.register(settle.name.toLowerCase()+"-smarker",new createPl3xMapSettlementLayer(Objects.requireNonNull(Pl3xMap.api().getWorldRegistry().get("world")), Vectore2.fromBlockVectorList(region.getPoints()),settle));
        }
    }

    public Optional<settlement> checkIfPlayerIsWithinClaim(Player player) {
        LocalPlayer lp = WorldGuardPlugin.inst().wrapPlayer(player);
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regions = container.get(lp.getWorld());
        assert regions != null;
        RegionQuery query = container.createQuery();
        ApplicableRegionSet set = query.getApplicableRegions(lp.getLocation());
        for (ProtectedRegion region : set) {
            for (settlement settle : settlements.values()) {
                UUID settlementUUID = UUID.fromString(Objects.requireNonNull(region.getFlag(settlementFlag.SETTLEMENT_UUID_FLAG)));
                if(settle.id.equals(settlementUUID)) {
                    return Optional.of(settlements.get(settlementUUID));
                }
            }
        }
        return Optional.empty();
    }

    public boolean canSettle(Player p) {
        for (settlement settlement : settlements.values()) {
            for (AccessLevelEnum acess : settlement.members.values()) {
                if(acess.equals(AccessLevelEnum.MAJOR)) {
                    return false;
                }
            }
        }
        return true;
    }

    public void changeSkin(Player p, settlement settlement) {
        //new TownAdmSkinGUI(p,settlement.level);
    }

}
