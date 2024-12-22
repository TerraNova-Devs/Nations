package de.terranova.nations.regions.base;

import de.mcterranova.terranovaLib.utils.Chat;
import de.terranova.nations.regions.access.AccessLevel;
import de.terranova.nations.regions.access.AccessControlled;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TerraSelectCache {
    public static Map<UUID, TerraSelectCache> selectCache = new HashMap<>();

    private RegionType region;
    private AccessLevel access;
    public TerraSelectCache(RegionType region, UUID uuid) {
        this.region = region;
        if(region instanceof AccessControlled access){
            this.access = access.getAccess().getAccessLevel(uuid);
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
}
