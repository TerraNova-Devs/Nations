package de.terranova.nations.gui.nations;

import de.terranova.nations.nations.Nation;
import de.terranova.nations.nations.SettlementRank;
import de.terranova.nations.regions.RegionManager;
import de.terranova.nations.regions.grid.SettleRegion;
import de.mcterranova.terranovaLib.utils.Chat;
import de.mcterranova.terranovaLib.roseGUI.RoseGUI;
import de.mcterranova.terranovaLib.roseGUI.RoseItem;
import de.mcterranova.terranovaLib.roseGUI.RosePagination;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.codehaus.plexus.util.StringUtils;

public class NationSettlementsGUI extends RoseGUI {
  private final Nation nation;
  RosePagination pagination;

  public NationSettlementsGUI(Player player, Nation nation) {
    super(player, "nation-settlements-gui", Chat.blueFade("<b>Nationsstädte"), 6);
    this.nation = nation;
    this.pagination = new RosePagination(this);
  }

  @Override
  public void onOpen(InventoryOpenEvent event) {
    pagination.registerPageSlotsBetween(10, 16);
    pagination.registerPageSlotsBetween(19, 25);
    pagination.registerPageSlotsBetween(28, 34);
    pagination.registerPageSlotsBetween(37, 43);

    for (Map.Entry<UUID, SettlementRank> settlement : nation.getSettlements().entrySet()) {
      UUID settlementId = settlement.getKey();
      Optional<SettleRegion> settle = RegionManager.retrieveRegion("settle", settlementId);

      if (settle.isPresent()) {
        String displayName = StringUtils.capitalise(settle.get().getName());
        if (settlement.getValue().equals(SettlementRank.CAPITAL)) {
          displayName += " (Hauptstadt)";
        }
        RoseItem settlementItem =
            new RoseItem.Builder()
                .material(Material.HEAVY_CORE)
                .displayName(Chat.blueFade(displayName))
                .addLore(Chat.cottonCandy("Level: " + settle.get().getRank().getLevel()))
                .addLore(
                    Chat.cottonCandy(
                        "Bürgermeister: "
                            + Bukkit.getOfflinePlayer(settle.get().getAccess().getMajor())
                                .getName()))
                .build();

        pagination.addItem(settlementItem);
      }
    }
    pagination.update();

    addNavigationItems();
  }

  @Override
  public void onClose(InventoryCloseEvent event) {
    // No special action needed on close
  }

  private void addNavigationItems() {
    // Previous Page
    RoseItem previousPage =
        new RoseItem.Builder()
            .material(Material.ARROW)
            .displayName(Component.text("§eVorherige Seite"))
            .build()
            .onClick(
                (InventoryClickEvent e) -> {
                  if (!pagination.isFirstPage()) {
                    pagination.goPreviousPage();
                    pagination.update();
                  }
                });
    addItem(48, previousPage);

    // Next Page
    RoseItem nextPage =
        new RoseItem.Builder()
            .material(Material.ARROW)
            .displayName(Component.text("§eNächste Seite"))
            .build()
            .onClick(
                (InventoryClickEvent e) -> {
                  if (!pagination.isLastPage()) {
                    pagination.goNextPage();
                    pagination.update();
                  }
                });
    addItem(50, nextPage);

    // Back Button
    RoseItem back =
        new RoseItem.Builder()
            .material(Material.SPECTRAL_ARROW)
            .displayName(Chat.yellowFade("<b>Zurück</b>"))
            .build();
    back.onClick(
        e -> {
          new NationGUI(player, nation).open();
        });

    addItem(45, back);

    fillBorder();
  }

  private void fillBorder() {
    RoseItem filler =
        new RoseItem.Builder().material(Material.BLACK_STAINED_GLASS_PANE).displayName("").build();

    // Fill slots except for navigation buttons
    for (int i :
        new int[] {
          0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 44, 46, 47, 49, 51, 52, 53
        }) {
      addItem(i, filler);
    }
  }
}
