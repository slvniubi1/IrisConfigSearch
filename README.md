# Iris Config Search

A client-side Minecraft mod for searching shader-pack configuration options exposed by NeOculus.

## Compatibility

- **Minecraft:** 1.21.1
- **NeoForge:** 21.1.x (project CI uses 21.1.172)
- **Java:** 21
- **Shader integration:** NeOculus with a compatible active shader pack

This is an early release. The live NeOculus integration uses reflection and has not been verified against every NeOculus build.

## Install

1. Install Minecraft 1.21.1 with NeoForge 21.1.x.
2. Install a compatible NeOculus version and a shader pack if you want live option discovery.
3. Download the `iris-config-search-*.jar` file from the project's GitHub Releases.
4. Place the JAR in your Minecraft instance's `mods` folder.
5. Launch the game and press **K** to open the search screen. The key can be changed in Controls.

If the screen reports no options, confirm that NeOculus is installed, a shader pack is active, and that your versions are compatible.

## Features

- Case-insensitive multi-term search across option names, descriptions, source files, and available current values.
- Type filter: all, boolean, string, and shader define entries.
- Default-state filter: all, default-enabled, or default-disabled.
- Refresh and mouse-wheel scrolling.
- Shows option type, description, current value, and default value when available.
- English and Simplified Chinese (`zh_cn`) localization.
- Standalone shader-source scanner supports `.vsh`, `.fsh`, `.gsh`, `.tsh`, and `.glsl` files in directories and ZIP archives.

## Limitations

- The in-game screen is **read-only**. It does not change shader options or write configuration changes back to a shader pack.
- Live option discovery currently reads parsed boolean and string options exposed by NeOculus. Shader `#define` scanning is implemented as a separate scanner and is not the same as live NeOculus option discovery.
- If NeOculus is absent, no pack is active, or its API differs, live results may be empty or an integration error may be shown.
- Unit tests and CI builds do not replace launching the game with the intended NeOculus version and shader pack. Please treat this release as an early compatibility build.

## Build from source

Requires Java 21. Run:

```sh
gradle test build
```

The JAR is written to `build/libs/`.

## Development checks

GitHub Actions runs unit tests and builds the mod on pushes and pull requests. Creating a tag beginning with `v` (for example `v0.1.0-alpha.1`) triggers the release workflow, which reruns tests/build and attaches the JAR to a GitHub Release.

## License

MIT. See [LICENSE](LICENSE).
