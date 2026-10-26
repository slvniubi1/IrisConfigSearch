# NeOculus integration notes (Minecraft 1.21.1)

## Verified source entry point

The NeOculus `1.21.1` branch defines `net.irisshaders.iris.shaderpack.ShaderPack` and exposes a public `getShaderPackOptions()` method. The returned `ShaderPackOptions` instance is the starting point for accessing the parsed option model. Its constructor also receives a map of changed configuration values.

Source inspected:
- https://github.com/KaguyaStudio-MC/NeOculus/blob/1.21.1/src/main/java/net/irisshaders/iris/shaderpack/ShaderPack.java

## What is not verified yet

This project has not yet confirmed:
- a stable public accessor for the currently active `ShaderPack`;
- the exact public methods and data structures for enumerating option names, descriptions, defaults, and current values;
- the correct Maven artifact/repository and compatible published NeOculus version for this branch;
- runtime behavior when NeOculus is absent or its version differs.

Do not add direct imports or a hard dependency until these points are checked against the exact target build. The source class existing in a repository is not, by itself, proof that a compatible dependency can be resolved or that the active pack can be obtained safely.

## Adapter implementation plan

1. Pin a specific Minecraft 1.21.1 / NeoForge 21.1.x compatible NeOculus build.
2. Confirm the active-pack accessor and the option model's public enumeration/value methods from that exact build.
3. Implement the adapter in a client-only package. Keep all NeOculus types out of the loader-independent search model.
4. Convert parsed options to `ShaderOptionEntry` values, using an unavailable value rather than guessing whenever the loader does not expose a value.
5. Handle no active pack and missing/incompatible loader gracefully.
6. Add tests for mapping and empty/no-pack cases, then verify with GitHub Actions and an actual client launch.

## Current project boundary

`ShaderOptionSource` remains the interface between the future UI/search session and a loader-specific adapter. The existing ZIP/directory scanner is useful for source inspection but should not be treated as the authoritative source of user-facing shader settings when NeOculus already has a parsed option model.

## Additional API details confirmed from the 1.21.1 source

The current-pack lookup is available as `Iris.getCurrentPack()`, returning `Optional<ShaderPack>`. When present, the route to parsed options is:

`Iris.getCurrentPack() -> ShaderPack.getShaderPackOptions() -> ShaderPackOptions.getOptionSet()/getOptionValues()`

The option set separates boolean and string options:
- `OptionSet.getBooleanOptions()` returns the boolean-option map.
- `OptionSet.getStringOptions()` returns the string-option map.
- Boolean option metadata is available from each merged option's `getOption()`; the option exposes its name, optional comment, and default value.
- String option metadata likewise exposes its default value and optional comment.
- `OptionValues.getBooleanValue(name)` returns `OptionalBoolean`; `getStringValue(name)` returns `Optional<String>`. The adapter should use these typed accessors rather than treating every option as a boolean.
- `MergedBooleanOption.getLocations()` and `MergedStringOption.getLocations()` provide source locations that may support displaying source paths.

These APIs were checked in the NeOculus `1.21.1` branch. The project still needs a resolvable, version-pinned dependency and a verified runtime/client build before these classes can safely be imported. Keep the adapter client-only and treat an empty `Optional<ShaderPack>` as “no active pack”.

