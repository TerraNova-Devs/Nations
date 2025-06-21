package de.terranova.nations.gui;

import com.destroystokyo.paper.profile.PlayerProfile;
import de.mcterranova.terranovaLib.roseGUI.RoseGUI;
import de.mcterranova.terranovaLib.roseGUI.RoseItem;
import de.mcterranova.terranovaLib.roseGUI.RosePagination;
import de.mcterranova.terranovaLib.utils.Chat;
import de.terranova.nations.NationsPlugin;
import de.terranova.nations.regions.access.TownAccess;
import de.terranova.nations.regions.access.TownAccessControlled;
import de.terranova.nations.regions.access.TownAccessLevel;
import de.terranova.nations.regions.grid.SettleRegion;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class TownPlayersGUI extends RoseGUI {

    SettleRegion settle;
    TownAccessControlled access;
    RosePagination pagination;

    public TownPlayersGUI(Player player, SettleRegion settle, TownAccessControlled access) {
        super(player, "players-gui", Chat.blueFade("<b>Einwohner"), 6);
        this.settle = settle;
        this.access = access;
        this.pagination = new RosePagination(this);
    }

    @Override
    public void onOpen(InventoryOpenEvent event) {
        pagination.registerPageSlotsBetween(10, 16);
        pagination.registerPageSlotsBetween(19, 25);
        pagination.registerPageSlotsBetween(28, 34);
        pagination.registerPageSlotsBetween(37, 43);

        registerPlayerSlots();

        addNavigationItems();
    }

    private void registerPlayerSlots() {
        pagination.getItems().clear();

        HashMap<UUID, TownAccessLevel> accessLevels = access.getAccess().getAccessLevels();
        // Sort them by weight descending:
        LinkedHashMap<UUID, TownAccessLevel> sortedAccessLevels = accessLevels.entrySet().stream()
                .sorted((entry1, entry2) -> Integer.compare(entry2.getValue().getWeight(), entry1.getValue().getWeight()))
                .collect(LinkedHashMap::new, (map, e) -> map.put(e.getKey(), e.getValue()), LinkedHashMap::putAll);

        // For each UUID -> build a placeholder head, then fetch real skin asynchronously
        sortedAccessLevels.forEach((uuid, level) -> {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);

            // 1) Create a placeholder head item immediately (so we can show something in the GUI)
            ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) skull.getItemMeta();

            // Display either the player's known name or their UUID if name is null
            String displayName = offlinePlayer.getName() != null ? offlinePlayer.getName() : uuid.toString();
            meta.displayName(Component.text("§e" + displayName));
            meta.lore(List.of(Component.text("§7" + level.name()), Component.text("§8Loading skin...")));
            skull.setItemMeta(meta);

            // Wrap in your RoseItem
            RoseItem playerItem = new RoseItem.Builder().copyStack(skull).build()
                    .onClick(event -> handlePlayerClick(event, player, access, level, uuid));

            // Add the placeholder item to pagination right away
            pagination.addItem(playerItem);

            // 2) Fetch correct skin data ASYNCHRONOUSLY
            Bukkit.getScheduler().runTaskAsynchronously(NationsPlugin.plugin, () -> {
                // Attempt to complete the player's profile with Mojang
                PlayerProfile profile = offlinePlayer.getPlayerProfile();
                profile.complete(true); // Contacts Mojang for the texture if needed

                // 3) Once the skin is fetched, update the item on the main thread
                Bukkit.getScheduler().runTask(NationsPlugin.plugin, () -> {
                    // If the GUI is still open for this player, proceed
                    if (!player.getOpenInventory().getTopInventory().equals(getInventory())) {
                        return; // The GUI was closed; no need to update
                    }

                    // Update the same ItemStack with the newly fetched profile
                    ItemStack updatedSkull = playerItem.stack; // The same stack we used
                    SkullMeta updatedMeta = (SkullMeta) updatedSkull.getItemMeta();
                    updatedMeta.setPlayerProfile(profile);

                    // Re-set name & lore with the final data
                    updatedMeta.displayName(Component.text("§e" + displayName));
                    updatedMeta.lore(List.of(Component.text("§7" + level.name())));
                    updatedSkull.setItemMeta(updatedMeta);

                    // Finally, refresh pagination so the updated item shows in the GUI
                    pagination.update();
                });
            });
        });

        // Initial pagination refresh to show the placeholders
        pagination.update();
    }


    private void handlePlayerClick(InventoryClickEvent e, Player player, TownAccessControlled access, TownAccessLevel level, UUID uuid) {
        if(!TownAccess.hasAccess(access.getAccess().getAccessLevel(player.getUniqueId()), TownAccessLevel.VICE)) {
            player.sendMessage(Chat.errorFade("Du musst mindestens Vize sein um den Rang dieses Spielers ändern zu können."));
            return;
        }

        if(e.isRightClick()) {
            if(level == TownAccessLevel.CITIZEN) {
                player.sendMessage(Chat.errorFade("Dieser Spieler ist bereits Einwohner."));
            } else if(level == TownAccessLevel.COUNCIL) {
                access.getAccess().setAccessLevel(uuid, TownAccessLevel.CITIZEN);
                player.sendMessage(Chat.greenFade("Der Spieler ist nun Einwohner."));
                registerPlayerSlots();
            }

            if (TownAccess.hasAccess(access.getAccess().getAccessLevel(player.getUniqueId()), TownAccessLevel.MAJOR)) {
                if (level == TownAccessLevel.VICE) {
                    access.getAccess().setAccessLevel(uuid, TownAccessLevel.COUNCIL);
                    player.sendMessage(Chat.greenFade("Der Spieler ist nun im Stadtrat."));
                    registerPlayerSlots();
                }
            }
        }
        if(e.isLeftClick()) {
            if(level == TownAccessLevel.CITIZEN) {
                access.getAccess().setAccessLevel(uuid, TownAccessLevel.COUNCIL);
                player.sendMessage(Chat.greenFade("Dieser Spieler ist nun im Stadtrat."));
                registerPlayerSlots();
            }

            if (TownAccess.hasAccess(access.getAccess().getAccessLevel(player.getUniqueId()), TownAccessLevel.MAJOR)) {
                if (level == TownAccessLevel.COUNCIL) {
                    access.getAccess().setAccessLevel(uuid, TownAccessLevel.VICE);
                    player.sendMessage(Chat.greenFade("Der Spieler ist nun Vize."));
                    registerPlayerSlots();
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
