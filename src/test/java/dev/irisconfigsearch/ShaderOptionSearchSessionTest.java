package dev.irisconfigsearch;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ShaderOptionSearchSessionTest {
    @Test
    void appliesQueryAndDefaultStateFilterToSourceSnapshot() {
        List<ShaderOptionEntry> entries = List.of(
                new ShaderOptionEntry("SHADOWS", "Shadow quality", true,
                        "shaders/shadow.fsh", Optional.of("HIGH")),
                new ShaderOptionEntry("BLOOM", "Glow effect", false,
                        "shaders/post.fsh", Optional.of("LOW")));
        ShaderOptionSearchSession session = new ShaderOptionSearchSession(() -> entries);

        session.setQuery("high");
        assertEquals(List.of(entries.get(0)), session.results());

        session.setQuery("");
        session.setEnabledByDefault(false);
        assertEquals(List.of(entries.get(1)), session.results());
    }

    @Test
    void refreshReplacesTheSnapshot() {
        List<ShaderOptionEntry> initial = List.of(
                new ShaderOptionEntry("OLD", "Old option", true, "old.fsh", Optional.empty()));
        List<ShaderOptionEntry> updated = List.of(
                new ShaderOptionEntry("NEW", "New option", false, "new.fsh", Optional.empty()));
        List<List<ShaderOptionEntry>> snapshots = List.of(initial, updated);
        int[] calls = {0};
        ShaderOptionSearchSession session = new ShaderOptionSearchSession(() -> snapshots.get(calls[0]++));

        session.refresh();

        assertEquals(List.of(updated.get(0)), session.results());
    }
}
