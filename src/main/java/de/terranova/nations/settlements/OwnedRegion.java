package de.terranova.nations.settlements;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.entity.Player;

import java.util.UUID;

public abstract class OwnedRegion {

    public UUID id;
    public String name;
    public ProtectedRegion region;

    public OwnedRegion(String name, UUID id) {
        this.id = id;
        this.name = name;
    }

}
