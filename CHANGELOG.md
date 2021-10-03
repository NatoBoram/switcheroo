# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/) and this project adheres to [Semantic Versioning](https://semver.org/).

## [Unreleased]

### Added

### Changed

* Now uses swords on bamboo and cobweb.
* Now uses shears on cobweb.
* Uses item with more attack damage on players or more damage per seconds on mobs.

### Deprecated

### Removed

### Fixed

* Dependencies are now correctly resolved in `fabric.mod.json`.
* Dependencies are mentioned in `README.md`.

### Security

## [1.0.5] - 2021-10-02

### Added

* Configuration to always use the fast tool instead of the slowest efficient tool.
  * Accessible via [ModMenu](https://github.com/TerraformersMC/ModMenu).
  * `/switcheroo alwaysFastest`
* Configuration for the minimum durability.
  * Accessible via [ModMenu](https://github.com/TerraformersMC/ModMenu).
  * `/switcheroo minDurability`

### Changed

* Don't consider items when they have 5 durability instead of 1.

### Fixed

* Now takes into account [Efficiency](https://minecraft.fandom.com/wiki/Efficiency) levels.

## [1.0.4] - 2021-10-01

🎃

### Added

* Block and entity blacklist using [ModMenu](https://github.com/TerraformersMC/ModMenu).
  * Blacklist blocks `farmland` and `glass_pane` by default.
  * Blacklist entity `axolotl`, `bat`, `cat`, `donkey`, `fox`, `horse`, `mule`, `ocelot`, `parrot`, `skeleton_horse`,
  `snow_golem`, `strider`, `villager` and `wandering_trader` by default.
* Commands to manage the blacklist:
  * `/switcheroo blacklist blocks add farmland`
  * `/switcheroo blacklist blocks remove farmland`
  * `/switcheroo blacklist mobs add axolotl`
  * `/switcheroo blacklist mobs remove axolotl`

### Changed

* Prefer shears when clicking on leaves.
* Prefer shears when clicking on plants.
* Ignore axe when clicking on plants.

## [1.0.3] - 2021-09-26

### Added

* Select the tool from the hotbar instead of picking it from the inventory.

### Changed

* The mod's ID is now `switcheroo` instead of `fabric_switcheroo`.

## [1.0.2] - 2021-09-25

### Changed

* Updated to 1.17.1.

## [1.0.1] - 2021-02-01

### Changed

* Now selects the best weapon according to __damage per seconds__.
* Sword is now blacklisted when hitting blocks.
* Now uses hoe when clicking on a crop.
* Farmland is now blacklisted from triggering a switcheroo.

## [1.0.0] - 2021-01-28

First public release.

The switcheroo technically works, but it's kinda annoying and awkward to use. Please check the [issues](https://gitlab.com/NatoBoram/fabric-switcheroo/issues)
if you want to contribute. Since it's my first Fabric project and my first client-side mod, there's a few things I don't
really know how to do.

## Types of changes

* `Added` for new features.
* `Changed` for changes in existing functionality.
* `Deprecated` for soon-to-be removed features.
* `Removed` for now removed features.
* `Fixed` for any bug fixes.
* `Security` in case of vulnerabilities.
