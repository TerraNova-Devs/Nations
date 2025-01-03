package de.terranova.nations.gui;


import de.mcterranova.terranovaLib.roseGUI.RoseGUI;
import de.mcterranova.terranovaLib.roseGUI.RoseItem;
import de.mcterranova.terranovaLib.utils.Chat;
import de.terranova.nations.NationsPlugin;
import de.terranova.nations.regions.access.Access;
import de.terranova.nations.regions.access.AccessControlled;
import de.terranova.nations.regions.access.AccessLevel;
import de.terranova.nations.regions.grid.SettleRegionType;
import de.terranova.nations.regions.npc.NPCSkins;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

import java.util.Objects;
import java.util.Optional;

public class TownGUI extends RoseGUI {

    private SettleRegionType settle;

    public TownGUI(Player player, SettleRegionType settleRegionType) {
        super(player, "town-gui", Chat.blueFade("<b>Town Menu"), 5);
        this.settle = settleRegionType;
    }

    @Override
    public void onOpen(InventoryOpenEvent event) {
        if(!(settle instanceof AccessControlled access)) {
            return;
        }


        RoseItem filler = new RoseItem.Builder()
                .material(Material.BLACK_STAINED_GLASS_PANE)
                .displayName("")
                .build();
        fillGui(filler);

        RoseItem level = new RoseItem.Builder()
                .material(Material.NETHER_STAR)
                .displayName(Chat.redFade("<b>Stadtlevel: " + settle.getRank().getLevel()))
                .addLore(Chat.cottonCandy(String.format("<i>%s/%s Claims", settle.getClaims(),settle.getMaxClaims())))
                .build();

        RoseItem skins = new RoseItem.Builder()
                .material(Material.PLAYER_HEAD)
                .displayName(Chat.yellowFade("<b>Skins"))
                .addLore(Chat.cottonCandy("<i>Hier klicken um den Skin zu ändern."))
                .build();

        RoseItem score = new RoseItem.Builder()
                .material(Material.GOLD_INGOT)
                .displayName(Chat.yellowFade("<b>Score"))
                .addLore(Chat.cottonCandy("<i>Hier klicken fuer mehr infos."))
                .build();

        RoseItem upgrades = new RoseItem.Builder()
                .material(Material.IRON_INGOT)
                .displayName(Chat.yellowFade("<b>Upgrades"))
                .addLore(Chat.cottonCandy("<i>Hier klicken um die Stadt zu verbessern."))
                .build();

        RoseItem farm = new RoseItem.Builder()
                .material(Material.GRASS_BLOCK)
                .displayName(Chat.yellowFade("<b>Farm"))
                .addLore(Chat.cottonCandy("<i>Hier klicken um die Farmwelt zu betreten."))
                .build();

        RoseItem settings = new RoseItem.Builder()
                .material(Material.COMPARATOR)
                .displayName(Chat.yellowFade("<b>Settings"))
                .addLore(Chat.cottonCandy("<i>Hier kannst du deine Stadt einstellen."))
                .build();

        addItem(13, level);
        addItem(19, skins);
        addItem(21, score);
        addItem(23, upgrades);
        addItem(25, farm);
        addItem(31, settings);

        upgrades.onClick(e -> {
            if (!player.hasPermission("nations.menu.upgrades")) return;
            if(Access.hasAccess(access.getAccess().getAccessLevel(player.getUniqueId()), AccessLevel.COUNCIL)  || player.isOp()){
                new TownUpgradeGUI(player, settle).open();
            } else player.sendMessage(Chat.errorFade("Dein Rang in der Stadt ist leider nicht hoch genug um hierauf zuzugreifen."));
        });

        settings.onClick(e -> {
            if (!player.hasPermission("nations.menu.settings")) return;
            if (Access.hasAccess(access.getAccess().getAccessLevel(player.getUniqueId()), AccessLevel.COUNCIL) || player.isOp()) {
                new TownSettingsGUI(player, settle).open();
            } else {
                player.sendMessage(Chat.errorFade("Wende dich an den Besitzer major Error."));
            }
        });

        skins.onClick(e -> {
            if (!player.hasPermission("nations.menu.skin")) return;
            int rowsSkins = 3;
            if (NPCSkins.values().length >= 8) {
                rowsSkins = 4;
            } else if (NPCSkins.values().length >= 15) {
                rowsSkins = 5;
            } else if (NPCSkins.values().length >= 22) {
                rowsSkins = 6;
            }
            new TownSkinGUI(player, rowsSkins, settle).open();
        });
    }

    @Override
    public void onClose(InventoryCloseEvent event) {

    }

}
