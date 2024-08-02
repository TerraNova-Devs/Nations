package de.terranova.nations.gui;

import de.mcterranova.bona.lib.chat.Chat;
import mc.obliviate.inventory.Gui;
import mc.obliviate.inventory.Icon;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class TownSettingsGUI extends Gui {

    public TownSettingsGUI(Player player) {
        super(player, "town-settings-gui", Chat.blueFade("Settings Menu"), 5);
    }

    @Override
    public void onOpen(InventoryOpenEvent event) {
        ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta mfiller = filler.getItemMeta();
        mfiller.displayName(Chat.stringToComponent(""));
        filler.setItemMeta(mfiller);
        fillGui(filler);

        ItemStack snow = new ItemStack(Material.SNOW);
        ItemMeta msnow = snow.getItemMeta();
        List<Component> lsnow = new ArrayList<>();
        lsnow.add(Chat.cottonCandy("<i>Soll sich Schnee in deinem Gebiet bilden?"));
        lsnow.add(Chat.cottonCandy(String.format("<i>Currently: %s", "enabled")));
        msnow.lore(lsnow);
        msnow.displayName(Chat.blueFade("Flag: snowfall"));
        snow.setItemMeta(msnow);
        Icon isnow = new Icon(snow);



        isnow.onClick(e -> {

        });
    }

    @Override
    public void onClose(InventoryCloseEvent event) {

    }

}
