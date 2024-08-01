package de.terranova.nations.settlements;

import de.terranova.nations.NationsPlugin;
import de.terranova.nations.gui.TownGUI;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.api.util.DataKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

@TraitName("nations-settlement-uuid")
public class SettlementTrait extends Trait {

    NationsPlugin plugin;

    @Persist("nations-settlement-uuid")
    UUID settlement_uuid;

    public SettlementTrait() {
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
        if(event.getNPC() != this.getNPC()) return;
        Player player = event.getClicker().getPlayer();
        assert player != null;
        player.sendMessage("UUID:" +event.getNPC().getOrAddTrait(SettlementTrait.class).getUUID());
        new TownGUI(player).open();
    }

    public UUID getUUID() {
        return settlement_uuid;
    }

    public void setUUID(UUID uuid) {
        settlement_uuid = uuid;
    }

}
