package org.nations.settlements;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.nations.customData.playerdata;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class settlement {

    private playerdata owner;
    private String name;
    private Location location;
    private int level;
    Hologram hologram;

    public settlement(UUID uuid, Location location, String name) {
        this.owner = new playerdata(uuid);
        this.name = name;
        this.location  = location;
        this.level = 0;
        updateHolo();
    }

    public boolean canSettle() {
        return owner.canSettle;
    }

    public void updateHolo() {

        if (hologram == null) {
            this.hologram = DHAPI.createHologram(this.name, location.add(0, 4, 0));
            updateHolo();
        }
        List<String> lines = new ArrayList<>();
        lines.add(this.name + " [" + this.level + "]");
        lines.add(this.name + "Hay");
        DHAPI.setHologramLines(hologram, lines);
    }

    public void resetHolo() {
        hologram.delete();
        updateHolo();
    }

}

