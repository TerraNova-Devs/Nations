package de.terranova.nations.gui;

import de.mcterranova.terranovaLib.roseGUI.RoseGUI;
import de.mcterranova.terranovaLib.roseGUI.RoseItem;
import de.mcterranova.terranovaLib.roseGUI.RosePagination;
import de.mcterranova.terranovaLib.utils.Chat;
import de.terranova.nations.professions.*;
import de.terranova.nations.regions.grid.SettleRegion;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class TownProfessionGUI extends RoseGUI {
    private final SettleRegion settle;
    private final List<String> professionTypes = Arrays.asList("FISHERY", "MINING", "FARMING");
    private final RosePagination pagination;

    public TownProfessionGUI(Player player, SettleRegion settle) {
        super(player, "town-profession-gui", Chat.blueFade("<b>Professionen"), 6);
        this.settle = settle;
        this.pagination = new RosePagination(this);
    }

    @Override
    public void onOpen(InventoryOpenEvent event) {
        // Welche Slots für die Pagination? (max 16 Items pro Seite)
        pagination.registerPageSlots(10, 12, 14, 16, 19, 21, 23, 25, 28, 30, 32, 34, 37, 39, 41, 43);

        // Hintergrund füllen
        RoseItem filler = new RoseItem.Builder()
                .material(Material.BLACK_STAINED_GLASS_PANE)
                .displayName("")
                .build();
        fillGui(filler);

        // Lade den ProfessionProgressManager
        ProfessionProgressManager mgr = ProfessionProgressManager.loadForSettlement(settle.getId());

        // Für jeden Professionstyp (z. B. FISHERY) zeigen wir **eine** Stufe an:
        for (String type : professionTypes) {
            // Alle Stufen 1..4 zur jeweiligen Profession, sortieren
            List<Profession> profs = ProfessionManager.getProfessionsByType(type);
            profs.sort(Comparator.comparingInt(Profession::getLevel));

            // Finde die erste Stufe, die NICHT completed ist
            Profession nextProf = null;
            for (Profession p : profs) {
                ProfessionStatus st = mgr.getProfessionStatus(p.getProfessionId());
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
                pagination.addItem(new RoseItem.Builder()
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
     * Erzeugt ein hübsches Item für die gegebene Profession,
     * inklusive Status, benötigte Gebäude, Objectives usw.
     */
    private RoseItem createProfessionItem(Profession prof, ProfessionProgressManager mgr) {
        ProfessionStatus status = mgr.getProfessionStatus(prof.getProfessionId());

        // Passendes Item (Angel / Spitzhacke / etc.)
        ItemStack icon = getIconForProfession(prof);
        ItemMeta meta = icon.getItemMeta();

        // Lore
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("§8§m                                     "));

        meta.displayName(Component.text(String.format("§b%s §7| Stufe: §b%d", Profession.prettyName(prof.getType()), prof.getLevel())));
        // Status
        lore.add(Component.text("§7Status: " + (status == ProfessionStatus.ACTIVE ? "§a" : "§f") + status.name()));

        // Kosten & Score
        String costLine = String.format("§7Kosten: §e%d §7Silber  §8|  §7Score-Bonus: §e%d", prof.getPrice(), prof.getScore());
        lore.add(Component.text(costLine));

        // Buildings
        List<Building> requiredBuildings = ProfessionManager.getBuildingsForProfession(prof.getProfessionId());
        if (!requiredBuildings.isEmpty()) {
            lore.add(Component.text("§6Benötigte Gebäude:"));
            for (Building b : requiredBuildings) {
                boolean isBuilt = mgr.hasBuilding(b.getBuildingId());
                String bLine = (isBuilt ? "§a✔ " : "§c✖ ") + "§7" + b.getName();
                lore.add(Component.text("   " + bLine));
            }
        }

        // Objectives
        List<ProfessionObjective> objectives = ProfessionManager.getObjectivesForProfession(prof.getProfessionId());
        if (!objectives.isEmpty()) {
            lore.add(Component.text("§6Ziel / Objective(s):"));
            for (ProfessionObjective obj : objectives) {
                long current = mgr.getObjectiveProgress(obj.getObjectiveId());
                lore.add(Component.text(String.format("   §7- %s §f%s: %d/%d %s",
                        obj.getAction(), obj.getObject(), current, obj.getAmount(),
                        buildProgressBar(current, obj.getAmount(), 8))));
            }
        }

        // Status-spezifische Info
        switch (status) {
            case LOCKED -> lore.add(Component.text("§cNoch gesperrt! Vorstufe nicht abgeschlossen."));
            case AVAILABLE -> lore.add(Component.text("§eKlicke, um diesen Beruf zu aktivieren."));
            case ACTIVE -> lore.add(Component.text("§aAktiv! Klicke, um diesen Beruf zu pausieren."));
            case PAUSED -> lore.add(Component.text("§7Pausiert! Klicke, um weiterzugrinden."));
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

        RoseItem item = builder.build()
                .onClick(e -> handleProfessionClick(e, prof, status, mgr));
        return item;
    }

    private void handleProfessionClick(InventoryClickEvent e, Profession prof, ProfessionStatus status, ProfessionProgressManager mgr) {
        e.setCancelled(true);

        switch (status) {
            case LOCKED -> {
                player.sendMessage(Chat.errorFade("Dieser Beruf ist noch gesperrt!"));
            }
            case AVAILABLE -> {
                mgr.setProfessionStatus(prof.getProfessionId(), ProfessionStatus.ACTIVE);
                player.sendMessage(Chat.greenFade("Du hast nun " + prof.getType() + " (Stufe " + prof.getLevel() + ") aktiviert!"));
                new TownProfessionGUI(player, settle).open();
            }
            case ACTIVE -> {
                if(mgr.completeProfession(prof.getProfessionId())) {
                    player.sendMessage(Chat.greenFade("Glückwunsch! Du hast " + Profession.prettyName(prof.getType()) + " (Stufe " + prof.getLevel() + ") abgeschlossen!"));
                } else {
                    mgr.setProfessionStatus(prof.getProfessionId(), ProfessionStatus.PAUSED);
                    player.sendMessage(Chat.greenFade("Du hast " + Profession.prettyName(prof.getType()) + " (Stufe " + prof.getLevel() + ") pausiert."));
                }
                new TownProfessionGUI(player, settle).open();
            }
            case PAUSED -> {
                mgr.setProfessionStatus(prof.getProfessionId(), ProfessionStatus.ACTIVE);
                player.sendMessage(Chat.greenFade("Du arbeitest wieder an " + prof.getType() + " (Stufe " + prof.getLevel() + ")."));
                new TownProfessionGUI(player, settle).open();
            }
            case COMPLETED -> {
                player.sendMessage(Chat.errorFade("Dieser Beruf ist bereits komplett abgeschlossen!"));
            }
        }
    }

    private void addNavigationItems() {
        // Prev
        RoseItem previousPage = new RoseItem.Builder()
                .material(Material.ARROW)
                .displayName(Component.text("§eVorherige Seite"))
                .build()
                .onClick(e -> {
                    if (!pagination.isFirstPage()) {
                        pagination.goPreviousPage();
                        pagination.update();
                    }
                });
        addItem(48, previousPage);

        // Next
        RoseItem nextPage = new RoseItem.Builder()
                .material(Material.ARROW)
                .displayName(Component.text("§eNächste Seite"))
                .build()
                .onClick(e -> {
                    if (!pagination.isLastPage()) {
                        pagination.goNextPage();
                        pagination.update();
                    }
                });
        addItem(50, nextPage);

        // Back
        RoseItem back = new RoseItem.Builder()
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

    private ItemStack getIconForProfession(Profession prof) {
        Material mat;
        switch (prof.getType().toUpperCase()) {
            case "FISHERY" -> mat = Material.FISHING_ROD;
            case "MINING" -> mat = Material.IRON_PICKAXE;
            case "FARMING" -> mat = Material.IRON_HOE;
            default -> mat = Material.PAPER;
        }
        ItemStack stack = new ItemStack(mat);

        // Ggf. Stack-Amount = Level
        if (prof.getLevel() >= 1 && prof.getLevel() <= 64) {
            stack.setAmount(prof.getLevel());
        }
        return stack;
    }
}
