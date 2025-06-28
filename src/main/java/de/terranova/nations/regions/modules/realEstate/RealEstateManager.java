package de.terranova.nations.regions.modules.realEstate;

import de.terranova.nations.regions.RegionManager;
import de.terranova.nations.regions.base.Region;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RealEstateManager {
    private static Map<UUID, CanBeSold> realEstates = new HashMap<>();

    public static void addRealestate(CanBeSold estate){
        realEstates.put(estate.getAgent().ownerId, estate);
    }


}
