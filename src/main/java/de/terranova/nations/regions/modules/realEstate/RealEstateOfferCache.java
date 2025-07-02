package de.terranova.nations.regions.modules.realEstate;

import java.util.*;

public class RealEstateOfferCache {
    private static final Map<UUID, List<CanBeSold>> realEstates = new HashMap<>();

    public static void addRealestate(UUID parent, CanBeSold estate) {
        List<CanBeSold> list = realEstates.computeIfAbsent(parent, k -> new ArrayList<>());
        // Upsert: remove old entry with same ID if it exists
        list.removeIf(existing -> existing.getAgent().region.getId().equals(estate.getAgent().region.getId()));

        // Add the new/updated entry
        list.add(estate);

    }

    public static List<CanBeSold> getRealestate(UUID agentId) {
        return realEstates.getOrDefault(agentId, Collections.emptyList());
    }


    public static void removeRealestate(UUID parent, UUID agentId) {
        List<CanBeSold> list = realEstates.computeIfAbsent(parent, k -> new ArrayList<>());
        // Upsert: remove old entry with same ID if it exists
        list.removeIf(existing -> existing.getAgent().region.getId().equals(agentId));
    }

}
