package de.terranova.nations.professions;

import de.terranova.nations.database.dao.BuildingDAO;
import de.terranova.nations.database.dao.ProfessionDAO;
import de.terranova.nations.database.dao.ProfessionObjectiveDAO;

import java.util.*;

public class ProfessionManager {
    private static final Map<Integer, Profession> professionMap = new HashMap<>();
    private static final Map<Integer, List<ProfessionObjective>> objectivesMap = new HashMap<>();
    private static final Map<Integer, List<Building>> buildingsMap = new HashMap<>();

    public static void loadAll() {
        // 1) Professionen laden
        var allProfs = ProfessionDAO.getAllProfessions();
        for (Profession p : allProfs) {
            professionMap.put(p.getProfessionId(), p);

            // 2) Objectives
            var objs = ProfessionObjectiveDAO.getObjectivesForProfession(p.getProfessionId());
            objectivesMap.put(p.getProfessionId(), objs);

            // 3) Buildings
            var blds = BuildingDAO.getBuildingsByProfession(p.getProfessionId());
            buildingsMap.put(p.getProfessionId(), blds);
        }
    }

    public static Profession getProfessionById(int profId) {
        return professionMap.get(profId);
    }

    public static List<ProfessionObjective> getObjectivesForProfession(int profId) {
        return objectivesMap.getOrDefault(profId, Collections.emptyList());
    }

    public static List<Building> getBuildingsForProfession(int profId) {
        return buildingsMap.getOrDefault(profId, Collections.emptyList());
    }

    /** Beispiel: Finde alle Professionen eines bestimmten Typs (z.B. "FISHERY"), sortiert nach Level. */
    public static List<Profession> getProfessionsByType(String type) {
        ArrayList<Profession> result = new ArrayList<>();
        for (Profession p : professionMap.values()) {
            if (p.getType().equalsIgnoreCase(type)) {
                result.add(p);
            }
        }
        result.sort(Comparator.comparingInt(Profession::getLevel));
        return result;
    }
}
