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
import de.terranova.nations.worldguard.NationsRegionFlag.RegionFlag;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Example subcommands for property creation, removal, subproperty creation,
 * trusting, and buying. This depends on:
 * - WorldEdit for selection
 * - WorldGuard for storing the polygon
 */
public class PropertyCommands extends AbstractCommand {

    public PropertyCommands() {
        // Register your subcommands or however your annotation system works
        registerSubCommand(this, "create");
        registerSubCommand(this, "remove");
        registerSubCommand(this, "removal");
        registerSubCommand(this, "buy");
        registerSubCommand(this, "trust");
        registerSubCommand(this, "kick");
        // etc.
        setupHelpCommand();
        initialize();
    }

    /**
     * /property create <name> <price>
     * For subproperty creation inside a property the user owns.
     * Exactly the same approach but we set WG's parent region to the parent's WG region.
     */
    @CommandAnnotation(
            domain = "create.$0.$0",
            permission = "nations.property.create",
            description = "Create a subproperty region inside the property you own, from your WorldEdit selection.",
            usage = "/property create <name> <price>"
    )
    public boolean createSubProperty(Player p, String[] args) {
        if (args.length < 3) {
            p.sendMessage(Chat.errorFade("Usage: /property create <name> <price>"));
            return true;
        }
        String propName = args[1];
        int price;
        try {
            price = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            p.sendMessage(Chat.errorFade("Invalid price number!"));
            return true;
        }

        // 1) Check if player is standing in a property they own
        Optional<PropertyRegion> parentOpt = getPropertyRegionAtPlayer(p);
        if (parentOpt.isEmpty()) {
            p.sendMessage(Chat.errorFade("You are not inside a property region that you own."));
            return true;
        }
        PropertyRegion parentProp = parentOpt.get();
        if (!parentProp.getAccess().isOwner(p.getUniqueId())) {
            p.sendMessage(Chat.errorFade("You are not the owner of this parent property."));
            return true;
        }

        // 2) Grab WE selection
        Region weRegion = getWorldEditSelection(p);
        if (weRegion == null) {
            p.sendMessage(Chat.errorFade("No valid WorldEdit selection."));
            return true;
        }
        List<BlockVector2> polygonPoints = weRegion.polygonize(5);
        int minY = weRegion.getMinimumPoint().y();
        int maxY = weRegion.getMaximumPoint().y();

        // 3) Make new WG region
        String wgRegionId = "subprop_" + UUID.randomUUID().toString().substring(0, 8);
        ProtectedPolygonalRegion subRegion = new ProtectedPolygonalRegion(wgRegionId, polygonPoints, minY, maxY);

        // owners
        DefaultDomain owners = new DefaultDomain();
        owners.addPlayer(WorldGuardPlugin.inst().wrapPlayer(p));
        subRegion.setOwners(owners);

        // Link to plugin's property ID
        UUID subPropUuid = UUID.randomUUID();
        subRegion.setFlag(RegionFlag.REGION_UUID_FLAG, subPropUuid.toString());

        // 4) find the parent's WG region and set as parent
        com.sk89q.worldguard.protection.managers.RegionManager rm = getRegionManager(p.getWorld().getName());
        if (rm == null) {
            p.sendMessage(Chat.errorFade("No region manager for world??"));
            return true;
        }
        ProtectedPolygonalRegion parentWg = findWgRegionFor(parentProp.getId(), rm);
        if (parentWg == null) {
            p.sendMessage(Chat.errorFade("Failed to find parent property in WG!"));
            return true;
        }
        try {
            subRegion.setParent(parentWg);
        } catch (Exception e) {
            p.sendMessage(Chat.errorFade("Could not set parent? " + e.getMessage()));
            return true;
        }
        rm.addRegion(subRegion);

        // 5) create plugin sub property
        PropertyRegion subProp = new PropertyRegion(propName, subPropUuid);
        subProp.setPrice(price);
        subProp.setParent(parentProp.getId());
        // set owner
        subProp.getAccess().setAccessLevel(p.getUniqueId(), de.terranova.nations.regions.access.PropertyAccessLevel.OWNER);

        PropertyRegionDAO.saveProperty(subProp, p.getWorld().getName());

        p.sendMessage(Chat.greenFade("Subproperty '" + propName + "' created with parent '" + parentProp.getName() + "'."));
        return true;
    }

    /**
     * /property buy
     * The user stands in a property with price>0, we do an economy transaction,
     * then set the user as the new owner.
     */
    @CommandAnnotation(
            domain = "buy",
            permission = "nations.property.buy",
            description = "Buy a property if it is for sale",
            usage = "/property buy"
    )
    public boolean buyProperty(Player p, String[] args) {
        Optional<PropertyRegion> propOpt = getPropertyRegionAtPlayer(p);
        if (propOpt.isEmpty()) {
            p.sendMessage(Chat.errorFade("You are not inside a property region."));
            return true;
        }
        PropertyRegion prop = propOpt.get();
        if (prop.getPrice() <= 0) {
            p.sendMessage(Chat.errorFade("This property is not for sale."));
            return true;
        }
        if (prop.getAccess().isOwner(p.getUniqueId())) {
            p.sendMessage(Chat.errorFade("You already own this property!"));
            return true;
        }
        int price = prop.getPrice();
        // check economy
        if (!hasEnoughMoney(p, price)) {
            p.sendMessage(Chat.errorFade("You do not have enough money to buy it."));
            return true;
        }
        // do the transaction
        withdrawFromPlayer(p, price);

        UUID oldOwner = prop.getAccess().getOwner();
        if (oldOwner != null) {
            depositToPlayer(oldOwner, price);
        }
        // set new owner
        prop.getAccess().removeAccess(oldOwner); // remove old owner's access if you want
        prop.getAccess().setAccessLevel(p.getUniqueId(), de.terranova.nations.regions.access.PropertyAccessLevel.OWNER);
        prop.setPrice(0); // no longer for sale

        // save
        PropertyRegionDAO.saveProperty(prop, p.getWorld().getName());
        p.sendMessage(Chat.greenFade("You bought the property for " + price + "! You are now the owner."));
        return true;
    }

    /**
     * /property trust <player>
     * Owner can trust a new user.
     */
    @CommandAnnotation(
            domain = "trust.$ONLINEPLAYERS",
            permission = "nations.property.trust",
            description = "Trust a player in your current property",
            usage = "/property trust <player>"
    )
    public boolean propertyTrust(Player p, String[] args) {
        if (args.length < 2) {
            p.sendMessage(Chat.errorFade("Usage: /property trust <player>"));
            return true;
        }
        String targetName = args[1];
        Player target = Bukkit.getPlayerExact(targetName);
        if (target == null) {
            p.sendMessage(Chat.errorFade("Player not found or not online."));
            return true;
        }

        Optional<PropertyRegion> propOpt = getPropertyRegionAtPlayer(p);
        if (propOpt.isEmpty()) {
            p.sendMessage(Chat.errorFade("You are not inside a property region."));
            return true;
        }
        PropertyRegion prop = propOpt.get();
        if (!prop.getAccess().isOwner(p.getUniqueId())) {
            p.sendMessage(Chat.errorFade("You are not the owner of this property."));
            return true;
        }
        prop.getAccess().setAccessLevel(target.getUniqueId(), de.terranova.nations.regions.access.PropertyAccessLevel.MEMBER);
        p.sendMessage(Chat.greenFade("You trusted " + targetName + " in this property."));
        return true;
    }

    /**
     * /property kick <player>
     * Owner can untrust a user from the property.
     */
    @CommandAnnotation(
            domain = "kick.$ONLINEPLAYERS",
            permission = "nations.property.kick",
            description = "Untrust a player from your property",
            usage = "/property kick <player>"
    )
    public boolean propertyKick(Player p, String[] args) {
        if (args.length < 2) {
            p.sendMessage(Chat.errorFade("Usage: /property kick <player>"));
            return true;
        }
        String targetName = args[1];
        Player target = Bukkit.getPlayerExact(targetName);
        if (target == null) {
            p.sendMessage(Chat.errorFade("Player not found or not online."));
            return true;
        }
        Optional<PropertyRegion> propOpt = getPropertyRegionAtPlayer(p);
        if (propOpt.isEmpty()) {
            p.sendMessage(Chat.errorFade("You are not inside a property region."));
            return true;
        }
        PropertyRegion prop = propOpt.get();
        if (!prop.getAccess().isOwner(p.getUniqueId())) {
            p.sendMessage(Chat.errorFade("You are not the owner of this property."));
            return true;
        }
        prop.getAccess().removeAccess(target.getUniqueId());
        p.sendMessage(Chat.greenFade("You removed " + targetName + " from property access."));
        return true;
    }

    // ----------------------------------------------------------------------
    // Internal Helpers

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

    private com.sk89q.worldguard.protection.managers.RegionManager getRegionManager(String worldName) {
        org.bukkit.World bukkitWorld = Bukkit.getWorld(worldName);
        if (bukkitWorld == null) return null;
        com.sk89q.worldedit.world.World weWorld = BukkitAdapter.adapt(bukkitWorld);
        return WorldGuard.getInstance().getPlatform().getRegionContainer().get(weWorld);
    }

    private ProtectedPolygonalRegion findWgRegionFor(UUID propertyId, com.sk89q.worldguard.protection.managers.RegionManager rm) {
        if (rm == null) return null;
        for (ProtectedRegion r : rm.getRegions().values()) {
            String flagVal = r.getFlag(RegionFlag.REGION_UUID_FLAG);
            if (flagVal != null && flagVal.equals(propertyId.toString())) {
                if (r instanceof ProtectedPolygonalRegion ppr) {
                    return ppr;
                }
            }
        }
        return null;
    }

    private Optional<PropertyRegion> getPropertyRegionAtPlayer(Player p) {
        // do a WorldGuard region query at the player's location
        // find a region that has the RegionFlag.REGION_UUID_FLAG set
        // then load that from DB
        org.bukkit.World bw = p.getWorld();
        com.sk89q.worldguard.protection.managers.RegionManager rm = getRegionManager(bw.getName());
        if (rm == null) return Optional.empty();

        com.sk89q.worldedit.util.Location weLoc = BukkitAdapter.adapt(p.getLocation());
        com.sk89q.worldguard.protection.ApplicableRegionSet set =
                WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery().getApplicableRegions(weLoc);

        for (ProtectedRegion pr : set.getRegions()) {
            String flagVal = pr.getFlag(RegionFlag.REGION_UUID_FLAG);
            if (flagVal != null) {
                UUID propId = UUID.fromString(flagVal);
                return PropertyRegionDAO.loadProperty(propId);
            }
        }
        return Optional.empty();
    }

    // Example stubs for economy or money
    private boolean hasEnoughMoney(Player p, int amount) {
        // your economy check
        return true;
    }
    private void withdrawFromPlayer(Player p, int amount) {
        // remove money
    }
    private void depositToPlayer(UUID playerId, int amount) {
        // deposit money to that player
    }
}
