package de.terranova.nations.regions.modules.access;

public enum TownAccessLevel {
    ADMIN(1000000),
    MAJOR(10000),
    VICE(1000),
    COUNCIL(100),
    CITIZEN(10),
    TRUSTED(1);

    int weight;

    TownAccessLevel(int weight) {
        this.weight = weight;
    }

    public int getWeight() {
        return weight;
    }

}
