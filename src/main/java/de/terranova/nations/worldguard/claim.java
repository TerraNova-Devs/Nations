package de.terranova.nations.worldguard;

import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector2;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class claim {
  public static void createClaim(String name, Player p) {

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


    RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
    RegionManager regions = container.get(lp.getWorld());

    assert regions != null;
    regions.addRegion(region);
  }

  public static void addToExistingClaim(Player p, ProtectedRegion oldRegion) {
    if (oldRegion instanceof ProtectedPolygonalRegion oldPolygonalRegion) {

      int nx = (int) (Math.floor(p.getLocation().x() / 48) * 48);
      int nz = (int) (Math.floor(p.getLocation().z() / 48) * 48);

      Vectore2 nw = new Vectore2(nx, nz);
      Vectore2 sw = new Vectore2(nx, nz + 47);
      Vectore2 se = new Vectore2(nx + 47, nz + 47);
      Vectore2 ne = new Vectore2(nx + 47, nz);

      List<Vectore2> newPoints = Arrays.asList(nw, ne, se, sw);
      List<Vectore2> oldPoints = new ArrayList<>();

      for (BlockVector2 v : oldPolygonalRegion.getPoints()) {

        oldPoints.add(new Vectore2(v.x(), v.z()));
      }


      for (Vectore2 v : oldPoints) {
        p.sendMessage("Old: x:" + v.x + " | z:" + v.z);

      }
      for (Vectore2 v : newPoints) {
        p.sendMessage("New: x:" + v.x + " | z:" + v.z);

      }

      //change direction of new points to clockwise
      List<Vectore2> oldPointsRotated = new ArrayList<>();

      for (int i = newPoints.size() - 1; i >= 0; i--) {
        oldPointsRotated.add(newPoints.get(i));
      }
      for (Vectore2 v : oldPointsRotated) {
        p.sendMessage("OldR: x:" + v.x + " | z:" + v.z);
      }
      List<Vectore2> newPointsRotatedNormalized = new ArrayList<>();

      int index = 1;
      for (Vectore2 v : oldPointsRotated) {
        //System.out.println("Debug x:" + v.x + " | z:" + v.z);
        //System.out.println("Debug2 x:" + newPointsRotated.get(index).x + " | z:" + newPointsRotated.get(index).z);
        if (index == oldPointsRotated.size()) {
          index = 0;
        }
        System.out.println("Debug3 " + v.z + "|" + oldPointsRotated.get(index).z);
        System.out.println("Debug3 " + v.x + "|" + oldPointsRotated.get(index).x);
        if (v.x == oldPointsRotated.get(index).x) {
          if (v.z < oldPointsRotated.get(index).z) {
            newPointsRotatedNormalized.add(new Vectore2(v.x + 1.5,v.z+0.5));
            index++;
            continue;
          }
          if (v.z > oldPointsRotated.get(index).z) {
            newPointsRotatedNormalized.add(new Vectore2(v.x + 1.5,v.z +0.5));
            index++;
            continue;
          }
        }
        if (v.z == oldPointsRotated.get(index).z) {
          if (v.x < oldPointsRotated.get(index).x) {
            newPointsRotatedNormalized.add(new Vectore2(v.x,v.z + 0.5));
            index++;
            continue;
          }
          if (v.x > oldPointsRotated.get(index).x) {
            newPointsRotatedNormalized.add(new Vectore2( v.x,v.z - 0.5));
            index++;
          }
        }

      }

      for (Vectore2 v : newPointsRotatedNormalized) {
        p.sendMessage("OldA: x:" + v.x + " | z:" + v.z);
      }

            /*
            ArrayList<BlockVector2> newTest = new ArrayList<>(newPoints);
            ArrayList<BlockVector2> oldTest = new ArrayList<>(oldPoints);

            p.sendMessage(newTest.size() + " | " + oldTest.size());

            int sizeOld = oldTest.size();
            int sizeNew = newPoints.size();
            int offsets = 0;

            if(newTest.get(0).getZ()>oldTest.get(0).getZ()) {
                p.sendMessage("z");
                for(int i = 0; i < sizeOld;i++) {
                    p.sendMessage("Stage 1");
                    for(int n = 0; n < sizeNew;n++) {
                        p.sendMessage("Gut 2");
                        if(newTest.get(n).getZ()- oldTest.get(i).getZ() == 1 || newTest.get(n).getZ()- oldTest.get(i).getZ() == -1) {
                            p.sendMessage("Fehler 3");
                            p.sendMessage("i:" + i + " n:" + n);
                            oldTest.remove(i-offsets);
                            newTest.remove(n-offsets);
                            offsets++;
                            sizeNew--;
                            sizeOld--;
                        }
                    }
                }
            }



            if(newTest.get(0).getX()>oldTest.get(0).getX()) {
                p.sendMessage("x");
                for(int i = 0; i < sizeOld;i++) {
                    p.sendMessage("1");
                    for(int n = 0; n < sizeNew;n++) {
                        p.sendMessage("2");
                        if(newTest.get(n).getX()- oldTest.get(i).getX() == 1 || newTest.get(n).getX()- oldTest.get(i).getX() == -1) {
                            p.sendMessage("Fehler 3");
                            p.sendMessage("i:" + i + " n:" + n);
                            oldTest.remove(i-offsets);
                            newTest.remove(n-offsets);
                            offsets++;
                            sizeNew--;
                            sizeOld--;
                        }
                    }
                }
            }

            List<BlockVector2> newRegionPoints = Stream.concat(oldTest.stream(), newTest.stream()).toList();

             */

      //List<Vectore2> oldPointsPlustered = claimpolycalc.aufplustern(oldPoints);
      //List<Vectore2> newPointsPlustered = claimpolycalc.aufplustern(newPoints);
      List<Vectore2> oldPointsProj = claimpolycalc.projezieren(oldPoints);
      Optional<List<Vectore2>> mergedPoints = claimpolycalc.mergen(oldPointsProj, newPoints);
      if (mergedPoints.isEmpty()) {
        return;
      }
      List<Vectore2> entprojPoints = claimpolycalc.entprojezieren(mergedPoints.get());
      //List<Vectore2> reversePlusteredPoints = claimpolycalc.reverseaufplustern(entprojPoints);

      List<BlockVector2> finalNewRegion = new ArrayList<>();

      for (Vectore2 v : entprojPoints) {
        finalNewRegion.add(BlockVector2.at(v.x, v.z));
        p.sendMessage(String.valueOf(BlockVector2.at(v.x, v.z)));
      }


      ProtectedPolygonalRegion region = new ProtectedPolygonalRegion(oldRegion.getId(), finalNewRegion, oldPolygonalRegion.getMinimumPoint().y(), oldPolygonalRegion.getMaximumPoint().y());
      region.copyFrom(oldRegion);

      p.sendMessage("Wal");

      LocalPlayer lp = WorldGuardPlugin.inst().wrapPlayer(p);
      RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
      RegionManager regions = container.get(lp.getWorld());
      assert regions != null;
      regions.addRegion(region);

      p.sendMessage("Marienkaefer");
    }
  }

  public static Optional<ProtectedRegion> checkSurrAreaForSettles(Player p) {
    p.sendMessage("Papagei");
    int nx = (int) Math.floor(p.getLocation().x() / 48);
    int nz = (int) Math.floor(p.getLocation().z() / 48);

    Vector2 north = Vector2.at(nx - 1, nz);
    Vector2 south = Vector2.at(nx + 1, nz);
    Vector2 west = Vector2.at(nx, nz - 1);
    Vector2 east = Vector2.at(nx, nz + 1);

    BlockVector3[] bpos = new BlockVector3[]{BlockVector3.at(north.x() * 48, -64, north.z() * 48), BlockVector3.at(south.x() * 48, -64, south.z() * 48), BlockVector3.at(west.x() * 48, -64, west.z() * 48), BlockVector3.at(east.x() * 48, -64, east.z() * 48)};

    LocalPlayer lp = WorldGuardPlugin.inst().wrapPlayer(p);
    RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
    RegionManager regions = container.get(lp.getWorld());

    for (BlockVector3 pos : bpos) {
      assert regions != null;
      ApplicableRegionSet set = regions.getApplicableRegions(pos);
      if (!(set.size() == 0)) {
        return set.getRegions().stream().findFirst();
      }
    }
    return Optional.empty();
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


  public static void remove(String name) {

  }
}
