package de.terranova.nations.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SecretsReader {
    private static final Map<String, String> VALUES = new HashMap<>();
    private static final File SECRETS_FILE = new File("secrets.env");
    private static final File BLUEPRINT_FILE = new File("secrets.env.blueprint");

    // ---- Public static final values (auto-filled at class load) ---- //
    public static final String DISCORD_WEBHOOK_URL;
    public static final String DATABASE_USERNAME;
    public static final String DATABASE_PASSWORD;

    static {
        load(BLUEPRINT_FILE, true);   // load defaults first
        load(SECRETS_FILE, false);    // override with real values

        // initialize constants from loaded map
        DISCORD_WEBHOOK_URL = value("DISCORD_WEBHOOK_URL");
        DATABASE_USERNAME = value("DATABASE_USERNAME");
        DATABASE_PASSWORD = value("DATABASE_PASSWORD");
    }

    private SecretsReader() {}

    // ---- Helpers ---- //

    private static void load(File file, boolean isBlueprint) {
        if (!file.exists()) {
            if (isBlueprint)
                System.err.println("[SecretsReader] Missing secrets.env.blueprint (required default values).");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                String[] parts = line.split("=", 2);
                if (parts.length != 2) continue;

                String key = parts[0].trim();
                String value = parts[1].trim();

                // blueprint only sets defaults, actual secrets override
                if (isBlueprint && !VALUES.containsKey(key)) {
                    VALUES.put(key, value);
                } else if (!isBlueprint) {
                    VALUES.put(key, value);
                }
            }
        } catch (IOException e) {
            System.err.println("[SecretsReader] Failed to read " + file.getName() + ": " + e.getMessage());
        }
    }

    private static String value(String key) {
        return VALUES.getOrDefault(key, "");
    }
}
