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
        String[] testSegments = test; // Since test is already an array of segments

        Method bestMethod = null;
        int maxMatchLength = -1;

        for (Map.Entry<String, Method> entry : commandMethods.entrySet()) {
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
    public static Map<String, Object> buildDomainTree(List<String> domains) {
        Map<String, Object> tree = new HashMap<>();

        for (String domain : domains) {
            String[] parts = domain.split("\\.");
            Map<String, Object> currentLevel = tree;

            for (int i = 0; i < parts.length; i++) {
                String part = parts[i];

                if (i == parts.length - 1) {
                    // At the last level, handle as a list
                    currentLevel.putIfAbsent(part, new ArrayList<String>());
                    Object value = currentLevel.get(part);

                    // Add to the list only if it's a list
                    if (value instanceof List) {
                        @SuppressWarnings("unchecked")
                        List<String> list = (List<String>) value;
                        list.add(domain);
                    } else {
                        // Conflict resolution: a non-list exists where a list is expected
                        throw new IllegalStateException("Conflict: Expected a list at the last level but found something else.");
                    }
                } else {
                    // At intermediate levels, handle as a map
                    currentLevel.putIfAbsent(part, new HashMap<>());
                    Object value = currentLevel.get(part);

                    if (value instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> nextLevel = (Map<String, Object>) value;
                        currentLevel = nextLevel;
                    } else if (value instanceof List) {
                        // Conflict resolution: Convert a list to a map if a deeper level is needed
                        Map<String, Object> newMap = new HashMap<>();
                        currentLevel.put(part, newMap);
                        currentLevel = newMap;
                    } else {
                        throw new IllegalStateException("Conflict: Unexpected type encountered.");
                    }
                }
            }
        }
        return tree;
    }

    public static void printTree(Map<String, Object> tree, String prefix) {
        for (Map.Entry<String, Object> entry : tree.entrySet()) {
            NationsPlugin.logger.info(prefix + entry.getKey());
            if (entry.getValue() instanceof Map) {
                printTree((Map<String, Object>) entry.getValue(), prefix + "  ");
            } else if (entry.getValue() instanceof List) {
                @SuppressWarnings("unchecked")
                List<String> list = (List<String>) entry.getValue();
                NationsPlugin.logger.info(prefix + "  " + list);
            }
        }
    }

    public static List<String> resolveTree(Map<String, Object> tree, String[] args) {
        Map<String, Object> currentLevel = tree;

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];

            if (arg.isEmpty()) {
                // If the argument is empty (e.g., trailing comma), do not proceed further
                return Collections.emptyList();
            }

            Object nextNode = currentLevel.get(arg);

            if (nextNode == null) {
                // No exact match, so check for approximate matches
                String matchedKey = getApproximateMatch(arg, currentLevel.keySet());
                if (matchedKey != null) {
                    nextNode = currentLevel.get(matchedKey);
                    if (i == args.length - 1) {
                        // If this is the last argument, return the matched key
                        return List.of(matchedKey);
                    }
                } else {
                    // Check for placeholders like "<name>" or "$TYPES"
                    for (String key : currentLevel.keySet()) {
                        if (key.startsWith("<") && key.endsWith(">") || key.startsWith("$")) {
                            nextNode = currentLevel.get(key);
                            break;
                        }
                    }
                }
            }

            if (nextNode instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> nextLevel = (Map<String, Object>) nextNode;
                currentLevel = nextLevel;
            } else {
                // If we can't traverse deeper, return nothing
                return Collections.emptyList();
            }
        }

        // After traversal, return all keys at the current level
        return collectKeys(currentLevel);
    }

    private static List<String> collectKeys(Map<String, Object> level) {
        List<String> results = new ArrayList<>();
        for (String key : level.keySet()) {
            if (key.startsWith("$")) {
                // Replace placeholder keys starting with "$" with a generic list placeholder
                results.add("<placeholder>");
            } else {
                results.add(key);
            }
        }
        return results;
    }

    private static String getApproximateMatch(String input, Set<String> keys) {
        for (String key : keys) {
            if (key.startsWith(input)) {
                return key;
            }
        }
        return null;
    }
    public static List<String> getNextElements(List<String> domains, String[] args) {
        List<String[]> domainPartsList = new ArrayList<>();
        for (String domain : domains) {
            String[] parts = domain.split("\\.");
            domainPartsList.add(parts);
        }

        Set<String> nextElements = new HashSet<>();
        boolean inputEndsWithEmpty = args.length > 0 && args[args.length - 1].isEmpty();
        boolean inputIsEmpty = args.length == 0;

        if (inputIsEmpty) {
            // Return all possible first elements
            for (String[] domainParts : domainPartsList) {
                if (domainParts.length > 0) {
                    nextElements.add(domainParts[0]);
                }
            }
        } else if (args.length == 1 && !args[0].isEmpty()) {
            // If input is a partial string, return matching first elements
            String inputPart = args[0];
            for (String[] domainParts : domainPartsList) {
                if (domainParts.length > 0) {
                    String domainPart = domainParts[0];
                    boolean isWildcard = domainPart.startsWith("$") || (domainPart.startsWith("<") && domainPart.endsWith(">"));
                    if (isWildcard || domainPart.startsWith(inputPart)) {
                        nextElements.add(domainPart);
                    }
                }
            }
        } else {
            // Process domains to find matching next elements
            for (String[] domainParts : domainPartsList) {
                boolean matches = true;
                int inputSize = args.length;
                int domainSize = domainParts.length;
                boolean domainTooShort = domainSize < (inputEndsWithEmpty ? inputSize - 1 : inputSize);
                if (domainTooShort) {
                    continue; // Skip domains that are too short to match
                }

                int minLength = inputEndsWithEmpty ? inputSize - 1 : inputSize;

                for (int i = 0; i < minLength; i++) {
                    String inputPart = args[i];
                    String domainPart = domainParts[i];

                    boolean isWildcard = domainPart.startsWith("$") || (domainPart.startsWith("<") && domainPart.endsWith(">"));

                    if (inputPart.isEmpty()) {
                        // Should not happen here since empty input parts are handled differently
                        matches = false;
                        break;
                    } else {
                        if (i == 0 && args.length == 1 && !inputEndsWithEmpty) {
                            // Allow partial match on the first part if input length is 1 and doesn't end with empty
                            if (!isWildcard && !domainPart.startsWith(inputPart)) {
                                matches = false;
                                break;
                            }
                        } else {
                            // Require exact match for subsequent parts or if input ends with empty
                            if (!isWildcard && !domainPart.equals(inputPart)) {
                                matches = false;
                                break;
                            }
                        }
                    }
                }

                if (matches && inputEndsWithEmpty) {
                    // Collect possible elements at the position of the empty string
                    if (domainSize > inputSize - 1) {
                        String nextPart = domainParts[inputSize - 1];
                        nextElements.add(nextPart);
                    }
                }
            }
        }

        return new ArrayList<>(nextElements);
    }
}
