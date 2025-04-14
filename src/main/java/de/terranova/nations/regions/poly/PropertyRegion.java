package de.terranova.nations.regions.poly;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import de.terranova.nations.regions.access.PropertyAccess;
import de.terranova.nations.regions.access.PropertyAccessControlled;
import de.terranova.nations.regions.base.PolyRegion;
import de.terranova.nations.worldguard.NationsRegionFlag.RegionFlag;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Objects;
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

    private PropertyState state;

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

    @Override
    public ProtectedRegion getWorldguardRegion() {
        World world = Bukkit.getWorld("world");
        if (world == null) {
            Bukkit.getLogger().severe("World 'world' not found.");
            return null;
        }

        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager rm = container.get(BukkitAdapter.adapt(world));

        if (rm == null) return null;
        for (ProtectedRegion r : rm.getRegions().values()) {
            String propertyIdStr = r.getId();
            if (propertyIdStr != null && propertyIdStr.equals(name)) {
                if (r instanceof ProtectedPolygonalRegion ppr) {
                    return ppr;
                }
            }
        }
        return null;
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

    public PropertyState getState() {
        return state;
    }

    public void setState(PropertyState state) {
        this.state = state;
    }
}
