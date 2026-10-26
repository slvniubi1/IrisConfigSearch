package dev.irisconfigsearch;

import java.util.Objects;
import java.util.Optional;

/** UI-independent view model for one shader option. */
public record ShaderOptionEntry(
        String name,
        String description,
        boolean enabledByDefault,
        String sourceFile,
        Optional<String> currentValue,
        String type,
        Optional<String> defaultValue) {

    public ShaderOptionEntry {
        Objects.requireNonNull(name, "name");
        description = description == null ? "" : description.strip();
        Objects.requireNonNull(sourceFile, "sourceFile");
        currentValue = currentValue == null ? Optional.empty() : currentValue;
        type = type == null || type.isBlank() ? "unknown" : type;
        defaultValue = defaultValue == null ? Optional.empty() : defaultValue;
    }

    /** Backwards-compatible constructor for scanner candidates and existing integrations. */
    public ShaderOptionEntry(
            String name, String description, boolean enabledByDefault,
            String sourceFile, Optional<String> currentValue) {
        this(name, description, enabledByDefault, sourceFile, currentValue,
                "boolean", Optional.of(Boolean.toString(enabledByDefault)));
    }

    public static ShaderOptionEntry fromCandidate(ShaderOptionScanner.Candidate candidate) {
        Objects.requireNonNull(candidate, "candidate");
        return new ShaderOptionEntry(
                candidate.name(), candidate.description(), candidate.enabledByDefault(),
                candidate.sourceFile(), Optional.empty(), "define",
                Optional.of(Boolean.toString(candidate.enabledByDefault())));
    }

    public ShaderOptionEntry withCurrentValue(String value) {
        return new ShaderOptionEntry(
                name, description, enabledByDefault, sourceFile, Optional.ofNullable(value),
                type, defaultValue);
    }
}
