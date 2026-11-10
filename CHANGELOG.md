# Changelog

## 1.0.2
- The mod now requires Botania Revived 2.0.0 or newer, and is built and tested against Botania Revived 26.1.2 (2.0.0).

## 1.0.1
- Ported to Minecraft 26.1.2 / NeoForge 26.1.2.126, built against Botania Revived 26.1.2 (1.0.7) and LibX 26.1.2-7.0.6.
- Updated to the LibX 7 API: registered items and blocks receive their `Properties` in the constructor, inventories use the new `ResourceHandler` transfer API and the datagen providers moved to `ModelProvider`.
- The recipes `masterfulbotany:infuser` and `masterfulbotany:rune_ritual` store their result as an `ItemStackTemplate`, as 26.1 requires.
- Alfheim biomes and dimension use the new environment attributes. The Alfheim grass uses the vanilla `grass_jungle` feature.
- Items and the Alfsteel pylon use item definitions (`assets/masterfulbotany/items/`).
- Removed the `@OnlyIn` annotations, which no longer strip anything at runtime in 26.1. This also removes the warning screen when the game starts.
- The message shown when you cannot lift Mjoellnir now also mentions that the Ring of Thor equipped in a Curios slot lets you hold it.
- Known difference: the sleep handler was removed because the overworld clock is shared in 26.1.
- 82 GameTests (recipes, rune rituals, mana infuser, flowers, Mjoellnir, alfsteel, rings, Alf Pixie, Alfheim terrain and structures) in a separate `gametest` source set that is not part of the release jar.

## 1.0.0 
- First release of Masterful Botany for NeoForge 1.21.1, built against Botania Revived 1.0.3. It is a derivative work of MythicBotany by noeppi_noeppi (https://github.com/noeppi-noeppi/MythicBotany, version 4.0 for 1.20.1 Forge); see NOTICE.
- Ported to the 1.21 APIs: data components, codecs/stream codecs, new recipe system, block entity capabilities, payload networking, data driven enchantments and new datapack paths.
- The Alfheim dimension, worldgen, structures, runic rituals, mana infuser, alfsteel tools and armor, functional/generating flowers, Mjoellnir and the Patchouli entries are included.
- Renamed to Masterful Botany (mod id `masterfulbotany`).
- Source reorganized under `velrondevs.masterfulbotany`: `registry/` (one class per registry type), `datagen/` (providers for advancements, loot, models, recipes, tags and worldgen), `common/`, `client/`, `network/`, `integration/`, `mixin/`, `config/`, `event/`.
- Fixed while testing: the rune holder accepted any item, the Ring of Muspelheim and the Ring of Niflheim checked the victim instead of the attacker, the configured durability of the alfsteel pickaxe, axe and sword was ignored, and the Gjallarhorn and the Mead of Kvasir had no drinking time.
- 81 GameTests (recipes, rune rituals, mana infuser, flowers, Mjoellnir, alfsteel, rings, Alf Pixie, Alfheim terrain and structures) in a separate `gametest` source set that is not part of the release jar.
