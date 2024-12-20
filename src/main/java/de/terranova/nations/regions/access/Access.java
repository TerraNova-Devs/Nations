package de.terranova.nations.regions.access;

import de.terranova.nations.regions.RegionManager;
import de.terranova.nations.regions.base.RegionType;
import de.terranova.nations.regions.base.RegionTypeListener;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

public class Access implements RegionTypeListener {

    private RegionType regionType;
    private HashMap<UUID, AccessLevel> accessLevel = new HashMap<>();
    AccessDatabase accessDatabase;

    public Access(RegionType regionType) {
        if(!(regionType instanceof AccessControlled)) throw new IllegalArgumentException();
        this.regionType = regionType;
        this.accessDatabase = new AccessDatabase(regionType.getId());
        this.accessLevel = accessDatabase.getMembersAccess();
        regionType.addListener(this);

    }

    public HashMap<UUID, AccessLevel> getAccessLevels() {
        return this.accessLevel;
    }

    public void setAccessLevels(HashMap<UUID, AccessLevel> accessLevels) {
        this.accessLevel = accessLevels;
    }



    public void removeAccess(UUID uuid) {
        HashMap<UUID, AccessLevel> accessLevels = getAccessLevels();
        accessLevels.remove(uuid);
        accessDatabase.changeMemberAccess(uuid, null);
        setAccessLevels(accessLevels);
    };

    public void setAccessLevel(UUID uuid, AccessLevel accessLevel){
        HashMap<UUID, AccessLevel> accessLevels = getAccessLevels();
        if(getAccessLevels().containsKey(uuid)) {
            accessLevels.replace(uuid, accessLevel);
        } else {
            accessLevels.put(uuid, accessLevel);
        }
        accessDatabase.changeMemberAccess(uuid, accessLevel);
        setAccessLevels(accessLevels);
    }

    public boolean hasAccess(AccessLevel access, AccessLevel neededAcess) {
        if(access == null) return false;
        return access.getWeight() >= neededAcess.getWeight();
    }

    public AccessLevel getAccessLevel(UUID uuid) {
        if(getAccessLevels().containsKey(uuid)) {
            return getAccessLevels().get(uuid);
        }
        return null;
    }

    public Collection<UUID> getEveryUUIDWithCertainAccessLevel(AccessLevel access) {
        Collection<UUID> output = new ArrayList<>();
        for (UUID uuid : accessLevel.keySet()) {
            if (accessLevel.get(uuid).equals(access)) {
                output.add(uuid);
            }
        }
        return output;
    }

    public Collection<String> getEveryMemberNameWithCertainAccessLevel(AccessLevel access) {
        Collection<String> output = new ArrayList<>();
        for (UUID uuid : accessLevel.keySet()) {
            if (accessLevel.get(uuid).equals(access)) {
                OfflinePlayer p = Bukkit.getOfflinePlayer(uuid);
                if (p.getName() == null) continue;
                output.add(p.getName());
            }
        }
        return output;
    }

    @Override
    public void onRegionTypeRemoved(){
        accessDatabase.removeEveryAccess();
    }

}
