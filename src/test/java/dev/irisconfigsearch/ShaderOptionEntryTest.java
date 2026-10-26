package dev.irisconfigsearch;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShaderOptionEntryTest {
    @Test
    void createsEntryFromSourceCandidateWithoutInventingRuntimeValue() {
        ShaderOptionScanner.Candidate candidate =
                new ShaderOptionScanner.Candidate("SHADOWS", "Shadow toggle", true, "shaders/world.fsh");

        ShaderOptionEntry entry = ShaderOptionEntry.fromCandidate(candidate);

        assertEquals("SHADOWS", entry.name());
        assertEquals("Shadow toggle", entry.description());
        assertTrue(entry.enabledByDefault());
        assertEquals("shaders/world.fsh", entry.sourceFile());
        assertEquals(Optional.empty(), entry.currentValue());
    }

    @Test
    void canAttachCurrentValueWithoutChangingSourceMetadata() {
        ShaderOptionScanner.Candidate candidate =
                new ShaderOptionScanner.Candidate("QUALITY", "Quality", false, "shaders/quality.glsl");

        ShaderOptionEntry entry = ShaderOptionEntry.fromCandidate(candidate).withCurrentValue("HIGH");

        assertEquals("HIGH", entry.currentValue().orElseThrow());
        assertFalse(entry.enabledByDefault());
        assertEquals("QUALITY", entry.name());
        assertEquals("Quality", entry.description());
    }

    @Test
    void filtersEntriesByTextCurrentValueAndDefaultState() {
        List<ShaderOptionEntry> entries = List.of(
                new ShaderOptionEntry("SHADOW_QUALITY", "Shadow quality", true,
                        "shaders/shadow.fsh", Optional.of("HIGH")),
                new ShaderOptionEntry("BLOOM", "Glow effect", false,
                        "shaders/post.fsh", Optional.of("LOW")),
                new ShaderOptionEntry("WATER", "Water reflections", true,
                        "shaders/water.fsh", Optional.empty()));

        assertEquals(List.of(entries.get(0)),
                ShaderOptionSearch.filterEntries(entries, "shadow high", true));
        assertEquals(List.of(entries.get(1)),
                ShaderOptionSearch.filterEntries(entries, "low", false));
        assertEquals(List.of(entries.get(0), entries.get(2)),
                ShaderOptionSearch.filterEntries(entries, "", true));
    }

    @Test
    void nullCurrentValueMeansUnavailable() {
        ShaderOptionScanner.Candidate candidate =
                new ShaderOptionScanner.Candidate("BLOOM", "", true, "shaders/post.fsh");

        assertEquals(Optional.empty(),
                ShaderOptionEntry.fromCandidate(candidate).withCurrentValue(null).currentValue());
    }
}
