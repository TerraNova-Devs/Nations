package de.terranova.nations.gui;


import de.mcterranova.terranovaLib.roseGUI.RoseGUI;
import de.mcterranova.terranovaLib.roseGUI.RoseItem;
import de.mcterranova.terranovaLib.utils.Chat;
import de.terranova.nations.regions.access.TownAccess;
import de.terranova.nations.regions.access.TownAccessControlled;
import de.terranova.nations.regions.access.TownAccessLevel;
import de.terranova.nations.regions.grid.SettleRegion;
import de.terranova.nations.regions.npc.NPCSkins;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

public class TownGUI extends RoseGUI {

    private SettleRegion settle;

    public TownGUI(Player player, SettleRegion settle) {
        super(player, "town-gui", Chat.blueFade("<b>Stadt Menü - " + settle.getName()), 5);
        this.settle = settle;
    }

    @Override
    public void onOpen(InventoryOpenEvent event) {
        if(!(settle instanceof TownAccessControlled access)) {
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
                .material(Material.ARMOR_STAND)
                .displayName(Chat.yellowFade("<b>Skins"))
                .addLore(Chat.cottonCandy("<i>Hier klicken um den Skin zu ändern."))
                .build();

        RoseItem players = new RoseItem.Builder()
                .material(Material.PLAYER_HEAD)
                .displayName(Chat.yellowFade("<b>Einwohner"))
                .addLore(Chat.cottonCandy("<i>Hier klicken um die Einwohner zu sehen."))
                .build();

        RoseItem upgrades = new RoseItem.Builder()
                .material(Material.IRON_INGOT)
                .displayName(Chat.yellowFade("<b>Verbesserungen"))
                .addLore(Chat.cottonCandy("<i>Hier klicken um die Stadt zu verbessern."))
                .build();

        RoseItem farm = new RoseItem.Builder()
                .material(Material.GRASS_BLOCK)
                .displayName(Chat.yellowFade("<b>Farmwelt"))
                .addLore(Chat.cottonCandy("<i>Hier klicken um die Farmwelt zu betreten."))
                .build();

        RoseItem settings = new RoseItem.Builder()
                .material(Material.COMPARATOR)
                .displayName(Chat.yellowFade("<b>Einstellungen"))
                .addLore(Chat.cottonCandy("<i>Hier kannst du deine Stadt einstellen."))
                .build();

        addItem(13, level);
        addItem(19, skins);
        addItem(21, players);
        addItem(23, upgrades);
        addItem(25, farm);
        addItem(31, settings);

        upgrades.onClick(e -> {
            if (!player.hasPermission("nations.menu.upgrades")) return;
            if(TownAccess.hasAccess(access.getAccess().getAccessLevel(player.getUniqueId()), TownAccessLevel.COUNCIL)  || player.isOp()){
                new TownUpgradeGUI(player, settle).open();
            } else player.sendMessage(Chat.errorFade("Dein Rang in der Stadt ist leider nicht hoch genug um hierauf zuzugreifen."));
        });

        settings.onClick(e -> {
            if (!player.hasPermission("nations.menu.settings")) return;
            if (TownAccess.hasAccess(access.getAccess().getAccessLevel(player.getUniqueId()), TownAccessLevel.COUNCIL) || player.isOp()) {
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

        players.onClick(e -> {
            if (!player.hasPermission("nations.menu.players")) return;
            new TownPlayersGUI(player, settle, access).open();
        });

        farm.onClick(e -> {
            if (!player.hasPermission("nations.menu.farm")) return;
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "send " + player.getName() + " farmwelt");
        });
    }

    @Override
    public void onClose(InventoryCloseEvent event) {

    }

}
