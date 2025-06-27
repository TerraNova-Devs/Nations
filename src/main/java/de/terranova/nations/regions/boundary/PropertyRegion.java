package de.terranova.nations.regions.boundary;

import de.terranova.nations.regions.grid.SettleRegion;
import de.terranova.nations.regions.modules.HasParent;
import de.terranova.nations.regions.modules.access.PropertyAccess;
import de.terranova.nations.regions.modules.access.PropertyAccessControlled;
import de.terranova.nations.regions.modules.access.PropertyAccessLevel;
import de.terranova.nations.regions.modules.bank.Bank;
import de.terranova.nations.regions.modules.bank.BankHolder;
import de.terranova.nations.regions.base.BoundaryRegion;
import java.util.*;

public class PropertyRegion extends BoundaryRegion implements PropertyAccessControlled , BankHolder, HasParent<SettleRegion> {
    public static final String REGION_TYPE = "property";

    private PropertyAccess access;
    private Bank bank;
    private SettleRegion parent;

    public PropertyRegion(String name, UUID ruuid, UUID owner) {
        super(name, ruuid, REGION_TYPE);
        this.access = new PropertyAccess(this);
        this.setOwner(owner);
        this.addNameToCache(name);
    }

    public UUID getOwner() {
        return this.access.getOwner();
    }

    public void setOwner(UUID owner) {
        this.access.setAccessLevel(owner, PropertyAccessLevel.OWNER);
    }

    public Collection<UUID> getTrusted() {
        return this.access.getEveryUUIDWithCertainAccessLevel(PropertyAccessLevel.MEMBER);
    }

    public void addTrusted(UUID uuid) {
        this.access.setAccessLevel(uuid, PropertyAccessLevel.MEMBER);
    }

    public void removeTrusted(UUID uuid) {
        this.access.removeAccess(uuid);
    }

    @Override
    public PropertyAccess getAccess() {
        return null;
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