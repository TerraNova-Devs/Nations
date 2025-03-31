package de.terranova.nations.regions.rank;

import java.io.Serializable;

public class RankObjective implements Serializable {

    int score;
    int silver;

    public RankObjective(int score, int silver) {
        this.score = score;
        this.silver = silver;
    }

    public RankObjective() {

    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getSilver() {
        return silver;
    }

    public void setSilver(int silver) {
        this.silver = silver;
    }
}
