package de.terranova.paperweight.nations.worldguard;

public class Vectore2 {
    // Members
    public double x;
    public double z;

    // Constructors
    public Vectore2() {
        this.x = 0.0f;
        this.z = 0.0f;
    }

    public Vectore2(double z, double x) {
        this.x = x;
        this.z = z;
    }

    public boolean equals(Vectore2 other) {
        return (this.x == other.x && this.z == other.z);
    }



}
