package de.terranova.nations.regions.modules.realEstate;

import java.util.*;
import java.util.stream.Collectors;

public class RealEstateMarketCache {

    //public listings
    private static final Map<UUID, List<HasRealEstateAgent>> realEstateListings = new HashMap<>();

    public static void upsertListing(UUID parent, HasRealEstateAgent estate) {
        List<HasRealEstateAgent> list = realEstateListings.computeIfAbsent(parent, k -> new ArrayList<>());
        list.removeIf(existing -> existing.getAgent().region.getId().equals(estate.getAgent().region.getId()));
        list.add(estate);
    }

    public static List<HasRealEstateAgent> getListing(UUID agentId) {
        return realEstateListings.getOrDefault(agentId, Collections.emptyList());
    }

    public static List<HasRealEstateAgent> getAllListings() {
        return realEstateListings.values().stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }


    public static void removeListing(UUID parent, UUID agentId) {
        List<HasRealEstateAgent> list = realEstateListings.computeIfAbsent(parent, k -> new ArrayList<>());
        list.removeIf(existing -> existing.getAgent().region.getId().equals(agentId));
    }

    public static boolean hasListing(UUID parent, UUID agentId) {
        List<HasRealEstateAgent> list = realEstateListings.get(parent);
        if (list == null) return false;
        return list.stream()
                .anyMatch(existing -> existing.getAgent().region.getId().equals(agentId));
    }

}
