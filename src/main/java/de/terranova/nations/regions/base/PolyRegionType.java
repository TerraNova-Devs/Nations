package de.terranova.nations.regions.base;

import java.util.UUID;

public abstract class PolyRegionType extends RegionType {
    public PolyRegionType(String name, UUID id, String type) {
        super(name, id, type);
    }
    @Override
    public final void dataBaseCall(){

    }
}
