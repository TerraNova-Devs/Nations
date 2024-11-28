package de.terranova.nations.regions.base;

import de.terranova.nations.regions.SettleManager;
import de.terranova.nations.regions.grid.SettleRegionType;
import de.terranova.nations.worldguard.math.Vectore2;
import org.bukkit.entity.Player;

import java.util.UUID;

public abstract class GridRegionType extends RegionType {

    protected int claims;
    protected final Vectore2 location;

    public GridRegionType(String name, UUID id, String type, Vectore2 location) {
        super(name, id, type);
        this.location = location;
    }

    @Override
    public abstract void remove();

    public static void registerType() {
        RegionType.registerRegionType("grid", new RegionCreator() {
            @Override
            public RegionType create(String name, Player player, UUID id, Vectore2 location) {
                return new SettleRegionType(name, player);
            }

            @Override
            public boolean conditionCheck(Player player, String name) {
                // Implement specific condition checks for GridRegionType
                return true;
            }
        });
    }


    public abstract int getMaxClaims();



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
