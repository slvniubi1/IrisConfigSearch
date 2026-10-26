package dev.irisconfigsearch;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ShaderOptionScannerTest {
    @Test
    void parsesEnabledDefineAndDescription() {
        Optional<ShaderOptionScanner.Candidate> result =
                ShaderOptionScanner.parseLine("#define BLOOM // Enable bloom", "shaders/post.fsh");

        assertTrue(result.isPresent());
        assertEquals("BLOOM", result.orElseThrow().name());
        assertEquals("Enable bloom", result.orElseThrow().description());
        assertTrue(result.orElseThrow().enabledByDefault());
        assertEquals("shaders/post.fsh", result.orElseThrow().sourceFile());
    }

    @Test
    void parsesCommentedOutDefineAsDisabled() {
        Optional<ShaderOptionScanner.Candidate> result =
                ShaderOptionScanner.parseLine("// #define SHADOWS // Enable shadows", "shaders/common.glsl");

        assertTrue(result.isPresent());
        assertEquals("SHADOWS", result.orElseThrow().name());
        assertEquals("Enable shadows", result.orElseThrow().description());
        assertFalse(result.orElseThrow().enabledByDefault());
    }

    @Test
    void parsesDefineWithoutDescription() {
        Optional<ShaderOptionScanner.Candidate> result =
                ShaderOptionScanner.parseLine("  # define CLOUDS  ", "shaders/sky.vsh");

        assertTrue(result.isPresent());
        assertEquals("CLOUDS", result.orElseThrow().name());
        assertEquals("", result.orElseThrow().description());
        assertTrue(result.orElseThrow().enabledByDefault());
    }

    @Test
    void parsesDefineWithAssignedValueAndDescription() {
        Optional<ShaderOptionScanner.Candidate> result =
                ShaderOptionScanner.parseLine("#define SHADOW_QUALITY 3 // Shadow quality level", "shaders/shadows.glsl");

        assertTrue(result.isPresent());
        assertEquals("SHADOW_QUALITY", result.orElseThrow().name());
        assertEquals("Shadow quality level", result.orElseThrow().description());
        assertTrue(result.orElseThrow().enabledByDefault());
    }

    @Test
    void parsesBooleanLikeDefineWithNumericValue() {
        Optional<ShaderOptionScanner.Candidate> result =
                ShaderOptionScanner.parseLine("#define BLOOM 1", "shaders/post.fsh");

        assertTrue(result.isPresent());
        assertEquals("BLOOM", result.orElseThrow().name());
        assertEquals("", result.orElseThrow().description());
    }

    @Test
    void ignoresNonDefineLines() {
        assertTrue(ShaderOptionScanner.parseLine("// just a comment", "shaders/a.glsl").isEmpty());
        assertTrue(ShaderOptionScanner.parseLine("uniform float brightness;", "shaders/a.fsh").isEmpty());
    }
}
