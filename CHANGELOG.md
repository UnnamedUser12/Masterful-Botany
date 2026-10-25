# Changelog

## 1.0.0 
- First release of Masterful Botany for NeoForge 1.21.1, built against Botania Revived 1.0.3. It is a derivative work of MythicBotany by noeppi_noeppi (https://github.com/noeppi-noeppi/MythicBotany, version 4.0 for 1.20.1 Forge); see NOTICE.
- Ported to the 1.21 APIs: data components, codecs/stream codecs, new recipe system, block entity capabilities, payload networking, data driven enchantments and new datapack paths.
- The Alfheim dimension, worldgen, structures, runic rituals, mana infuser, alfsteel tools and armor, functional/generating flowers, Mjoellnir and the Patchouli entries are included.
- Renamed to Masterful Botany (mod id `masterfulbotany`).
- Source reorganized under `velrondevs.masterfulbotany`: `registry/` (one class per registry type), `datagen/` (providers for advancements, loot, models, recipes, tags and worldgen), `common/`, `client/`, `network/`, `integration/`, `mixin/`, `config/`, `event/`.
- Fixed while testing: the rune holder accepted any item, the Ring of Muspelheim and the Ring of Niflheim checked the victim instead of the attacker, the configured durability of the alfsteel pickaxe, axe and sword was ignored, and the Gjallarhorn and the Mead of Kvasir had no drinking time.
- 81 GameTests (recipes, rune rituals, mana infuser, flowers, Mjoellnir, alfsteel, rings, Alf Pixie, Alfheim terrain and structures) in a separate `gametest` source set that is not part of the release jar.
