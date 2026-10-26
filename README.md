# Iris Config Search

A Minecraft 1.21.1 + NeoForge mod project intended to search configuration options exposed by the currently active shader pack.

## Status

**Early development — not yet feature-complete or build-verified.** The project currently has a loader-independent shader-source candidate scanner, a pure-Java search filter, JUnit tests, and a GitHub Actions workflow that attempts tests and a build.

## Requirements

- Minecraft 1.21.1
- NeoForge 21.1.x
- Java 21

## Current implementation

- `ShaderOptionScanner.scanZip(ZipFile)` scans `.vsh`, `.fsh`, `.gsh`, `.tsh`, and `.glsl` files.
- Candidate options are deduplicated by option name.
- `ShaderOptionSearch.filter(candidates, query)` performs case-insensitive matching against option names and descriptions. An empty query returns all candidates.
- JUnit tests cover scanner line parsing and search filtering.
- GitHub Actions is configured to run `gradle test` and `gradle build`.

The workflow has not yet been confirmed successful. The scanner and filter are not connected to the active shader pack or a Minecraft search screen.

## NeOculus integration research

The NeOculus 1.21.1 source exposes parsed shader options through `ShaderPack.getShaderPackOptions()`, followed by `ShaderPackOptions.getOptionSet()` and `getOptionValues()`. Boolean and string options are represented separately. These are promising integration points, but the project does not yet declare or verify a compatible NeOculus dependency, discover the active pack, or access its API at runtime.

This is a candidate scanner, **not** a complete Iris/Oculus option parser. Shader packs may expose settings through metadata such as `shaders.properties`; the loader's parsed option model is preferable for real user-facing settings.

## Planned next steps

1. Verify the GitHub Actions build and test results; fix any toolchain or test issues.
2. Select a specific NeOculus-compatible artifact/version and configure its dependency safely.
3. Add a client-side bridge to the active shader pack's parsed options.
4. Add a client-side search screen.
5. Add a Gradle wrapper and verify a clean build.

## License

MIT
