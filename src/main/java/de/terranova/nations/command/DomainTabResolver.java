package de.terranova.nations.command;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The DomainTabCompleter class processes a list of domain strings and provides
 * functionality to retrieve the next possible domain elements based on input arguments.
 */
public class DomainTabResolver {

    private List<String[]> domainPartsList;

    /**
     * Constructs a DomainTabCompleter with the given list of domains.
     *
     * @param domains List of domain strings to be processed.
     */
    public DomainTabResolver(List<String> domains) {
        this.domainPartsList = new ArrayList<>();
        for (String domain : domains) {
            String[] parts = domain.split("\\.");
            this.domainPartsList.add(parts);
        }
    }

    /**
     * Returns the list of next possible elements based on the provided arguments.
     *
     * @param args Array of input strings representing parts of a domain.
     * @return List of possible next elements.
     */
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

        return new ArrayList<>(nextElements);
    }

    /**
     * Collects all possible first elements from the domain parts.
     *
     * @param nextElements Set to collect next elements into.
     */
    private void collectFirstElements(Set<String> nextElements) {
        for (String[] domainParts : domainPartsList) {
            if (domainParts.length > 0) {
                nextElements.add(domainParts[0]);
            }
        }
    }

    /**
     * Collects matching first elements based on the partial input.
     *
     * @param nextElements Set to collect next elements into.
     * @param inputPart    The partial input string.
     */
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

    /**
     * Processes domains to find matching next elements based on the input arguments.
     *
     * @param nextElements      Set to collect next elements into.
     * @param args              Array of input strings representing parts of a domain.
     * @param inputEndsWithEmpty Flag indicating if the input ends with an empty string.
     */
    private void processDomains(Set<String> nextElements, String[] args, boolean inputEndsWithEmpty) {
        for (String[] domainParts : domainPartsList) {
            if (domainPartsMatchInput(domainParts, args, inputEndsWithEmpty)) {
                if (inputEndsWithEmpty && domainParts.length > args.length - 1) {
                    String nextPart = domainParts[args.length - 1];
                    nextElements.add(nextPart);
                }
            }
        }
    }

    /**
     * Checks if the domain parts match the input arguments.
     *
     * @param domainParts       Array of domain parts.
     * @param args              Array of input strings representing parts of a domain.
     * @param inputEndsWithEmpty Flag indicating if the input ends with an empty string.
     * @return True if the domain parts match the input, false otherwise.
     */
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
                if (i == 0 && args.length == 1 && !inputEndsWithEmpty) {
                    if (!isWildcard && !domainPart.startsWith(inputPart)) {
                        return false;
                    }
                } else {
                    if (!isWildcard && !domainPart.equals(inputPart)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Checks if the domain part is a wildcard.
     *
     * @param domainPart The domain part string.
     * @return True if the domain part is a wildcard, false otherwise.
     */
    private boolean isWildcard(String domainPart) {
        return domainPart.startsWith("$") || (domainPart.startsWith("<") && domainPart.endsWith(">"));
    }
}
