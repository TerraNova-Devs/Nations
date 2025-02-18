package de.terranova.nations.regions.access;

public enum AccessLevel {
    ADMIN(1000000),
    MAJOR(10000),
    VICE(1000),
    COUNCIL(100),
    CITIZEN(10),
    TRUSTED(1);

    int weight;

    AccessLevel(int weight) {
        this.weight = weight;
    }

    public int getWeight() {
        return weight;
    }

}
