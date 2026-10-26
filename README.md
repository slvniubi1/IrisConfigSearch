# Iris Config Search

A Minecraft 1.21.1 + NeoForge mod project intended to search configuration options exposed by the currently active shader pack.

## Status

**Early development — not yet feature-complete or build-verified.** The project currently has a loader-independent shader-source candidate scanner and a pure-Java search filter.

## Requirements

- Minecraft 1.21.1
- NeoForge 21.1.x
- Java 21

## Current implementation

- `ShaderOptionScanner.scanZip(ZipFile)` scans `.vsh`, `.fsh`, `.gsh`, `.tsh`, and `.glsl` files.
- Candidate options are deduplicated by option name.
- `ShaderOptionSearch.filter(candidates, query)` performs case-insensitive matching against option names and descriptions. An empty query returns all candidates.
- The scanner and filter are not yet connected to the active shader pack or a Minecraft search screen.

This is a candidate scanner, **not** a complete Iris/Oculus option parser. Shader packs may expose settings through metadata such as `shaders.properties`, so the scanner alone cannot guarantee that every result is a real user-facing setting.

## Compatibility note

Iris is primarily associated with Fabric-based setups, while NeoForge shader support commonly uses Oculus or compatible forks. Integration must target the actual loader version and its exposed configuration API. This project does not yet claim compatibility with Iris or Oculus.

## Planned next steps

1. Add automated tests for scanning and filtering.
2. Identify and integrate with a specific NeoForge shader loader and active-pack API.
3. Add a client-side search screen.
4. Add a Gradle wrapper and verify the build.

## License

MIT
