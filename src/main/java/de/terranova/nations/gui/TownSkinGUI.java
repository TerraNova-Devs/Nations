package de.terranova.nations.gui;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import de.mcterranova.bona.lib.chat.Chat;
import de.terranova.nations.NationsPlugin;
import de.terranova.nations.settlements.TownSkins;
import de.terranova.nations.settlements.settlement;
import mc.obliviate.inventory.Gui;
import mc.obliviate.inventory.Icon;
import mc.obliviate.inventory.pagination.PaginationManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.UUID;

public class TownSkinGUI extends Gui {

    private static Method metaSetProfileMethod;
    private final PaginationManager pagination = new PaginationManager(this);

    public TownSkinGUI(Player player, int townskins) {
        super(player, "town-skin-gui", Chat.blueFade("Skin Menu"), townskins);
        this.pagination.registerPageSlotsBetween(10, 44);
    }

    @Override
    public void onOpen(InventoryOpenEvent event) {
        ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta mfiller = filler.getItemMeta();
        mfiller.displayName(Chat.stringToComponent(""));
        filler.setItemMeta(mfiller);
        fillGui(filler);
        int index = 0;

        ItemStack back = new ItemStack(Material.SPECTRAL_ARROW);
        ItemMeta mback = back.getItemMeta();
        mback.displayName(Chat.redFade("<b>Go Back</b>"));
        back.setItemMeta(mback);
        Icon iback = new Icon(back);
        addItem(18, iback);
        iback.onClick(e -> {
            new TownGUI(player).open();
        });

        for (TownSkins skin : TownSkins.values()) {

            // SKININVENTAR AUTOMATISCHEN ZEILENUMBRUCH UND SEITEN EINFÜGEN

            Optional<settlement> settlement = JavaPlugin.getPlugin(NationsPlugin.class).settlementManager.checkIfPlayerIsWithinClaim(player);
            if (settlement.isPresent()) {
                ItemStack skull = new ItemStack(Material.PLAYER_HEAD, 1);
                SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
                mutateSkullMetaSkinBy64(skin.getSkinTexture(), skullMeta);
                skull.setItemMeta(skullMeta);
                Icon iconSkull = new Icon(skull);
                addItem(index + 10, iconSkull);
                iconSkull.onClick(e -> {
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

    private void mutateSkullMetaSkinBy64(String b64, SkullMeta skullMeta) {

        try {
            metaSetProfileMethod = skullMeta.getClass().getDeclaredMethod("setProfile", GameProfile.class);
            metaSetProfileMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        UUID id = new UUID(b64.substring(b64.length() - 20).hashCode(), b64.substring(b64.length() - 10).hashCode());
        GameProfile profile = new GameProfile(id, "Player");
        profile.getProperties().put("textures", new Property("textures", b64));
        try {
            metaSetProfileMethod.invoke(skullMeta, profile);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }


    }

}
