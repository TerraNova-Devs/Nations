package de.terranova.nations.regions.modules.realEstate;

import java.util.*;
import java.util.stream.Collectors;

public class RealEstateOfferCache {
    private static final Map<UUID, List<CanBeSold>> realEstates = new HashMap<>();

    public static void addRealestate(UUID parent, CanBeSold estate) {
        List<CanBeSold> list = realEstates.computeIfAbsent(parent, k -> new ArrayList<>());
        // Upsert: remove old entry with same ID if it exists
        list.removeIf(existing -> existing.getAgent().region.getId().equals(estate.getAgent().region.getId()));

        // Add the new/updated entry
        list.add(estate);
        //realEstates.values().forEach(k -> k.forEach(c -> System.out.println(c.getAgent().getRegion().getName())));
        //realEstates.values().forEach(k -> k.forEach(c -> System.out.println(c.getAgent().data.buyPrice)));
        //realEstates.values().forEach(k -> k.forEach(c -> System.out.println(c.getAgent().data.rentPrice)));

    }

    public static List<CanBeSold> getRealestate(UUID agentId) {
        return realEstates.getOrDefault(agentId, Collections.emptyList());
    }

    public static List<CanBeSold> getAllRealEstates() {
        return realEstates.values().stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }


    public static void removeRealestate(UUID parent, UUID agentId) {
        List<CanBeSold> list = realEstates.computeIfAbsent(parent, k -> new ArrayList<>());
        // Upsert: remove old entry with same ID if it exists
        list.removeIf(existing -> existing.getAgent().region.getId().equals(agentId));
    }

}
