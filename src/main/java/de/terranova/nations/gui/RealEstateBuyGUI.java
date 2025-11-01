package de.terranova.nations.gui;

import de.terranova.nations.regions.modules.realEstate.RealEstateAgent;
import de.mcterranova.terranovaLib.utils.Chat;
import de.mcterranova.terranovaLib.roseGUI.RoseGUI;
import de.mcterranova.terranovaLib.roseGUI.RoseItem;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.jetbrains.annotations.NotNull;

public class RealEstateBuyGUI extends RoseGUI {

  RealEstateAgent agent;
  boolean isOffer;

  public RealEstateBuyGUI(@NotNull Player p, RealEstateAgent agent, boolean isOffer) {
    super(p, "re-buy", Chat.cottonCandy(agent.getRegion().getName()), 3);
    this.agent = agent;
    this.isOffer = isOffer;
  }

  @Override
  public void onOpen(InventoryOpenEvent event) {

    RoseItem fillerDark =
        new RoseItem.Builder()
            .showTooltip(false)
            .material(Material.BLACK_STAINED_GLASS_PANE)
            .build();
    fillGui(fillerDark);

    Material material = Material.RED_STAINED_GLASS_PANE;
    Component lore = null;

    if (!isOffer && agent.isForBuy()) {
      material = Material.SLIME_BLOCK;
      lore = Chat.cottonCandy(agent.getBuyPrice() + " Silber");
    } else if (isOffer && agent.hasOffer(player) && agent.getOfferType().equals("buy")) {
      material = Material.SLIME_BLOCK;
      lore = Chat.cottonCandy(agent.getOfferAmount() + " Silber");
    } else {
      material = Material.SLIME_BLOCK;
      lore = Chat.errorFade("Error");
    }

    RoseItem buy =
        new RoseItem.Builder()
            .material(material)
            .displayName(Chat.cottonCandy("Buy " + agent.getRegion().getName()))
            .addLore(lore)
            .build()
            .onClick(
                e -> {
                  if (agent.isForBuy() && !isOffer) {
                    player.performCommand("re buy " + agent.getRegion().getName());
                    player.playSound(player.getLocation(), Sound.ITEM_GOAT_HORN_SOUND_4, 1f, 1f);
                    player.closeInventory();
                  } else if (agent.hasOffer(player)
                      && agent.getOfferType().equals("buy")
                      && isOffer) {
                    agent.acceptOffer(player);
                    player.playSound(player.getLocation(), Sound.ITEM_GOAT_HORN_SOUND_4, 1f, 1f);
                    player.closeInventory();
                  } else {
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
                  }
                });
    if(!isOffer) addItem(11, buy);
    if(isOffer && agent.offeredType.equals("buy")) addItem(13, buy);

    material = Material.RED_STAINED_GLASS_PANE;
    lore = null;

    if (!isOffer && agent.isForRent()) {
      material = Material.SLIME_BLOCK;
      lore = Chat.cottonCandy(agent.getRentPrice() + " Silber");
    } else if (isOffer && agent.hasOffer(player) && agent.getOfferType().equals("rent")) {
      material = Material.SLIME_BLOCK;
      lore = Chat.cottonCandy(agent.getOfferAmount() + " Silber");
    } else {
      lore = Chat.errorFade("ERROR");
    }

    RoseItem rent =
        new RoseItem.Builder()
            .material(material)
            .displayName(Chat.cottonCandy("Rent " + agent.getRegion().getName()))
            .addLore(lore)
            .build()
            .onClick(
                e -> {
                  if (agent.isForRent() && !isOffer) {
                    player.performCommand("re rent " + agent.getRegion().getName());
                    player.playSound(player.getLocation(), Sound.ITEM_GOAT_HORN_SOUND_4, 1f, 1f);
                    player.closeInventory();
                  } else if (agent.getOfferType().equals("rent")) {
                    agent.acceptOffer(player);
                    player.playSound(player.getLocation(), Sound.ITEM_GOAT_HORN_SOUND_4, 1f, 1f);
                    player.closeInventory();
                  } else {
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
                  }
                });

    if(!isOffer)addItem(15, rent);
    if(isOffer && agent.offeredType.equals("rent")) addItem(13, rent);
  }
}
