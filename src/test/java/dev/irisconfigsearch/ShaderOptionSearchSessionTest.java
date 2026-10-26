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
    void filtersByTypeCaseInsensitivelyAndCanClearFilter() {
        ShaderOptionEntry bool = new ShaderOptionEntry(
                "SHADOWS", "Toggle shadows", true, "shaders/shadow.fsh",
                Optional.of("true"), "boolean", Optional.of("false"));
        ShaderOptionEntry string = new ShaderOptionEntry(
                "QUALITY", "Shadow quality", true, "shaders/shadow.fsh",
                Optional.of("HIGH"), "string", Optional.of("MEDIUM"));
        ShaderOptionEntry define = new ShaderOptionEntry(
                "BLOOM", "Glow effect", false, "shaders/post.fsh",
                Optional.empty(), "define", Optional.of("false"));
        ShaderOptionSearchSession session = new ShaderOptionSearchSession(() -> List.of(bool, string, define));

        session.setTypeFilter("STRING");
        assertEquals(List.of(string), session.results());

        session.setTypeFilter("all");
        assertEquals(List.of(bool, string, define), session.results());
    }

    @Test
    void combinesTypeFilterWithTextAndDefaultStateFilters() {
        ShaderOptionEntry bool = new ShaderOptionEntry(
                "SHADOWS", "Toggle shadows", true, "shaders/shadow.fsh",
                Optional.of("true"), "boolean", Optional.of("false"));
        ShaderOptionEntry string = new ShaderOptionEntry(
                "QUALITY", "Shadow quality", true, "shaders/shadow.fsh",
                Optional.of("HIGH"), "string", Optional.of("MEDIUM"));
        ShaderOptionSearchSession session = new ShaderOptionSearchSession(() -> List.of(bool, string));

        session.setTypeFilter("string");
        session.setQuery("shadow");
        session.setEnabledByDefault(true);
        assertEquals(List.of(string), session.results());
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
