package de.terranova.nations.commands;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CommandUtil {
    public static Map<String, Method> findCommands(Map<String, Method> commandMethods, String[] test) {
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
    public static Method findExactCommand(Map<String, Method> commandMethods, String[] test) {
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
}
