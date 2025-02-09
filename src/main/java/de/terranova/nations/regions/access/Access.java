package de.terranova.nations.regions.access;

import de.mcterranova.terranovaLib.utils.Chat;
import de.terranova.nations.database.dao.AccessDAO;
import de.terranova.nations.regions.base.Region;
import de.terranova.nations.regions.base.RegionListener;
import org.bukkit.Bukkit;

import java.util.*;
import java.util.stream.Collectors;

public class Access implements RegionListener {

    private Region region;
    private HashMap<UUID, AccessLevel> accessLevel;

    public Access(Region region) {
        if (!(region instanceof AccessControlled)) throw new IllegalArgumentException();
        this.region = region;
        this.accessLevel = new HashMap<>(AccessDAO.getMembersAccess(region.getId()));
        region.addListener(this);
    }

    public void setAccessLevels(HashMap<UUID, AccessLevel> accessLevels) {
        this.accessLevel = accessLevels;
    }

    public HashMap<UUID, AccessLevel> getAccessLevels() {
        return this.accessLevel;
    }

    public void setAccessLevel(UUID uuid, AccessLevel accessLevel) {
        HashMap<UUID, AccessLevel> accessLevels = getAccessLevels();
        if (getAccessLevels().containsKey(uuid)) {
            accessLevels.replace(uuid, accessLevel);
        } else {
            accessLevels.put(uuid, accessLevel);
        }
        AccessDAO.changeMemberAccess(region.getId(), uuid, accessLevel);
        setAccessLevels(accessLevels);
    }

    public AccessLevel getAccessLevel(UUID uuid) {
        return getAccessLevels().get(uuid);
    }

    public void removeAccess(UUID uuid) {
        HashMap<UUID, AccessLevel> accessLevels = getAccessLevels();
        accessLevels.remove(uuid);
        AccessDAO.changeMemberAccess(region.getId(), uuid, null);
        setAccessLevels(accessLevels);
    }

    public static boolean hasAccess(AccessLevel access, AccessLevel neededAccess) {
        return access != null && access.getWeight() >= neededAccess.getWeight();
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
    public void onRegionRenamed(String newRegionName) {
    }

    @Override
    public void onRegionRemoved() {
        AccessDAO.removeEveryAccess(region.getId());
    }
}