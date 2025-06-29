package de.terranova.nations.regions.boundary;

import de.terranova.nations.regions.base.BoundaryRegion;
import de.terranova.nations.regions.grid.SettleRegion;
import de.terranova.nations.regions.modules.HasParent;
import de.terranova.nations.regions.modules.realEstate.CanBeSold;
import de.terranova.nations.regions.modules.realEstate.RealEstateAgent;

import java.util.UUID;

public class PropertyRegion extends BoundaryRegion implements HasParent<SettleRegion>, CanBeSold {
    public static final String REGION_TYPE = "property";

    private SettleRegion parent;
    private final RealEstateAgent realEstateAgent;

    public PropertyRegion(String name, UUID ruuid, SettleRegion parent) {
        super(name, ruuid, REGION_TYPE);
        this.addNameToCache(name);
        setParent(parent);
        this.realEstateAgent = new RealEstateAgent(this);
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