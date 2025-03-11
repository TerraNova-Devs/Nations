package de.terranova.nations.gui;

import de.mcterranova.terranovaLib.roseGUI.RoseGUI;
import de.mcterranova.terranovaLib.roseGUI.RoseItem;
import de.mcterranova.terranovaLib.roseGUI.RosePagination;
import de.mcterranova.terranovaLib.utils.Chat;
import de.terranova.nations.professions.*;
import de.terranova.nations.database.dao.SettlementObjectiveProgressDAO;
import de.terranova.nations.regions.grid.SettleRegion;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * GUI, das alle Professionen (z.B. FISHERY, MINING, FARMING)
 * und deren 4 Level in einer Pagination anzeigt.
 * Berücksichtigt 5 Status (LOCKED, AVAILABLE, ACTIVE, PAUSED, COMPLETED).
 * Wechselt Fokus, pausinert usw.
 */
public class TownProfessionGUI extends RoseGUI {
    private final SettleRegion settle;
    private final List<String> professionTypes = Arrays.asList("FISHERY", "MINING", "FARMING");
    private final RosePagination pagination;

    public TownProfessionGUI(Player player, SettleRegion settle) {
        super(player, "town-profession-gui", Chat.blueFade("<b>Stadt Professionen"), 6);
        this.settle = settle;
        this.pagination = new RosePagination(this);
    }

    @Override
    public void onOpen(InventoryOpenEvent event) {
        pagination.registerPageSlots(10, 12, 14, 16, 19, 21, 23, 25, 28, 30, 32, 34, 37, 39, 41, 43);

        // Filler
        RoseItem filler = new RoseItem.Builder().material(Material.BLACK_STAINED_GLASS_PANE).displayName("").build();
        fillGui(filler);

        // Lade Manager
        ProfessionProgressManager mgr = ProfessionProgressManager.loadForSettlement(settle.getId());

        // Pro ProfessionType => 4 Level => Items
        for (String type : professionTypes) {
            List<Profession> profs = ProfessionManager.getProfessionsByType(type);
            // Sortieren nach Level
            profs.sort(Comparator.comparingInt(Profession::getLevel));
            for (int i = 0; i < 4; i++) {
                if (i >= profs.size()) {
                    // Leeres / Barrier-Item
                    pagination.addItem(new RoseItem.Builder().material(Material.BARRIER)
                            .displayName("Keine Stufe").build());
                } else {
                    Profession prof = profs.get(i);
                    pagination.addItem(createProfessionItem(prof, mgr));
                }
            }
        }
        pagination.update();
        addNavigationItems();
    }

    @Override
    public void onClose(InventoryCloseEvent event) {
        // Nothing
    }

    private RoseItem createProfessionItem(Profession prof, ProfessionProgressManager mgr) {
        ProfessionStatus status = mgr.getProfessionStatus(prof.getProfessionId());

        // Erstelle ItemStack (Angel / Spitzhacke / Hoe etc.)
        ItemStack icon = getIconForProfession(prof);
        ItemMeta meta = icon.getItemMeta();

        // Fake-Enchant wenn ACTIVE oder COMPLETED
        if (status == ProfessionStatus.ACTIVE || status == ProfessionStatus.COMPLETED) {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        }

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("§e" + prof.getType() + " Stufe " + prof.getLevel()));
        lore.add(Component.text("§7Status: " + status.name()));

        // Optional: Kosten / Score
        lore.add(Component.text("§7Kosten: " + prof.getPrice() + " Silber, Score: " + prof.getScore()));

        // Objectives:
        var objectives = ProfessionManager.getObjectivesForProfession(prof.getProfessionId());
        if (!objectives.isEmpty()) {
            lore.add(Component.text("§7Objectives:"));
            for (ProfessionObjective obj : objectives) {
                long current = mgr.getObjectiveProgress(obj.getObjectiveId());
                lore.add(Component.text("§7 - " + obj.getAction() + " " + obj.getObject() + ": "
                        + current + "/" + obj.getAmount() + " " + buildProgressBar(current, obj.getAmount(), 8)));
            }
        }

        // Je nach Status
        switch (status) {
            case LOCKED -> lore.add(Component.text("§cNoch gesperrt! Vorstufe nicht fertig."));
            case AVAILABLE -> lore.add(Component.text("§eKlicke, um diese Profession zu aktivieren/fokussieren"));
            case ACTIVE -> lore.add(Component.text("§aIn Arbeit - Klicke, um zu pausieren"));
            case PAUSED -> lore.add(Component.text("§7Pausiert - Klicke, um wieder fortzufahren"));
            case COMPLETED -> lore.add(Component.text("§aBereits abgeschlossen!"));
        }

        RoseItem.Builder builder = new RoseItem.Builder().copyStack(icon);

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
            case LOCKED:
                player.sendMessage(Chat.errorFade("Diese Profession ist noch gesperrt."));
                break;

            case AVAILABLE:
                mgr.setProfessionStatus(prof.getProfessionId(), ProfessionStatus.ACTIVE);

                player.sendMessage(Chat.greenFade("Du grindest nun für " + prof.getType() + " L" + prof.getLevel()));
                new TownProfessionGUI(player, settle).open();
                break;

            case ACTIVE:
                mgr.setProfessionStatus(prof.getProfessionId(), ProfessionStatus.PAUSED);
                player.sendMessage(Chat.greenFade(prof.getType() + " L" + prof.getLevel() + " wurde pausiert."));
                new TownProfessionGUI(player, settle).open();
                break;

            case PAUSED:
                mgr.setProfessionStatus(prof.getProfessionId(), ProfessionStatus.ACTIVE);
                player.sendMessage(Chat.greenFade("Weiter geht's mit " + prof.getType() + " L" + prof.getLevel()));
                new TownProfessionGUI(player, settle).open();
                break;

            case COMPLETED:
                player.sendMessage(Chat.errorFade("Diese Profession ist bereits abgeschlossen!"));
                break;
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
        back.onClick(e -> {
            new TownGUI(player, settle).open();
        });
        addItem(45, back);
    }

    private String buildProgressBar(long current, long needed, int barsize) {
        if (needed <= 0) return "§a[##########]";
        double ratio = (double) current / (double) needed;
        if (ratio > 1.0) ratio = 1.0;
        int filled = (int) Math.floor(ratio * barsize);

        StringBuilder sb = new StringBuilder("§7[");
        for (int i = 0; i < barsize; i++) {
            sb.append(i < filled ? "§a#" : "§8-");
        }
        sb.append("§7]");
        return sb.toString();
    }

    private ItemStack getIconForProfession(Profession prof) {
        Material mat;
        switch (prof.getType().toUpperCase()) {
            case "FISHERY":
                mat = Material.FISHING_ROD;
                break;
            case "MINING":
                mat = Material.IRON_PICKAXE;
                break;
            case "FARMING":
                mat = Material.IRON_HOE;
                break;
            default:
                mat = Material.PAPER;
        }
        ItemStack stack = new ItemStack(mat);
        // z. B. setAmount = level
        if (prof.getLevel() >= 1 && prof.getLevel() <= 64) {
            stack.setAmount(prof.getLevel());
        }
        return stack;
    }
}
