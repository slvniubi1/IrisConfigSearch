# Iris Config Search

A Minecraft 1.21.1 + NeoForge mod project intended to search configuration options exposed by the currently active shader pack.

## Status

**Early development — not yet feature-complete or build-verified.** The project currently has a loader-independent shader-source candidate scanner, a pure-Java search filter, and JUnit tests for both.

## Requirements

- Minecraft 1.21.1
- NeoForge 21.1.x
- Java 21

## Current implementation

- `ShaderOptionScanner.scanZip(ZipFile)` scans `.vsh`, `.fsh`, `.gsh`, `.tsh`, and `.glsl` files.
- Candidate options are deduplicated by option name.
- `ShaderOptionSearch.filter(candidates, query)` performs case-insensitive matching against option names and descriptions. An empty query returns all candidates.
- Unit tests cover define parsing, commented-out defines, descriptions, ignored lines, and search filtering.
- The scanner and filter are not yet connected to the active shader pack or a Minecraft search screen.

This is a candidate scanner, **not** a complete Iris/Oculus option parser. Shader packs may expose settings through metadata such as `shaders.properties`, so the scanner alone cannot guarantee that every result is a real user-facing setting.

## Compatibility note

The NeOculus 1.21.1 source tree contains shader-pack option and menu classes, which are promising integration points. The project has not yet added a dependency on NeOculus or verified access to its runtime API. Integration must target the exact loader version and avoid hard-coding unsupported internals.

## Planned next steps

1. Verify the test and build setup locally or in CI.
2. Integrate with a specific NeoForge shader loader and active-pack API.
3. Add a client-side search screen.
4. Add a Gradle wrapper and verify a clean build.

## License

MIT
