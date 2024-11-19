package de.terranova.nations.settlements.level;

import java.io.Serializable;

public class Objective implements Serializable {

    int score;
    int silver;

    int objective_a;
    String material_a;

    int objective_b;
    String material_b;

    int objective_c;
    String material_c;


    public Objective(int score, int silver, int objective_a, int objective_b, int objective_c, String material_a, String material_b, String material_c) {
        this.score = score;
        this.objective_a = objective_a;
        this.objective_b = objective_b;
        this.objective_c = objective_c;
        this.material_a = material_a;
        this.material_b = material_b;
        this.material_c = material_c;
        this.silver = silver;
    }

    public Objective() {

    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getObjective_a() {
        return objective_a;
    }

    public void setObjective_a(int objective_a) {
        this.objective_a = objective_a;
    }

    public String getMaterial_a() {
        return material_a;
    }

    public void setMaterial_a(String material_a) {
        this.material_a = material_a;
    }

    public int getObjective_b() {
        return objective_b;
    }

    public void setObjective_b(int objective_b) {
        this.objective_b = objective_b;
    }

    public String getMaterial_b() {
        return material_b;
    }

    public void setMaterial_b(String material_b) {
        this.material_b = material_b;
    }

    public int getObjective_c() {
        return objective_c;
    }

    public void setObjective_c(int objective_c) {
        this.objective_c = objective_c;
    }

    public String getMaterial_c() {
        return material_c;
    }

    public void setMaterial_c(String material_c) {
        this.material_c = material_c;
    }

    public int getSilver() {
        return silver;
    }

    public void setSilver(int silver) {
        this.silver = silver;
    }
}
