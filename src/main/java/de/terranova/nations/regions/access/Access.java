package de.terranova.nations.regions.access;

import de.mcterranova.terranovaLib.utils.Chat;
import de.terranova.nations.regions.RegionManager;
import de.terranova.nations.regions.base.RegionType;
import de.terranova.nations.regions.base.RegionTypeListener;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;

import java.util.*;
import java.util.stream.Collectors;

public class Access implements RegionTypeListener {

    private RegionType regionType;
    private HashMap<UUID, AccessLevel> accessLevel;
    AccessDatabase accessDatabase;

    public Access(RegionType regionType) {
        if(!(regionType instanceof AccessControlled)) throw new IllegalArgumentException();
        this.regionType = regionType;
        this.accessDatabase = new AccessDatabase(regionType.getId());
        this.accessLevel = accessDatabase.getMembersAccess();
        regionType.addListener(this);
    }

    public void setAccessLevels(HashMap<UUID, AccessLevel> accessLevels) {
        this.accessLevel = accessLevels;
    }

    public HashMap<UUID, AccessLevel> getAccessLevels() {
        return this.accessLevel;
    }

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

    public AccessLevel getAccessLevel(UUID uuid) {
        if(getAccessLevels().containsKey(uuid)) return getAccessLevels().get(uuid);
        return null;
    }

    public void removeAccess(UUID uuid) {
        HashMap<UUID, AccessLevel> accessLevels = getAccessLevels();
        accessLevels.remove(uuid);
        accessDatabase.changeMemberAccess(uuid, null);
        setAccessLevels(accessLevels);
    };

    public static boolean hasAccess(AccessLevel access, AccessLevel neededAcess) {
        if (access == null) return false;
        return access.getWeight() >= neededAcess.getWeight();
    }

    public Collection<UUID> getEveryUUIDWithCertainAccessLevel(AccessLevel access) {
        return accessLevel.keySet()
                .stream().filter(uuid -> accessLevel.get(uuid).equals(access))
                .collect(Collectors.toSet());
    }

    public Collection<String> getEveryMemberNameWithCertainAccessLevel(AccessLevel access) {
        return accessLevel.keySet()
                .stream().filter(uuid -> accessLevel.get(uuid).equals(access))
                .map(uuid -> Bukkit.getOfflinePlayer(uuid).getName())
                .filter(Objects::nonNull).collect(Collectors.toSet());
    }

    public UUID getMajor() {
        return accessLevel.entrySet()
                .stream().filter(entry -> entry.getValue() == AccessLevel.MAJOR)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    public List<UUID> getAllUUIDsOfLevel(AccessLevel level) {
        return accessLevel.entrySet()
                .stream().filter(entry -> entry.getValue() == level)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public void broadcast(String message) {
        accessLevel.keySet()
                .stream().map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .forEach(p -> p.sendMessage(Chat.greenFade(message)));
    }

    @Override
    public void onRegionTypeRemoved(){
        accessDatabase.removeEveryAccess();
    }

}
