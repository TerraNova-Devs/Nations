package de.terranova.nations.gui;

import de.mcterranova.terranovaLib.roseGUI.RoseGUI;
import de.mcterranova.terranovaLib.roseGUI.RoseItem;
import de.mcterranova.terranovaLib.roseGUI.RosePagination;
import de.mcterranova.terranovaLib.utils.Chat;
import de.terranova.nations.regions.access.TownAccess;
import de.terranova.nations.regions.access.TownAccessLevel;
import de.terranova.nations.regions.grid.SettleRegion;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TownPlayersGUI extends RoseGUI {

    SettleRegion settle;
    private final RosePagination pagination;

    public TownPlayersGUI(Player player, SettleRegion settle) {
        super(player, "players-gui", Chat.blueFade("<b>Einwohner"), 6);
        this.settle = settle;
        this.pagination = new RosePagination(this);
    }

    @Override
    public void onOpen(InventoryOpenEvent event) {
        pagination.registerPageSlotsBetween(10, 16);
        pagination.registerPageSlotsBetween(19, 25);
        pagination.registerPageSlotsBetween(28, 34);
        pagination.registerPageSlotsBetween(37, 43);

        TownAccess access = settle.getAccess();

        HashMap<UUID, TownAccessLevel> accessLevels = access.getAccessLevels();
        HashMap<UUID, TownAccessLevel> sortedAccessLevels = accessLevels.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(HashMap::new, (m, e) -> m.put(e.getKey(), e.getValue()), HashMap::putAll);


        sortedAccessLevels.forEach((uuid, level) -> {
            try {
                OfflinePlayer member = Bukkit.getOfflinePlayer(uuid);
                RoseItem playerItem = new RoseItem.Builder()
                        .material(Material.PLAYER_HEAD)
                        .displayName(Component.text("§e" + Bukkit.getOfflinePlayer(uuid).getName()))
                        .addLore(Component.text("§7" + level.name()))
                        .build();
                SkullMeta skullMeta = (SkullMeta) playerItem.stack.getItemMeta();
                skullMeta.setOwningPlayer(member);
                playerItem.stack.setItemMeta(skullMeta);
                playerItem.onClick(e -> {
                    handlePlayerClick(e, player, access, level, uuid);
                });
                pagination.addItem(playerItem);
            } catch (Exception ex) {
                RoseItem playerItem = new RoseItem.Builder()
                        .material(Material.PLAYER_HEAD)
                        .displayName(Component.text("§e" + uuid))
                        .addLore(Component.text("§7" + level.name()))
                        .build();
                playerItem.onClick(e -> {
                    handlePlayerClick(e, player, access, level, uuid);
                });
                pagination.addItem(playerItem);
            }
        });



        addNavigationItems();
    }

    private void handlePlayerClick(InventoryClickEvent e, Player player, TownAccess access, TownAccessLevel level, UUID uuid) {
        if(!TownAccess.hasAccess(access.getAccessLevel(player.getUniqueId()), TownAccessLevel.VICE)) {
            player.sendMessage(Chat.errorFade("Du musst mindestens Vize sein um den Rang dieses Spielers ändern zu können."));
            return;
        }

        if(e.isRightClick()) {
            if(level == TownAccessLevel.CITIZEN) {
                player.sendMessage(Chat.errorFade("Dieser Spieler ist bereits Einwohner."));
            } else if(level == TownAccessLevel.COUNCIL) {
                access.setAccessLevel(uuid, TownAccessLevel.CITIZEN);
                player.sendMessage(Chat.greenFade("Der Spieler ist nun Einwohner."));
            }

            if (TownAccess.hasAccess(access.getAccessLevel(player.getUniqueId()), TownAccessLevel.MAJOR)) {
                if (level == TownAccessLevel.VICE) {
                    access.setAccessLevel(uuid, TownAccessLevel.COUNCIL);
                    player.sendMessage(Chat.greenFade("Der Spieler ist nun im Stadtrat."));
                }
            }
        }
        if(e.isLeftClick()) {
            if(level == TownAccessLevel.CITIZEN) {
                access.setAccessLevel(uuid, TownAccessLevel.COUNCIL);
                player.sendMessage(Chat.errorFade("Dieser Spieler ist nun im Stadtrat."));
            }

            if (TownAccess.hasAccess(access.getAccessLevel(player.getUniqueId()), TownAccessLevel.MAJOR)) {
                if (level == TownAccessLevel.COUNCIL) {
                    access.setAccessLevel(uuid, TownAccessLevel.VICE);
                    player.sendMessage(Chat.greenFade("Der Spieler ist nun Vize."));
                }
            }
        }
    }

    private void addNavigationItems() {
        // Previous Page
        RoseItem previousPage = new RoseItem.Builder()
                .material(Material.ARROW)
                .displayName(Component.text("§eVorherige Seite"))
                .build()
                .onClick((InventoryClickEvent e) -> {
                    if (!pagination.isFirstPage()) {
                        pagination.goPreviousPage();
                        pagination.update();
                    }
                });
        addItem(48, previousPage);

        // Next Page
        RoseItem nextPage = new RoseItem.Builder()
                .material(Material.ARROW)
                .displayName(Component.text("§eNächste Seite"))
                .build()
                .onClick((InventoryClickEvent e) -> {
                    if (!pagination.isLastPage()) {
                        pagination.goNextPage();
                        pagination.update();
                    }
                });
        addItem(50, nextPage);

        // Back Button
        RoseItem back = new RoseItem.Builder()
                .material(Material.SPECTRAL_ARROW)
                .displayName(Chat.yellowFade("<b>Zurück</b>"))
                .build();
        back.onClick(e -> {
            new TownGUI(player, settle).open();
        });

        addItem(45, back);

        fillBorder();
    }

    private void fillBorder() {
        RoseItem filler = new RoseItem.Builder()
                .material(Material.BLACK_STAINED_GLASS_PANE)
                .displayName("")
                .build();

        // Fill slots except for navigation buttons
        for (int i : new int[]{0,1,2,3,4,5,6,7,8,9,17,18,26,27,35,36,44, 46, 47, 49, 51, 52, 53}) {
            addItem(i, filler);
        }
    }
}
