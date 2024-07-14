package org.nations.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;

import java.util.Random;

public class ChatUtils {

    public static String chatPrefix = "[Nations] ";

    public static void sendMessage(Player p, String message) {
        p.sendMessage(MiniMessage.miniMessage().deserialize("<gradient:#88EBFF:#C9FFC2>" + chatPrefix + message + "</gradient>"));
    }

    public static Component returnRedFade(String message) {
        return MiniMessage.miniMessage().deserialize("<gradient:#e3173c:#9f17e3>" + message + "</gradient>");
    }

    public static Component returnGreenFade(String message) {
        return MiniMessage.miniMessage().deserialize("<gradient:#17e373:#17e332>" + message + "</gradient>");
    }

    public static Component returnBlueFade(String message) {
        return MiniMessage.miniMessage().deserialize("<gradient:#1669f7:#16d6f7>" + message + "</gradient>");
    }

    public static Component returnYellowFade(String message) {
        return MiniMessage.miniMessage().deserialize("<gradient:#F4F30F:#F5BD0F>" + message + "</gradient>");
    }

    public static Component stringToComponent(String message) {
        return MiniMessage.miniMessage().deserialize(message);
    }

    public static String getRandomColor() {
        Random random = new Random();
        int nextInt = random.nextInt(0xffffff + 1);
        return String.format("#%06x", nextInt);
    }
}
