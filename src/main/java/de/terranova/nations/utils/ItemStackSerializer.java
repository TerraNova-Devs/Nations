package de.terranova.nations.utils;


import org.bukkit.inventory.ItemStack;

import java.util.Base64;

public class ItemStackSerializer {

    public static ItemStack getItemStackFromBase64String(final String base64) {
        if (base64 == null || base64.isEmpty()) return null;
        return ItemStack.deserializeBytes(Base64.getDecoder().decode(base64.replaceAll("\\s", "")));
    }

    public static String getBase64StringFromItemStack(final ItemStack item) {
        if (item == null) return null;
        return Base64.getEncoder().encodeToString(item.serializeAsBytes());
    }
}
