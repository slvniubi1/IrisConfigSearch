package dev.irisconfigsearch;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Case-insensitive in-memory search for shader option candidates.
 *
 * The query matches an option's name, description, or source file. Multiple
 * whitespace-separated terms may match different fields; every term must match.
 * An empty query returns all options in their original order.
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

        String[] terms = normalizedQuery.split("\\s+");
        return candidates.stream()
                .filter(candidate -> matchesAllTerms(candidate, terms))
                .toList();
    }

    private static boolean matchesAllTerms(ShaderOptionScanner.Candidate candidate, String[] terms) {
        String searchableText = (candidate.name() + " " + candidate.description() + " "
                + candidate.sourceFile()).toLowerCase(Locale.ROOT);
        for (String term : terms) {
            if (!searchableText.contains(term)) {
                return false;
            }
        }
        return true;
    }
}
