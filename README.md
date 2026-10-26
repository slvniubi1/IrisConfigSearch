# Iris Config Search

A Minecraft 1.21.1 + NeoForge mod project for searching options exposed by the currently active NeOculus shader pack.

## Requirements

- Minecraft 1.21.1
- NeoForge 21.1.x
- Java 21

## Current implementation

- Press **K** (changeable in Controls) to open the client-side shader option search screen. English and Simplified Chinese (`zh_cn`) localization are included.
- Search is case-insensitive and supports multiple terms across option names, descriptions, source files, and available current values.
- Type filter cycles through all, boolean, string, and shader define entries. A separate state filter cycles through all, default-enabled, and default-disabled options.
- The screen includes refresh and mouse-wheel scrolling. Option names, types, descriptions, default values, and current values are displayed when available.
- `NeOculusOptionSource` reflectively reads parsed boolean and string options from the active NeOculus shader pack without a compile-time dependency.
- If NeOculus is missing, no pack is active, or the API is incompatible, the screen shows an empty/error state rather than intentionally crashing the game.
- `ShaderOptionScanner` can independently scan shader source files (.vsh, .fsh, .gsh, .tsh, .glsl) from directories and ZIP files.
- JUnit tests cover parsing, search, catalog ordering, ZIP/directory scanning, and search-session behavior.
- GitHub Actions runs the test suite and builds a development JAR.

## Development status

The in-game screen is an initial read-only search UI. It displays option names, option types, default values, and current values; editing options and writing changes back to the shader pack are not implemented yet. Runtime compatibility should still be tested with Minecraft 1.21.1, NeoForge 21.1.x, and an installed NeOculus build.

## Build

Use Java 21 and run `gradle test build`, or download the development artifact from the [GitHub Actions page](https://github.com/slvniubi1/IrisConfigSearch/actions) after a successful workflow run.
