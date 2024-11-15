package de.terranova.nations.settlements;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public enum AccessLevel {
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
