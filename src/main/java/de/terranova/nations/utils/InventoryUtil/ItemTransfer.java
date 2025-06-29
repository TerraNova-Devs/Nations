package de.terranova.nations.utils.InventoryUtil;

import com.nexomc.nexo.api.NexoItems;
import org.apache.commons.lang3.EnumUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ItemTransfer {

    public static Integer charge(Player player, String itemString, int amount, boolean onlyFullCharge) {
        ItemStack item = resolveItem(itemString);
        ItemStack[] inventory = player.getInventory().getContents();

        // Berechnung des Gesamtlagerbestands, falls nur vollständige Abbuchung erlaubt ist
        if (onlyFullCharge && countTotalItems(inventory, item) < amount) {
            return -1;
        }

        int remaining = amount;

        for (int i = 0; i < inventory.length && remaining > 0; i++) {
            ItemStack stack = inventory[i];
            if (stack == null || !stack.isSimilar(item)) continue;

            int stackAmount = stack.getAmount();
            if (stackAmount <= remaining) {
                inventory[i] = null;
                remaining -= stackAmount;
            } else {
                stack.setAmount(stackAmount - remaining);
                remaining = 0;
            }
        }

        player.getInventory().setContents(inventory);
        player.updateInventory();
        return amount - remaining;
    }

    public static Integer credit(Player player, String itemString, int amount, boolean onlyFullCredit) {
        ItemStack item = resolveItem(itemString);
        ItemStack[] inventory = player.getInventory().getContents();

        // Berechnung des maximal verfügbaren Platzes
        if (onlyFullCredit && countAvailableSpace(inventory, item) < amount) {
            return -1;
        }

        int remaining = amount;

        for (int i = 0; i < inventory.length && remaining > 0; i++) {
            ItemStack stack = inventory[i];

            if (stack == null) {
                int addable = Math.min(remaining, 64);
                inventory[i] = item.asQuantity(addable);
                remaining -= addable;
            } else if (stack.isSimilar(item)) {
                int addable = Math.min(remaining, 64 - stack.getAmount());
                stack.setAmount(stack.getAmount() + addable);
                remaining -= addable;
            }
        }

        player.getInventory().setContents(inventory);
        player.updateInventory();
        return amount - remaining;
    }

    // Hilfsmethode: Item auflösen
    private static ItemStack resolveItem(String itemString) {
        if (NexoItems.exists(itemString)) {
            return NexoItems.itemFromId(itemString).build();
        } else if (EnumUtils.isValidEnum(Material.class, itemString)) {
            return new ItemStack(Material.valueOf(itemString));
        } else {
            throw new IllegalArgumentException("Folgendes Item konnte nicht gefunden werden:" + itemString);
        }
    }

    // Hilfsmethode: Gesamte Menge eines Items im Inventar zählen
    private static int countTotalItems(ItemStack[] inventory, ItemStack item) {
        int total = 0;
        for (ItemStack stack : inventory) {
            if (stack != null && stack.isSimilar(item)) {
                total += stack.getAmount();
            }
        }
        return total;
    }

    // Hilfsmethode: Verfügbaren Platz für ein Item im Inventar zählen
    private static int countAvailableSpace(ItemStack[] inventory, ItemStack item) {
        int totalSpace = 0;
        for (ItemStack stack : inventory) {
            if (stack == null) {
                totalSpace += 64;
            } else if (stack.isSimilar(item)) {
                totalSpace += 64 - stack.getAmount();
            }
        }
        return totalSpace;
    }

}
