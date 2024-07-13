package org.nations.utils;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;

import java.util.Random;

public class ChatUtils {

    public static String chatPrefix = "[Nations] ";

    public static void sendMessage(Player p, String message) {
        p.sendMessage(MiniMessage.miniMessage().deserialize("<gradient:#88EBFF:#C9FFC2>" + chatPrefix + message + "</gradient>"));
    }

    public static void sendMessage(Player p, String message, String color) {
        p.sendMessage(MiniMessage.miniMessage().deserialize("<" + color + ">" + chatPrefix + message));
    }

    public static void sendMessage(Player p, String message, String color1, String color2) {
        p.sendMessage(MiniMessage.miniMessage().deserialize("<gradient:" + color1 + ":" + color2 + ">" + chatPrefix + message + "</gradient>"));
    }

    public static void sendRedFade(Player p, String message) {
        p.sendMessage(MiniMessage.miniMessage().deserialize("<gradient:#e3173c:#9f17e3>" + chatPrefix + message + "</gradient>"));
    }

    public static void sendGreenFade(Player p, String message) {
        p.sendMessage(MiniMessage.miniMessage().deserialize("<gradient:#17e373:#17e332>" + chatPrefix + message + "</gradient>"));
    }

    public static void sendBlueFade(Player p, String message) {
        p.sendMessage(MiniMessage.miniMessage().deserialize("<gradient:#1669f7:#16d6f7>" + chatPrefix + message + "</gradient>"));
    }

    public static String getRandomColor() {
        Random random = new Random();
        int nextInt = random.nextInt(0xffffff + 1);
        return String.format("#%06x", nextInt);
    }
}
