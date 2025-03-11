package de.terranova.nations.professions;

public class ProfessionObjective {
    private final int objectiveId;
    private final int professionId;
    private final String action;  // "FISH", "HARVEST", "DESTROY", etc.
    private final String object;  // z.B. "COD", "WHEAT", "STONE"
    private final long amount;    // Wie viel benötigt man?

    public ProfessionObjective(int objectiveId, int professionId, String action, String object, long amount) {
        this.objectiveId = objectiveId;
        this.professionId = professionId;
        this.action = action;
        this.object = object;
        this.amount = amount;
    }

    public int getObjectiveId() {
        return objectiveId;
    }

    public int getProfessionId() {
        return professionId;
    }

    public String getAction() {
        return action;
    }

    public String getObject() {
        return object;
    }

    public long getAmount() {
        return amount;
    }
}
