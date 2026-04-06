package de.terranova.nations.utils;


import it.unimi.dsi.fastutil.io.FastByteArrayInputStream;
import libs.org.simpleyaml.configuration.implementation.snakeyaml.lib.external.biz.base64Coder.Base64Coder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;

import java.util.Base64;

public class ItemStackSerializer {

    @Deprecated
    // will be removed in favor of the new format
    public static ItemStack getItemStackFromBase64String(final String base64) {
        if (base64 == null || base64.isEmpty()) return null;

        // Try new format first (serializeAsBytes + standard Base64)
        try {
            return ItemStack.deserializeBytes(Base64.getDecoder().decode(base64.replaceAll("\\s", "")));
        } catch (Exception ignored) {}

        // Fallback: legacy BukkitObjectInputStream + Base64Coder format
        try {
            FastByteArrayInputStream inputStream = new FastByteArrayInputStream(Base64Coder.decodeLines(base64));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            ItemStack item = (ItemStack) dataInput.readObject();
            dataInput.close();
            return item;
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to deserialize ItemStack from Base64", e);
        }
    }

    public static String getBase64StringFromItemStack(final ItemStack item) {
        if (item == null) return null;
        return Base64.getEncoder().encodeToString(item.serializeAsBytes());
    }
}