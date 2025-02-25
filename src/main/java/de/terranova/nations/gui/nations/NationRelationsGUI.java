package de.terranova.nations.gui.nations;

import de.mcterranova.terranovaLib.roseGUI.RoseGUI;
import de.mcterranova.terranovaLib.roseGUI.RoseItem;
import de.mcterranova.terranovaLib.utils.Chat;
import de.terranova.nations.NationsPlugin;
import de.terranova.nations.nations.Nation;
import de.terranova.nations.nations.NationRelationType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

import java.util.Map;
import java.util.UUID;

public class NationRelationsGUI extends RoseGUI {
    private final Nation nation;

    public NationRelationsGUI(Player player, Nation nation) {
        super(player, "nation-relations-gui", Chat.blueFade("<b>Nation Relations"), 5);
        this.nation = nation;
    }

    @Override
    public void onOpen(InventoryOpenEvent event) {
        fillGui(new RoseItem.Builder().material(Material.BLACK_STAINED_GLASS_PANE).displayName("").build());

        int slot = 0;
        for (Map.Entry<UUID, NationRelationType> entry : nation.getRelations().entrySet()) {
            UUID otherNationId = entry.getKey();
            NationRelationType relationType = entry.getValue();

            Nation otherNation = NationsPlugin.nationManager.getNation(otherNationId);
            if (otherNation != null) {
                RoseItem relationItem = new RoseItem.Builder()
                        .material(Material.PAPER)
                        .displayName(otherNation.getName())
                        .addLore("Relation: " + relationType.name())
                        .addLore("Click to change relation")
                        .build();

                addItem(slot++, relationItem);

                // Set click action to change relation
                relationItem.onClick(e -> {
                    // Open a GUI or prompt to change relation
                    new NationChangeRelationGUI(player, nation, otherNation).open();
                });
            }
        }
    }

    @Override
    public void onClose(InventoryCloseEvent event) {
        // No special action needed on close
    }
}
