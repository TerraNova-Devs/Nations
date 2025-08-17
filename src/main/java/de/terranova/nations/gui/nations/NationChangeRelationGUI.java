package de.terranova.nations.gui.nations;

import de.terranova.nations.NationsPlugin;
import de.terranova.nations.nations.Nation;
import de.terranova.nations.nations.NationRelationType;
import de.mcterranova.terranovaLib.utils.Chat;
import de.mcterranova.terranovaLib.roseGUI.RoseGUI;
import de.mcterranova.terranovaLib.roseGUI.RoseItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.codehaus.plexus.util.StringUtils;

public class NationChangeRelationGUI extends RoseGUI {
  private final Nation nation;
  private final Nation targetNation;

  public NationChangeRelationGUI(Player player, Nation nation, Nation targetNation) {
    super(
        player,
        "change-relation-gui",
        Chat.blueFade("<b>Beziehung mit " + targetNation.getName()),
        3);
    this.nation = nation;
    this.targetNation = targetNation;
  }

  @Override
  public void onOpen(InventoryOpenEvent event) {
    fillGui(
        new RoseItem.Builder().material(Material.BLACK_STAINED_GLASS_PANE).displayName("").build());

    // Ally Item
    RoseItem allyItem =
        new RoseItem.Builder()
            .material(Material.EMERALD)
            .displayName(Chat.greenFade("<b>Setze zu Verbündeten"))
            .build();

    // Neutral Item
    RoseItem neutralItem =
        new RoseItem.Builder()
            .material(Material.GOLD_INGOT)
            .displayName(Chat.yellowFade("<b>Setze zu Neutral"))
            .build();

    // Enemy Item
    RoseItem enemyItem =
        new RoseItem.Builder()
            .material(Material.REDSTONE)
            .displayName(Chat.redFade("<b>Setze zu Feind"))
            .build();

    // Add items to the GUI
    addItem(11, allyItem);
    addItem(13, neutralItem);
    addItem(15, enemyItem);

    // Set click actions
    allyItem.onClick(e -> setRelation(NationRelationType.ALLY));
    neutralItem.onClick(e -> setRelation(NationRelationType.NEUTRAL));
    enemyItem.onClick(e -> setRelation(NationRelationType.ENEMY));
  }

  private void setRelation(NationRelationType relationType) {
    nation.setRelation(targetNation.getId(), relationType);
    NationsPlugin.nationManager.saveNation(nation);
    player.sendMessage(
        Chat.cottonCandy(
            "Beziehung mit "
                + StringUtils.capitalise(targetNation.getName())
                + " zu "
                + relationType.name()
                + " gesetzt."));
    player.closeInventory();
  }

  @Override
  public void onClose(InventoryCloseEvent event) {
    // No special action needed on close
  }
}
