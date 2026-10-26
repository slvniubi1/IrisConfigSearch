package dev.irisconfigsearch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Extracts candidate user-facing shader options from shader source files.
 *
 * This is a small, loader-independent first step. It does not replace Iris/Oculus'
 * own option parser and should not be treated as a complete implementation of
 * OptiFine/Iris option semantics.
 */
public final class ShaderOptionScanner {
    private static final Pattern DEFINE = Pattern.compile(
            "^\\s*(//\\s*)?#\\s*define\\s+([A-Za-z_][A-Za-z0-9_]*)\\b(?:\\s*//\\s*(.*))?$");

    private ShaderOptionScanner() {
    }

    public record Candidate(String name, String description, boolean enabledByDefault, String sourceFile) {
        public Candidate {
            Objects.requireNonNull(name, "name");
            description = description == null ? "" : description.strip();
            Objects.requireNonNull(sourceFile, "sourceFile");
        }
    }

    /**
     * Scans a shader-pack ZIP and returns candidate #define options, deduplicated
     * by option name while preserving the first occurrence.
     */
    public static List<Candidate> scanZip(ZipFile zip) throws IOException {
        Map<String, Candidate> candidates = new LinkedHashMap<>();
        Enumeration<? extends ZipEntry> entries = zip.entries();

        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            if (entry.isDirectory() || !isShaderSource(entry.getName())) {
                continue;
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                    zip.getInputStream(entry), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    parseLine(line, entry.getName()).ifPresent(candidate ->
                            candidates.putIfAbsent(candidate.name(), candidate));
                }
            }
        }

        return List.copyOf(candidates.values());
    }

    private static boolean isShaderSource(String path) {
        String lower = path.toLowerCase(java.util.Locale.ROOT);
        return lower.endsWith(".vsh")
                || lower.endsWith(".fsh")
                || lower.endsWith(".gsh")
                || lower.endsWith(".tsh")
                || lower.endsWith(".glsl");
    }

    static Optional<Candidate> parseLine(String line, String sourceFile) {
        Matcher matcher = DEFINE.matcher(line);
        if (!matcher.matches()) {
            return Optional.empty();
        }

        String name = matcher.group(2);
        String description = matcher.group(3) == null ? "" : matcher.group(3);
        boolean enabled = matcher.group(1) == null;
        return Optional.of(new Candidate(name, description, enabled, sourceFile));
    }
}
