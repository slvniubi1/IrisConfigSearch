# Iris Config Search

A client-side Minecraft mod for searching and editing shader-pack options exposed by NeOculus.

## Compatibility

- **Minecraft:** 1.21.1
- **NeoForge:** 21.1.x (project CI uses 21.1.172)
- **Java:** 21
- **Shader integration:** NeOculus with a compatible active shader pack

The NeOculus bridge uses reflection to keep NeOculus optional. Runtime compatibility should be checked with the exact NeOculus build used by your instance.

## Install

1. Install Minecraft 1.21.1 with NeoForge 21.1.x.
2. Install a compatible NeOculus version and activate a shader pack.
3. Download the `iris-config-search-*.jar` file from the project's GitHub Releases.
4. Place the JAR in your Minecraft instance's `mods` folder.
5. Open the search screen with **K**, or use the search button on a recognized Iris/NeOculus shader configuration screen.

## Features

- Case-insensitive multi-term search across option names, descriptions, source files, and available current values.
- Type and default-state filters, refresh, and scrolling.
- Shows option type, description, current value, and default value when available.
- Click **Change / 修改** on boolean options to toggle them.
- Click **Change / 修改** on string options to cycle through the allowed values declared by the shader pack.
- Applies queued changes through NeOculus' option queue and reloads the active shader pack; NeOculus writes the resulting changed values to the pack's configuration file.
- English and Simplified Chinese localization.
- Standalone shader-source scanner supports common GLSL shader files in directories and ZIP archives.

## Editing behavior and limitations

- Editing is currently supported for parsed NeOculus boolean and string options only.
- String options cycle through the allowed values declared in the shader pack. Free-form numeric sliders and arbitrary text editing are not implemented.
- Shader define scan results and unknown option types remain read-only.
- Applying a change reloads the shader pack and may briefly interrupt rendering. Save a backup of your shader-pack configuration before testing.
- If NeOculus is absent, no pack is active, or its API differs, options may be unavailable or applying changes may fail.
- CI builds and unit tests do not replace launching the game with the exact NeOculus version and shader pack you use.

## Build from source

Requires Java 21. Run:

```sh
gradle test build
```

The JAR is written to `build/libs/`.

## Development checks

GitHub Actions runs unit tests and builds the mod on pushes and pull requests. Creating a tag beginning with `v` triggers the release workflow.

## License

MIT. See [LICENSE](LICENSE).
