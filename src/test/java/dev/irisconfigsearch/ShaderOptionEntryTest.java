package dev.irisconfigsearch;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
    void nullCurrentValueMeansUnavailable() {
        ShaderOptionScanner.Candidate candidate =
                new ShaderOptionScanner.Candidate("BLOOM", "", true, "shaders/post.fsh");

        assertEquals(Optional.empty(),
                ShaderOptionEntry.fromCandidate(candidate).withCurrentValue(null).currentValue());
    }
}
