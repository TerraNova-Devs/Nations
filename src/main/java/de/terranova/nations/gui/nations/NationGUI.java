package de.terranova.nations.gui.nations;

import de.terranova.nations.NationsPlugin;
import de.terranova.nations.nations.Nation;
import de.terranova.nations.nations.NationPlayerRank;
import de.terranova.nations.utils.Chat;
import de.terranova.nations.utils.roseGUI.RoseGUI;
import de.terranova.nations.utils.roseGUI.RoseItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.codehaus.plexus.util.StringUtils;

import java.util.UUID;

public class NationGUI extends RoseGUI {
    private final Nation nation;

    public NationGUI(Player player, Nation nation) {
        super(player, "nation-gui", Chat.blueFade("<b>Nation Menu"), 5);
        this.nation = nation;
    }

    @Override
    public void onOpen(InventoryOpenEvent event) {
        fillGui(new RoseItem.Builder().material(Material.BLACK_STAINED_GLASS_PANE).displayName("").build());

        // Nation Info Item
        RoseItem nationInfo = new RoseItem.Builder()
                .material(Material.PAPER)
                .displayName(Chat.greenFade("<b>Nation Information"))
                .addLore(Chat.cottonCandy("<i>Name: " + StringUtils.capitalise(nation.getName())))
                .addLore(Chat.cottonCandy("<i>Anführer: " + getPlayerName(nation.getLeader())))
                .addLore(Chat.cottonCandy("<i>Städte: " + nation.getSettlements().size()))
                .build();

        // Members Item
        RoseItem membersItem = new RoseItem.Builder()
                .material(Material.PLAYER_HEAD)
                .displayName(Chat.yellowFade("<b>Nationsmitglieder"))
                .addLore(Chat.cottonCandy("<i>Klicke um Mitglieder zu sehen"))
                .build();

        // Settlements Item
        RoseItem settlementsItem = new RoseItem.Builder()
                .material(Material.OAK_DOOR)
                .displayName(Chat.yellowFade("<b>Städte"))
                .addLore(Chat.cottonCandy("<i>Klicke um die Städte zu sehen"))
                .build();

        // Relations Item
        RoseItem relationsItem = new RoseItem.Builder()
                .material(Material.PAPER)
                .displayName(Chat.yellowFade("<b>Beziehungen"))
                .addLore(Chat.cottonCandy("<i>Klicke um Beziehungen zu sehen"))
                .build();

        // Invite Settlement Item (only for leaders)
        RoseItem inviteSettlementItem = null;
        if (nation.getLeader().equals(player.getUniqueId())) {
            inviteSettlementItem = new RoseItem.Builder()
                    .material(Material.WRITABLE_BOOK)
                    .displayName(Chat.yellowFade("<b>Siedlung einladen"))
                    .addLore(Chat.cottonCandy("<i>Klicke um eine Siedlung einzuladen"))
                    .build();
        }

        // Add items to the GUI
        addItem(10, nationInfo);
        addItem(12, membersItem);
        addItem(14, settlementsItem);
        addItem(16, relationsItem);

        if (inviteSettlementItem != null) {
            addItem(22, inviteSettlementItem);
            inviteSettlementItem.onClick(e -> {
                player.closeInventory();
                player.sendMessage(Chat.cottonCandy("Schreib '/nation invite <Stadt-Name>' um eine Stadt zur Nation einzuladen."));
            });
        }

        // Set item click actions
        membersItem.onClick(e -> new NationMembersGUI(player, nation).open());
        settlementsItem.onClick(e -> new NationSettlementsGUI(player, nation).open());
        relationsItem.onClick(e -> new NationRelationsGUI(player, nation).open());


        //Banner change
        boolean canEditBanner = (nation.getPlayerRank(player.getUniqueId()).getWeight() >= NationPlayerRank.VICE_LEADER.getWeight());

        if (canEditBanner) {
            ItemStack customBanner = nation.getBanner();
            if (customBanner == null) {
                customBanner = new ItemStack(Material.WHITE_BANNER);
            }

            RoseItem changeBanner = new RoseItem.Builder()
                    .copyStack(customBanner)
                    .displayName(Chat.yellowFade("<b>Banner ändern"))
                    .addLore(Chat.cottonCandy("<i>Klicke, während du das gewünschte Banner in der Hand hast."))
                    .build();

            addItem(28, changeBanner);

            changeBanner.onClick(e -> {
                // 1) Check if player is holding a banner
                ItemStack handItem = e.getCursor();
                if (handItem == null || !handItem.getType().toString().endsWith("BANNER")) {
                    player.sendMessage(Chat.errorFade("Du musst ein Banner in der Hand halten, um es zu setzen!"));
                    return;
                }

                // 2) Set the nation's banner
                nation.setBanner(handItem);
                // 3) Persist changes
                NationsPlugin.nationManager.saveNation(nation);

                new NationGUI(player, nation).open();
                // 4) Confirm
                player.sendMessage(Chat.greenFade("Nationsbanner aktualisiert!"));
            });
        }
    }

    @Override
    public void onClose(InventoryCloseEvent event) {
        // No special action needed on close
    }

    private String getPlayerName(UUID uuid) {
        return Bukkit.getOfflinePlayer(uuid).getName();
    }
}
