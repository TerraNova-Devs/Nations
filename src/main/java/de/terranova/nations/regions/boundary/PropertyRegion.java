package de.terranova.nations.regions.boundary;

import de.terranova.nations.regions.grid.SettleRegion;
import de.terranova.nations.regions.modules.HasParent;

import de.terranova.nations.regions.modules.bank.Bank;
import de.terranova.nations.regions.modules.bank.BankHolder;
import de.terranova.nations.regions.base.BoundaryRegion;
import java.util.*;

public class PropertyRegion extends BoundaryRegion implements  BankHolder, HasParent<SettleRegion> {
    public static final String REGION_TYPE = "property";

    private Bank bank;
    private SettleRegion parent;

    public PropertyRegion(String name, UUID ruuid, SettleRegion parent) {
        super(name, ruuid, REGION_TYPE);
        this.addNameToCache(name);
        setParent(parent);
    }


    @Override
    public Bank getBank() {
        return bank;
    }

    @Override
    public SettleRegion getParent() {
        return parent;
    }

    @Override
    public void setParentRaw(SettleRegion parent) {
        this.parent = parent;
    }
}