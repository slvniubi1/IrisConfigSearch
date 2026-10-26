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
        return filter(candidates, query, null);
    }

    /**
     * Searches by text and optionally restricts results to a default enabled state.
     * A null state includes both enabled and disabled candidates.
     */
    public static List<ShaderOptionScanner.Candidate> filter(
            List<ShaderOptionScanner.Candidate> candidates, String query, Boolean enabledByDefault) {
        Objects.requireNonNull(candidates, "candidates");

        String normalizedQuery = query == null ? "" : query.strip().toLowerCase(Locale.ROOT);
        String[] terms = normalizedQuery.isEmpty() ? new String[0] : normalizedQuery.split("\\s+");
        return candidates.stream()
                .filter(candidate -> enabledByDefault == null
                        || candidate.enabledByDefault() == enabledByDefault)
                .filter(candidate -> matchesAllTerms(candidate, terms))
                .toList();
    }

    /**
     * Returns only candidates whose source define is enabled or disabled by default.
     */
    public static List<ShaderOptionScanner.Candidate> filterByDefaultState(
            List<ShaderOptionScanner.Candidate> candidates, boolean enabledByDefault) {
        Objects.requireNonNull(candidates, "candidates");
        return candidates.stream()
                .filter(candidate -> candidate.enabledByDefault() == enabledByDefault)
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
