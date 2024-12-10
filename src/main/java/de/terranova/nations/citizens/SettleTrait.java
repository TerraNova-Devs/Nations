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

    NationsPlugin plugin;

    @Persist("nations-settlement-uuid")
    UUID settlement_uuid;

    public SettleTrait() {
        super("nations-settlement-uuid");
        plugin = JavaPlugin.getPlugin(NationsPlugin.class);
    }

    public void load(DataKey key) {
        settlement_uuid = UUID.fromString(key.getString("settlement_uuid"));
    }

    public void save(DataKey key) {
        key.setString("settlement_uuid", settlement_uuid.toString());
    }

    @EventHandler
    public void onRightClickNPC(NPCRightClickEvent event) {
        System.out.println("0");
        if (event.getNPC() != this.getNPC()) return;
        System.out.println("1");
        Player player = event.getClicker().getPlayer();
        if (player == null) return;
        System.out.println("2");
        if (!player.hasPermission("nations.menu")) return;
        System.out.println("3");

        Optional<SettleRegionType> osettle = RegionManager.retrieveRegion("settle", settlement_uuid);
        if(osettle.isEmpty()) return;
        System.out.println("4");
        new TownGUI(player, osettle.get()).open();
    }

    public UUID getUUID() {
        return settlement_uuid;
    }

    public void setUUID(UUID uuid) {
        settlement_uuid = uuid;
    }

}
