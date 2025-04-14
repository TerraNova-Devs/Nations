package de.terranova.nations.regions.base;

import de.terranova.nations.regions.poly.PropertyRegion;

import java.util.UUID;

public abstract class PolyRegion extends Region {
    public PolyRegion(String name, UUID id, String type) {
        super(name, id, type);
    }
    @Override
    public final void dataBaseCall(){
        for (PropertyRegion subProperty : getSubRegions()) {
            subProperty.dataBaseCall();
        }
    }
}
