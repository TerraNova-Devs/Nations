package de.terranova.nations.gui;


import de.terranova.nations.regions.grid.SettleRegion;
import de.terranova.nations.regions.modules.access.Access;
import de.terranova.nations.regions.modules.access.AccessLevel;
import de.terranova.nations.regions.modules.npc.NPCSkins;
import de.terranova.nations.utils.Chat;
import de.terranova.nations.utils.roseGUI.RoseGUI;
import de.terranova.nations.utils.roseGUI.RoseItem;
import de.terranova.nations.utils.roseGUI.RosePagination;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

import java.lang.reflect.Method;

public class TownSkinGUI extends RoseGUI {

    private static Method metaSetProfileMethod;
    private final RosePagination pagination = new RosePagination(this);
    SettleRegion settle;

    public TownSkinGUI(Player player, int townskins, SettleRegion settle) {
        super(player, "town-skin-gui", Chat.blueFade("<b>Skins"), townskins);
        this.pagination.registerPageSlotsBetween(10, 44);
        this.settle = settle;
    }

    @Override
    public void onOpen(InventoryOpenEvent event) {


        RoseItem filler = new RoseItem.Builder()
                .material(Material.BLACK_STAINED_GLASS_PANE)
                .displayName("")
                .build();
        fillGui(filler);

        RoseItem back = new RoseItem.Builder()
                .material(Material.SPECTRAL_ARROW)
                .displayName(Chat.yellowFade("<b>Zurück</b>"))
                .build();
        back.onClick(e -> {
            new TownGUI(player, settle).open();
        });

        addItem(18, back);

        int index = 0;

        for (NPCSkins skin : NPCSkins.values()) {

            // SKININVENTAR AUTOMATISCHEN ZEILENUMBRUCH UND SEITEN EINFÜGEN

            RoseItem skull = new RoseItem.Builder()
                    .setSkull(skin.getSkinTexture())
                    .displayName(Chat.yellowFade("<b>" + WordUtils.capitalize(skin.name().replaceAll("_", " ").toLowerCase())))
                    .addLore(settle.getRank().getLevel() >= skin.getLEVEL() ? Chat.greenFade("Level: " + skin.getLEVEL()) : Chat.redFade("Level: " + skin.getLEVEL()))
                    .build();
            addItem(index + 10, skull);
            skull.onClick(e -> {
                if (!Access.hasAccess(settle.getAccess().getAccessLevel(player.getUniqueId()), AccessLevel.VICE)) {
                    player.sendMessage(Chat.errorFade("Du musst mindestens Vize sein um den Skin ändern zu können."));
                    return;
                }
                if (settle.getRank().getLevel() >= skin.getLEVEL()) settle.getNPC().reskinNpc(skin);
            });
            index++;

        }
    }

    @Override
    public void onClose(InventoryCloseEvent event) {

    }

}
