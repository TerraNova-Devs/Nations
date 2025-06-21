package de.terranova.nations.regions.poly;

import de.terranova.nations.regions.access.PropertyAccess;
import de.terranova.nations.regions.access.PropertyAccessControlled;
import de.terranova.nations.regions.access.PropertyAccessLevel;
import de.terranova.nations.regions.base.Region;
import de.terranova.nations.worldguard.math.Vectore2;
import org.bukkit.entity.Player;

import java.util.*;

public class PropertyRegion extends Region implements PropertyAccessControlled {
    public static final String REGION_TYPE = "property";

    private PropertyAccess access;

    public PropertyRegion(String name, UUID ruuid, UUID owner, Vectore2 location) {
        super(name, ruuid, REGION_TYPE);
        this.access = new PropertyAccess(this);
        this.setOwner(owner);
        this.addNameToCache(name);
    }

    public UUID getOwner() {
        return this.access.getOwner();
    }

    public void setOwner(UUID owner) {
        this.access.setAccessLevel(owner, PropertyAccessLevel.OWNER);
    }

    public Collection<UUID> getTrusted() {
        return this.access.getEveryUUIDWithCertainAccessLevel(PropertyAccessLevel.MEMBER);
    }

    public void addTrusted(UUID uuid) {
        this.access.setAccessLevel(uuid, PropertyAccessLevel.MEMBER);
    }

    public void removeTrusted(UUID uuid) {
        this.access.removeAccess(uuid);
    }

    @Override
    public void dataBaseCall() {
        // Implement database call to save the property region
    }

    @Override
    public void onCreation(Player p) {
        // Implement actions to be taken when the property region is created
        this.access.setAccessLevel(p.getUniqueId(), PropertyAccessLevel.OWNER);
    }

    @Override
    public void onRemove() {
        // Implement actions to be taken when the property region is removed
        this.access.onRegionRemoved();
    }

    @Override
    public void onRename(String name) {
        // Implement actions to be taken when the property region is renamed
    }

    @Override
    public PropertyAccess getAccess() {
        return null;
    }
}