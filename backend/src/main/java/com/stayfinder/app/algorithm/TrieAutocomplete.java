package com.stayfinder.app.algorithm;

import java.util.*;

/**
 * Case-insensitive prefix index.
 * Insert: O(m), prefix lookup: O(m + output), where m is the text length.
 */
public class TrieAutocomplete {
    private final Node root = new Node();

    public void clear() {
        root.children.clear();
        root.terminalValues.clear();
    }

    public void insert(String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        Node current = root;
        for (char character : normalized.toCharArray()) {
            current = current.children.computeIfAbsent(character, ignored -> new Node());
        }
        current.terminalValues.add(value.trim());
    }

    public List<String> suggest(String prefix, int limit) {
        if (prefix == null || prefix.isBlank() || limit <= 0) {
            return List.of();
        }

        Node current = root;
        String normalized = prefix.trim().toLowerCase(Locale.ROOT);
        for (char character : normalized.toCharArray()) {
            current = current.children.get(character);
            if (current == null) {
                return List.of();
            }
        }

        List<String> result = new ArrayList<>(limit);
        collect(current, result, limit);
        result.sort(String.CASE_INSENSITIVE_ORDER);
        return result;
    }

    private void collect(Node node, List<String> result, int limit) {
        if (result.size() >= limit) {
            return;
        }
        for (String value : node.terminalValues) {
            if (result.size() >= limit) {
                return;
            }
            result.add(value);
        }
        for (Node child : node.children.values()) {
            collect(child, result, limit);
            if (result.size() >= limit) {
                return;
            }
        }
    }

    private static final class Node {
        private final Map<Character, Node> children = new TreeMap<>();
        private final Set<String> terminalValues = new LinkedHashSet<>();
    }
}
