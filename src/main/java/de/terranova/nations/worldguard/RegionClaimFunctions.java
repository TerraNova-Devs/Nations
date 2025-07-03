package de.terranova.nations.worldguard;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Polygonal2DRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.*;
import de.terranova.nations.utils.Chat;
import de.terranova.nations.worldguard.NationsRegionFlag.RegionFlag;
import de.terranova.nations.worldguard.NationsRegionFlag.TypeFlag;
import de.terranova.nations.worldguard.math.Vectore2;
import de.terranova.nations.worldguard.math.claimCalc;
import net.goldtreeservers.worldguardextraflags.flags.Flags;
import net.goldtreeservers.worldguardextraflags.flags.helpers.ForcedStateFlag;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.*;

public class RegionClaimFunctions {

    public static ProtectedRegion createGridClaim(String name, Player p, UUID uuid, String type) {

        int nx = (int) (Math.floor(p.getLocation().x() / 48) * 48);
        int nz = (int) (Math.floor(p.getLocation().z() / 48) * 48);

        BlockVector2 nw = BlockVector2.at(nx, nz);
        BlockVector2 sw = BlockVector2.at(nx, nz + 47);
        BlockVector2 se = BlockVector2.at(nx + 47, nz + 47);
        BlockVector2 ne = BlockVector2.at(nx + 47, nz);

        List<BlockVector2> corners = Arrays.asList(nw, ne, se, sw);

        LocalPlayer lp = WorldGuardPlugin.inst().wrapPlayer(p);


        ProtectedPolygonalRegion region = new ProtectedPolygonalRegion(name, corners, -64, 320);

        DefaultDomain owners = region.getOwners();
        owners.addPlayer(lp);
        region.setOwners(owners);
        region.setFlag(RegionFlag.REGION_UUID_FLAG, uuid.toString());
        region.setFlag(TypeFlag.NATIONS_TYPE, type);
        region.setFlag(Flags.GLIDE, ForcedStateFlag.ForcedState.ALLOW);
        region.setPriority(100);

        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regions = container.get(lp.getWorld());

        assert regions != null;
        regions.addRegion(region);
        return regions.getRegion(region.getId());
    }

    public static ProtectedRegion createBoundaryClaim(String name, Player p, UUID uuid, String type) {
        BukkitPlayer wePlayer = BukkitAdapter.adapt(p);
        LocalSession session = WorldEdit.getInstance().getSessionManager().get(wePlayer);
        Region selection;
        try {
            selection = session.getSelection(wePlayer.getWorld());
        } catch (IncompleteRegionException e) {
            p.sendMessage("You need to make a complete selection first.");
            return null;
        }
        if (!(selection instanceof CuboidRegion || selection instanceof Polygonal2DRegion)) {
            p.sendMessage("Only Cuboid and Polygonal2D regions are supported.");
            return null;
        }
        org.bukkit.World bukkitWorld = p.getWorld();
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        com.sk89q.worldedit.world.World weWorld = BukkitAdapter.adapt(bukkitWorld);
        RegionManager regionManager = container.get(weWorld);
        if (regionManager == null) {
            p.sendMessage("WorldGuard region manager not found for this world.");
            return null;
        }

        ProtectedRegion region;
        if (selection instanceof CuboidRegion cuboid) {
            region = new ProtectedCuboidRegion(name, cuboid.getMinimumPoint(), cuboid.getMaximumPoint());
        } else {
            Polygonal2DRegion poly = (Polygonal2DRegion) selection;
            region = new ProtectedPolygonalRegion(name, poly.getPoints(), poly.getMinimumY(), poly.getMaximumY());
        }
        region.setFlag(RegionFlag.REGION_UUID_FLAG, uuid.toString());
        region.setFlag(TypeFlag.NATIONS_TYPE, type);
        DefaultDomain owners = region.getOwners();
        owners.addPlayer(p.getUniqueId());
        region.setOwners(owners); // optional, da `getOwners()` nicht kopiert
        regionManager.addRegion(region);
        return region;
    }

    public static void changeFlag(Player p, UUID settlementID, Flag flag) {
        World world = Bukkit.getWorld("world");
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        assert world != null;
        RegionManager regions = container.get(BukkitAdapter.adapt(world));
        for (ProtectedRegion region : regions.getRegions().values()) {
            if (!Objects.equals(region.getFlag(RegionFlag.REGION_UUID_FLAG), settlementID)) continue;
        }


    }

    public static void addToExistingClaim(Player p, ProtectedRegion oldRegion) {
        if (oldRegion instanceof ProtectedPolygonalRegion oldPolygonalRegion) {

            int nx = (int) (Math.floor(p.getLocation().x() / 48) * 48);
            int nz = (int) (Math.floor(p.getLocation().z() / 48) * 48);

            Vectore2 nw = new Vectore2(nx + 0.5, nz + 0.5);
            Vectore2 ne = new Vectore2(nx + 0.5 + 47, nz + 0.5);
            Vectore2 sw = new Vectore2(nx + 0.5, nz + 47 + 0.5);
            Vectore2 se = new Vectore2(nx + 47 + 0.5, nz + 47 + 0.5);


            List<Vectore2> newPoints = Arrays.asList(nw, ne, se, sw);
            List<Vectore2> oldPoints = new ArrayList<>();

            for (BlockVector2 v : oldPolygonalRegion.getPoints()) {
                oldPoints.add(new Vectore2(v.x(), v.z()));
            }


            Optional<List<Vectore2>> claims = claimCalc.dothatshitforme(oldPoints, newPoints);
            if (claims.isEmpty()) {
                p.sendMessage(Chat.errorFade("Bitte keine leeren flächen umclaimen."));
                return;
            }


            List<BlockVector2> finalNewRegion = new ArrayList<>();

            for (Vectore2 v : claims.get()) {
                finalNewRegion.add(BlockVector2.at(v.x, v.z));
                //p.sendMessage(String.valueOf(BlockVector2.at(v.x, v.z)));
            }


            ProtectedPolygonalRegion region = new ProtectedPolygonalRegion(oldRegion.getId(), finalNewRegion, oldPolygonalRegion.getMinimumPoint().y(), oldPolygonalRegion.getMaximumPoint().y());
            region.copyFrom(oldRegion);


            LocalPlayer lp = WorldGuardPlugin.inst().wrapPlayer(p);
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager regions = container.get(lp.getWorld());
            assert regions != null;
            regions.addRegion(region);

        }
    }

    public static Vectore2 getSChunkMiddle(Location location) {
        int x = (int) (Math.floor(location.x() / 48) * 48);
        int z = (int) (Math.floor(location.z() / 48) * 48);
        return new Vectore2(x + 24, z + 24);
    }

    public static Vectore2 getSChunkMiddle(Vectore2 location) {
        int x = (int) (Math.floor(location.x / 48) * 48);
        int z = (int) (Math.floor(location.z / 48) * 48);
        return new Vectore2(x + 24, z + 24);
    }

    public static boolean checkAreaForSettles(Player p) {
        LocalPlayer lp = WorldGuardPlugin.inst().wrapPlayer(p);
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regions = container.get(lp.getWorld());
        assert regions != null;
        RegionQuery query = container.createQuery();
        ApplicableRegionSet set = query.getApplicableRegions(lp.getLocation());
        return !(set.size() == 0);
    }

    public static int getClaimAnzahl(UUID settle) {
        World world = Bukkit.getWorld("world");
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        assert world != null;
        RegionManager regions = container.get(BukkitAdapter.adapt(world));

        for (ProtectedRegion region : regions.getRegions().values()) {

            if (!Objects.equals(region.getFlag(RegionFlag.REGION_UUID_FLAG), settle.toString())) continue;

            List<Vectore2> list2 = new ArrayList();
            list2.addAll(Vectore2.fromBlockVectorList(region.getPoints()));
            List<Vectore2> list3;
            list3 = claimCalc.aufplustern(claimCalc.normalisieren(list2));

            return (int) claimCalc.area(list3.toArray(new Vectore2[list3.size()])) / 2304;
        }
        return 1;
    }

    public static void remove(String name) {

    }

}
