package de.terranova.nations.gui;

import de.terranova.nations.professions.ProfessionManager;
import de.terranova.nations.professions.ProfessionProgressManager;
import de.terranova.nations.professions.ProfessionStatus;
import de.terranova.nations.professions.pojo.BuildingConfig;
import de.terranova.nations.professions.pojo.ObjectiveConfig;
import de.terranova.nations.professions.pojo.ProfessionConfig;
import de.terranova.nations.regions.grid.SettleRegion;
import de.terranova.nations.regions.modules.access.Access;
import de.terranova.nations.regions.modules.access.AccessLevel;
import de.terranova.nations.utils.Chat;
import de.terranova.nations.utils.roseGUI.RoseGUI;
import de.terranova.nations.utils.roseGUI.RoseItem;
import de.terranova.nations.utils.roseGUI.RosePagination;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class TownProfessionGUI extends RoseGUI {
  private final SettleRegion settle;
  private final RosePagination pagination;

  public TownProfessionGUI(Player player, SettleRegion settle) {
    super(player, "town-profession-gui", Chat.blueFade("<b>Professionen"), 6);
    this.settle = settle;
    this.pagination = new RosePagination(this);
  }

  @Override
  public void onOpen(InventoryOpenEvent event) {
    // Welche Slots für die Pagination? (max 12 Items pro Seite)
    pagination.registerPageSlots(10, 12, 14, 16, 19, 21, 23, 25, 28, 30, 32, 34);

    // Hintergrund füllen
    RoseItem filler =
        new RoseItem.Builder()
            .showTooltip(false)
            .material(Material.BLACK_STAINED_GLASS_PANE)
            .build();
    fillGui(filler);

    // Lade den ProfessionProgressManager
    ProfessionProgressManager mgr = ProfessionProgressManager.loadForSettlement(settle.getId());

    // Für jeden Professionstyp (z. B. FISHERY) zeigen wir **eine** Stufe an:
    for (String type : ProfessionManager.getProfessionTypes()) {
      // Alle Stufen 1..4 zur jeweiligen Profession, sortieren
      List<ProfessionConfig> profs = ProfessionManager.getProfessionsByType(type);
      profs.sort(Comparator.comparingInt(ProfessionConfig::getLevel));

      // Finde die erste Stufe, die NICHT completed ist
      ProfessionConfig nextProf = null;
      for (ProfessionConfig p : profs) {
        ProfessionStatus st = mgr.getProfessionStatus(p.professionId);
        if (st != ProfessionStatus.COMPLETED) {
          nextProf = p;
          break;
        }
      }

      // Falls ALLE completed sind => nimm die letzte (Stufe 4), um sie als "fertig" anzuzeigen
      if (nextProf == null && !profs.isEmpty()) {
        nextProf = profs.get(profs.size() - 1); // = Level 4
      }

      if (nextProf != null) {
        pagination.addItem(createProfessionItem(nextProf, mgr));
      } else {
        // Falls es gar keine Professionen dieses Typs gibt, setze z. B. Barrier
        pagination.addItem(
            new RoseItem.Builder()
                .material(Material.BARRIER)
                .displayName("Keine Profession-Daten vorhanden")
                .build());
      }
    }

    pagination.update();
    addNavigationItems();
  }

  @Override
  public void onClose(InventoryCloseEvent event) {
    // Keine besondere Aktion beim Schließen
  }

  /**
   * Erzeugt ein hübsches Item für die gegebene Profession, inklusive Status, benötigte Gebäude,
   * Objectives usw.
   */
  private RoseItem createProfessionItem(ProfessionConfig prof, ProfessionProgressManager mgr) {
    ProfessionStatus status = mgr.getProfessionStatus(prof.professionId);

    // Passendes Item (Angel / Spitzhacke / etc.)
    ItemStack icon = ItemStack.of(Material.getMaterial(prof.icon));
    ItemMeta meta = icon.getItemMeta();

    // Lore
    List<Component> lore = new ArrayList<>();
    lore.add(Component.text("§8§m                                     "));

    meta.displayName(
        Component.text(String.format("§b%s §7| Stufe: §b%d", prof.prettyName, prof.getLevel())));
    // Status
    lore.add(
        Component.text(
            "§7Status: " + (status == ProfessionStatus.ACTIVE ? "§a" : "§f") + status.name()));

    if (!(status == ProfessionStatus.COMPLETED)) {
      // Kosten & Score
      String costLine =
          String.format(
              "§7Kosten: §e%d §7Silber  §8|  §7Score-Bonus: §e%d", prof.price, prof.score);
      lore.add(Component.text(costLine));

      // Buildings
      List<BuildingConfig> requiredBuildings =
          ProfessionManager.getBuildingsForProfession(prof.professionId);
      if (!requiredBuildings.isEmpty()) {
        lore.add(Component.text("§6Benötigte Gebäude:"));
        for (BuildingConfig b : requiredBuildings) {
          boolean isBuilt = mgr.hasBuilding(b.buildingId);
          String bLine = (isBuilt ? "§a✔ " : "§c✖ ") + "§7" + b.name;
          lore.add(Component.text("   " + bLine));
        }
      }

      // Objectives
      List<ObjectiveConfig> objectives =
          ProfessionManager.getObjectivesForProfession(prof.professionId);
      if (!objectives.isEmpty()) {
        lore.add(Component.text("§6Ziel / Objective(s):"));
        for (ObjectiveConfig obj : objectives) {
          long current = mgr.getObjectiveProgress(obj.objectiveId);
          lore.add(
              Component.text(
                  String.format(
                      "   §7- %s §f%s: %d/%d %s",
                      obj.action,
                      obj.object,
                      current,
                      obj.amount,
                      buildProgressBar(current, obj.amount, 8))));
        }
      }
    }

    // Status-spezifische Info
    switch (status) {
      case LOCKED -> lore.add(Component.text("§cNoch gesperrt! Vorstufe nicht abgeschlossen."));
      case AVAILABLE -> lore.add(Component.text("§eKlicke, um an dieser Profession zu arbeiten."));
      case ACTIVE ->
          lore.add(
              Component.text("§aAktiv! Klicke, um die Arbeit an dieser Profession zu pausieren."));
      case PAUSED ->
          lore.add(
              Component.text("§7Pausiert! Klicke, um weiter an dieser Profession zu arbeiten."));
      case COMPLETED -> lore.add(Component.text("§aAbgeschlossen!"));
    }

    // Abschluss-Trenner
    lore.add(Component.text("§8§m                                     "));

    icon.setItemMeta(meta);

    RoseItem.Builder builder = new RoseItem.Builder().copyStack(icon);

    // Glitzern wenn ACTIVE oder COMPLETED
    if (status == ProfessionStatus.ACTIVE || status == ProfessionStatus.COMPLETED) {
      builder.isEnchanted(true);
    }

    for (int i = 0; i < lore.size(); i++) {
      builder.addLore(lore.get(i));
    }

    RoseItem item = builder.build().onClick(e -> handleProfessionClick(e, prof, status, mgr));
    return item;
  }

  private void handleProfessionClick(
      InventoryClickEvent e,
      ProfessionConfig prof,
      ProfessionStatus status,
      ProfessionProgressManager mgr) {
    e.setCancelled(true);

    Player player = (Player) e.getWhoClicked();

    if (!Access.hasAccess(
        settle.getAccess().getAccessLevel(player.getUniqueId()), AccessLevel.CITIZEN)) {
      player.sendMessage(Chat.errorFade("Du bist kein Mitglied dieser Stadt!"));
      return;
    }

    if (!Access.hasAccess(
        settle.getAccess().getAccessLevel(player.getUniqueId()), AccessLevel.VICE)) {
      player.sendMessage(
          Chat.errorFade("Du hast nicht die nötigen Rechte, um Professions zu bearbeiten!"));
      return;
    }

    if (e.isLeftClick()) {

      switch (status) {
        case LOCKED -> {
          player.sendMessage(Chat.errorFade("Dieser Beruf ist noch gesperrt!"));
        }
        case AVAILABLE -> {
          mgr.setProfessionStatus(prof.professionId, ProfessionStatus.ACTIVE);
          player.sendMessage(
              Chat.greenFade(
                  "Du hast nun " + prof.type + " (Stufe " + prof.getLevel() + ") aktiviert!"));
          new TownProfessionGUI(player, settle).open();
        }
        case ACTIVE -> {
          mgr.setProfessionStatus(prof.professionId, ProfessionStatus.PAUSED);
          player.sendMessage(
              Chat.greenFade(
                  "Du hast " + prof.prettyName + " (Stufe " + prof.getLevel() + ") pausiert."));

          new TownProfessionGUI(player, settle).open();
        }
        case PAUSED -> {
          mgr.setProfessionStatus(prof.professionId, ProfessionStatus.ACTIVE);
          player.sendMessage(
              Chat.greenFade(
                  "Du arbeitest wieder an " + prof.type + " (Stufe " + prof.getLevel() + ")."));
          new TownProfessionGUI(player, settle).open();
        }
        case COMPLETED -> {
          player.sendMessage(Chat.errorFade("Dieser Beruf ist bereits komplett abgeschlossen!"));
        }
      }
    } else if (e.isRightClick()) {
      if (mgr.completeProfession(prof.professionId)) {
        player.sendMessage(
            Chat.greenFade(
                "Glückwunsch! Du hast "
                    + prof.prettyName
                    + " (Stufe "
                    + prof.getLevel()
                    + ") abgeschlossen!"));
      }
      new TownProfessionGUI(player, settle).open();
    }
  }

  private void addNavigationItems() {
    // Prev
    RoseItem previousPage =
        new RoseItem.Builder()
            .material(Material.ARROW)
            .displayName(Component.text("§eVorherige Seite"))
            .build()
            .onClick(
                e -> {
                  if (!pagination.isFirstPage()) {
                    pagination.goPreviousPage();
                    pagination.update();
                  }
                });
    addItem(48, previousPage);

    // Next
    RoseItem nextPage =
        new RoseItem.Builder()
            .material(Material.ARROW)
            .displayName(Component.text("§eNächste Seite"))
            .build()
            .onClick(
                e -> {
                  if (!pagination.isLastPage()) {
                    pagination.goNextPage();
                    pagination.update();
                  }
                });
    addItem(50, nextPage);

    // Back
    RoseItem back =
        new RoseItem.Builder()
            .material(Material.SPECTRAL_ARROW)
            .displayName(Chat.yellowFade("<b>Zurück</b>"))
            .build();
    back.onClick(e -> new TownGUI(player, settle).open());
    addItem(45, back);
  }

  private String buildProgressBar(long current, long needed, int barsize) {
    if (needed <= 0) return "§a[##########]";
    double ratio = (double) current / (double) needed;
    if (ratio > 1.0) ratio = 1.0;
    int filled = (int) Math.floor(ratio * barsize);

    StringBuilder sb = new StringBuilder("§7[");
    for (int i = 0; i < barsize; i++) {
      if (i < filled) sb.append("§a#");
      else sb.append("§8-");
    }
    sb.append("§7]");
    return sb.toString();
  }
}
