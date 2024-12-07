package de.terranova.nations.regions.base;

import de.terranova.nations.regions.SettleManager;
import de.terranova.nations.regions.access.Access;
import de.terranova.nations.regions.access.AccessLevel;
import de.terranova.nations.regions.bank.Bank;
import de.terranova.nations.regions.grid.SettleRegionType;
import de.terranova.nations.regions.npc.NPCr;
import de.terranova.nations.regions.rank.Rank;
import de.terranova.nations.regions.rank.RankObjective;
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

    public abstract int getMaxClaims();

    @Override
    public void postInit(Player p) {

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
