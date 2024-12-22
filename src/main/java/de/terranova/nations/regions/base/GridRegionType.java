package de.terranova.nations.regions.base;

import de.terranova.nations.worldguard.RegionClaimFunctions;
import de.terranova.nations.worldguard.math.Vectore2;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class GridRegionType extends RegionType {

    public static List<Vectore2> locationCache = new ArrayList<>();
    protected final Vectore2 location;
    protected int claims;

    public GridRegionType(String name, UUID id, String type, Vectore2 location) {
        super(name, id, type);
        this.location = location;
        this.claims = RegionClaimFunctions.getClaimAnzahl(this.id);
        locationCache.add(location);
    }

    public abstract int getMaxClaims();

    @Override
    public final void onCreation(Player p) {
        this.region = RegionClaimFunctions.createClaim(name, p, this.id);
        onGridCreation(p);
    }

    public void onGridCreation(Player p) {

    }

    @Override
    public final void onRemove() {
        GridRegionType.locationCache.remove(this.location);
        RegionTypeDatabase database = new RegionTypeDatabase(getId());
        database.removeGridRegion();
        onGridRemove();
    }

    @Override
    public void onRename(String name) {
        RegionTypeDatabase database = new RegionTypeDatabase(getId());
        database.updateGridRegionName(name);
    }

    public void onGridRemove() {

    }

    @Override
    public final void dataBaseCall() {
        RegionTypeDatabase database = new RegionTypeDatabase(getId());
        database.insertGridRegion(this);
    }

    public Vectore2 getLocation() {
        return this.location;
    }

    public int getClaims() {
        return this.claims;
    }

    public void setClaims(int claims) {
        if (claims <= getMaxClaims()) {
            this.claims = claims;
        } else {
            throw new IllegalArgumentException("Claims cannot exceed maximum allowed claims: " + getMaxClaims());
        }
    }
}
