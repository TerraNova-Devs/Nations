package de.terranova.nations.professions;

public class Building {
    private final int buildingId;
    private final int professionId;
    private final String name;
    private final String description;

    public Building(int buildingId, int professionId, String name, String description) {
        this.buildingId = buildingId;
        this.professionId = professionId;
        this.name = name;
        this.description = description;
    }

    public int getBuildingId() {
        return buildingId;
    }

    public int getProfessionId() {
        return professionId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
