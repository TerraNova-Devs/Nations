package de.terranova.nations.citizens;

import de.terranova.nations.NationsPlugin;
import de.terranova.nations.gui.TownGUI;
import de.terranova.nations.regions.grid.SettleRegionType;
import de.terranova.nations.regions.RegionManager;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.api.util.DataKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Optional;
import java.util.UUID;

@TraitName("nations-settlement-uuid")
public class SettleTrait extends Trait {

    @Persist("nations-settlement-uuid")
    UUID settlement_uuid;

    public SettleTrait() {
        super("nations-settlement-uuid");
    }

    public void load(DataKey key) {
        settlement_uuid = UUID.fromString(key.getString("settlement_uuid"));
        System.out.println("DEBUG data key loadet");
    }

    public void save(DataKey key) {
        key.setString("settlement_uuid", settlement_uuid.toString());
    }

    @EventHandler
    public void onRightClickNPC(NPCRightClickEvent event) {
        if (event.getNPC() != this.getNPC()) return;
        Player player = event.getClicker().getPlayer();
        if (player == null) return;
        if (!player.hasPermission("nations.menu")) return;

        Optional<SettleRegionType> osettle = RegionManager.retrieveRegion("settle", settlement_uuid);
        if(osettle.isEmpty()) return;
        new TownGUI(player, osettle.get()).open();
    }

    public UUID getUUID() {
        return settlement_uuid;
    }

    public void setUUID(UUID uuid) {
        settlement_uuid = uuid;
    }

}
