package de.terranova.nations.discord;
import de.terranova.nations.NationsPlugin;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

/**
 * A simple object-oriented Discord webhook sender.
 * Compatible with Java 11+.
 */
public class DiscordWebhook {

    private final String webhookUrl;
    private final HttpClient httpClient;

    public DiscordWebhook(String webhookUrl) {
        this.webhookUrl = webhookUrl;
        this.httpClient = HttpClient.newHttpClient();
    }

    /**
     * Sends a simple text message to the Discord webhook.
     */
    public void sendMessage(String content) {
        sendRawJson("{\"content\": \"" + escapeJson(content) + "\"}");
    }

    /**
     * Sends an embed message (JSON format).
     * Use this only if you want full control.
     */
    public void sendEmbed(String jsonEmbed) {
        sendRawJson(jsonEmbed);
    }

    /**
     * Internal method to send JSON to Discord.
     */
    private void sendRawJson(String json) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(webhookUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            NationsPlugin.plugin.getLogger().info("[DiscordWebhook] Sent -> " + response.statusCode() + ": " + response.body());
        } catch (Exception e) {
            NationsPlugin.plugin.getLogger().severe("[DiscordWebhook] Failed to send message: " + e.getMessage());
        }
    }

    /**
     * Escapes double quotes and backslashes for JSON safety.
     */
    private String escapeJson(String text) {
        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}


