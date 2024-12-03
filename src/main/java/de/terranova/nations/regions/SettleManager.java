package de.terranova.nations.regions;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import de.terranova.nations.pl3xmap.Pl3xMapSettlementLayer;
import de.terranova.nations.regions.access.AccessLevel;
import de.terranova.nations.regions.grid.SettleRegionType;
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

    public List<String> getNameCache() {
        return nameCache;
    }

    private List<String> nameCache;

    public void addNameToCache(String name) {
        nameCache.add(name);
    }

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

    public boolean isNameCached(String name) {
        return nameCache.contains(name.toLowerCase());
    }

    public void removeSettlement(UUID uuid) {
        // Settle aus dem Arbeitsspeicher nehmen
        settlements.remove(uuid);
    }

    public Optional<AccessLevel> getAccessLevel(Player p, UUID settlementUUID) {
        if(p.hasPermission("nations.admin.bypass")) return Optional.of(AccessLevel.ADMIN);
        AccessLevel access = settlements.get(settlementUUID).getAccess().getAccessLevel(p.getUniqueId());
        if(access == null) return Optional.empty();
        return Optional.of(access);
    }

    public Optional<SettleRegionType> getOwnedSettlement(Player p) {
        for (SettleRegionType settle : settlements.values()) {

            AccessLevel access = settle.getAccess().getAccessLevel(p.getUniqueId());
            if(access.equals(AccessLevel.MAJOR)) return Optional.of(settle);
        }
        return Optional.empty();
    }

    public Optional<SettleRegionType> getSettleByName(String name) {
        for (SettleRegionType settle : settlements.values()) {
            if(name.equalsIgnoreCase(settle.getName())) return Optional.of(settle);
        }
        return Optional.empty();
    }

    public void addSettlementsToPl3xmap() {
        layerRegistry.register("settlement-layer",new Pl3xMapSettlementLayer(Objects.requireNonNull(Pl3xMap.api().getWorldRegistry().get("world"))));
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
                if(settle.getId().equals(settlementUUID)) {
                    return Optional.of(settlements.get(settlementUUID));
                }
            }
        }
        return Optional.empty();
    }



}
