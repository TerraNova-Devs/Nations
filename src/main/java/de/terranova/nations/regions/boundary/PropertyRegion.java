package de.terranova.nations.regions.boundary;

import de.terranova.nations.database.dao.GridRegionDAO;
import de.terranova.nations.database.dao.RealEstateDAO;
import de.terranova.nations.regions.base.BoundaryRegion;
import de.terranova.nations.regions.grid.SettleRegion;
import de.terranova.nations.regions.modules.HasParent;
import de.terranova.nations.regions.modules.realEstate.CanBeSold;
import de.terranova.nations.regions.modules.realEstate.RealEstateAgent;
import de.terranova.nations.regions.modules.realEstate.RealEstateData;
import de.terranova.nations.utils.Chat;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PropertyRegion extends BoundaryRegion implements HasParent<SettleRegion>, CanBeSold {
    public static final String REGION_TYPE = "property";

    private SettleRegion parent;
    private RealEstateAgent realEstateAgent;

    public PropertyRegion(String name, UUID ruuid, SettleRegion parent) {
        super(name, ruuid, REGION_TYPE);
        this.addNameToCache(name);
        setParent(parent);
        this.region = getWorldguardRegion();
        if(region != null) {
            this.realEstateAgent = new RealEstateAgent(this,RealEstateDAO.getRealEstateById(this.getId()));
            realEstateAgent.addToOfferCacheMarket();
        }


    }

    @Override
    public void onBoundaryCreation(Player p) {
        GridRegionDAO.insertParent(this.id,parent.getId());
        this.realEstateAgent = new RealEstateAgent(this,new RealEstateData(null,false,0,false,0, null));
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