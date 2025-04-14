package de.terranova.nations.regions.access;

import de.mcterranova.terranovaLib.utils.Chat;
import de.terranova.nations.database.dao.PropertyAccessDAO;
import de.terranova.nations.regions.base.Region;
import de.terranova.nations.regions.base.RegionListener;
import org.bukkit.Bukkit;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A property-based access system that uses a separate `property_access` table
 * and the `PropertyAccessDAO` for DB reads/writes, rather than the "access" table.
 */
public class PropertyAccess implements RegionListener {

    private final Region region;
    private HashMap<UUID, PropertyAccessLevel> accessLevels;

    public PropertyAccess(Region region) {
        if (!(region instanceof PropertyAccessControlled)) {
            throw new IllegalArgumentException("Region does not implement PropertyAccessControlled!");
        }
        this.region = region;
        // load from property_access table
        this.accessLevels = new HashMap<>(PropertyAccessDAO.getMembersAccess(region.getId()));
        region.addListener(this);
    }

    public HashMap<UUID, PropertyAccessLevel> getAccessLevels() {
        return accessLevels;
    }

    public void setAccessLevels(HashMap<UUID, PropertyAccessLevel> newLevels) {
        this.accessLevels = newLevels;
    }

    /**
     * Sets or updates a player's access level, storing in the DB via PropertyAccessDAO.
     */
    public void setAccessLevel(UUID uuid, PropertyAccessLevel newLevel) {
        if (newLevel == null) {
            removeAccess(uuid);
            return;
        }
        // local update
        getAccessLevels().put(uuid, newLevel);
        // DB update
        PropertyAccessDAO.changeMemberAccess(region.getId(), uuid, newLevel);
    }

    public PropertyAccessLevel getAccessLevel(UUID uuid) {
        return getAccessLevels().get(uuid);
    }

    /**
     * Removes a player's access from this property region.
     */
    public void removeAccess(UUID uuid) {
        getAccessLevels().remove(uuid);
        PropertyAccessDAO.changeMemberAccess(region.getId(), uuid, null);
    }

    public static boolean hasAccess(PropertyAccessLevel current, PropertyAccessLevel needed) {
        return current != null && current.getWeight() >= needed.getWeight();
    }

    public Collection<UUID> getEveryUUIDWithCertainAccessLevel(PropertyAccessLevel level) {
        return accessLevels.entrySet().stream()
                .filter(e -> e.getValue() == level)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    public Collection<String> getEveryMemberNameWithCertainAccessLevel(PropertyAccessLevel lvl) {
        return getEveryUUIDWithCertainAccessLevel(lvl).stream()
                .map(u -> Bukkit.getOfflinePlayer(u).getName())
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    /**
     * Returns the single user with OWNER rank, or null if none is set.
     */
    public UUID getOwner() {
        return accessLevels.entrySet().stream()
                .filter(e -> e.getValue() == PropertyAccessLevel.OWNER)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    /**
     * True if the given user is the property owner.
     */
    public boolean isOwner(UUID user) {
        return (getAccessLevel(user) == PropertyAccessLevel.OWNER);
    }

    /**
     * For broadcasting chat messages to all property members
     */
    public void broadcast(String message) {
        for (UUID uuid : accessLevels.keySet()) {
            if (Bukkit.getPlayer(uuid) != null) {
                Bukkit.getPlayer(uuid).sendMessage(Chat.greenFade(message));
            }
        }
    }

    /**
     * Called when the region is renamed. We do not store the name in our "property_access" table, so no action needed.
     */
    @Override
    public void onRegionRenamed(String newRegionName) {
        // no action needed
    }

    /**
     * Called when the region is removed from the plugin. We remove all property_access rows for it.
     */
    @Override
    public void onRegionRemoved() {
        PropertyAccessDAO.removeEveryAccess(region.getId());
    }
}
