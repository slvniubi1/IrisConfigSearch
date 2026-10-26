package dev.irisconfigsearch;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ShaderOptionSourceTest {
    @Test
    void sourceCanProvideAnImmutableSnapshotForSearchUi() {
        List<ShaderOptionEntry> snapshot = List.of(
                new ShaderOptionEntry("SHADOWS", "Shadow quality", true,
                        "shaders/shadow.fsh", Optional.of("HIGH")));
        ShaderOptionSource source = () -> snapshot;

        assertEquals(snapshot, source.getOptions());
        assertEquals("SHADOWS", source.getOptions().get(0).name());
    }
}
