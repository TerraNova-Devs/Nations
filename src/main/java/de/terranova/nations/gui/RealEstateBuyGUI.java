package de.terranova.nations.gui;

import de.terranova.nations.regions.modules.realEstate.RealEstateAgent;
import de.terranova.nations.utils.Chat;
import de.terranova.nations.utils.roseGUI.RoseGUI;
import de.terranova.nations.utils.roseGUI.RoseItem;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.jetbrains.annotations.NotNull;

public class RealEstateBuyGUI extends RoseGUI {

    RealEstateAgent agent;

    public RealEstateBuyGUI(@NotNull Player player,RealEstateAgent agent) {
        super(player, "re-buy", Chat.cottonCandy(agent.getRegion().getName()), 3);
        this.agent = agent;
    }

    @Override
    public void onOpen(InventoryOpenEvent event) {

        RoseItem fillerDark = new RoseItem.Builder()
                .showTooltip(false)
                .material(Material.BLACK_STAINED_GLASS_PANE)
                .build();
        fillGui(fillerDark);

        RoseItem buy = new RoseItem.Builder()
                .material(agent.isForBuy() ? Material.SLIME_BLOCK : Material.RED_STAINED_GLASS_PANE)
                .displayName("Buy "  + agent.getRegion().getName())
                .addLore(agent.isForBuy() ? Chat.cottonCandy(agent.getBuyPrice() + " Silber") : null)
                .build()
                .onClick(e -> {
                    if(agent.isForBuy()) {
                        player.performCommand("re buy " + agent.getRegion().getName());
                        player.playSound(player.getLocation(), Sound.ITEM_GOAT_HORN_SOUND_4, 1f, 1f);
                        player.closeInventory();
                    } else {
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
                    }

                });

        addItem(11, buy);

        RoseItem rent = new RoseItem.Builder()
                .material(agent.isForRent() ? Material.HONEY_BLOCK : Material.RED_STAINED_GLASS_PANE)
                .displayName("Rent " + agent.getRegion().getName())
                .addLore(agent.isForRent() ? Chat.cottonCandy(agent.getRentPrice() + " Silber") : null)
                .build()
                .onClick(e -> {
                    if(agent.isForRent()) {
                        player.performCommand("re rent " + agent.getRegion().getName());
                        player.playSound(player.getLocation(), Sound.ITEM_GOAT_HORN_SOUND_4, 1f, 1f);
                        player.closeInventory();
                    } else {
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
                    }


                });

        addItem(15, rent);
    }
}
