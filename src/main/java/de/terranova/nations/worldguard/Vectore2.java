package de.terranova.nations.worldguard;

public class Vectore2 {
    // Members
    public double z;
    public double x;

    // Constructors
    public Vectore2() {
        this.z = 0.0f;
        this.x = 0.0f;
    }

    public Vectore2(double x, double z) {
        this.z = z;
        this.x = x;
    }

    public boolean equals(Vectore2 other) {
        return (this.z == other.z && this.x == other.x);
    }


}
