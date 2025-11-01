package de.terranova.nations.gui;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import de.terranova.nations.NationsPlugin;
import de.terranova.nations.gui.nations.NationGUI;
import de.terranova.nations.nations.Nation;
import de.terranova.nations.regions.grid.SettleRegion;
import de.terranova.nations.regions.modules.access.Access;
import de.terranova.nations.regions.modules.access.AccessControlled;
import de.terranova.nations.regions.modules.access.AccessLevel;
import de.terranova.nations.regions.modules.npc.NPCSkins;
import de.mcterranova.terranovaLib.utils.Chat;
import de.mcterranova.terranovaLib.roseGUI.RoseGUI;
import de.mcterranova.terranovaLib.roseGUI.RoseItem;
import de.terranova.nations.regions.modules.rank.Rank;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;

public class TownGUI extends RoseGUI {

  private final SettleRegion settle;

  public TownGUI(Player player, SettleRegion settle) {
    super(player, "town-gui", Chat.blueFade("<b>Stadt Menü - " + settle.getName()), 5);
    this.settle = settle;
  }

  @Override
  public void onOpen(InventoryOpenEvent event) {
    if (!(settle instanceof AccessControlled access)) {
      return;
    }

    Nation nation = NationsPlugin.nationManager.getNationBySettlement(settle.getId());

    RoseItem filler =
        new RoseItem.Builder()
            .showTooltip(false)
            .material(Material.BLACK_STAINED_GLASS_PANE)
            .build();
    fillGui(filler);
    RoseItem level =
        new RoseItem.Builder()
            .material(Material.NETHER_STAR)
            .displayName(Chat.redFade("<b>Stadtlevel: " + settle.getRank().getLevel()))
            .addLore(
                Chat.cottonCandy(
                    String.format("<i>%s/%s Claims", settle.getClaims(), settle.getMaxClaims())))
          .addLore(
                    Chat.cottonCandy(String.format("<i>%s/%s Plot Volume", settle.getMaximalRegionPoints() - settle.getAvaibleRegionPoints(),settle.getMaximalRegionPoints())))
            .build();

    RoseItem skins =
        new RoseItem.Builder()
            .material(Material.ARMOR_STAND)
            .displayName(Chat.yellowFade("<b>Skins"))
            .addLore(Chat.cottonCandy("<i>Hier klicken um den Skin zu ändern."))
            .build();

    RoseItem players =
        new RoseItem.Builder()
            .material(Material.PLAYER_HEAD)
            .displayName(Chat.yellowFade("<b>Einwohner"))
            .addLore(Chat.cottonCandy("<i>Hier klicken um die Einwohner zu sehen."))
            .build();

    RoseItem upgrades =
        new RoseItem.Builder()
            .material(Material.IRON_INGOT)
            .displayName(Chat.yellowFade("<b>Verbesserungen"))
            .addLore(Chat.cottonCandy("<i>Hier klicken um die Stadt zu verbessern."))
            .build();

    RoseItem farm =
        new RoseItem.Builder()
            .material(Material.GRASS_BLOCK)
            .displayName(Chat.yellowFade("<b>Farmwelt"))
            .addLore(Chat.cottonCandy("<i>Hier klicken um die Farmwelt zu betreten."))
            .build();

    RoseItem settings =
        new RoseItem.Builder()
            .material(Material.COMPARATOR)
            .displayName(Chat.yellowFade("<b>Einstellungen"))
            .addLore(Chat.cottonCandy("<i>Hier kannst du deine Stadt einstellen."))
            .build();

    RoseItem professionsButton =
        new RoseItem.Builder()
            .material(Material.BOOK)
            .displayName(Chat.yellowFade("<b>Professionen"))
            .addLore(Chat.cottonCandy("Öffnet das Professionen-Menü"))
            .build();

    ItemStack nationItem;
    if (nation == null) {
      // No nation => default item or maybe a gray banner
      nationItem = new ItemStack(Material.GRAY_BANNER);
    } else {
      // If the nation has a custom banner, use that. Else default banner
      ItemStack customBanner = nation.getBanner();
      if (customBanner != null) {
        nationItem =
            customBanner.clone(); // safer to clone so we don't accidentally modify reference
      } else {
        nationItem = new ItemStack(Material.WHITE_BANNER);
      }
    }

    RoseItem nations =
        new RoseItem.Builder()
            .copyStack(nationItem)
            .displayName(Chat.yellowFade("<b>Nationen"))
            .addLore(Chat.cottonCandy("<i>Hier kannst du die Nation verwalten."))
            .build();

    addItem(13, level);
    addItem(19, skins);
    addItem(21, players);
    addItem(23, upgrades);
    addItem(25, farm);
    addItem(28, settings);
    addItem(31, nations);
    addItem(33, professionsButton);

    upgrades.onClick(
        e -> {
          if (!player.hasPermission("nations.menu.upgrades")) return;
          if (Access.hasAccess(
                  access.getAccess().getAccessLevel(player.getUniqueId()), AccessLevel.COUNCIL)
              || player.isOp()) {
            new TownUpgradeGUI(player, settle).open();
          } else
            player.sendMessage(
                Chat.errorFade(
                    "Dein Rang in der Stadt ist leider nicht hoch genug um hierauf zuzugreifen."));
        });

    settings.onClick(
        e -> {
          if (!player.hasPermission("nations.menu.settings")) return;
          if (Access.hasAccess(
                  access.getAccess().getAccessLevel(player.getUniqueId()), AccessLevel.COUNCIL)
              || player.isOp()) {
            new TownSettingsGUI(player, settle).open();
          } else {
            player.sendMessage(Chat.errorFade("Wende dich an den Besitzer major Error."));
          }
        });

    skins.onClick(
        e -> {
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

    players.onClick(
        e -> {
          if (!player.hasPermission("nations.menu.players")) return;
          new TownPlayersGUI(player, settle, access).open();
        });

    farm.onClick(
        e -> {
          if (!player.hasPermission("nations.menu.farm")) return;
          sendToServer(player, "farmwelt");
          Bukkit.dispatchCommand(
              Bukkit.getConsoleSender(), "send " + player.getName() + " farmwelt");
        });

    nations.onClick(
        e -> {
          if (!player.hasPermission("nations.menu.nations")) return;
          if (nation == null) {
            player.sendMessage(Chat.errorFade("Diese Stadt gehört keiner Nation an."));
            return;
          }
          new NationGUI(player, nation).open();
        });

    professionsButton.onClick(
        e -> {
          new TownProfessionGUI(player, settle).open();
        });
  }

  @Override
  public void onClose(InventoryCloseEvent event) {}

  private void sendToServer(Player player, String serverName) {
    ByteArrayDataOutput out = ByteStreams.newDataOutput();

    // "Connect" tells the proxy (Velocity in your case) to move the player
    out.writeUTF("Connect");
    out.writeUTF(serverName); // must match the server name in velocity.toml

    // Send the plugin message over the "BungeeCord" channel
    player.sendPluginMessage(NationsPlugin.plugin, "BungeeCord", out.toByteArray());
  }
}
