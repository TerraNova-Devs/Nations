package de.terranova.nations.regions.poly;

import de.terranova.nations.regions.access.PropertyAccess;
import de.terranova.nations.regions.access.PropertyAccessControlled;
import de.terranova.nations.regions.base.PolyRegion;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Represents a property region that depends on WorldGuard to store the polygon shape.
 * We only store basic info: ID, name, price, optional parent property, plus Access.
 */
public class PropertyRegion extends PolyRegion implements PropertyAccessControlled {
    public static final String REGION_TYPE = "property";

    private final PropertyAccess access;
    private UUID parent; // The UUID of the parent property if this is a subproperty

    private int price; // The property’s sale price (0 means not for sale)

    /**
     * Basic constructor for top-level property region.
     */
    public PropertyRegion(String name, UUID ruuid) {
        super(name, ruuid, REGION_TYPE);
        this.access = new PropertyAccess(this);
        this.price = 0;
    }

    @Override
    public void onCreation(Player p) {
        // Called after you create the region in WG.
        // Could do any post-creation logic, e.g. "broadcast property created."
    }

    @Override
    public void onRemove() {
        // Called when removing the property from the plugin.
        // We call "onRegionRemoved()" in the PropertyAccess to clear DB rows, etc.
        this.access.onRegionRemoved();
    }

    @Override
    public void onRename(String newName) {
        addNameToCache(newName);
    }

    @Override
    public PropertyAccess getAccess() {
        return this.access;
    }

    public UUID getParent() {
        return parent;
    }

    public void setParent(UUID parent) {
        this.parent = parent;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }
}
