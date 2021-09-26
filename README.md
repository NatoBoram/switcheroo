# [Switcheroo](https://gitlab.com/NatoBoram/fabric-switcheroo)

[![pipeline status](https://gitlab.com/NatoBoram/fabric-switcheroo/badges/master/pipeline.svg)](https://gitlab.com/NatoBoram/fabric-switcheroo/-/commits/master)
[![StackShare](https://img.shields.io/badge/tech-stack-0690fa.svg?style=flat)](https://stackshare.io/NatoBoram/switcheroo)

Switch your currently held item for an effective item when clicking on a block.

This project is in early development. It *technically* works, but it could be better.

## Usage

* Click on a block or entity to switch to the appropriate item
  * Press *Sneak* to prevent the switch from happening
  * Press *Sprint* to use the most effective item

By default, the switcheroo will use the least effective tool to break blocks and the most damaging item to attack entities.

![Switcheroo Demo](docs/Switcheroo Demo.webm)

## Status

### What works

* Switcheroo when clicking blocks
* Switcheroo when clicking mobs

### What doesn't work

* Switcheroo when using the last unit of a stack #4
  * In the meantime, you can use [Inventory Profiles](https://github.com/jsnimda/Inventory-Profiles)

## Installation

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
