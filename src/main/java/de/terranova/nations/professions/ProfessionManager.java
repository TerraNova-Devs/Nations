package de.terranova.nations.professions;


import de.terranova.nations.NationsPlugin;
import de.terranova.nations.professions.pojo.BuildingConfig;
import de.terranova.nations.professions.pojo.ObjectiveConfig;
import de.terranova.nations.professions.pojo.ProfessionConfig;

import java.util.*;

public class ProfessionManager {
    private static final Map<String, ProfessionConfig> professionMap = new HashMap<>();
    private static final Map<String, List<ObjectiveConfig>> objectivesMap = new HashMap<>();
    private static final Map<String, List<BuildingConfig>> buildingsMap = new HashMap<>();

    public static void loadAll() {
        // 1) Professionen laden
        var allProfs = NationsPlugin.professionConfigs;
        for (ProfessionConfig p : allProfs) {
            professionMap.put(p.professionId, p);

            // 2) Objectives
            var objs = p.objectives;
            objectivesMap.put(p.professionId, objs);

            // 3) Buildings
            var blds = p.buildings;
            buildingsMap.put(p.professionId, blds);
        }
    }

    public static ProfessionConfig getProfessionById(String profId) {
        return professionMap.get(profId);
    }

    public static List<ObjectiveConfig> getObjectivesForProfession(String profId) {
        return objectivesMap.getOrDefault(profId, Collections.emptyList());
    }

    public static List<BuildingConfig> getBuildingsForProfession(String profId) {
        return buildingsMap.getOrDefault(profId, Collections.emptyList());
    }

    /**
     * Beispiel: Finde alle Professionen eines bestimmten Typs (z.B. "FISHERY"), sortiert nach Level.
     */
    public static List<ProfessionConfig> getProfessionsByType(String type) {
        ArrayList<ProfessionConfig> result = new ArrayList<>();
        for (ProfessionConfig p : professionMap.values()) {
            if (p.type.equalsIgnoreCase(type)) {
                result.add(p);
            }
        }
        result.sort(Comparator.comparingInt(ProfessionConfig::getLevel));
        return result;
    }

    public static List<String> getBuildingIds() {
        ArrayList<String> result = new ArrayList<>();
        for (List<BuildingConfig> b : buildingsMap.values()) {
            for (BuildingConfig bc : b) {
                result.add(bc.buildingId);
            }
        }
        return result;
    }

    public static List<String> getProfessionTypes() {
        Set<String> result = new HashSet<>();
        for (ProfessionConfig p : professionMap.values()) {
            result.add(p.type);
        }
        return new ArrayList<>(result);
    }
}
