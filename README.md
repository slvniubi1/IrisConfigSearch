# Iris Config Search

A Minecraft 1.21.1 + NeoForge mod project intended to search configuration options exposed by the currently active shader pack.

## Status

**Early development — not yet feature-complete or build-verified.** The initial Gradle/NeoForge project scaffold is committed. Shader-pack configuration discovery and the searchable client UI still need implementation and testing.

## Requirements

- Minecraft 1.21.1
- NeoForge 21.1.x
- Java 21

## Build

Run `gradlew build` (or `./gradlew build` on macOS/Linux) after installing a compatible Gradle wrapper. A wrapper has not been added yet.

## Important compatibility note

Iris is primarily associated with Fabric-based setups, while NeoForge shader support commonly uses Oculus or compatible forks. The implementation must target the actual shader loader and its exposed configuration API; this project does not yet claim compatibility with Iris or Oculus.

## Planned features

- Search only configuration entries exposed by the active shader pack
- Filter entries by name
- Open the relevant setting in the shader configuration UI when supported

## License

MIT
