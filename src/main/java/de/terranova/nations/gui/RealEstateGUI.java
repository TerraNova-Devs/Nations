package de.terranova.nations.gui;

import de.terranova.nations.utils.Chat;
import de.terranova.nations.utils.roseGUI.RoseGUI;
import de.terranova.nations.utils.roseGUI.RoseItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.jetbrains.annotations.NotNull;

public class RealEstateGUI extends RoseGUI {


    public RealEstateGUI(@NotNull Player player) {
        super(player, "hanseecke_3", Chat.cottonCandy("Hanseecke_3"), 6);
    }

    @Override
    public void onOpen(InventoryOpenEvent event) {

        RoseItem blue = new RoseItem.Builder().material(Material.BLUE_STAINED_GLASS_PANE).displayName("").build();
        RoseItem cyan = new RoseItem.Builder().material(Material.CYAN_STAINED_GLASS_PANE).displayName("").build();
        fillGui(cyan);
        addItem(blue, 0, 1, 7, 8, 9, 17, 18, 26, 27, 35, 36, 44, 45, 46, 47, 53, 54);

        RoseItem worth = new RoseItem.Builder().material(Material.GOLD_BLOCK).displayName("3000 S").build();
        addItem(worth, 13);

        RoseItem triggerBuy = new RoseItem.Builder().material(Material.GOLD_BLOCK).displayName("3000 S").build();
        RoseItem percentBuy = new RoseItem.Builder().material(Material.GOLD_BLOCK).displayName("3000 S").build();
        addItem(triggerBuy, 20);
        addItem(percentBuy, 29);

        RoseItem triggerRent = new RoseItem.Builder().material(Material.GOLD_BLOCK).displayName("3000 S").build();
        RoseItem percentRent = new RoseItem.Builder().material(Material.GOLD_BLOCK).displayName("3000 S").build();
        addItem(triggerRent, 31);
        addItem(percentRent, 40);

        RoseItem triggerLease = new RoseItem.Builder().material(Material.GOLD_BLOCK).displayName("3000 S").build();
        RoseItem percentLease = new RoseItem.Builder().material(Material.GOLD_BLOCK).displayName("3000 S").build();
        addItem(triggerLease, 24);
        addItem(percentLease, 33);

    }
}
