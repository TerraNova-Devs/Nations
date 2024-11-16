package de.terranova.nations.settlements;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import de.terranova.nations.database.SettleDBstuff;
import de.terranova.nations.pl3xmap.Pl3xMapSettlementLayer;
import de.terranova.nations.settlements.RegionTypes.SettleRegionType;
import de.terranova.nations.worldguard.math.Vectore2;
import de.terranova.nations.worldguard.NationsRegionFlag.SettleFlag;
import net.pl3x.map.core.Pl3xMap;
import net.pl3x.map.core.markers.layer.Layer;
import net.pl3x.map.core.registry.Registry;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;

public class SettleManager {

    public HashMap<UUID, SettleRegionType> settlements;
    public static List<Integer> claimsPerLevel = new ArrayList<>(Arrays.asList(2,2,2,2,3,2,2,2,2,4));

    public List<Vectore2> locationCache;
    public List<String> nameCache;

    private Registry<Layer> layerRegistry;

    public SettleManager() {
        this.settlements = new HashMap<>();
        this.locationCache = new ArrayList<>();
        this.nameCache = new ArrayList<>();
        this.layerRegistry = Objects.requireNonNull(Pl3xMap.api().getWorldRegistry().get("world")).getLayerRegistry();
    }


    public void setSettlements(HashMap<UUID, SettleRegionType> settlements) {
        this.settlements = settlements;
    }

    public void addSettlement(UUID uuid, SettleRegionType settle) {
        settlements.put(uuid, settle);
    }

    public boolean isNameAvaible(String name) {
        return !nameCache.contains(name.toLowerCase());
    }

    public void removeSettlement(UUID uuid) {
        SettleRegionType settle = settlements.get(uuid);
        //Citizen NPC töten & World Guard Region löschen
        settle.remove();
        //Settle aus der Datenbank nehmen
        SettleDBstuff settleDB = new SettleDBstuff(settle.id);
        settleDB.dropSettlement();
        // Settle aus dem Arbeitsspeicher nehmen
        settlements.remove(uuid);
    }

    public Optional<AccessLevel> getAccessLevel(Player p, UUID settlementUUID) {
        if(p.hasPermission("nations.admin.bypass")) return Optional.of(AccessLevel.ADMIN);
        AccessLevel access = settlements.get(settlementUUID).getAccessLevel(p.getUniqueId());
        if(access == null) return Optional.empty();
        return Optional.of(access);
    }

    public Optional<SettleRegionType> getOwnedSettlement(Player p) {
        for (SettleRegionType settle : settlements.values()) {
            AccessLevel access = settle.getAccessLevel(p.getUniqueId());
            if(access.equals(AccessLevel.MAJOR)) return Optional.of(settle);
        }
        return Optional.empty();
    }

    public Optional<SettleRegionType> getSettleByName(String name) {
        for (SettleRegionType settle : settlements.values()) {
            if(name.equalsIgnoreCase(settle.name)) return Optional.of(settle);
        }
        return Optional.empty();
    }

    public void addSettlementsToPl3xmap() {
        layerRegistry.register("settlement-layer",new Pl3xMapSettlementLayer(Objects.requireNonNull(Pl3xMap.api().getWorldRegistry().get("world"))));
    }

    public void addSettlementToPl3xmap(SettleRegionType settle) {
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

    public Optional<SettleRegionType> getSettle(UUID settlementUUID){
        return Optional.ofNullable(settlements.get(settlementUUID));
    }

    public Optional<SettleRegionType> getSettle(Location location) {

        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        ApplicableRegionSet set = query.getApplicableRegions(BukkitAdapter.adapt(location));

        for (ProtectedRegion region : set) {
            for (SettleRegionType settle : settlements.values()) {
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
        for (SettleRegionType settle : settlements.values()) {
            for (AccessLevel acess : settle.getAccessLevel().values()) {
                if(acess.equals(AccessLevel.MAJOR)) {
                    return false;
                }
            }
        }
        return true;
    }

    public void changeSkin(Player p, SettleRegionType settle) {
        //new TownAdmSkinGUI(p,settlement.level);
    }

}
