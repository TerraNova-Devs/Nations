package de.terranova.nations.gui.nations;

import de.mcterranova.terranovaLib.roseGUI.RoseGUI;
import de.mcterranova.terranovaLib.roseGUI.RoseItem;
import de.mcterranova.terranovaLib.roseGUI.RosePagination;
import de.mcterranova.terranovaLib.utils.Chat;
import de.terranova.nations.NationsPlugin;
import de.terranova.nations.nations.Nation;
import de.terranova.nations.nations.NationRelationType;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.codehaus.plexus.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public class NationRelationsGUI extends RoseGUI {
    private final Nation nation;
    RosePagination pagination;

    public NationRelationsGUI(Player player, Nation nation) {
        super(player, "nation-relations-gui", Chat.blueFade("<b>Beziehungen"), 6);
        this.nation = nation;
        this.pagination = new RosePagination(this);
    }

    @Override
    public void onOpen(InventoryOpenEvent event) {
        pagination.registerPageSlotsBetween(10, 16);
        pagination.registerPageSlotsBetween(19, 25);
        pagination.registerPageSlotsBetween(28, 34);
        pagination.registerPageSlotsBetween(37, 43);

        for (Map.Entry<UUID, NationRelationType> entry : nation.getRelations().entrySet()) {
            UUID otherNationId = entry.getKey();
            NationRelationType relationType = entry.getValue();

            Nation otherNation = NationsPlugin.nationManager.getNation(otherNationId);
            if (otherNation != null) {
                RoseItem relationItem = new RoseItem.Builder()
                        .material(Material.PAPER)
                        .displayName(Chat.blueFade(StringUtils.capitalise(otherNation.getName())))
                        .addLore(getRelationLore(true, relationType))
                        .addLore(getRelationLore(false, otherNation.getRelation(nation.getId())))
                        .addLore(Chat.cottonCandy("Klicke um Beziehung zu ändern"))
                        .build();

                pagination.addItem(relationItem);

                // Set click action to change relation
                relationItem.onClick(e -> {
                    // Open a GUI or prompt to change relation
                    new NationChangeRelationGUI(player, nation, otherNation).open();
                });
            }
        }
        pagination.update();

        addNavigationItems();
    }

    private void addNavigationItems() {
        // Previous Page
        RoseItem previousPage = new RoseItem.Builder()
                .material(Material.ARROW)
                .displayName(Component.text("§eVorherige Seite"))
                .build()
                .onClick((InventoryClickEvent e) -> {
                    if (!pagination.isFirstPage()) {
                        pagination.goPreviousPage();
                        pagination.update();
                    }
                });
        addItem(48, previousPage);

        // Next Page
        RoseItem nextPage = new RoseItem.Builder()
                .material(Material.ARROW)
                .displayName(Component.text("§eNächste Seite"))
                .build()
                .onClick((InventoryClickEvent e) -> {
                    if (!pagination.isLastPage()) {
                        pagination.goNextPage();
                        pagination.update();
                    }
                });
        addItem(50, nextPage);

        // Back Button
        RoseItem back = new RoseItem.Builder()
                .material(Material.SPECTRAL_ARROW)
                .displayName(Chat.yellowFade("<b>Zurück</b>"))
                .build();
        back.onClick(e -> {
            new NationGUI(player, nation).open();
        });

        addItem(45, back);

        fillBorder();
    }

    private Component getRelationLore(boolean self, NationRelationType relationType) {

        if (self) {
            switch (relationType) {
                case ALLY:
                    return Chat.greenFade("Unsere Haltung: " + relationType.name());
                case NEUTRAL:
                    return Chat.yellowFade("Unsere Haltung: " + relationType.name());
                case ENEMY:
                    return Chat.redFade("Unsere Haltung: " + relationType.name());
            }
        } else {
            switch (relationType) {
                case ALLY:
                    return Chat.greenFade("Ihre Haltung: " + relationType.name());
                case NEUTRAL:
                    return Chat.yellowFade("Ihre Haltung: " + relationType.name());
                case ENEMY:
                    return Chat.redFade("Ihre Haltung: " + relationType.name());
            }
        }
        return null;
    }

    private void fillBorder() {
        RoseItem filler = new RoseItem.Builder()
                .material(Material.BLACK_STAINED_GLASS_PANE)
                .displayName("")
                .build();

        // Fill slots except for navigation buttons
        for (int i : new int[]{0,1,2,3,4,5,6,7,8,9,17,18,26,27,35,36,44, 46, 47, 49, 51, 52, 53}) {
            addItem(i, filler);
        }
    }

    @Override
    public void onClose(InventoryCloseEvent event) {
        // No special action needed on close
    }
}
