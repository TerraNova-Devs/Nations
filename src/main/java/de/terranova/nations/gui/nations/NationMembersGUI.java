package de.terranova.nations.gui.nations;

import com.destroystokyo.paper.profile.PlayerProfile;
import de.mcterranova.terranovaLib.roseGUI.RoseGUI;
import de.mcterranova.terranovaLib.roseGUI.RoseItem;
import de.mcterranova.terranovaLib.roseGUI.RosePagination;
import de.mcterranova.terranovaLib.utils.Chat;
import de.terranova.nations.NationsPlugin;
import de.terranova.nations.nations.Nation;
import de.terranova.nations.nations.NationPlayerRank;
import de.terranova.nations.nations.SettlementRank;
import de.terranova.nations.regions.RegionManager;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.codehaus.plexus.util.StringUtils;

import java.util.*;

public class NationMembersGUI extends RoseGUI {
    private final Nation nation;
    RosePagination pagination;


    public NationMembersGUI(Player player, Nation nation) {
        super(player, "nation-members-gui", Chat.blueFade("<b>Nationsmitglieder"), 6);
        this.nation = nation;
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

        HashMap<UUID, TownAccessLevel> accessLevels = new HashMap<>();

        // Fetch all access levels for each settlement which are at least CITIZEN
        for (Map.Entry<UUID, SettlementRank> settlement : nation.getSettlements().entrySet()) {
            UUID settleId = settlement.getKey();
            Optional<SettleRegion> settle = RegionManager.retrieveRegion("settle", settleId);
            if (settle.isPresent()) {
                TownAccess access = settle.get().getAccess();
                if (access != null) {
                    accessLevels.putAll(access.getAccessLevels().entrySet().stream()
                            .filter(entry -> TownAccess.hasAccess(entry.getValue(), TownAccessLevel.CITIZEN))
                            .collect(HashMap::new, (map, e) -> map.put(e.getKey(), e.getValue()), HashMap::putAll));
                }
            }
        }

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
                    .onClick(event -> handlePlayerClick(event, player, uuid));

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
                    updatedMeta.lore(List.of(
                            Component.text("§7Stadt: " + StringUtils.capitalise(RegionManager.retrievePlayersSettlement(offlinePlayer.getUniqueId()).get().getName())),
                            Component.text("§7Stadtrang: " + level.name()),
                            Component.text("§7Nationsrang: " + nation.getPlayerRank(offlinePlayer.getUniqueId()).name())));
                    updatedSkull.setItemMeta(updatedMeta);

                    // Finally, refresh pagination so the updated item shows in the GUI
                    pagination.update();
                });
            });
        });

        // Initial pagination refresh to show the placeholders
        pagination.update();
    }


    private void handlePlayerClick(InventoryClickEvent e, Player player, UUID uuid) {
        NationPlayerRank ownRank = nation.getPlayerRank(player.getUniqueId());
        NationPlayerRank currentRank = nation.getPlayerRank(uuid);
        if (ownRank.getWeight() < NationPlayerRank.VICE_LEADER.getWeight()) {
            player.sendMessage(Chat.errorFade("Du musst mindestens Vize-Anführer sein um den Rang dieses Spielers ändern zu können."));
            return;
        }

        if(e.isRightClick()) {
            if(currentRank == NationPlayerRank.MEMBER) {
                player.sendMessage(Chat.errorFade("Dieser Spieler ist bereits Mitglied."));
            } else if(currentRank == NationPlayerRank.COUNCIL) {
                nation.setPlayerRank(uuid, NationPlayerRank.MEMBER);
                NationsPlugin.nationManager.saveNation(nation);
                player.sendMessage(Chat.greenFade("Der Spieler ist nun Mitglied."));
                registerPlayerSlots();
            }

            if (ownRank == NationPlayerRank.LEADER) {
                if (currentRank == NationPlayerRank.VICE_LEADER) {
                    nation.setPlayerRank(uuid, NationPlayerRank.COUNCIL);
                    NationsPlugin.nationManager.saveNation(nation);
                    player.sendMessage(Chat.greenFade("Der Spieler ist nun im Nationsrat."));
                    registerPlayerSlots();
                }
            }
        }
        if(e.isLeftClick()) {
            if(currentRank == NationPlayerRank.MEMBER) {
                nation.setPlayerRank(uuid, NationPlayerRank.COUNCIL);
                NationsPlugin.nationManager.saveNation(nation);
                player.sendMessage(Chat.greenFade("Dieser Spieler ist nun im Nationsrat."));
                registerPlayerSlots();
            }

            if (ownRank == NationPlayerRank.LEADER) {
                if (currentRank == NationPlayerRank.COUNCIL) {
                    nation.setPlayerRank(uuid, NationPlayerRank.VICE_LEADER);
                    NationsPlugin.nationManager.saveNation(nation);
                    player.sendMessage(Chat.greenFade("Der Spieler ist nun Vize-Anführer."));
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
            new NationGUI(player, nation).open();
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
