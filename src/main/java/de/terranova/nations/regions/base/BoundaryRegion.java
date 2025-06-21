package de.terranova.nations.regions.base;

import de.terranova.nations.database.dao.GridRegionDAO;
import de.terranova.nations.worldguard.RegionClaimFunctions;
import de.terranova.nations.worldguard.math.Vectore2;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;
//Supports Poly & Squared Regions
public abstract class BoundaryRegion extends Region {
    public BoundaryRegion(String name, UUID id, String type) {
        super(name, id, type);
    }

    @Override
    public final void onCreation(Player p) {
        //TODO Implement createBoundaryClaim()
        this.region = RegionClaimFunctions.createBoundaryClaim();
        onPolyCreation(p);
    }

    public void onPolyCreation(Player p) {

    }

    @Override
    public final void onRemove() {
        //BoundaryRegionDAO.removeRegion
        onBoundaryRemove();
    }

    public void onBoundaryRemove() {

    }
    @Override
    public void onRename(String name) {
        //BoundaryRegionDAO.updateRegionName
    }
    @Override
    public final void dataBaseCall() {
        //Boundary Region Insert
    }

}
