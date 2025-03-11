package de.terranova.nations.professions;

import de.terranova.nations.database.dao.*;
import de.terranova.nations.regions.grid.SettleRegion;

import java.util.*;

/**
 * Verwaltet alle Profession-bezogenen Infos einer Stadt.
 * Lädt Status, Objectives, Buildings etc. und wendet Logik an.
 */
public class ProfessionProgressManager {

    private final UUID settlementId;
    private final Map<Integer, ProfessionStatus> professionStatuses = new HashMap<>();
    private final Map<Integer, Long> objectiveProgress = new HashMap<>();
    private final Set<Integer> builtBuildings = new HashSet<>();

    private Integer activeProfessionId = null;

    private ProfessionProgressManager(UUID settlementId) {
        this.settlementId = settlementId;
    }

    public static ProfessionProgressManager loadForSettlement(UUID settlementId) {
        ProfessionProgressManager mgr = new ProfessionProgressManager(settlementId);

        // 1) Lade professionStatuses
        Map<Integer, ProfessionStatus> map = SettlementProfessionRelationDAO.getAllStatuses(settlementId.toString());
        mgr.professionStatuses.putAll(map);

        // 2) Lade objective progress
        Map<Integer, Long> progressMap = SettlementObjectiveProgressDAO.getAllProgress(settlementId.toString());
        mgr.objectiveProgress.putAll(progressMap);

        // 3) Lade gebaute Gebäude
        Set<Integer> built = SettlementBuildingsDAO.getBuiltBuildings(settlementId.toString());
        mgr.builtBuildings.addAll(built);

        // 4) Option: Aus grid_regions oder einer Extra-Spalte "ActiveProfession" auslesen
        // mgr.activeProfessionId = ...;

        return mgr;
    }

    public UUID getSettlementId() {
        return settlementId;
    }

    /**
     * Gibt den "echten" Status einer Profession zurück, ggf.
     * dynamisch berechnet (LOCKED -> AVAILABLE), etc.
     */
    public ProfessionStatus getProfessionStatus(int professionId) {
        // Basisstatus aus DB
        ProfessionStatus base = professionStatuses.getOrDefault(professionId, ProfessionStatus.LOCKED);

        // Falls COMPLETED => direkt zurück
        if (base == ProfessionStatus.COMPLETED) return ProfessionStatus.COMPLETED;

        // Checke, ob Vorstufe bereits COMPLETED => wenn ja, kann es AVAILABLE sein
        Profession p = ProfessionManager.getProfessionById(professionId);
        if (p == null) {
            return ProfessionStatus.LOCKED; // fallback
        }
        if (p.getLevel() > 1) {
            // Vorherige Stufe suchen
            Profession prev = findProfession(p.getType(), p.getLevel() - 1);
            if (prev != null) {
                ProfessionStatus prevStat = professionStatuses.getOrDefault(prev.getProfessionId(), ProfessionStatus.LOCKED);
                if (prevStat != ProfessionStatus.COMPLETED) {
                    // Vorstufe ist NICHT fertig => LOCKED
                    return ProfessionStatus.LOCKED;
                }
            }
        }

        // Wenn Basis LOCKED => wir machen es zu AVAILABLE
        if (base == ProfessionStatus.LOCKED) {
            base = ProfessionStatus.AVAILABLE;
        }

        // Falls "ACTIVE" in DB => check, ob wir es auch wirklich active haben
        if (base == ProfessionStatus.ACTIVE) {
            if (!professionIdEquals(activeProfessionId, professionId)) {
                base = ProfessionStatus.PAUSED;
            }
        }

        return base;
    }

    public void setProfessionStatus(int professionId, ProfessionStatus newStatus) {
        professionStatuses.put(professionId, newStatus);
        SettlementProfessionRelationDAO.setStatus(settlementId.toString(), professionId, newStatus);
        if (newStatus == ProfessionStatus.ACTIVE) {
            pauseAllOtherActive(professionId);
            this.activeProfessionId = professionId;
        }
    }

    public long getObjectiveProgress(int objectiveId) {
        return objectiveProgress.getOrDefault(objectiveId, 0L);
    }

    public void setObjectiveProgress(int objectiveId, long newValue) {
        objectiveProgress.put(objectiveId, newValue);
        SettlementObjectiveProgressDAO.setProgress(settlementId.toString(), objectiveId, newValue);
    }

    public boolean hasBuilding(int buildingId) {
        return builtBuildings.contains(buildingId);
    }

    public void setBuildingBuilt(int buildingId, boolean built) {
        if (built) builtBuildings.add(buildingId);
        else builtBuildings.remove(buildingId);
        SettlementBuildingsDAO.setBuilt(settlementId.toString(), buildingId, built);
    }

    /**
     * Wenn wir eine Profession aktivieren,
     * wollen wir evtl. alle anderen, die "ACTIVE" sind, auf "PAUSED" setzen.
     */
    public void pauseAllOtherActive(int exceptProfessionId) {
        for (Map.Entry<Integer, ProfessionStatus> e : professionStatuses.entrySet()) {
            if (e.getKey() == exceptProfessionId) continue;
            if (e.getValue() == ProfessionStatus.ACTIVE) {
                setProfessionStatus(e.getKey(), ProfessionStatus.PAUSED);
            }
        }
    }

    private Profession findProfession(String type, int level) {
        for (Profession pr : ProfessionManager.getProfessionsByType(type)) {
            if (pr.getLevel() == level) return pr;
        }
        return null;
    }

    private boolean professionIdEquals(Integer a, int b) {
        return (a != null && a == b);
    }
}
