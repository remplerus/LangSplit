## LangSplit
I made this mod so my siblings can learn English while still being able to play the game in Swedish. 
What it does is getting the translation from the specified language file and displays the translation next to or under the current language. 

This mod is for NeoForge and Fabric, it is fully client side and doesn't do anything if installed on a server.

 

### Current features:
 * It translates most text in the game that has a translation.
 * It translates tooltips for items in the inventory.
 * Select a secondary language to display.
 * Inline translation or on two rows.
 * Select a color for the translated text.
 * Color blending to make the selected color mix with already colored text.
 * Works with resource packs that contains translations.
 * Mod friendly! :)

 

### Compatibility:

It should translate text in any mod as long as the mod has translation for that language (otherwise it should safely ignore the translation).

 

### Permission:

You can use my mods in any modpack as long as the host remains on CurseForge or Modrinth.

 

##### [Join me on Discord](https://discord.gg/nHck2RKaJ3)

 

## Building (Stonecutter multi-version setup)

One source tree builds all supported Minecraft versions and loaders using
[Stonecutter](https://stonecutter.kikugie.dev/). Architectury is no longer used.

| Minecraft | Fabric (Loader 0.20.1+) | NeoForge  | Java |
|-----------|-------------------------|-----------|------|
| 1.21.1    | ✔ (Fabric API, Cloth Config 15, Mod Menu optional) | ✔ 21.1    | 21 |
| 1.21.11   | ✔ (Cloth Config 21)     | ✔ 21.11   | 21 |
| 26.1.2    | ✔ (Cloth Config 26.1)   | ✔ 26.1.2  | 25 |
| 26.2      | ✔ (Cloth Config 26.2)   | ✔ 26.2    | 25 |
| 26.3      | ✔ (Cloth Config 26.3)   | ✔ 26.3    | 25 |

Requirements: **JDK 25** to run Gradle (the Java 21 toolchain for 1.21.x is downloaded automatically).

```bash
# build everything into build/libs/<mod version>/
./gradlew buildAndCollect          # all targets; single one: ./gradlew :1.21.1-fabric:build

# run a client for one target
./gradlew :1.21.11-neoforge:runClient
```

Layout:

- `src/main/java` – the only source set. Version/loader differences are written as Stonecutter comments,
  e.g. `//? if fabric {`, `//? if >=26.1 {`. The files on disk are in the state of the *active* version
  (`stonecutter.gradle.kts`, committed as `26.3-fabric`). Switch the active version with the Stonecutter
  IntelliJ plugin or `./gradlew "Set active project to 1.21.1-fabric"` – and switch back to `26.3-fabric`
  (`"Reset active project"`) before committing.
- `stonecutter.properties.toml` – mod version and all dependency versions per Minecraft version/loader.
- `build.fabric.gradle.kts` / `build.neoforge.gradle.kts` – per-loader build scripts.
  Fabric uses `loom-back-compat`, which picks `fabric-loom-remap` (≤ 1.21.11, Mojang mappings) or the
  non-remapping `fabric-loom` (26.1+, unobfuscated) automatically.

Rendering hooks by version:

- all versions: `GuiGraphics#drawString/drawCenteredString` (1.21.1 `PoseStack`, 1.21.11 `Matrix3x2fStack`)
- 1.21.11+: widget labels go through `ActiveTextCollector#acceptScrolling` (hooked separately)
- 26.1+: `GuiGraphics` is called `GuiGraphicsExtractor`, the methods `text`/`centeredText`;
  26.2+: screens are opened through `Minecraft#gui`
