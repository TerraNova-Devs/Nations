package de.terranova.nations.gui;

import de.mcterranova.bona.lib.chat.Chat;
import de.terranova.nations.settlements.TownSkins;
import mc.obliviate.inventory.Gui;
import mc.obliviate.inventory.Icon;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class TownGUI extends Gui {
  String i = "10";


  public TownGUI(Player player) {
    super(player, "town-gui", Chat.returnBlueFade("Town Menu"), 4);
  }

  @Override
  public void onOpen(InventoryOpenEvent event) {

    ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
    ItemMeta mfiller = filler.getItemMeta();
    mfiller.displayName(Chat.stringToComponent(""));
    filler.setItemMeta(mfiller);
    fillGui(filler);

    ItemStack level = new ItemStack(Material.NETHER_STAR);
    level.setAmount(7);
    ItemStack skins = new ItemStack(Material.PLAYER_HEAD);
    ItemStack score = new ItemStack(Material.GOLD_INGOT);
    ItemStack upgrades = new ItemStack(Material.IRON_INGOT);
    ItemStack farm = new ItemStack(Material.GRASS_BLOCK);

    ItemMeta mlevel = level.getItemMeta();
    ItemMeta mskins = skins.getItemMeta();
    ItemMeta mscore = score.getItemMeta();
    ItemMeta mupgrades = upgrades.getItemMeta();
    ItemMeta mfarm = farm.getItemMeta();

    List<Component> llevel = new ArrayList<>();
    llevel.add(Chat.stringToComponent("<italic><color:#5EE118>Vorteile Level 8:</italic>"));
    llevel.add(Chat.stringToComponent("<italic><color:#5EE118>7->8 Claims</italic>"));
    mlevel.lore(llevel);
    List<Component> lskins = new ArrayList<>();
    lskins.add(Chat.stringToComponent("<italic><color:#E1679C>Hier klicken um den Skin zu ändern.</italic>"));
    mskins.lore(lskins);
    List<Component> lscore = new ArrayList<>();
    lscore.add(Chat.stringToComponent("<italic><color:#E1679C>Hier klicken fuer mehr infos.</italic>"));
    mscore.lore(lscore);
    List<Component> lupgrades = new ArrayList<>();
    lupgrades.add(Chat.stringToComponent("<italic><color:#E1679C>Hier klicken um die Stadt zu verbessern.</italic>"));
    mupgrades.lore(lupgrades);
    List<Component> lfarm = new ArrayList<>();
    lfarm.add(Chat.stringToComponent("<italic><color:#E1679C>Hier klicken um die Farmwelt zu betreten.</italic>"));
    mfarm.lore(lfarm);

    mlevel.displayName(Chat.returnRedFade("Stadtlevel"));
    mskins.displayName(Chat.returnYellowFade("Skins"));
    mscore.displayName(Chat.returnYellowFade("Score"));
    mupgrades.displayName(Chat.returnYellowFade("Upgrades"));
    mfarm.displayName(Chat.returnYellowFade("Farm"));

    level.setItemMeta(mlevel);
    skins.setItemMeta(mskins);
    score.setItemMeta(mscore);
    upgrades.setItemMeta(mupgrades);
    farm.setItemMeta(mfarm);

    Icon iskins = new Icon(skins);
    addItem(13, level);
    addItem(19, iskins);

    iskins.onClick(e -> {
      int rowsSkins = 3;
      if (TownSkins.values().length >= 8) {
        rowsSkins = 4;
      } else if (TownSkins.values().length >= 15) {
        rowsSkins = 5;
      } else if (TownSkins.values().length >= 22) {
        rowsSkins = 6;
      }

      new TownAdmSkinGUI(player, rowsSkins).open();
    });
    addItem(21, score);
    addItem(23, upgrades);
    addItem(25, farm);
  }
}
