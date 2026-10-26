package dev.irisconfigsearch;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Creates a stable, UI-ready list from source candidates and optional runtime values. */
public final class ShaderOptionCatalog {
    private static final Comparator<ShaderOptionEntry> DISPLAY_ORDER =
            Comparator.comparing(ShaderOptionEntry::name, String.CASE_INSENSITIVE_ORDER)
                    .thenComparing(ShaderOptionEntry::name)
                    .thenComparing(ShaderOptionEntry::sourceFile);

    private ShaderOptionCatalog() { }

    /** Missing runtime values remain unavailable rather than being guessed. */
    public static List<ShaderOptionEntry> build(
            List<ShaderOptionScanner.Candidate> candidates, Map<String, String> currentValues) {
        Objects.requireNonNull(candidates, "candidates");
        Objects.requireNonNull(currentValues, "currentValues");
        return candidates.stream()
                .map(ShaderOptionEntry::fromCandidate)
                .map(entry -> entry.withCurrentValue(currentValues.get(entry.name())))
                .sorted(DISPLAY_ORDER)
                .toList();
    }
}
