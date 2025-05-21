package de.nekyia.nations.regions.base;

import de.nekyia.nations.regions.RegionManager;
import de.nekyia.nations.regions.access.AccessLevel;
import de.nekyia.nations.regions.access.AccessControlled;
import de.nekyia.nations.utils.Chat;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class TerraSelectCache {
    public static Map<UUID, TerraSelectCache> selectCache = new HashMap<>();

    private RegionType region;
    private AccessLevel access;
    public TerraSelectCache(RegionType region, Player p) {
        this.region = region;
        if(region instanceof AccessControlled access){
            if(p.isOp()) this.access = AccessLevel.ADMIN;
            else this.access = access.getAccess().getAccessLevel(p.getUniqueId());
        } else {
            this.access = null;
        }
    }

    public RegionType getRegion() {
        return region;
    }

    public AccessLevel getAccess() {
        return access;
    }

    public static TerraSelectCache hasSelect(Player p) {
        if(selectCache.containsKey(p.getUniqueId())) return selectCache.get(p.getUniqueId());
        p.sendMessage(Chat.errorFade("Bitte nutze für die Aktion erst ./t select <Stadtname> umd die zu betreffende Stadt auszuwählen."));
        return null;
    }

    public static TerraSelectCache renewSelect(Player p) {
        if(selectCache.containsKey(p.getUniqueId())){
            TerraSelectCache oldCache = selectCache.get(p.getUniqueId());
            selectCache.remove(p.getUniqueId());
            Optional<RegionType> updatedRegion = RegionManager.retrieveRegion(oldCache.region.type,oldCache.region.id);
            updatedRegion.ifPresent(regionType -> selectCache.put(p.getUniqueId(), new TerraSelectCache(regionType, p)));
        }
        return null;
    }

    public static Optional<TerraSelectCache> getSelect(UUID uuid) {
        if(selectCache.containsKey(uuid)) return Optional.of(selectCache.get(uuid));
        return Optional.empty();
    }


}
