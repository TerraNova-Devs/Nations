package de.terranova.nations.regions.base;

import de.terranova.nations.database.dao.BoundaryRegionDAO;
import de.terranova.nations.worldguard.RegionClaimFunctions;
import org.bukkit.entity.Player;

import java.util.UUID;

//Supports Poly & Squared Regions
public abstract class BoundaryRegion extends Region {
    public BoundaryRegion(String name, UUID id, String type) {
        super(name, id, type);
    }

    @Override
    public final void onCreation(Player p) {
        this.region = RegionClaimFunctions.createBoundaryClaim(this.name, p, this.id, this.type);
        onPolyCreation(p);
    }

    public void onPolyCreation(Player p) {

    }

    @Override
    public final void onRemove() {
        BoundaryRegionDAO.removeRegion(this.id);
        onBoundaryRemove();
    }

    public void onBoundaryRemove() {

    }

    @Override
    public void onRename(String name) {
        BoundaryRegionDAO.updateRegionName(this.id, name);
    }

    @Override
    public final void dataBaseCall() {
        BoundaryRegionDAO.insertRegion(this);
    }

}
