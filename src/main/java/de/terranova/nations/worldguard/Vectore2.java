package de.terranova.nations.worldguard;

import org.bukkit.Location;

public class Vectore2 {

    public double x;
    public double z;

    public Vectore2() {
        this.z = 0.0f;
        this.x = 0.0f;
    }

    public Vectore2(double x, double z) {
        this.x = x;
        this.z = z;
    }

    public Vectore2(String s) {
        String[] parts = s.split(",");
        this.x = Double.parseDouble(parts[1]);
        this.z = Double.parseDouble(parts[0]);
    }

    public Vectore2(Location loc) {
        this.x = loc.x();
        this.z = loc.z();
    }

    public boolean equals(Vectore2 other) {
        return (this.z == other.z && this.x == other.x);
    }

    public String asString(){
        return this.x + "," +  this.z;
    }

    public Vectore2 fromString(String s){
        String[] parts = s.split(",");
        return new Vectore2(Double.parseDouble(parts[1]),Double.parseDouble(parts[0]) );
    }

}
