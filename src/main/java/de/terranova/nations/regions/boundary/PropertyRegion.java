package de.terranova.nations.regions.boundary;

import de.terranova.nations.database.dao.GridRegionDAO;
import de.terranova.nations.database.dao.RealEstateDAO;
import de.terranova.nations.regions.base.BoundaryRegion;
import de.terranova.nations.regions.grid.SettleRegion;
import de.terranova.nations.regions.modules.HasParent;
import de.terranova.nations.regions.modules.realEstate.HasRealEstateAgent;
import de.terranova.nations.regions.modules.realEstate.RealEstateAgent;
import de.terranova.nations.regions.modules.realEstate.RealEstateListing;
import de.mcterranova.terranovaLib.utils.Chat;
import java.util.UUID;
import org.bukkit.entity.Player;

public class PropertyRegion extends BoundaryRegion
    implements HasParent<SettleRegion>, HasRealEstateAgent {
  public static final String REGION_TYPE = "property";

  private SettleRegion parent;
  private RealEstateAgent realEstateAgent;

  public PropertyRegion(String name, UUID ruuid, SettleRegion parent) {
    super(name, ruuid, REGION_TYPE);
    this.addNameToCache(name);
    setParent(parent);
    this.region = getWorldguardRegion();
    if (region != null) {
      this.realEstateAgent =
          new RealEstateAgent(this, RealEstateDAO.getRealEstateById(this.getId()));
      realEstateAgent.addToOfferCacheMarket();
    }
  }

  @Override
  public void onBoundaryCreation(Player p) {
    GridRegionDAO.insertParent(this.id, parent.getId());
    this.realEstateAgent =
        new RealEstateAgent(this, new RealEstateListing(null, false, 0, false, 0, null));
    region.setPriority(getParent().getWorldguardRegion().getPriority() + 1);
    p.sendMessage(Chat.greenFade("Dein Grundstück " + name + " wurde erfolgreich erstellt."));
  }

  @Override
  public SettleRegion getParent() {
    return parent;
  }

  @Override
  public void setParentRaw(SettleRegion parent) {
    this.parent = parent;
  }

  @Override
  public RealEstateAgent getAgent() {
    return realEstateAgent;
  }
}
