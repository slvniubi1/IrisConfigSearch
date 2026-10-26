# Iris Config Search

A Minecraft 1.21.1 + NeoForge mod project intended to search configuration options exposed by the currently active shader pack.

## Status

**Early development — not yet feature-complete or build-verified.** The project currently has a loader-independent shader-source candidate scanner, a pure-Java search filter, JUnit tests, and a GitHub Actions workflow that runs tests and attempts a build.

## Requirements

- Minecraft 1.21.1
- NeoForge 21.1.x
- Java 21

## Current implementation

- `ShaderOptionScanner.scanZip(ZipFile)` scans `.vsh`, `.fsh`, `.gsh`, `.tsh`, and `.glsl` files.
- Candidate options are deduplicated by option name.

- `ShaderOptionEntry` is a UI-independent view model that carries source metadata and an optional runtime value. Source-only entries deliberately leave the current value unavailable instead of guessing it.
- `ShaderOptionCatalog.build(candidates, currentValues)` creates a case-insensitively sorted list for a future UI and attaches runtime values when available.
- `ShaderOptionSearchSession` stores the query and default-state filter, exposes filtered results, and can refresh its option snapshot when the active pack changes.
- `ShaderOptionSearch.filterEntries(entries, query, enabledByDefault)` searches unified entries, including available current values, and optionally filters by default state.
- Search can locate an option by its identifier, human-readable comment, or shader source path.
- `ShaderOptionSearch.filter(candidates, query)` performs case-insensitive, multi-term matching against option names, descriptions, and source-file paths. Every whitespace-separated term must match somewhere; an empty query returns all candidates.
- `ShaderOptionSearch.filterByDefaultState(candidates, enabledByDefault)` filters candidates by whether the source define is enabled or commented out by default.
- JUnit tests cover line parsing, search filtering, scanning shader sources from ZIP archives and directories, the unified option entry model, catalog ordering/value mapping, and search-session refresh/filtering.
- GitHub Actions runs `gradle test` and `gradle build`, then uploads the built JAR as a development artifact if both steps succeed.

## Downloading a development build

1. Open the repository's [Actions page](https://github.com/slvniubi1/IrisConfigSearch/actions).
2. Select a successful **Build and test** run.
3. Download the artifact named `iris-config-search-dev-<commit-sha>`.

Artifacts are retained for 14 days. If no successful run or artifact is shown, a build has not been confirmed.

**Important:** a compiled JAR is not proof that the intended feature works. The active shader pack is not yet connected to the scanner, and the in-game search screen is not implemented. Do not treat development artifacts as a finished release.

## NeOculus integration research

The NeOculus 1.21.1 source exposes parsed shader options through `ShaderPack.getShaderPackOptions()`, followed by `ShaderPackOptions.getOptionSet()` and `getOptionValues()`. Boolean and string options are represented separately. These are promising integration points, but this project does not yet declare or verify a compatible NeOculus dependency, discover the active pack, or access its API at runtime.

This is a candidate scanner, **not** a complete Iris/Oculus option parser. Shader packs may expose settings through metadata such as `shaders.properties`; the loader's parsed option model is preferable for real user-facing settings.

## Planned next steps

1. Obtain a confirmed GitHub Actions run and fix any build/test failures.
2. Select and declare a compatible NeOculus dependency.
3. Add a client-side bridge to the active pack's parsed options.
4. Add a client-side search screen with query and default-state controls.
5. Add a Gradle wrapper and verify a clean build.

## License

MIT
