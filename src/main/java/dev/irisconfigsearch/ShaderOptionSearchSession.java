package dev.irisconfigsearch;

import java.util.List;
import java.util.Objects;

/**
 * UI-independent state holder for the client search screen.
 * It keeps query and filtering in one testable place.
 */
public final class ShaderOptionSearchSession {
    private final ShaderOptionSource source;
    private String query = "";
    private Boolean enabledByDefault;
    private String typeFilter;
    private List<ShaderOptionEntry> snapshot = List.of();

    public ShaderOptionSearchSession(ShaderOptionSource source) {
        this.source = Objects.requireNonNull(source, "source");
        refresh();
    }

    /** Re-reads the source, for example after the active shader pack changes. */
    public void refresh() {
        List<ShaderOptionEntry> entries = Objects.requireNonNull(
                source.getOptions(), "ShaderOptionSource.getOptions() returned null");
        snapshot = List.copyOf(entries);
    }

    public void setQuery(String query) {
        this.query = query == null ? "" : query;
    }

    /** null means show both default-enabled and default-disabled entries. */
    public void setEnabledByDefault(Boolean enabledByDefault) {
        this.enabledByDefault = enabledByDefault;
    }

    /** null or "all" means show all types. */
    public void setTypeFilter(String typeFilter) {
        this.typeFilter = typeFilter == null || typeFilter.isBlank()
                || typeFilter.equalsIgnoreCase("all") ? null : typeFilter.strip();
    }

    public String query() {
        return query;
    }

    public Boolean enabledByDefault() {
        return enabledByDefault;
    }

    public String typeFilter() {
        return typeFilter;
    }

    public List<ShaderOptionEntry> results() {
        return ShaderOptionSearch.filterEntries(snapshot, query, enabledByDefault, typeFilter);
    }
}
