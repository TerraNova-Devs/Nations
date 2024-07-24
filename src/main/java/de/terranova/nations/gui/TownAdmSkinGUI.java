package de.terranova.nations.gui;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import de.terranova.nations.NationsPlugin;
import de.terranova.nations.settlements.TownSkins;
import de.terranova.nations.settlements.settlement;
import de.terranova.nations.utils.ChatUtils;
import mc.obliviate.inventory.Gui;
import mc.obliviate.inventory.Icon;
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

public class TownAdmSkinGUI extends Gui {
  private static Method metaSetProfileMethod;

  public TownAdmSkinGUI(Player player, int townskins) {
    super(player, "town-admn-skin-gui", ChatUtils.returnBlueFade("Skin Menu"), townskins);
  }

  @Override
  public void onOpen(InventoryOpenEvent event) {

    ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
    ItemMeta mfiller = filler.getItemMeta();
    mfiller.displayName(ChatUtils.stringToComponent(""));
    filler.setItemMeta(mfiller);
    fillGui(filler);

    int index = 0;
    for (TownSkins skin : TownSkins.values()) {

      // SKININVENTAR AUTOMATISCHEN ZEILENUMBRUCH UND SEITEN EINFÜGEN

      Optional<settlement> settlement = JavaPlugin.getPlugin(NationsPlugin.class).settlementManager.checkIfPlayerIsInsideHisClaim(player);
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
        player.sendMessage(ChatUtils.returnRedFade(ChatUtils.chatPrefix + "Bitte befinde dich innerhalb deines Claimes für diese Aktion."));
        player.sendMessage(ChatUtils.returnRedFade(ChatUtils.chatPrefix + "Meistens hilft schon ein kleiner Schritt zur Seite. ^^"));
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
