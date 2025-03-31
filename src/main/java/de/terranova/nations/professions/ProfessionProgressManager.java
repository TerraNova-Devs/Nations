package de.terranova.nations.professions;

import de.terranova.nations.database.dao.*;
import de.terranova.nations.professions.pojo.BuildingConfig;
import de.terranova.nations.professions.pojo.ObjectiveConfig;
import de.terranova.nations.professions.pojo.ProfessionConfig;
import de.terranova.nations.regions.RegionManager;
import de.terranova.nations.regions.grid.SettleRegion;

import java.util.*;

/**
 * Verwaltet alle Profession-bezogenen Infos einer Stadt.
 * Lädt Status, Objectives, Buildings etc. und wendet Logik an.
 */
public class ProfessionProgressManager {

    private final UUID settlementId;
    private final HashMap<String, ProfessionStatus> professionStatuses = new HashMap<>();
    private final Set<String> builtBuildings = new HashSet<>();

    public String activeProfessionId = null;

    private ProfessionProgressManager(UUID settlementId) {
        this.settlementId = settlementId;
    }

    public static ProfessionProgressManager loadForSettlement(UUID settlementId) {
        ProfessionProgressManager mgr = new ProfessionProgressManager(settlementId);

        // 1) Lade professionStatuses
        Map<String, ProfessionStatus> map = SettlementProfessionRelationDAO.getAllStatuses(settlementId.toString());
        mgr.professionStatuses.putAll(map);

        // 3) Lade gebaute Gebäude
        Set<String> built = SettlementBuildingsDAO.getBuiltBuildings(settlementId.toString());
        mgr.builtBuildings.addAll(built);

        mgr.activeProfessionId = SettlementProfessionRelationDAO.getActiveProfessionID(settlementId.toString());

        return mgr;
    }

    public UUID getSettlementId() {
        return settlementId;
    }

    /**
     * Gibt den "echten" Status einer Profession zurück, ggf.
     * dynamisch berechnet (LOCKED -> AVAILABLE), etc.
     */
    public ProfessionStatus getProfessionStatus(String professionId) {
        // Basisstatus aus DB
        ProfessionStatus base = professionStatuses.getOrDefault(professionId, ProfessionStatus.LOCKED);

        // Falls COMPLETED => direkt zurück
        if (base == ProfessionStatus.COMPLETED) return ProfessionStatus.COMPLETED;

        // Checke, ob Vorstufe bereits COMPLETED => wenn ja, kann es AVAILABLE sein
        ProfessionConfig p = ProfessionManager.getProfessionById(professionId);
        if (p == null) {
            return ProfessionStatus.LOCKED; // fallback
        }
        if (p.getLevel() > 1) {
            // Vorherige Stufe suchen
            ProfessionConfig prev = findProfession(p.type, p.getLevel() - 1);
            if (prev != null) {
                ProfessionStatus prevStat = professionStatuses.getOrDefault(prev.professionId, ProfessionStatus.LOCKED);
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

    public void setProfessionStatus(String professionId, ProfessionStatus newStatus) {
        professionStatuses.put(professionId, newStatus);
        SettlementProfessionRelationDAO.setStatus(settlementId.toString(), professionId, newStatus);
        if (newStatus == ProfessionStatus.ACTIVE) {
            pauseAllOtherActive(professionId);
            this.activeProfessionId = professionId;
        }
    }

    public long getObjectiveProgress(String objectiveId) {
        return SettlementObjectiveProgressDAO.getProgress(settlementId.toString(),objectiveId);
    }

    public boolean hasBuilding(String buildingId) {
        return builtBuildings.contains(buildingId);
    }

    public void setBuildingBuilt(String buildingId, boolean built) {
        if (built) builtBuildings.add(buildingId);
        else builtBuildings.remove(buildingId);
        SettlementBuildingsDAO.setBuilt(settlementId.toString(), buildingId, built);
    }

    public boolean completeProfession(String professionId) {
        ProfessionConfig prof = ProfessionManager.getProfessionById(professionId);
        List<ObjectiveConfig> objectives = ProfessionManager.getObjectivesForProfession(professionId);
        for (ObjectiveConfig obj : objectives) {
            if(obj.amount > getObjectiveProgress(obj.objectiveId)) {
                return false;
            }
        }
        List<BuildingConfig> buildings = ProfessionManager.getBuildingsForProfession(professionId);
        for (BuildingConfig b : buildings) {
            if (!hasBuilding(b.buildingId)) {
                return false;
            }
        }
        Optional<SettleRegion> settleOpt = RegionManager.retrieveRegion("settle", settlementId);

        if (settleOpt.isEmpty()) {
            return false;
        }

        SettleRegion settle = settleOpt.get();

        if(settle.getBank().getCredit() < prof.price){
            return false;
        }

        settle.getBank().cashTransfer("Profession " + prof.professionId, -prof.price);
        setProfessionStatus(professionId, ProfessionStatus.COMPLETED);
        return true;
    }

    /**
     * Wenn wir eine Profession aktivieren,
     * wollen wir evtl. alle anderen, die "ACTIVE" sind, auf "PAUSED" setzen.
     */
    public void pauseAllOtherActive(String exceptProfessionId) {
        for (Map.Entry<String, ProfessionStatus> e : professionStatuses.entrySet()) {
            if (e.getKey().equals(exceptProfessionId)) continue;
            if (e.getValue() == ProfessionStatus.ACTIVE) {
                setProfessionStatus(e.getKey(), ProfessionStatus.PAUSED);
            }
        }
    }

    public int getScore() {
        int score = 0;
        for (Map.Entry<String, ProfessionStatus> e : professionStatuses.entrySet()) {
            if (e.getValue() == ProfessionStatus.COMPLETED) {
                score += ProfessionManager.getProfessionById(e.getKey()).score;
            }
        }
        return score;
    }

    private ProfessionConfig findProfession(String type, int level) {
        for (ProfessionConfig pr : ProfessionManager.getProfessionsByType(type)) {
            if (pr.getLevel() == level) return pr;
        }
        return null;
    }

    private boolean professionIdEquals(String a, String b) {
        return (a != null && a.equals(b));
    }
}
