# [Switcheroo](https://github.com/NatoBoram/switcheroo)

[![Java CI](https://github.com/NatoBoram/switcheroo/actions/workflows/gradle.yaml/badge.svg)](https://github.com/NatoBoram/switcheroo/actions/workflows/gradle.yaml)
[![GitHub Pages](https://github.com/NatoBoram/switcheroo/actions/workflows/github-pages.yaml/badge.svg)](https://github.com/NatoBoram/switcheroo/actions/workflows/github-pages.yaml)
[![Modrinth Downloads](https://img.shields.io/badge/dynamic/json?color=1bd96a&label=Modrinth&query=downloads&suffix=%20downloads&url=https%3A%2F%2Fapi.modrinth.com%2Fv2%2Fproject%2Fwq6HaMZG)](https://modrinth.com/mod/switcheroo)
[![minepkg](https://img.shields.io/badge/dynamic/json?color=ff9800&label=minepkg&query=stats.totalDownloads&suffix=%20downloads&url=https%3A%2F%2Fapi.preview.minepkg.io%2Fv1%2Fprojects%2Fswitcheroo)](https://preview.minepkg.io/projects/switcheroo)
[![CurseForge Downloads](https://img.shields.io/badge/dynamic/json?color=f16436&label=CurseForge&query=downloads.total&suffix=%20downloads&url=https%3A%2F%2Fapi.cfwidget.com%2F441128)](https://www.curseforge.com/minecraft/mc-mods/switcheroo)
[![Planet Minecraft](https://img.shields.io/badge/Planet%20Minecraft-download-3366cc)](https://www.planetminecraft.com/mod/switcheroo-5459816/) [![Wakapi](https://wakapi.dev/api/badge/NatoBoram/interval:any/project:switcheroo)](https://wakapi.dev/summary?interval=any&project=switcheroo)

Switch your currently held item for an effective item when clicking on a block.

## Usage

- Click on a block or entity to switch to the appropriate item
  - Press _Sneak_ to prevent the switch from happening
  - Press _Sprint_ to use the most effective item

Switcheroo will use the least effective tool that's still effective to break blocks and the most damaging item per seconds to attack entities.

![Trailer GIF](https://github.com/NatoBoram/switcheroo/assets/10495562/2a2b7471-272d-43b8-98a2-fca5f7291289)

[Demo](https://www.youtube.com/watch?v=JnvHyhDPlpY) | [Trailer](https://www.youtube.com/watch?v=SpE6-4D1x28) | [Meme](https://www.youtube.com/watch?v=2Wm2cTkdJzk)

## Features

- Switcheroo when clicking blocks
- Switcheroo when clicking mobs
  - Uses max damage per seconds for mobs
  - Uses max attack damage for players
- Blacklist
  - Accessible via `/switcheroo blacklist blocks` and `/switcheroo blacklist mobs`
  - Compatible with [ModMenu](https://github.com/TerraformersMC/ModMenu)
- Doesn't consider items when they have 5 durability or less
- Prefer Silk Touch on certain blocks
  - `/switcheroo prefer silk_touch`
    - `/switcheroo prefer silk_touch add dirt_path`
    - `/switcheroo prefer silk_touch remove dirt_path`
  - The Silk Touch selection is really shaky, though. Use at your own risk.
- Enable or disable it with `/switcheroo enable` and `/switcheroo disable`

## Installation

[![cloth-config2](https://img.shields.io/badge/Cloth%20Config%20API-18.0.145-9cff55)](https://github.com/shedaniel/cloth-config)
[![fabric](https://img.shields.io/badge/Fabric%20API-0.121.0-dbd0b4)](https://github.com/FabricMC/fabric)
[![modmenu](https://img.shields.io/badge/Mod%20Menu-14.0.0-134bff)](https://github.com/TerraformersMC/ModMenu)

Downloads are available in [Releases](https://github.com/NatoBoram/switcheroo/releases). You can also test development builds by downloading them form the [Actions](https://github.com/NatoBoram/switcheroo/actions/workflows/gradle.yaml?query=branch%3Amain).

- [Modrinth](https://modrinth.com/mod/switcheroo)
- [minepkg](https://preview.minepkg.io/projects/switcheroo)
- [CurseForge](https://www.curseforge.com/minecraft/mc-mods/switcheroo)
- [Planet Minecraft](https://www.planetminecraft.com/mod/switcheroo-5459816)

## Contributing

Requires Java 21.

### Developing

```bash
./gradlew downloadAssets genSources
```

On [VSCode](https://code.visualstudio.com), an additional step is required.

```bash
./gradlew eclipse vscode
```

### Building

```bash
./gradlew build
```
