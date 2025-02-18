package de.terranova.nations.regions.access;

public enum PropertyAccessLevel {
    OWNER(100),
    MEMBER(10);

    int weight;

    PropertyAccessLevel(int weight) {
        this.weight = weight;
    }

    public int getWeight() {
        return weight;
    }
}
