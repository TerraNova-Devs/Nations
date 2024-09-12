package de.terranova.nations.gui;


import de.mcterranova.terranovaLib.roseGUI.RoseGUI;
import de.mcterranova.terranovaLib.roseGUI.RoseItem;
import de.mcterranova.terranovaLib.roseGUI.RosePagination;
import de.mcterranova.terranovaLib.utils.Chat;
import de.terranova.nations.NationsPlugin;
import de.terranova.nations.settlements.Settlement;
import de.terranova.nations.settlements.TownSkins;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Method;
import java.util.Optional;

public class TownSkinGUI extends RoseGUI {

    private static Method metaSetProfileMethod;
    private final RosePagination pagination = new RosePagination(this);

    public TownSkinGUI(Player player, int townskins) {
        super(player, "town-skin-gui", Chat.blueFade("<b>Town Skins"), townskins);
        this.pagination.registerPageSlotsBetween(10, 44);
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
                .displayName(Chat.redFade("<b>Go Back</b>"))
                .build();
        back.onClick(e -> {
            new TownGUI(player).open();
        });

        addItem(18, back);

        int index = 0;

        for (TownSkins skin : TownSkins.values()) {

            // SKININVENTAR AUTOMATISCHEN ZEILENUMBRUCH UND SEITEN EINFÜGEN

            JavaPlugin.getPlugin(NationsPlugin.class);
            Optional<Settlement> settlement = NationsPlugin.settlementManager.checkIfPlayerIsWithinClaim(player);
            if (settlement.isPresent()) {
                RoseItem skull = new RoseItem.Builder()
                        .setSkull(skin.getSkinTexture())
                        .displayName(Chat.yellowFade("<b>" + WordUtils.capitalize(skin.name().replaceAll("_", " ").toLowerCase())))
                        .addLore(settlement.get().level >= skin.getLEVEL() ? Chat.greenFade("Level: " + skin.getLEVEL()) : Chat.redFade("Level: " + skin.getLEVEL()))
                        .build();
                addItem(index + 10, skull);
                skull.onClick(e -> {
                    if (settlement.get().level >= skin.getLEVEL()) settlement.get().reskinNpc(skin);
                });
                index++;
            } else {
                player.sendMessage(Chat.redFade(String.format("Bitte befinde dich innerhalb deines Claimes f%sr diese Aktion.", 0xC3)));
                player.sendMessage(Chat.redFade("Meistens hilft schon ein kleiner Schritt zur Seite. ^^"));
                break;
            }
        }
    }

    @Override
    public void onClose(InventoryCloseEvent event) {

    }

}
