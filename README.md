# Switcheroo

[![pipeline status](https://gitlab.com/NatoBoram/fabric-switcheroo/badges/master/pipeline.svg)](https://gitlab.com/NatoBoram/fabric-switcheroo/-/commits/master)
[![StackShare](https://img.shields.io/badge/tech-stack-0690fa.svg?style=flat)](https://stackshare.io/NatoBoram/switcheroo)

Switch your currently held item for an effective item when clicking on a block.

This project is in early development. It *technically* works, but it could be better.

## Usage

* Click on a block or entity to switch to the appropriate item
  * Press *Sneak* to prevent the switch from happening
  * Press *Sprint* to use the most effective item

By default, the switcheroo will use the least effective tool to break blocks and the most damaging item to attack entities.

## Status

### What works

* Switcheroo when clicking blocks
* Switcheroo when clicking mobs

### What doesn't work

* Switcheroo when clicking crops #1
  * Auto-replant harvested crops #2
* Switcheroo will mess up your hotbar's organization #3
* Switcheroo when using the last unit of a stack #4

## Installation

Downloads are available in [Releases](https://gitlab.com/NatoBoram/fabric-switcheroo/-/releases). You can also test
development builds by downloading them form the [Pipelines](https://gitlab.com/NatoBoram/fabric-switcheroo/-/pipelines).

## Building

```bash
./gradlew downloadAssets
./gradlew genSources
./gradlew build
```
