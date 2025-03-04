package de.terranova.nations.utils;

import it.unimi.dsi.fastutil.io.FastByteArrayInputStream;
import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.*;
import java.util.Base64;

public class ItemStackSerializer {

    public static ItemStack getItemStackFromBase64String(
            final String base64
    ) {
        if (base64 == null || base64.isEmpty()) return null;
        try {
            FastByteArrayInputStream inputStream = new FastByteArrayInputStream(Base64Coder.decodeLines(base64));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            ItemStack item = (ItemStack)dataInput.readObject();
            dataInput.close();
            return item;
        } catch (final Exception exception) {
            throw new IllegalArgumentException(exception);
        }
    }

    public static String getBase64StringFromItemStack(
            final ItemStack item
    ) {
        try {
            FastByteArrayOutputStream outputStream = new FastByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            dataOutput.writeObject(item);
            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.array);
        } catch (final Exception exception) {
            throw new IllegalArgumentException(exception);
        }
    }
}
