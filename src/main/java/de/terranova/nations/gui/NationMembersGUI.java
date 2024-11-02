package de.terranova.nations.gui;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import de.mcterranova.terranovaLib.roseGUI.RoseGUI;
import de.mcterranova.terranovaLib.roseGUI.RoseItem;
import de.mcterranova.terranovaLib.utils.Chat;
import de.terranova.nations.NationsPlugin;
import de.terranova.nations.nations.Nation;
import de.terranova.nations.settlements.Settle;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

import java.util.Optional;
import java.util.UUID;

public class NationMembersGUI extends RoseGUI {
    private final Nation nation;

    public NationMembersGUI(Player player, Nation nation) {
        super(player, "nation-members-gui", Chat.blueFade("<b>Nation Members"), 5);
        this.nation = nation;
    }

    @Override
    public void onOpen(InventoryOpenEvent event) {
        fillGui(new RoseItem.Builder().material(Material.BLACK_STAINED_GLASS_PANE).displayName("").build());

        int slot = 0;
        for (UUID settleId : nation.getSettlements()) {
            Optional<Settle> settle = NationsPlugin.settleManager.getSettle(settleId);

            Material headMaterial = Material.DROPPER;

            RoseItem memberItem = new RoseItem.Builder()
                    .material(headMaterial)
                    .displayName(settle.get().name)
                    .build();

            addItem(slot++, memberItem);
        }
    }

    @Override
    public void onClose(InventoryCloseEvent event) {
        // No special action needed on close
    }
}
