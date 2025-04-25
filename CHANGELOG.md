# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/) and this project adheres to [Semantic Versioning](https://semver.org/).

## [Unreleased]

### Added

### Changed

- Updated to Minecraft 1.21.4

### Deprecated

### Removed

### Fixed

### Security

## [1.0.14] - 2024-06-30

> [!NOTE]
> The Mace has lower damage per seconds than bare hands. The mod will not attempt to switch out of it, but won't switch into it either.

> [!WARNING]
> The Mace's enchantments are not supported at the moment. It's better to disable Switcheroo during your Mace activities.

### Added

- `/switcheroo enable` and `/switcheroo disable` commands

### Changed

- Updated to Minecraft 1.21

## [1.0.13] - 2024-05-25

### Added

- Now uses shears on vines
- Now uses brush on suspicious blocks

## [1.0.12] - 2024-04-01

### Changed

- Updated to Minecraft 1.20.1

### Fixed

- Now uses shears on glow berries

## [1.0.11] - 2023-06-07

- Updated to Minecraft 1.20.0

## [1.0.10] - 2023-03-19

- Updated to Minecraft 1.19.4

## [1.0.9] - 2022-06-13

### Added

- Prefer Silk Touch on certain blocks
  - `/switcheroo prefer silk_touch`
    - `/switcheroo prefer silk_touch add dirt_path`
    - `/switcheroo prefer silk_touch remove dirt_path`
  - By default, prefers Silk Touch on `bee_nest`, `beehive`, `black_stained_glass`, `black_stained_glass_pane`, `blue_ice`,
    `blue_stained_glass`, `blue_stained_glass_pane`, `bookshelf`, `brain_coral`, `brain_coral_block`, `brown_mushroom_block`,
    `brown_stained_glass`, `brown_stained_glass_pane`, `bubble_coral`, `bubble_coral_block`, `campfire`, `crimson_nylium`,
    `cyan_stained_glass`, `cyan_stained_glass_pane`, `dirt_path`, `ender_chest`, `fire_coral`, `fire_coral_block`, `glass`,
    `glass_pane`, `gray_stained_glass`, `gray_stained_glass_pane`, `green_stained_glass`, `green_stained_glass_pane`, `horn_coral`,
    `horn_coral_block`, `ice`, `infested_chiseled_stone_bricks`, `infested_cobblestone`, `infested_cracked_stone_bricks`,
    `infested_deepslate`, `infested_mossy_stone_bricks`, `infested_stone`, `infested_stone_bricks`, `large_amethyst_bud`,
    `light_blue_stained_glass`, `light_blue_stained_glass_pane`, `light_gray_stained_glass`, `light_gray_stained_glass_pane`,
    `lime_stained_glass`, `lime_stained_glass_pane`, `magenta_stained_glass`, `magenta_stained_glass_pane`, `medium_amethyst_bud`,
    `mushroom_stem`, `mycelium`, `orange_stained_glass`, `orange_stained_glass_pane`, `packed_ice`, `pink_stained_glass`,
    `pink_stained_glass_pane`, `purple_stained_glass`, `purple_stained_glass_pane`, `red_mushroom_block`, `red_stained_glass`,
    `red_stained_glass_pane`, `sculk`, `sculk_catalyst`, `sculk_sensor`, `sculk_shrieker`, `sculk_vein`, `sea_lantern`, `small_amethyst_bud`,
    `snow`, `snow_block`, `soul_campfire`, `tube_coral`, `tube_coral_block`, `turtle_egg`, `twisting_vines`, `twisting_vines_plant`,
    `warped_nylium`, `weeping_vines`, `weeping_vines_plant`, `white_stained_glass`, `white_stained_glass_pane`, `yellow_stained_glass`
    and `yellow_stained_glass_pane`.

## [1.0.8] - 2022-06-8

- Updated to Minecraft 1.19

## [1.0.7] - 2021-11-30

- Updated to Minecraft 1.18

## [1.0.6] - 2021-10-03

### Changed

- Now uses swords on bamboo and cobweb.
- Now uses shears on cobweb.
- Uses item with more attack damage on players or more damage per seconds on mobs.

### Fixed

- Dependencies are now correctly resolved in `fabric.mod.json`.
- Dependencies are mentioned in `README.md`.

## [1.0.5] - 2021-10-02

### Added

- Configuration to always use the fast tool instead of the slowest efficient tool.
  - Accessible via [ModMenu](https://github.com/TerraformersMC/ModMenu).
  - `/switcheroo alwaysFastest`
- Configuration for the minimum durability.
  - Accessible via [ModMenu](https://github.com/TerraformersMC/ModMenu).
  - `/switcheroo minDurability`

### Changed

- Don't consider items when they have 5 durability instead of 1.

### Fixed

- Now takes into account [Efficiency](https://minecraft.fandom.com/wiki/Efficiency) levels.

## [1.0.4] - 2021-10-01

🎃

### Added

- Block and entity blacklist using [ModMenu](https://github.com/TerraformersMC/ModMenu).
  - Blacklist blocks `farmland` and `glass_pane` by default.
  - Blacklist entity `axolotl`, `bat`, `cat`, `donkey`, `fox`, `horse`, `mule`, `ocelot`, `parrot`, `skeleton_horse`,
    `snow_golem`, `strider`, `villager` and `wandering_trader` by default.
- Commands to manage the blacklist:
  - `/switcheroo blacklist blocks add farmland`
  - `/switcheroo blacklist blocks remove farmland`
  - `/switcheroo blacklist mobs add axolotl`
  - `/switcheroo blacklist mobs remove axolotl`

### Changed

- Prefer shears when clicking on leaves.
- Prefer shears when clicking on plants.
- Ignore axe when clicking on plants.

## [1.0.3] - 2021-09-26

### Added

- Select the tool from the hotbar instead of picking it from the inventory.

### Changed

- The mod's ID is now `switcheroo` instead of `fabric_switcheroo`.

## [1.0.2] - 2021-09-25

### Changed

- Updated to 1.17.1.

## [1.0.1] - 2021-02-01

### Changed

- Now selects the best weapon according to **damage per seconds**.
- Sword is now blacklisted when hitting blocks.
- Now uses hoe when clicking on a crop.
- Farmland is now blacklisted from triggering a switcheroo.

## [1.0.0] - 2021-01-28

First public release.

The switcheroo technically works, but it's kinda annoying and awkward to use. Please check the [issues](https://github.com/NatoBoram/switcheroo/issues) if you want to contribute. Since it's my first Fabric project and my first client-side mod, there's a few things I don't really know how to do.

## Types of changes

- `Added` for new features.
- `Changed` for changes in existing functionality.
- `Deprecated` for soon-to-be removed features.
- `Removed` for now removed features.
- `Fixed` for any bug fixes.
- `Security` in case of vulnerabilities.

[Unreleased]: https://github.com/NatoBoram/switcheroo/compare/v1.0.14...main
[1.0.14]: https://github.com/NatoBoram/switcheroo/releases/tag/v1.0.14
[1.0.13]: https://github.com/NatoBoram/switcheroo/releases/tag/v1.0.13
[1.0.12]: https://github.com/NatoBoram/switcheroo/releases/tag/v1.0.12
[1.0.11]: https://github.com/NatoBoram/switcheroo/releases/tag/v1.0.11
[1.0.10]: https://github.com/NatoBoram/switcheroo/releases/tag/v1.0.10
[1.0.9]: https://github.com/NatoBoram/switcheroo/releases/tag/v1.0.9
[1.0.8]: https://github.com/NatoBoram/switcheroo/releases/tag/v1.0.8
[1.0.7]: https://github.com/NatoBoram/switcheroo/releases/tag/v1.0.7
[1.0.6]: https://github.com/NatoBoram/switcheroo/releases/tag/v1.0.6
[1.0.5]: https://github.com/NatoBoram/switcheroo/releases/tag/v1.0.5
[1.0.4]: https://github.com/NatoBoram/switcheroo/releases/tag/v1.0.4
[1.0.3]: https://github.com/NatoBoram/switcheroo/releases/tag/v1.0.3
[1.0.2]: https://github.com/NatoBoram/switcheroo/releases/tag/v1.0.2
[1.0.1]: https://github.com/NatoBoram/switcheroo/releases/tag/v1.0.1
[1.0.0]: https://github.com/NatoBoram/switcheroo/releases/tag/v1.0.0
