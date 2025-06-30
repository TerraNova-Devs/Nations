package de.terranova.nations.regions.modules.realEstate;

import java.util.*;

public class RealEstateManager {
    private static final Map<UUID, List<CanBeSold>> realEstates = new HashMap<>();

    public static void addRealestate(CanBeSold estate) {
        UUID agentId = estate.getAgent().ownerId;
        realEstates.computeIfAbsent(agentId, k -> new ArrayList<>()).add(estate);
    }

    public static List<CanBeSold> getRealestate(UUID agentId) {
        return realEstates.getOrDefault(agentId, Collections.emptyList());
    }

}
