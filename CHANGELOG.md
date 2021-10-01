# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/) and this project adheres to [Semantic Versioning](https://semver.org/).

## [Unreleased]

### Added

* Block and entity blacklist using ModMenu.
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

### Deprecated

### Removed

### Fixed

### Security

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
