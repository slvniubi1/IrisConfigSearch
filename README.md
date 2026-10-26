# Iris Config Search

A Minecraft 1.21.1 + NeoForge mod project intended to search configuration options exposed by the currently active shader pack.

## Status

**Early development — not yet feature-complete or build-verified.** The initial project scaffold and a loader-independent shader-source scanner are committed. The scanner finds candidate `#define` entries in shader source files inside a shader-pack ZIP, including commented-out defines, and records a trailing `//` description when present.

## Requirements

- Minecraft 1.21.1
- NeoForge 21.1.x
- Java 21

## Current implementation

- `ShaderOptionScanner.scanZip(ZipFile)` scans `.vsh`, `.fsh`, `.gsh`, `.tsh`, and `.glsl` files.
- Results are deduplicated by option name.
- This is a candidate scanner, **not** a complete Iris/Oculus option parser. It is not yet connected to the active shader pack or a search screen.

## Compatibility note

Iris is primarily associated with Fabric-based setups, while NeoForge shader support commonly uses Oculus or compatible forks. The integration must target the actual loader and its exposed configuration API. This project does not yet claim compatibility with Iris or Oculus.

## Planned next steps

1. Add search filtering and a client-side screen.
2. Integrate with the chosen NeoForge shader loader and active pack.
3. Add tests and a Gradle wrapper, then verify a build.

## License

MIT
