package de.terranova.nations.regions.boundary;

import de.terranova.nations.regions.access.PropertyAccess;
import de.terranova.nations.regions.access.PropertyAccessControlled;
import de.terranova.nations.regions.access.PropertyAccessLevel;
import de.terranova.nations.regions.bank.Bank;
import de.terranova.nations.regions.bank.BankHolder;
import de.terranova.nations.regions.base.BoundaryRegion;
import de.terranova.nations.regions.hierarchy.HasHierarchy;
import de.terranova.nations.regions.hierarchy.Hierarchy;

import java.util.*;

public class PropertyRegion extends BoundaryRegion implements PropertyAccessControlled , BankHolder, HasHierarchy {
    public static final String REGION_TYPE = "property";

    private PropertyAccess access;
    private Bank bank;
    private Hierarchy hierarchy;

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
    public Hierarchy getHierarchy() { return hierarchy; }
}