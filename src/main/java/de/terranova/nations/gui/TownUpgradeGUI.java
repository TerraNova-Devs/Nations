package de.terranova.nations.gui;

import de.mcterranova.bona.lib.chat.Chat;
import mc.obliviate.inventory.Gui;
import mc.obliviate.inventory.Icon;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class TownUpgradeGUI extends Gui {

    public TownUpgradeGUI(Player player) {
        super(player, "town-upgrade-gui", Chat.blueFade("Town Upgrade"), 6);
    }

    @Override
    public void onOpen(InventoryOpenEvent event) {

        boolean canLevelup = false;

        ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta mfiller = filler.getItemMeta();
        mfiller.displayName(Chat.stringToComponent(""));
        filler.setItemMeta(mfiller);
        fillGui(filler);


        ItemStack back = new ItemStack(Material.SPECTRAL_ARROW);
        ItemMeta mback = back.getItemMeta();
        mback.displayName(Chat.redFade("<b>Go Back</b>"));
        back.setItemMeta(mback);
        Icon iback = new Icon(back);
        addItem(45, iback);
        iback.onClick(e -> {
            new TownGUI(player).open();
        });

        ItemStack score = new ItemStack(Material.GOLD_BLOCK);
        ItemStack submit;
        if (canLevelup) {
            submit = new ItemStack(Material.EMERALD_BLOCK);
        } else {
            submit = new ItemStack(Material.REDSTONE_BLOCK);
        }
        ItemStack objective_a = new ItemStack(Material.PURPLE_WOOL);
        ItemStack objective_b = new ItemStack(Material.PURPLE_WOOL);
        ItemStack objective_c = new ItemStack(Material.PURPLE_WOOL);
        ItemStack objective_d = new ItemStack(Material.PURPLE_WOOL);

        addItem(13, score);
        addItem(40, submit);
        addItem(19, objective_a);
        addItem(21, objective_b);
        addItem(23, objective_c);
        addItem(25, objective_d);


    }

}
