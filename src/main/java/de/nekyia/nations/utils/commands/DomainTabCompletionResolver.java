package de.nekyia.nations.utils.commands;

import org.bukkit.command.CommandSender;

import java.util.*;
import java.util.function.Supplier;

/**
 * The DomainTabCompletionResolver class processes a list of domain strings
 * and provides functionality to retrieve the next possible domain elements
 * based on input arguments. It now supports player-based placeholders.
 */
public class DomainTabCompletionResolver {

    // Changed from Supplier<List<String>> to PlayerAwarePlaceholder
    protected final Map<String, PlayerAwarePlaceholder> commandTabPlaceholders;
    private final List<String[]> domainPartsList;

    public DomainTabCompletionResolver(List<String> domains,
                                       Map<String, PlayerAwarePlaceholder> commandTabPlaceholders) {
        this.domainPartsList = new ArrayList<>();
        for (String domain : domains) {
            String[] parts = domain.split("\\.");
            this.domainPartsList.add(parts);
        }
        this.commandTabPlaceholders = new HashMap<>(commandTabPlaceholders);
    }

    /**
     * We now accept a CommandSender, which can be cast to Player if needed.
     */
    public List<String> getNextElements(CommandSender sender, String[] input) {
        Set<String> nextElements = new HashSet<>();
        boolean inputEndsWithEmpty = input.length > 0 && input[input.length - 1].isEmpty();
        boolean inputIsEmpty = input.length == 0;

        if (inputIsEmpty) {
            collectFirstElements(nextElements);
        } else if (input.length == 1 && !input[0].isEmpty()) {
            collectMatchingFirstElements(nextElements, input[0]);
        } else {
            processDomains(nextElements, input, inputEndsWithEmpty);
        }

        // Now filter + replace placeholders, respecting the sender
        replaceAndFilterPlaceholders(sender, nextElements, input);

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

    private void processDomains(Set<String> nextElements, String[] input, boolean inputEndsWithEmpty) {
        for (String[] domainParts : domainPartsList) {
            if (domainPartsMatchInput(domainParts, input, inputEndsWithEmpty)) {
                if (inputEndsWithEmpty && domainParts.length > input.length - 1) {
                    String nextPart = domainParts[input.length - 1];
                    nextElements.add(nextPart);
                } else if (!inputEndsWithEmpty && domainParts.length >= input.length) {
                    String candidatePart = domainParts[input.length - 1];
                    if (candidatePart.startsWith(input[input.length - 1]) || isWildcard(candidatePart)) {
                        nextElements.add(candidatePart);
                    }
                }
            }
        }
    }

    private boolean domainPartsMatchInput(String[] domainParts, String[] input, boolean inputEndsWithEmpty) {
        int inputSize = input.length;
        int domainSize = domainParts.length;
        int minLength = inputEndsWithEmpty ? inputSize - 1 : inputSize;

        if (domainSize < minLength) {
            return false; // Skip domains that are too short to match
        }

        for (int i = 0; i < minLength; i++) {
            String inputPart = input[i];
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
        return domainPart.startsWith("$") || domainPart.startsWith("%");
    }

    /**
     * Replace placeholders in nextElements with the supplier results, filtered by the
     * current user input. Now uses PlayerAwarePlaceholder's getSuggestions(sender).
     */
    private void replaceAndFilterPlaceholders(CommandSender sender,
                                              Set<String> nextElements,
                                              String[] args) {
        // Determine the current user input
        String userInput = (args.length > 0) ? args[args.length - 1] : "";

        // Copy to avoid ConcurrentModification
        for (String placeholder : new HashSet<>(nextElements)) {
            if (isWildcard(placeholder)) {
                // If we have a placeholder for this wildcard, use it
                PlayerAwarePlaceholder pap = commandTabPlaceholders.get(placeholder);
                if (pap != null) {
                    List<String> allResults = pap.getSuggestions(sender);
                    // Filter them based on user input
                    List<String> filteredResults = new ArrayList<>();
                    for (String result : allResults) {
                        if (result.toLowerCase().startsWith(userInput.toLowerCase())) {
                            filteredResults.add(result);
                        }
                    }
                    nextElements.remove(placeholder);
                    nextElements.addAll(filteredResults);
                } else {
                    // No placeholder found in commandTabPlaceholders
                    // Use the text after the wildcard character
                    String fallback = placeholder.substring(1); // remove $ or %
                    nextElements.remove(placeholder);

                    // Only add it if it matches the user input
                    if (fallback.startsWith(userInput)) {
                        nextElements.add(fallback);
                    }
                }
            }
        }
    }
}
