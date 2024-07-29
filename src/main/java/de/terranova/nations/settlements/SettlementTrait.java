package de.terranova.nations.settlements;

import de.mcterranova.bona.lib.chat.Chat;
import de.terranova.nations.NationsPlugin;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.api.util.DataKey;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.java.JavaPlugin;

@TraitName("nations-settlement-uuid")
public class SettlementTrait extends Trait {


    NationsPlugin plugin = null;
    String settlement_uuid = "";
    // see the 'Persistence API' section
    @Persist("nations-settlement-uuid")
    boolean automaticallyPersistedSetting = false;

    public SettlementTrait() {
        super("nations-settlement-uuid");
        plugin = JavaPlugin.getPlugin(NationsPlugin.class);
    }

    public void load(DataKey key) {
        settlement_uuid = key.getString("uuid");
    }

    // Save settings for this NPC (optional). These values will be persisted to the Citizens saves file
    public void save(DataKey key) {
        key.setString("uuid", "uuid");
    }

    @EventHandler
    public void click(net.citizensnpcs.api.event.NPCRightClickEvent event) {
        event.getClicker().sendMessage(Chat.stringToComponent(settlement_uuid));

    }

    public String getUUID() {
        return settlement_uuid;
    }

    public void setUUID(String uuid) {
        settlement_uuid = uuid;
    }

}
