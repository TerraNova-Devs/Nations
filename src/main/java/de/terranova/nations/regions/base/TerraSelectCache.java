package de.terranova.nations.regions.base;

import de.mcterranova.terranovaLib.utils.Chat;
import de.terranova.nations.regions.RegionManager;
import de.terranova.nations.regions.access.TownAccessLevel;
import de.terranova.nations.regions.access.TownAccessControlled;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class TerraSelectCache {
    public static Map<UUID, TerraSelectCache> selectCache = new HashMap<>();

    private Region region;
    private TownAccessLevel access;
    public TerraSelectCache(Region region, Player p) {
        this.region = region;
        if(region instanceof Selectable){
            TownAccessControlled access = (TownAccessControlled) region;
            if(p.isOp()) this.access = TownAccessLevel.ADMIN;
            else this.access = access.getAccess().getAccessLevel(p.getUniqueId());
        } else {
            this.access = null;
        }
    }

    public Region getRegion() {
        return region;
    }

    public TownAccessLevel getAccess() {
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
            Optional<Region> updatedRegion = RegionManager.retrieveRegion(oldCache.region.type,oldCache.region.id);
            updatedRegion.ifPresent(regionType -> selectCache.put(p.getUniqueId(), new TerraSelectCache(regionType, p)));
        }
        return null;
    }

    public static Optional<TerraSelectCache> getSelect(UUID uuid) {
        if(selectCache.containsKey(uuid)) return Optional.of(selectCache.get(uuid));
        return Optional.empty();
    }


}
