# Masterful Botany

Masterful Botany is an addon for Botania that expands it while keeping its design principles. It targets
**Minecraft 1.21.1 / NeoForge 21.1.x** and is built against **Botania Revived** (package `velrondevs.botania`, mod id `botania`).

This is a derivative work of an existing mod by **noeppi_noeppi** (contributor: MelanX). See [NOTICE](NOTICE) for the origin,
the list of changes and the licensing information. It is not affiliated with or endorsed by the original author.

* Mod id: `masterfulbotany`
* Port author: UnnamedUser12
* License: Apache License 2.0 (see `LICENSE`)

## Requirements

* NeoForge 21.1.240+ (Minecraft 1.21.1)
* Botania Revived 1.0.3+
* Patchouli, Curios (required by Botania Revived / this mod)
* [LibX](https://github.com/ModdingX/LibX) `1.21.1-6.0.15`+
* JEI (optional, recipe categories for the mana infuser and the rune rituals)

## Notes

* Data generation: `./gradlew runData` writes the datapack and assets (recipes, loot, tags, advancements, worldgen,
  models, lexicon) to `src/generated/resources`. Hand written resources stay in `src/main/resources`.
* The enchantment *Hammer Mobility* is data driven (`data/masterfulbotany/enchantment/hammer_mobility.json`).
* Apotheosis IMC is not ported. The Botanic Additions and Feywild recipes/tags are kept as hand written conditional data in `src/main/resources`.
* The terra helmet check and the HUD background option are mixins (`masterfulbotany.mixins.json`); the Alfheim fancy sky is not ported.

## Building

```
./gradlew build
```
