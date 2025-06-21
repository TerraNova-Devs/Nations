package de.terranova.nations.regions.access;

import de.terranova.nations.database.dao.AccessDAO;
import de.terranova.nations.database.dao.PropertyAccessDAO;
import de.terranova.nations.regions.base.Region;
import de.terranova.nations.regions.base.RegionListener;
import de.terranova.nations.utils.Chat;
import org.bukkit.Bukkit;

import java.util.*;
import java.util.stream.Collectors;

public class PropertyAccess implements RegionListener {

    private Region region;
    private HashMap<UUID, PropertyAccessLevel> accessLevel;

    public PropertyAccess(Region region) {
        if (!(region instanceof PropertyAccessControlled)) throw new IllegalArgumentException();
        this.region = region;
        this.accessLevel = new HashMap<>(PropertyAccessDAO.getMembersAccess(region.getId()));
        region.addListener(this);
    }

    public void setAccessLevels(HashMap<UUID, PropertyAccessLevel> accessLevels) {
        this.accessLevel = accessLevels;
    }

    public HashMap<UUID, PropertyAccessLevel> getAccessLevels() {
        return this.accessLevel;
    }

    public void setAccessLevel(UUID uuid, PropertyAccessLevel accessLevel) {
        HashMap<UUID, PropertyAccessLevel> accessLevels = getAccessLevels();
        if (getAccessLevels().containsKey(uuid)) {
            accessLevels.replace(uuid, accessLevel);
        } else {
            accessLevels.put(uuid, accessLevel);
        }
        PropertyAccessDAO.changeMemberAccess(region.getId(), uuid, accessLevel);
        setAccessLevels(accessLevels);
    }

    public PropertyAccessLevel getAccessLevel(UUID uuid) {
        return getAccessLevels().get(uuid);
    }

    public void removeAccess(UUID uuid) {
        HashMap<UUID, PropertyAccessLevel> accessLevels = getAccessLevels();
        accessLevels.remove(uuid);
        AccessDAO.changeMemberAccess(region.getId(), uuid, null);
        setAccessLevels(accessLevels);
    }

    public static boolean hasAccess(PropertyAccessLevel access, PropertyAccessLevel neededAccess) {
        return access != null && access.getWeight() >= neededAccess.getWeight();
    }

    public Collection<UUID> getEveryUUIDWithCertainAccessLevel(PropertyAccessLevel access) {
        return accessLevel.keySet()
                .stream().filter(uuid -> accessLevel.get(uuid).equals(access))
                .collect(Collectors.toSet());
    }

    public Collection<String> getEveryMemberNameWithCertainAccessLevel(PropertyAccessLevel access) {
        return accessLevel.keySet()
                .stream().filter(uuid -> accessLevel.get(uuid).equals(access))
                .map(uuid -> Bukkit.getOfflinePlayer(uuid).getName())
                .filter(Objects::nonNull).collect(Collectors.toSet());
    }

    public UUID getOwner() {
        return accessLevel.entrySet()
                .stream().filter(entry -> entry.getValue() == PropertyAccessLevel.OWNER)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    public List<UUID> getAllUUIDsOfLevel(PropertyAccessLevel level) {
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
        PropertyAccessDAO.removeEveryAccess(region.getId());
    }
}
