package de.terranova.nations.settlements;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import de.terranova.nations.database.SettleDBstuff;
import de.terranova.nations.pl3xmap.Pl3xMapSettlementLayer;
import de.terranova.nations.worldguard.math.Vectore2;
import de.terranova.nations.worldguard.SettleFlag;
import net.pl3x.map.core.Pl3xMap;
import net.pl3x.map.core.markers.layer.Layer;
import net.pl3x.map.core.registry.Registry;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;

public class SettleManager {

    public HashMap<UUID, Settle> settlements;
    public List<Vectore2> locations;
    static List<Integer> claimsPerLevel = new ArrayList<>(Arrays.asList(2,2,2,2,3,2,2,2,2,4));

    private Registry<Layer> layerRegistry;

    public SettleManager() {
        this.settlements = new HashMap<>();
        this.locations = new ArrayList<>();
        this.layerRegistry = Objects.requireNonNull(Pl3xMap.api().getWorldRegistry().get("world")).getLayerRegistry();
    }


    public void setSettlements(HashMap<UUID, Settle> settlements) {
        this.settlements = settlements;
    }

    public void addSettlement(UUID uuid, Settle settle) {
        settlements.put(uuid, settle);
    }

    public boolean isNameAvaible(String name) {

        for (Settle settlements : settlements.values()) {

                if (Objects.equals(settlements.name.toLowerCase(), name.toLowerCase())) {
                    return false;
                }
        }
        return true;
    }

    public void removeSettlement(UUID uuid) {
        Settle settle = settlements.get(uuid);
        //Citizen NPC töten & World Guard Region löschen
        settle.remove();
        //Settle aus der Datenbank nehmen
        SettleDBstuff settleDB = new SettleDBstuff(settle.id);
        settleDB.dropSettlement();
        // Settle aus dem Arbeitsspeicher nehmen
        settlements.remove(uuid);
    }

    public Optional<AccessLevelEnum> getAccessLevel(Player p, UUID settlementUUID) {
        if(p.hasPermission("nations.admin.bypass")) return Optional.of(AccessLevelEnum.MAJOR);
        AccessLevelEnum access = settlements.get(settlementUUID).membersAccess.get(p.getUniqueId());
        if(access == null) return Optional.empty();
        return Optional.of(access);
    }

    public void addSettlementsToPl3xmap() {
        layerRegistry.register("settlement-layer",new Pl3xMapSettlementLayer(Objects.requireNonNull(Pl3xMap.api().getWorldRegistry().get("world"))));
    }

    public void addSettlementToPl3xmap(Settle settle) {
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

    public Optional<Settle> getSettle(UUID settlementUUID){
        return Optional.ofNullable(settlements.get(settlementUUID));
    }

    public Optional<Settle> getSettle(Location location) {

        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        ApplicableRegionSet set = query.getApplicableRegions(BukkitAdapter.adapt(location));

        for (ProtectedRegion region : set) {
            for (Settle settle : settlements.values()) {
                String UUIDstring = region.getFlag(SettleFlag.SETTLEMENT_UUID_FLAG);
                if(UUIDstring == null) continue;
                UUID settlementUUID = UUID.fromString(UUIDstring);
                if(settle.id.equals(settlementUUID)) {
                    return Optional.of(settlements.get(settlementUUID));
                }
            }
        }
        return Optional.empty();
    }

    public boolean canSettle(Player p) {
        for (Settle settle : settlements.values()) {
            for (AccessLevelEnum acess : settle.membersAccess.values()) {
                if(acess.equals(AccessLevelEnum.MAJOR)) {
                    return false;
                }
            }
        }
        return true;
    }

    public void changeSkin(Player p, Settle settle) {
        //new TownAdmSkinGUI(p,settlement.level);
    }

}
