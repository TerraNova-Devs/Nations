package de.terranova.nations.regions.base;

import java.util.UUID;

public abstract class PolyRegion extends Region {
    public PolyRegion(String name, UUID id, String type) {
        super(name, id, type);
    }
    @Override
    public final void dataBaseCall(){

    }
}
