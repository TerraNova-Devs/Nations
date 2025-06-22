package de.terranova.nations.worldguard.math;

import com.sk89q.worldedit.math.BlockVector2;
import net.pl3x.map.core.markers.Point;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.Collection;

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
        this.x = Double.parseDouble(parts[0]);
        this.z = Double.parseDouble(parts[1]);
    }

    public Vectore2(Location loc) {
        this.x = loc.x();
        this.z = loc.z();
    }

    public Vectore2(BlockVector2 vec) {
        this.x = vec.x();
        this.z = vec.z();
    }

    public static Collection<Vectore2> fromBlockVectorList(Collection<BlockVector2> vectors) {
        Collection<Vectore2> output = new ArrayList<Vectore2>();
        for (BlockVector2 vector : vectors) {
            output.add(new Vectore2(vector.x(), vector.z()));
        }
        return output;
    }

    public boolean equals(Vectore2 other) {
        return (this.z == other.z && this.x == other.x);
    }

    public String asString() {
        return this.x + "," + this.z;
    }

    public Point asPoint() {
        return new Point((int) this.x, (int) this.z);
    }

    public Location asLocation() {
        return new Location(Bukkit.getWorld("world"), this.x, 255, this.z);
    }

    public Vectore2 fromString(String s) {
        String[] parts = s.split(",");
        return new Vectore2(Double.parseDouble(parts[1]), Double.parseDouble(parts[0]));
    }

}
