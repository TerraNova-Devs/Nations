package de.terranova.nations.utils;

import de.terranova.nations.NationsPlugin;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class SecretsReader {

    private static final Map<String, String> VALUES = new HashMap<>();
    private static boolean initialized = false;

    // ---- Public static fields ---- //
    public static String DISCORD_WEBHOOK_URL;
    public static String DATABASE_USERNAME;
    public static String DATABASE_PASSWORD;

    private SecretsReader() {}

    /**
     * Initializes the secrets. Call this once in onEnable().
     * Loads server secrets first, then fills missing keys from the resource copy.
     */
    public static synchronized void init() {
        if (initialized) {
            NationsPlugin.plugin.getLogger().info("[SecretsReader] Already initialized.");
            return;
        }

        NationsPlugin.plugin.getLogger().info("[SecretsReader] Initializing...");

        // clear old values (in case of reload)
        VALUES.clear();

        // 1) Load server-side secrets (plugins/Nations/secrets.env)
        File serverSecrets = new File(NationsPlugin.plugin.getDataFolder(), "secrets.env");
        loadFromFile(serverSecrets, false);

        // 2) Fallback to secrets.env inside the plugin JAR (resources)
        loadFromResource("secrets.env", true);

        // 3) Initialize constants from loaded map
        DISCORD_WEBHOOK_URL = value("DISCORD_WEBHOOK_URL");
        DATABASE_USERNAME   = value("DATABASE_USERNAME");
        DATABASE_PASSWORD   = value("DATABASE_PASSWORD");

        initialized = true;
        NationsPlugin.plugin.getLogger().info("[SecretsReader] Initialization complete. Loaded " + VALUES.size() + " secrets.");
    }

    // ---- Helpers ---- //

    private static void loadFromFile(File file, boolean onlyIfMissing) {
        if (!file.exists()) {
            NationsPlugin.plugin.getLogger().warning("[SecretsReader] Server secrets file not found: " + file.getAbsolutePath());
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                parseLine(line, onlyIfMissing);
            }
            NationsPlugin.plugin.getLogger().info("[SecretsReader] Loaded secrets from server file: " + file.getAbsolutePath());
        } catch (IOException e) {
            NationsPlugin.plugin.getLogger().severe("[SecretsReader] Failed to read " + file.getName() + ": " + e.getMessage());
        }
    }

    private static void loadFromResource(String resourceName, boolean onlyIfMissing) {
        try (InputStream in = NationsPlugin.plugin.getResource(resourceName)) {
            if (in == null) {
                NationsPlugin.plugin.getLogger().warning("[SecretsReader] Resource " + resourceName + " not found in JAR.");
                return;
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    parseLine(line, onlyIfMissing);
                }
            }
            NationsPlugin.plugin.getLogger().info("[SecretsReader] Loaded fallback secrets from resource: " + resourceName);
        } catch (IOException e) {
            NationsPlugin.plugin.getLogger().severe("[SecretsReader] Failed to read resource " + resourceName + ": " + e.getMessage());
        }
    }

    private static void parseLine(String line, boolean onlyIfMissing) {
        line = line.trim();
        if (line.isEmpty() || line.startsWith("#")) return;

        String[] parts = line.split("=", 2);
        if (parts.length != 2) return;

        String key = parts[0].trim();
        String value = parts[1].trim();

        if (onlyIfMissing) {
            VALUES.putIfAbsent(key, value);
        } else {
            VALUES.put(key, value);
        }
    }

    private static String value(String key) {
        return VALUES.getOrDefault(key, "");
    }
}

