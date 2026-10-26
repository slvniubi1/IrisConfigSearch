package dev.irisconfigsearch;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Case-insensitive in-memory search for shader option candidates.
 *
 * The query matches either an option's name or its description. An empty query
 * returns all options in their original order.
 */
public final class ShaderOptionSearch {
    private ShaderOptionSearch() {
    }

    public static List<ShaderOptionScanner.Candidate> filter(
            List<ShaderOptionScanner.Candidate> candidates, String query) {
        Objects.requireNonNull(candidates, "candidates");

        String normalizedQuery = query == null ? "" : query.strip().toLowerCase(Locale.ROOT);
        if (normalizedQuery.isEmpty()) {
            return List.copyOf(candidates);
        }

        return candidates.stream()
                .filter(candidate -> matches(candidate, normalizedQuery))
                .toList();
    }

    private static boolean matches(ShaderOptionScanner.Candidate candidate, String query) {
        return candidate.name().toLowerCase(Locale.ROOT).contains(query)
                || candidate.description().toLowerCase(Locale.ROOT).contains(query);
    }
}
