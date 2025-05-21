package de.nekyia.nations.command;

import de.nekyia.nations.utils.Chat;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.*;

public class DomainCommandResolver {

    private final Map<String, Method> commandMethods;

    // Constructor
    public DomainCommandResolver(Map<String, Method> commandMethods) {
        this.commandMethods = commandMethods;
    }

    // Method to replace placeholders in command methods
    public Map<String, Method> replacePlaceholder(String[] input) {
        Map<String, Method> result = new HashMap<>();
        Set<String> exactKeys = new HashSet<>();

        // Collect exact keys
        for (String key : commandMethods.keySet()) {
            if (!key.contains("$ARGUMENT")) {
                exactKeys.add(key);
            }
        }

        for (Map.Entry<String, Method> entry : commandMethods.entrySet()) {
            String key = entry.getKey();
            Method method = entry.getValue();

            if (!key.contains("$") && !key.contains("%")) {
                result.put(key, method);
            } else {
                String[] tokens = key.split("\\.");
                boolean canReplace = true;
                StringBuilder newKeyBuilder = new StringBuilder();

                for (int i = 0; i < tokens.length; i++) {
                    if (tokens[i].startsWith("$")) {
                        if (i < input.length) {
                            tokens[i] = input[i];
                        } else {
                            canReplace = false;
                            break;
                        }
                    } else if (tokens[i].startsWith("%")) {
                        if (i < input.length) {
                            // Add all remaining elements of input to tokens starting at the current index
                            StringBuilder remainingArguments = new StringBuilder();
                            for (int j = i; j < input.length; j++) {
                                if (remainingArguments.length() > 0) {
                                    remainingArguments.append(".");
                                }
                                remainingArguments.append(input[j]);
                            }
                            tokens[i] = remainingArguments.toString(); // Replace $ARGUMENT... with combined elements
                        } else {
                            canReplace = false;
                            break;
                        }
                    }
                    // Build the new key
                    if (i > 0) {
                        newKeyBuilder.append(".");
                    }
                    newKeyBuilder.append(tokens[i]);
                }

                if (canReplace) {
                    String newKey = newKeyBuilder.toString();
                    if (!exactKeys.contains(newKey)) {
                        result.put(newKey, method);
                    }
                }
            }
        }

        return result;
    }

    // Method to match commands based on input test array
    public Method matchCommands(String[] input, Player p) {
        Map<String, Method> resolvedCommandMethods = replacePlaceholder(input);

        // Join the input array with dots
        String joinedCommand = String.join(".", input);
        // Look up the exact match
        Method exactMatch = resolvedCommandMethods.get(joinedCommand);
        if (exactMatch != null) {
            return exactMatch;
        }

        //Find Domains which the user could have wanted
        List<String> bestMatches = new ArrayList<>();

        for (String key : commandMethods.keySet()) {
            if (key.startsWith(joinedCommand) || joinedCommand.startsWith(key)) {
                bestMatches.add(key);
            }
        }

        if (!bestMatches.isEmpty()) {
            p.sendMessage(Chat.errorFade("Possible Commands:"));
            bestMatches.forEach(match -> {
                CommandAnnotation commandAnnotation = commandMethods.get(match).getAnnotation(CommandAnnotation.class);
                if (commandAnnotation != null) {
                    p.sendMessage(Chat.greenFade("Did you mean: " + commandAnnotation.usage() + "?"));
                }
            });
            return null;
        }
        //Checking if a command with arguments could have been wanted
        while (joinedCommand.contains(".")) {
            // Prüfen, ob der aktuelle Domain-String in der Liste enthalten ist
            for (String key : commandMethods.keySet()) {
                if (key.startsWith(joinedCommand) || joinedCommand.startsWith(key)) {
                    bestMatches.add(key);
                }
            }
            // Entferne alles nach dem letzten Punkt
            joinedCommand = joinedCommand.substring(0, joinedCommand.lastIndexOf("."));
        }

        if (!bestMatches.isEmpty()) {
            p.sendMessage(Chat.errorFade("Possible Commands:"));
            bestMatches.forEach(match -> {
                CommandAnnotation commandAnnotation = commandMethods.get(match).getAnnotation(CommandAnnotation.class);
                if (commandAnnotation != null) {
                    p.sendMessage(Chat.greenFade("Did you mean: " + commandAnnotation.usage() + "?"));
                }
            });
            return null;
        }

        p.sendMessage(Chat.errorFade("Es konnte kein command gefunden werden."));
        return null;
    }

}
