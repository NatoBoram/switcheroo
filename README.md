# Switcheroo

Switch your currently held item for an effective item when clicking on a block.

This project is in early development. It *technically* works, but it could be better.

## Status

### What works

* Switcheroo when attacking blocks
* Switcheroo when attacking mobs

### What doesn't work

* Switcheroo when attacking crops #1
  * Auto-replant attacked crops #2
* Switcheroo will mess up your hotbar's organization #3
* Switcheroo when using the last unit of a stack #4

## Installation

Right now, there's no way to install it other than building it yourself.

## Building

```bash
./gradlew downloadAssets
./gradlew genSources
./gradlew build
```
