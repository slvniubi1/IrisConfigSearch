package dev.irisconfigsearch;

import java.util.List;

/**
 * Boundary between the search UI and whichever shader-loader API supplies
 * the active pack's parsed options.
 *
 * Implementations should return a snapshot of the currently active pack's
 * options. This interface intentionally has no NeOculus imports so the search
 * and UI layers can be tested independently from a particular loader version.
 */
@FunctionalInterface
public interface ShaderOptionSource {
    List<ShaderOptionEntry> getOptions();
}
