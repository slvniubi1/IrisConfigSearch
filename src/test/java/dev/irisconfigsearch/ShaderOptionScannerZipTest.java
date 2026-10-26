package dev.irisconfigsearch;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ShaderOptionScannerZipTest {
    @Test
    void scansDefinesWithNumericValuesFromShaderArchive() throws IOException {
        Path archive = Files.createTempFile("shader-pack-values", ".zip");
        try {
            try (ZipOutputStream output = new ZipOutputStream(Files.newOutputStream(archive))) {
                addEntry(output, "shaders/shadows.glsl",
                        "#define SHADOW_QUALITY 3 // Shadow quality level\n#define BLOOM 1\n");
            }

            try (ZipFile zip = new ZipFile(archive.toFile())) {
                List<ShaderOptionScanner.Candidate> options = ShaderOptionScanner.scanZip(zip);

                assertEquals(List.of("SHADOW_QUALITY", "BLOOM"),
                        options.stream().map(ShaderOptionScanner.Candidate::name).toList());
                assertEquals("Shadow quality level", options.get(0).description());
            }
        } finally {
            Files.deleteIfExists(archive);
        }
    }

    @Test
    void scansShaderSourcesAndKeepsFirstOccurrenceOfDuplicateOption() throws IOException {
        Path archive = Files.createTempFile("shader-pack", ".zip");
        try {
            try (ZipOutputStream output = new ZipOutputStream(Files.newOutputStream(archive))) {
                addEntry(output, "shaders/first.fsh",
                        "#define BLOOM // First description\n// #define SHADOWS\n");
                addEntry(output, "shaders/second.GLSL",
                        "#define BLOOM // Duplicate description\n#define WATER_REFLECTIONS\n");
                addEntry(output, "readme.txt", "#define SHOULD_NOT_APPEAR\n");
            }

            try (ZipFile zip = new ZipFile(archive.toFile())) {
                List<ShaderOptionScanner.Candidate> options = ShaderOptionScanner.scanZip(zip);

                assertEquals(List.of("BLOOM", "SHADOWS", "WATER_REFLECTIONS"),
                        options.stream().map(ShaderOptionScanner.Candidate::name).toList());
                assertEquals("First description", options.get(0).description());
                assertEquals("shaders/first.fsh", options.get(0).sourceFile());
                assertEquals(false, options.get(1).enabledByDefault());
            }
        } finally {
            Files.deleteIfExists(archive);
        }
    }

    @Test
    void rejectsPathThatIsNotADirectory() throws IOException {
        Path file = Files.createTempFile("not-a-shader-pack", ".txt");
        try {
            assertThrows(IOException.class, () -> ShaderOptionScanner.scanDirectory(file));
        } finally {
            Files.deleteIfExists(file);
        }
    }

    @Test
    void scansUnpackedShaderPackDirectory() throws IOException {
        Path root = Files.createTempDirectory("shader-pack-directory");
        try {
            Path shaderDir = Files.createDirectories(root.resolve("shaders/world"));
            Files.writeString(shaderDir.resolve("terrain.fsh"),
                    "#define TERRAIN_DETAIL 2 // Terrain detail\n// #define OLD_WATER\n");
            Files.writeString(root.resolve("readme.txt"), "#define IGNORE_ME\n");

            List<ShaderOptionScanner.Candidate> options = ShaderOptionScanner.scanDirectory(root);

            assertEquals(List.of("TERRAIN_DETAIL", "OLD_WATER"),
                    options.stream().map(ShaderOptionScanner.Candidate::name).toList());
            assertEquals("shaders/world/terrain.fsh", options.get(0).sourceFile());
            assertEquals(false, options.get(1).enabledByDefault());
        } finally {
            try (var paths = Files.walk(root)) {
                for (Path path : paths.sorted(java.util.Comparator.reverseOrder()).toList()) {
                    Files.deleteIfExists(path);
                }
            }
        }
    }

    private static void addEntry(ZipOutputStream output, String name, String content) throws IOException {
        output.putNextEntry(new ZipEntry(name));
        output.write(content.getBytes(StandardCharsets.UTF_8));
        output.closeEntry();
    }
}
