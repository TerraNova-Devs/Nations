package de.nekyia.nations.regions.access;

public enum AccessLevel {
    ADMIN(1000000),
    MAJOR(1000),
    VICE(100),
    COUNCIL(10),
    CITIZEN(1);

    int weight;

    AccessLevel(int weight) {
        this.weight = weight;
    }

    public int getWeight() {
        return weight;
    }

}
