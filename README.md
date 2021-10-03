# [Switcheroo](https://gitlab.com/NatoBoram/fabric-switcheroo)

[![Modrinth Downloads](https://img.shields.io/badge/dynamic/json?color=5da426&label=Modrinth&query=downloads&suffix=%20downloads&url=https%3A%2F%2Fapi.modrinth.com%2Fapi%2Fv1%2Fmod%2Fwq6HaMZG)](https://modrinth.com/mod/switcheroo)
[![CurseForge Downloads](https://img.shields.io/badge/dynamic/json?color=f16436&label=CurseForge&query=downloads.total&suffix=%20downloads&url=https%3A%2F%2Fapi.cfwidget.com%2F441128)](https://www.curseforge.com/minecraft/mc-mods/switcheroo)
[![pipeline status](https://gitlab.com/NatoBoram/fabric-switcheroo/badges/master/pipeline.svg)](https://gitlab.com/NatoBoram/fabric-switcheroo/-/commits/master)
[![StackShare](https://img.shields.io/badge/tech-stack-0690fa.svg?style=flat)](https://stackshare.io/NatoBoram/switcheroo)

Switch your currently held item for an effective item when clicking on a block.

## Usage

* Click on a block or entity to switch to the appropriate item
  * Press *Sneak* to prevent the switch from happening
  * Press *Sprint* to use the most effective item

Switcheroo will use the least effective tool that's still effective to break blocks and the most damaging item per seconds
to attack entities.

![Switcheroo Demo](docs/Switcheroo Demo.webm)

## Features

* Switcheroo when clicking blocks
* Switcheroo when clicking mobs
  * Uses max damage per seconds for mobs
  * Uses max attack damage for players
* Blacklist
  * Accessible via `/switcheroo blacklist blocks` and `/switcheroo blacklist mobs`
  * Compatible with [ModMenu](https://github.com/TerraformersMC/ModMenu)
* Doesn't consider items when they have 5 durability or less

## Installation

[![cloth-config2](https://img.shields.io/badge/Cloth%20Config%20API-5.0.38-9cff55)](https://github.com/shedaniel/cloth-config)
[![fabric](https://img.shields.io/badge/Fabric%20API-0.40.1-dbd0b4)](https://github.com/FabricMC/fabric)
[![modmenu](https://img.shields.io/badge/Mod%20Menu-2.0.13-134bff)](https://github.com/TerraformersMC/ModMenu)

Downloads are available in [Releases](https://gitlab.com/NatoBoram/fabric-switcheroo/-/releases). You can also test
development builds by downloading them form the [Pipelines](https://gitlab.com/NatoBoram/fabric-switcheroo/-/pipelines).

* [Modrinth](https://modrinth.com/mod/switcheroo)
* [CurseForge](https://www.curseforge.com/minecraft/mc-mods/switcheroo)

## Contributing

Requires Java 16.

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
