package de.terranova.nations.regions.base;

import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import de.terranova.nations.database.dao.BoundaryRegionDAO;
import de.terranova.nations.worldguard.RegionClaimFunctions;
import java.util.UUID;
import org.bukkit.entity.Player;

// Supports Poly & Squared Regions
public abstract class BoundaryRegion extends Region {
  public BoundaryRegion(String name, UUID id, String type) {
    super(name, id, type);
  }

  @Override
  public final void onCreation(Player p) {
    this.region = RegionClaimFunctions.createBoundaryClaim(this.name, p, this.id, this.type);
    onBoundaryCreation(p);
  }

  public void onBoundaryCreation(Player p) {}

  public boolean isPoly() {
    return getWorldguardRegion() instanceof ProtectedPolygonalRegion;
  }

  @Override
  public final void onRemove() {
    BoundaryRegionDAO.removeRegion(this.id);
    onBoundaryRemove();
  }

  public void onBoundaryRemove() {}

  @Override
  public void onRename(String name) {
    BoundaryRegionDAO.updateRegionName(this.id, name);
  }

  @Override
  public final void dataBaseCall() {
    BoundaryRegionDAO.insertRegion(this);
  }
}
