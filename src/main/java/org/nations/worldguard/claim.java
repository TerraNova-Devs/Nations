package org.nations.worldguard;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.entity.Player;

public class claim {
    public static void createClaim(int x, int z, String name, Player p) {

        int nx = (int) Math.floor((double) x / 48);
        int nz = (int) Math.floor((double) z / 48);

        BlockVector3 pos1 = BlockVector3.at(nx*48, -64 , nz*48 + 47);
        BlockVector3 pos2 = BlockVector3.at(nx*48 + 47, 320, nz*48);

        ProtectedCuboidRegion region = new ProtectedCuboidRegion(name, pos1, pos2);

        LocalPlayer lp = WorldGuardPlugin.inst().wrapPlayer(p);
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regions = container.get(lp.getWorld());

        assert regions != null;
        regions.addRegion(region);
    }


    public static void remove(String name) {

    }
}
