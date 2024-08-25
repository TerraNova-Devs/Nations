package de.terranova.nations.gui;

import de.mcterranova.bona.lib.chat.Chat;
import de.terranova.nations.NationsPlugin;
import de.terranova.nations.gui.guiutil.roseItem;
import de.terranova.nations.settlements.AccessLevelEnum;
import de.terranova.nations.settlements.Settlement;
import de.terranova.nations.settlements.TownSkins;
import mc.obliviate.inventory.Gui;
import mc.obliviate.inventory.Icon;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.Optional;

public class TownGUI extends Gui {

    public TownGUI(Player player) {
        super(player, "town-gui", Chat.blueFade("Town Menu"), 5);
    }

    @Override
    public void onOpen(InventoryOpenEvent event) {

        JavaPlugin.getPlugin(NationsPlugin.class);
        Optional<Settlement> OSettle = NationsPlugin.settlementManager.checkIfPlayerIsWithinClaim(player);
        if (OSettle.isEmpty()) return;
        Settlement settle = OSettle.get();
        Optional<AccessLevelEnum> OAccess = NationsPlugin.settlementManager.getAccessLevel(player, settle.id);
        if (OAccess.isEmpty()) return;
        AccessLevelEnum access = OAccess.get();

        ItemStack filler = new roseItem.Builder()
                .material(Material.BLACK_STAINED_GLASS_PANE)
                .displayName("")
                .build().stack;
        fillGui(filler);

        Icon level = new Icon(new roseItem.Builder()
                .material(Material.NETHER_STAR)
                .displayName(Chat.redFade("Stadtlevel"))
                .addLore(Chat.cottonCandy(String.format("<i>Vorteile Level %s:", settle.level)))
                .addLore(Chat.cottonCandy(String.format("<i>%s/%s Claims", settle.claims,settle.getMaxClaims())))
                .build().stack);

        Icon skins = new Icon(new roseItem.Builder()
                .material(Material.PLAYER_HEAD)
                .displayName(Chat.yellowFade("Skins"))
                .addLore(Chat.cottonCandy("<i>Hier klicken um den Skin zu ändern."))
                .build().stack);

        Icon score = new Icon(new roseItem.Builder()
                .material(Material.GOLD_INGOT)
                .displayName(Chat.yellowFade("Score"))
                .addLore(Chat.cottonCandy("<i>Hier klicken fuer mehr infos."))
                .build().stack);

        Icon upgrades = new Icon(new roseItem.Builder()
                .material(Material.IRON_INGOT)
                .displayName(Chat.yellowFade("Upgrades"))
                .addLore(Chat.cottonCandy("<i>Hier klicken um die Stadt zu verbessern."))
                .build().stack);

        Icon farm = new Icon(new roseItem.Builder()
                .material(Material.GRASS_BLOCK)
                .displayName(Chat.yellowFade("Farm"))
                .addLore(Chat.cottonCandy("<i>Hier klicken um die Farmwelt zu betreten."))
                .build().stack);

        Icon settings = new Icon(new roseItem.Builder()
                .material(Material.COMPARATOR)
                .displayName(Chat.yellowFade("Settings"))
                .addLore(Chat.cottonCandy("<i>Hier kannst du deine Stadt einstellen."))
                .build().stack);

        addItem(13, level);
        addItem(19, skins);
        addItem(21, score);
        addItem(23, upgrades);
        addItem(25, farm);
        addItem(31, settings);

        upgrades.onClick(e -> {
            if (!player.hasPermission("nations.menu.upgrades")) return;
            if (Objects.equals(access, AccessLevelEnum.MAJOR) || Objects.equals(access, AccessLevelEnum.VICE) || Objects.equals(access, AccessLevelEnum.COUNCIL) || player.isOp()) {
                new TownUpgradeGUI(player, settle).open();
            } else {
                player.sendMessage(Chat.errorFade("Wende dich an den Besitzer major Error."));
            }
        });

        settings.onClick(e -> {
            if (!player.hasPermission("nations.menu.settings")) return;
            if (Objects.equals(access, AccessLevelEnum.MAJOR) || Objects.equals(access, AccessLevelEnum.VICE) || player.isOp()) {
                new TownSettingsGUI(player, settle).open();
            } else {
                player.sendMessage(Chat.errorFade("Wende dich an den Besitzer major Error."));
            }
        });

        skins.onClick(e -> {
            if (!player.hasPermission("nations.menu.skin")) return;
            int rowsSkins = 3;
            if (TownSkins.values().length >= 8) {
                rowsSkins = 4;
            } else if (TownSkins.values().length >= 15) {
                rowsSkins = 5;
            } else if (TownSkins.values().length >= 22) {
                rowsSkins = 6;
            }
            new TownSkinGUI(player, rowsSkins).open();
        });
    }

    @Override
    public void onClose(InventoryCloseEvent event) {

    }

}
