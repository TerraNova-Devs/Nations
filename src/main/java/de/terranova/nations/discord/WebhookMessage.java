package de.terranova.nations.discord;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Builder for Discord Webhook messages.
 * Supports: content, username, avatar, embeds, components, attachments.
 */
public class WebhookMessage {

    private String content;
    private String username;
    private String avatarUrl;
    private boolean tts;
    private final List<String> embeds = new ArrayList<>();
    private final List<String> components = new ArrayList<>();
    private final List<Attachment> attachments = new ArrayList<>();

    private WebhookMessage() {
    }

    public static WebhookMessage builder() {
        return new WebhookMessage();
    }

    public WebhookMessage content(String content) {
        this.content = content;
        return this;
    }

    public WebhookMessage username(String username) {
        this.username = username;
        return this;
    }

    public WebhookMessage avatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
        return this;
    }

    public WebhookMessage tts(boolean tts) {
        this.tts = tts;
        return this;
    }

    /**
     * Add a raw embed JSON string (e.g. {"title":"...","description":"..."}).
     * You can also build this with your own embed builder if you like.
     */
    public WebhookMessage addEmbed(String embedJson) {
        this.embeds.add(embedJson);
        return this;
    }

    /**
     * Add a component (e.g. button) as raw JSON.
     */
    public WebhookMessage addComponent(String componentJson) {
        this.components.add(componentJson);
        return this;
    }

    /**
     * Add a file to send alongside the message.
     */
    public WebhookMessage addAttachment(Path path, String fileName) {
        this.attachments.add(new Attachment(path, fileName));
        return this;
    }

    public boolean hasAttachments() {
        return !attachments.isEmpty();
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }

    /**
     * Builds the JSON part of the message for Discord.
     */
    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");

        if (content != null) {
            sb.append("\"content\":\"").append(escapeJson(content)).append("\",");
        }
        if (username != null) {
            sb.append("\"username\":\"").append(escapeJson(username)).append("\",");
        }
        if (avatarUrl != null) {
            sb.append("\"avatar_url\":\"").append(escapeJson(avatarUrl)).append("\",");
        }
        if (tts) {
            sb.append("\"tts\":true,");
        }

        // embeds (as array of raw JSON)
        if (!embeds.isEmpty()) {
            sb.append("\"embeds\":[");
            for (int i = 0; i < embeds.size(); i++) {
                sb.append(embeds.get(i));
                if (i < embeds.size() - 1) sb.append(",");
            }
            sb.append("],");
        }

        // components (array)
        if (!components.isEmpty()) {
            sb.append("\"components\":[");
            for (int i = 0; i < components.size(); i++) {
                sb.append(components.get(i));
                if (i < components.size() - 1) sb.append(",");
            }
            sb.append("],");
        }

        // remove trailing comma if present
        if (sb.charAt(sb.length() - 1) == ',') {
            sb.deleteCharAt(sb.length() - 1);
        }

        sb.append("}");
        return sb.toString();
    }

    private String escapeJson(String text) {
        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }

    public record Attachment(Path path, String fileName) {}
}
