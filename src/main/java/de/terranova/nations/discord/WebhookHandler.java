package de.terranova.nations.discord;

import de.terranova.nations.NationsPlugin;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.UUID;

public class WebhookHandler {

    private final String webhookUrl;
    private final HttpClient httpClient;

    public WebhookHandler(String webhookUrl) {
        this.webhookUrl = webhookUrl;
        this.httpClient = HttpClient.newHttpClient();
    }

    public void send(WebhookMessage message) {
        try {
            HttpRequest request;
            if (message.hasAttachments()) {
                request = buildMultipartRequest(message);
            } else {
                request = buildJsonRequest(message);
            }

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            NationsPlugin.plugin.getLogger().info("[DiscordWebhook] Sent -> " + response.statusCode() + ": " + response.body());
        } catch (Exception e) {
            NationsPlugin.plugin.getLogger().severe("[DiscordWebhook] Failed to send message: " + e.getMessage());
        }
    }

    private HttpRequest buildJsonRequest(WebhookMessage message) {
        String json = message.toJson();
        return HttpRequest.newBuilder()
                .uri(URI.create(webhookUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .build();
    }

    private HttpRequest buildMultipartRequest(WebhookMessage message) throws Exception {
        String boundary = "----DiscordWebhookBoundary" + UUID.randomUUID();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // payload_json part
        String payloadJsonPart =
                "--" + boundary + "\r\n" +
                        "Content-Disposition: form-data; name=\"payload_json\"\r\n" +
                        "Content-Type: application/json\r\n\r\n" +
                        message.toJson() + "\r\n";
        baos.write(payloadJsonPart.getBytes(StandardCharsets.UTF_8));

        // file parts
        int index = 0;
        for (WebhookMessage.Attachment att : message.getAttachments()) {
            baos.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
            baos.write(("Content-Disposition: form-data; name=\"files[" + index + "]\"; filename=\"" + att.fileName() + "\"\r\n")
                    .getBytes(StandardCharsets.UTF_8));
            baos.write(("Content-Type: application/octet-stream\r\n\r\n").getBytes(StandardCharsets.UTF_8));
            baos.write(Files.readAllBytes(att.path()));
            baos.write("\r\n".getBytes(StandardCharsets.UTF_8));
            index++;
        }

        baos.write(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));

        return HttpRequest.newBuilder()
                .uri(URI.create(webhookUrl))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(baos.toByteArray()))
                .build();
    }
}



