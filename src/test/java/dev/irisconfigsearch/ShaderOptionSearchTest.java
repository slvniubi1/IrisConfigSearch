package dev.irisconfigsearch;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ShaderOptionSearchTest {
    private static final List<ShaderOptionScanner.Candidate> OPTIONS = List.of(
            new ShaderOptionScanner.Candidate("SHADOW_QUALITY", "Shadow quality level", true, "shaders/common.glsl"),
            new ShaderOptionScanner.Candidate("BLOOM", "Enable bloom effect", false, "shaders/post.fsh"),
            new ShaderOptionScanner.Candidate("WATER_REFLECTIONS", "Water reflections", true, "shaders/water.glsl")
    );

    @Test
    void emptyQueryReturnsAllCandidatesInOriginalOrder() {
        assertEquals(OPTIONS, ShaderOptionSearch.filter(OPTIONS, ""));
        assertEquals(OPTIONS, ShaderOptionSearch.filter(OPTIONS, "   "));
        assertEquals(OPTIONS, ShaderOptionSearch.filter(OPTIONS, null));
    }

    @Test
    void matchesNamesWithoutCaseSensitivity() {
        assertEquals(List.of(OPTIONS.get(1)), ShaderOptionSearch.filter(OPTIONS, "bloom"));
        assertEquals(List.of(OPTIONS.get(2)), ShaderOptionSearch.filter(OPTIONS, "WATER_REF"));
    }

    @Test
    void matchesDescriptionsWithoutCaseSensitivity() {
        assertEquals(List.of(OPTIONS.get(0)), ShaderOptionSearch.filter(OPTIONS, "shadow quality"));
        assertEquals(List.of(OPTIONS.get(1)), ShaderOptionSearch.filter(OPTIONS, "ENABLE BLOOM"));
    }

    @Test
    void matchesSourceFilePathWithoutCaseSensitivity() {
        assertEquals(List.of(OPTIONS.get(0)), ShaderOptionSearch.filter(OPTIONS, "COMMON.GLSL"));
        assertEquals(List.of(OPTIONS.get(2)), ShaderOptionSearch.filter(OPTIONS, "water.glsl"));
    }

    @Test
    void noMatchReturnsEmptyList() {
        assertEquals(List.of(), ShaderOptionSearch.filter(OPTIONS, "nonexistent"));
    }
}
