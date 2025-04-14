package de.terranova.nations.command;

import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import de.mcterranova.terranovaLib.commands.AbstractCommand;
import de.mcterranova.terranovaLib.commands.CommandAnnotation;
import de.mcterranova.terranovaLib.utils.Chat;
import de.terranova.nations.database.dao.PropertyRegionDAO;
import de.terranova.nations.regions.RegionManager;
import de.terranova.nations.regions.access.TownAccess;
import de.terranova.nations.regions.access.TownAccessLevel;
import de.terranova.nations.regions.grid.SettleRegion;
import de.terranova.nations.regions.poly.PropertyRegion;
import de.terranova.nations.regions.poly.PropertyState;
import de.terranova.nations.worldguard.NationsRegionFlag.RegionFlag;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class TownPropertyCommands extends AbstractCommand {
    private static final Map<UUID /*owner*/, UUID /*propertyUuid*/> pendingRemovals = new HashMap<>();

    public TownPropertyCommands() {

    }

    /**
     * /town property create
     * For Council+ in the user's settlement. Uses WorldEdit selection.
     */
    @CommandAnnotation(
            domain = "property.create",
            permission = "nations.town.property.create",
            description = "Create a property region from your current WorldEdit selection.",
            usage = "/town property create"
    )
    public boolean createTownProperty(Player p, String[] args) {
        if (args.length < 2) {
            p.sendMessage(Chat.errorFade("Usage: /town property create"));
            return true;
        }

        // 1) Check settlement & rank
        Optional<SettleRegion> srOpt = RegionManager.retrievePlayersSettlement(p.getUniqueId());
        if (srOpt.isEmpty()) {
            p.sendMessage(Chat.errorFade("You are not in any settlement."));
            return true;
        }
        SettleRegion sr = srOpt.get();
        if (!TownAccess.hasAccess(sr.getAccess().getAccessLevel(p.getUniqueId()), TownAccessLevel.COUNCIL)) {
            p.sendMessage(Chat.errorFade("You must be at least Council to create a property."));
            return true;
        }

        // 2) Grab WorldEdit selection
        Region weRegion = getWorldEditSelection(p);
        if (weRegion == null) {
            p.sendMessage(Chat.errorFade("You don't have a valid WorldEdit selection!"));
            return true;
        }

        // 3) Convert to polygon
        List<BlockVector2> polygonPoints = weRegion.polygonize(5);
        int minY = weRegion.getMinimumPoint().y();
        int maxY = weRegion.getMaximumPoint().y();

        // 4) Create a new WG region
        String wgRegionId = "prop_" + UUID.randomUUID().toString().substring(0, 8);
        ProtectedPolygonalRegion wgProperty = new ProtectedPolygonalRegion(wgRegionId, polygonPoints, minY, maxY);

        // Set owners
        DefaultDomain owners = new DefaultDomain();
        owners.addPlayer(WorldGuardPlugin.inst().wrapPlayer(p));
        wgProperty.setOwners(owners);

        // Link the property to the plugin's property ID (for future lookups)
        UUID propertyUUID = UUID.randomUUID();

        // set parent = the settle region in WG
        ProtectedRegion settleRegion = sr.getWorldguardRegion();
        try {
            wgProperty.setParent(settleRegion);
        } catch (ProtectedRegion.CircularInheritanceException e) {
            p.sendMessage(Chat.errorFade("Circular inheritance detected!"));
            return true;
        }

        // 5) Insert into WG region manager
        com.sk89q.worldguard.protection.managers.RegionManager rm = getRegionManager(p.getWorld().getName());
        if (rm == null) {
            p.sendMessage(Chat.errorFade("No RegionManager for this world?"));
            return true;
        }
        rm.addRegion(wgProperty);

        // 6) Create our plugin's property object and store it
        PropertyRegion prop = new PropertyRegion(wgRegionId, propertyUUID);
        prop.setPrice(0);
        prop.setState(PropertyState.NONE);
        prop.setParent(sr.getId());

        // Set the owner in the property (maybe you do that via property.getAccess().setAccessLevel(...) or so)
        prop.getAccess().setAccessLevel(p.getUniqueId(), de.terranova.nations.regions.access.PropertyAccessLevel.OWNER);

        // 7) Save in DB
        PropertyRegionDAO.saveProperty(prop, p.getWorld().getName());

        p.sendMessage(Chat.greenFade("Property '" + wgRegionId + "' created."));
        return true;
    }

    private com.sk89q.worldguard.protection.managers.RegionManager getRegionManager(String worldName) {
        org.bukkit.World bukkitWorld = Bukkit.getWorld(worldName);
        if (bukkitWorld == null) return null;
        com.sk89q.worldedit.world.World weWorld = BukkitAdapter.adapt(bukkitWorld);
        return WorldGuard.getInstance().getPlatform().getRegionContainer().get(weWorld);
    }

    private Region getWorldEditSelection(Player p) {
        // get WE selection
        com.sk89q.worldedit.entity.Player wep = BukkitAdapter.adapt(p);
        LocalSession session = WorldEdit.getInstance().getSessionManager().get(wep);
        if (session == null) return null;
        try {
            return session.getSelection(wep.getWorld());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * /town property remove <name>
     * If the user is the property’s owner => remove it immediately,
     * else if user is Council => requires property owner acceptance
     * (owner must run /town property removal accept <name>).
     */
    @CommandAnnotation(
            domain = "property.remove.$0",
            permission = "nations.town.property.remove",
            description = "Remove a property region from your city or your own property",
            usage = "/town property remove <name>"
    )
    public boolean removeTownProperty(Player p, String[] args) {
        if (args.length < 3) {
            p.sendMessage(Chat.errorFade("Usage: /town property remove <name>"));
            return true;
        }
        String propName = args[2];

        ProtectedRegion wgRegion = getWgRegionByName(propName);
        if (wgRegion == null) {
            p.sendMessage(Chat.errorFade("No property found by that name."));
            return true;
        }



        // find property by name
        Optional<PropertyRegion> propOpt = findPropertyByName(propName);
        if (propOpt.isEmpty()) {
            p.sendMessage(Chat.errorFade("No property found by that name."));
            return true;
        }
        PropertyRegion prop = propOpt.get();

        // Is the user the property owner?
        if (prop.getAccess().isOwner(p.getUniqueId())) {
            // remove immediately
            doRemoveProperty(prop);
            p.sendMessage(Chat.greenFade("Property removed immediately (you are the owner)."));
            return true;
        }
        // else check if user is council in the settlement that encloses this property
        Optional<SettleRegion> srOpt = RegionManager.retrievePlayersSettlement(p.getUniqueId());
        if (srOpt.isEmpty()) {
            p.sendMessage(Chat.errorFade("You are not in any settlement, cannot forcibly remove someone else's property."));
            return true;
        }
        SettleRegion sr = srOpt.get();
        if (!TownAccess.hasAccess(sr.getAccess().getAccessLevel(p.getUniqueId()), TownAccessLevel.COUNCIL)) {
            p.sendMessage(Chat.errorFade("You must be property owner or Council to remove that property."));
            return true;
        }
        // Now we have a Council user forcibly removing. Must ask property owner to confirm
        UUID ownerId = prop.getAccess().getOwner();
        if (ownerId == null) {
            // no defined owner => we can remove it
            doRemoveProperty(prop);
            p.sendMessage(Chat.greenFade("Property had no owner, removed now."));
            return true;
        }
        // queue removal
        pendingRemovals.put(ownerId, prop.getId());
        p.sendMessage(Chat.yellowFade("Removal request pending. The owner must run /town property removal accept <name>."));
        return true;
    }

    /**
     * /town property removal accept <name>
     */
    @CommandAnnotation(
            domain = "property.removal.accept.$0",
            permission = "nations.town.property.remove",
            description = "Owner acceptance of property removal",
            usage = "/town property removal accept <name>"
    )
    public boolean removalAccept(Player p, String[] args) {
        if (args.length < 4) {
            p.sendMessage(Chat.errorFade("Usage: /town property removal accept <name>"));
            return true;
        }
        String name = args[3];

        if (!pendingRemovals.containsKey(p.getUniqueId())) {
            p.sendMessage(Chat.errorFade("You have no pending removal requests."));
            return true;
        }
        Optional<PropertyRegion> propOpt = findPropertyByName(name);
        if (propOpt.isEmpty()) {
            p.sendMessage(Chat.errorFade("That property no longer exists."));
            pendingRemovals.remove(p.getUniqueId());
            return true;
        }
        PropertyRegion prop = propOpt.get();
        if (!prop.getName().equalsIgnoreCase(name)) {
            p.sendMessage(Chat.errorFade("That property name does not match the pending request."));
            return true;
        }
        // remove
        doRemoveProperty(prop);
        pendingRemovals.remove(p.getUniqueId());
        p.sendMessage(Chat.greenFade("Property removed successfully."));
        return true;
    }

    private Optional<PropertyRegion> findPropertyByName(String name) {
        return PropertyRegionDAO.loadProperty(name);
    }

    private void doRemoveProperty(PropertyRegion prop) {
        // 1) remove from WorldGuard
        removeWgRegion(prop.getId());
        // 2) remove from DB
        PropertyRegionDAO.deleteProperty(prop.getId());
        // 3) remove from your plugin’s memory structures if needed
        prop.remove();
    }

    private void removeWgRegion(UUID propertyId) {
        // find region in WG
        // remove it
        for (org.bukkit.World bw : Bukkit.getWorlds()) {
            com.sk89q.worldguard.protection.managers.RegionManager rm = getRegionManager(bw.getName());
            if (rm == null) continue;
            for (String regionId : rm.getRegions().keySet()) {
                ProtectedRegion r = rm.getRegion(regionId);
                if (r != null) {
                    String flagVal = r.getFlag(RegionFlag.REGION_UUID_FLAG);
                    if (flagVal != null && flagVal.equals(propertyId.toString())) {
                        rm.removeRegion(regionId);
                        return;
                    }
                }
            }
        }
    }

    private ProtectedRegion getWgRegionByName(String propertyName) {
        for (org.bukkit.World bw : Bukkit.getWorlds()) {
            com.sk89q.worldguard.protection.managers.RegionManager rm = getRegionManager(bw.getName());
            if (rm == null) continue;
            for (String regionId : rm.getRegions().keySet()) {
                ProtectedRegion r = rm.getRegion(regionId);
                if (r != null && r.getId().equalsIgnoreCase(propertyName)) {
                    return r;
                }
            }
        }
        return null;
    }
}
