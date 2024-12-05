package de.terranova.nations.commands;

import de.terranova.nations.NationsPlugin;
import de.terranova.nations.regions.base.RegionType;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

public class CommandUtil {
    public static Map<String, Method> replacePlaceholder(Map<String, Method> commandMethods, String[] test) {
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

            if (!key.contains("$ARGUMENT")) {
                result.put(key, method);
            } else {
                String[] tokens = key.split("\\.");
                boolean canReplace = true;
                StringBuilder newKeyBuilder = new StringBuilder();

                for (int i = 0; i < tokens.length; i++) {
                    if (tokens[i].equals("$ARGUMENT")) {
                        if (i < test.length) {
                            tokens[i] = test[i];
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
    public static Method matchCommands(Map<String, Method> commandMethods, String[] test) {
        Map<String, Method> resolvedCommandMethods = CommandUtil.replacePlaceholder(commandMethods,test);
        String[] testSegments = test; // Since test is already an array of segments

        Method bestMethod = null;
        int maxMatchLength = -1;

        for (Map.Entry<String, Method> entry : resolvedCommandMethods.entrySet()) {
            String key = entry.getKey();
            String[] keySegments = key.split("\\.");

            int matchLength = 0;
            int minLength = Math.min(testSegments.length, keySegments.length);

            // Compare segments from the beginning
            for (int i = 0; i < minLength; i++) {
                if (testSegments[i].equals(keySegments[i])) {
                    matchLength++;
                } else {
                    break; // Stop at the first non-matching segment
                }
            }

            // Update the best match if current match is longer
            if (matchLength > maxMatchLength) {
                maxMatchLength = matchLength;
                bestMethod = entry.getValue();
            }
        }

        return bestMethod;
    }

    public static List<String> resolvePlaceholder(Map<String, String[]> commandTabDomains) {
        List<String> result = new ArrayList<>();
        for (Map.Entry<String, String[]> entry : commandTabDomains.entrySet()) {
            String domain = entry.getKey();
            String[] words = entry.getValue();
            int placeholderCount = countOccurrences(domain, "$ARGUMENT");
            if (placeholderCount == 0) {
                // No placeholders, add domain as is
                result.add(domain);
            } else if (placeholderCount == words.length) {
                // Replace placeholders with corresponding words
                String resolvedDomain = domain;
                for (String word : words) {
                    // Wrap the word with < and > unless it starts with $
                    String replacementWord = word.startsWith("$") ? word : "<" + word + ">";
                    // Escape the replacement string
                    String safeWord = Matcher.quoteReplacement(replacementWord);
                    resolvedDomain = resolvedDomain.replaceFirst("\\$ARGUMENT", safeWord);
                }
                result.add(resolvedDomain);
            } else {
                // Skip entries where placeholders don't match words
                continue;
            }
        }
        return result;
    }

    private static int countOccurrences(String str, String sub) {
        int count = 0;
        int idx = 0;
        while ((idx = str.indexOf(sub, idx)) != -1) {
            count++;
            idx += sub.length();
        }
        return count;
    }


}
