package dev.irisconfigsearch;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ShaderOptionCatalogTest {
    @Test
    void sortsEntriesAndAttachesAvailableRuntimeValues() {
        List<ShaderOptionScanner.Candidate> candidates = List.of(
                new ShaderOptionScanner.Candidate("WATER", "Water reflections", true, "shaders/water.fsh"),
                new ShaderOptionScanner.Candidate("bloom", "Glow", false, "shaders/post.fsh"),
                new ShaderOptionScanner.Candidate("SHADOWS", "Shadows", true, "shaders/shadow.fsh"));

        List<ShaderOptionEntry> entries = ShaderOptionCatalog.build(candidates, Map.of("WATER", "HIGH"));

        assertEquals(List.of("bloom", "SHADOWS", "WATER"), entries.stream().map(ShaderOptionEntry::name).toList());
        assertEquals(Optional.of("HIGH"), entries.get(2).currentValue());
        assertEquals(Optional.empty(), entries.get(0).currentValue());
    }
}
