package de.terranova.nations.command;

import java.util.*;
import java.util.function.Supplier;

/**
 * The DomainTabCompleter class processes a list of domain strings and provides
 * functionality to retrieve the next possible domain elements based on input arguments.
 */
public class DomainTabResolver {

    private List<String[]> domainPartsList;
    protected final Map<String, Supplier<List<String>>> commandTabPlaceholders = new HashMap<>();

    public DomainTabResolver(List<String> domains, Map<String, Supplier<List<String>>> commandTabPlaceholders) {
        this.domainPartsList = new ArrayList<>();
        for (String domain : domains) {
            String[] parts = domain.split("\\.");
            this.domainPartsList.add(parts);
        }
        this.commandTabPlaceholders.putAll(commandTabPlaceholders);
    }

    public List<String> getNextElements(String[] args) {
        Set<String> nextElements = new HashSet<>();
        boolean inputEndsWithEmpty = args.length > 0 && args[args.length - 1].isEmpty();
        boolean inputIsEmpty = args.length == 0;

        if (inputIsEmpty) {
            collectFirstElements(nextElements);
        } else if (args.length == 1 && !args[0].isEmpty()) {
            collectMatchingFirstElements(nextElements, args[0]);
        } else {
            processDomains(nextElements, args, inputEndsWithEmpty);
        }

        // Now filter and replace placeholders respecting the current user's input
        replaceAndFilterPlaceholders(nextElements, args);

        return new ArrayList<>(nextElements);
    }

    private void collectFirstElements(Set<String> nextElements) {
        for (String[] domainParts : domainPartsList) {
            if (domainParts.length > 0) {
                nextElements.add(domainParts[0]);
            }
        }
    }

    private void collectMatchingFirstElements(Set<String> nextElements, String inputPart) {
        for (String[] domainParts : domainPartsList) {
            if (domainParts.length > 0) {
                String domainPart = domainParts[0];
                boolean isWildcard = isWildcard(domainPart);
                if (isWildcard || domainPart.startsWith(inputPart)) {
                    nextElements.add(domainPart);
                }
            }
        }
    }

    private void processDomains(Set<String> nextElements, String[] args, boolean inputEndsWithEmpty) {
        for (String[] domainParts : domainPartsList) {
            if (domainPartsMatchInput(domainParts, args, inputEndsWithEmpty)) {
                if (inputEndsWithEmpty && domainParts.length > args.length - 1) {
                    String nextPart = domainParts[args.length - 1];
                    nextElements.add(nextPart);
                } else if (!inputEndsWithEmpty && domainParts.length >= args.length) {
                    String candidatePart = domainParts[args.length - 1];
                    if (candidatePart.startsWith(args[args.length - 1]) || isWildcard(candidatePart)) {
                        nextElements.add(candidatePart);
                    }
                }
            }
        }
    }

    private boolean domainPartsMatchInput(String[] domainParts, String[] args, boolean inputEndsWithEmpty) {
        int inputSize = args.length;
        int domainSize = domainParts.length;
        int minLength = inputEndsWithEmpty ? inputSize - 1 : inputSize;

        if (domainSize < minLength) {
            return false; // Skip domains that are too short to match
        }

        for (int i = 0; i < minLength; i++) {
            String inputPart = args[i];
            String domainPart = domainParts[i];
            boolean isWildcard = isWildcard(domainPart);

            if (inputPart.isEmpty()) {
                return false; // Empty input parts are not expected here
            } else {
                if (!isWildcard && !domainPart.startsWith(inputPart)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isWildcard(String domainPart) {
        return domainPart.startsWith("$") || (domainPart.startsWith("<") && domainPart.endsWith(">"));
    }

    /**
     * Replace placeholders in nextElements with the supplier results, filtered by the current user input.
     * For example, if the placeholder is $ONLINEPLAYERS and the user typed "test.add.a", we only show players
     * starting with "a".
     */
    private void replaceAndFilterPlaceholders(Set<String> nextElements, String[] args) {
        // Determine the current user input at this domain part (if any)
        String userInput = "";
        if (args.length > 0) {
            userInput = args[args.length - 1];
        }

        // For each placeholder found in nextElements, replace it with filtered results
        for (String placeholder : new HashSet<>(nextElements)) {
            if (commandTabPlaceholders.containsKey(placeholder) && nextElements.contains(placeholder)) {
                List<String> allResults = commandTabPlaceholders.get(placeholder).get();

                // Filter based on user's partial input
                List<String> filteredResults = new ArrayList<>();
                for (String result : allResults) {
                    if (result.startsWith(userInput)) {
                        filteredResults.add(result);
                    }
                }

                // Replace the placeholder in nextElements
                nextElements.remove(placeholder);
                nextElements.addAll(filteredResults);
            }
        }
    }

    public static List<String> processDomains(Map<String, String[]> inputMap) {
        List<String> results = new ArrayList<>();

        for (Map.Entry<String, String[]> entry : inputMap.entrySet()) {
            String processed = processDomain(entry.getKey(), entry.getValue());
            results.add(processed);
        }

        return results;
    }

    private static String processDomain(String domain, String[] arguments) {
        // If no placeholders present, just return the domain as is
        if (!domain.contains("$ARGUMENT")) {
            return domain;
        }

        String[] segments = domain.split("\\.");
        List<String> resultSegments = new ArrayList<>();

        int argIndex = 0;
        for (String segment : segments) {
            if (segment.equals("$ARGUMENT")) {
                // Replace with a single argument if available
                if (argIndex < arguments.length) {
                    resultSegments.add(arguments[argIndex]);
                    argIndex++;
                } else {
                    resultSegments.add(segment);
                }
            } else if (segment.equals("$ARGUMENTS")) {
                // Replace with all remaining arguments, joined by '.'
                if (argIndex < arguments.length) {
                    StringBuilder sb = new StringBuilder();
                    for (int j = argIndex; j < arguments.length; j++) {
                        if (sb.length() > 0) sb.append('.');
                        sb.append(arguments[j]);
                    }
                    resultSegments.add(sb.toString());
                    argIndex = arguments.length;
                } else {
                    resultSegments.add(segment);
                }
            } else {
                // Normal segment
                resultSegments.add(segment);
            }
        }

        return String.join(".", resultSegments);
    }
}

