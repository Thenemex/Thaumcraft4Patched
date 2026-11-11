# Thaumcraft 4 : Patched

An addon for Thaumcraft 4, in Minecraft 1.7.10. Bring bug patches to the mod.

### Patched Bugs :
# Thaumcraft 4 : Patched

An addon for Thaumcraft 4, in Minecraft 1.7.10. Bring bug patches to the mod.

### Patched Bugs :

- **Vanilla Minecraft**
  - Oak slabs. Fixes the oak recipe taking priority over other wooden slab recipes.
  - Villager trades. Checks item subtypes and required NBT while preserving wildcard metadata support.

- **Angelica**
  - Sign editor. Fixes the pink or missing sign texture caused by cached rendering.

- **Ender Zoo**
  - Dire Slime check. Recognises active Excavation Focus magic on dirt and grass when the Harvest Level Config integration is active.

- **Fast Leaf Decay**
  - Greatwood and Silverwood. Both leaf types follow the configured decay speed.
  - Warpwood. Tainted Magic's leaves also follow the configured decay speed.

- **Forbidden Magic**
  - Impact. Checks block-break permissions for each extra block mined by a Chameleon tool's 3x3 effect.

- **Gravestone**
  - Death notes. Respects enable_death_note when keepInventory is on.

- **Harvest Level Config**
  - Excavation Focus. Active magic counts as a diamond-tier pickaxe, axe, and shovel.
  - Harvest Level Config - Primal Crusher. Counts as a level-5 pickaxe and shovel.
  - Attached block drops. Preserves drops from attached blocks that break before the block you're mining.

- **Magic Cookies**
  - Dark Shrine fog. Fixes the opaque screen on newer Java versions and adds fog intensity and distance settings.
  - Dark Shrine generation. Caps foundation depth to prevent the Nether generator from freezing the server.
  - Golem Decorations. Stops invalid item metadata from crashing icon lookups in NEI and other mods.

- **MineTweaker 3**
  - Command rollback. Prevents a crash when switching between singleplayer and multiplayer.

- **Special AI**
  - Shambling Husks. Stops Special AI from adding its extra behaviours to them.

- **Thaumcraft 4**
  - Bone Bow research. Appears after you discover Telum.
  - Eldritch Rings and Obelisks. Prevents vanilla lake generation from cutting through them.
  - Lumber Golems. Can harvest wood whose harvest level was changed by another mod.
  - Research prerequisites. Replaces null prerequisite lists with empty lists. Off by default.
  - Shambling Husk drops. Restores guaranteed equipment drops when a husk dies.
  - Thaumonomicon lighting. Fixes dark 3D item models on recipe pages.

- **Thaumic Bases**
  - Entity Deconstructor. Prevents a crash from casting a non-player entity to a server player.
  - Thaumium Bracelet. Adds its missing Thaumium research prerequisite.
  - Void Bracelet. Adds its missing research prerequisites.
  - Void Wand Core. Adds Salis Mundus Block as a prerequisite.

- **Thaumic Concilium**
  - Mad Thaumaturges. Prevents them from mounting and riding their attack targets.
  - NPC combat. Stops Thaumaturges and Witchery Village Guards from attacking each other.

- **Thaumic Exploration**
  - Black Floating Candle. Fixes the crafting crash.
  - Oblivion Jar. Makes the jar drop itself when harvested.

- **Thaumic Horizons**
  - Alchemite. Sends Alchemite explosions through Forge's detonation event so claim mods can protect blocks.

- **Thaumic Tinkerer**
  - Elemental fires. All six can tick and transmute neighbouring blocks with doFireTick off.

- **Witchery**
  - Raise Land. Prevents the brew from moving or duplicating bedrock.

- **Witching Gadgets**
  - Witching Wearables. Adds Thaumium, Infusion, and Primal Arrows as prerequisites.

### Helpers :
- Huge thanks to [**Setokaiba218**](https://www.curseforge.com/members/setokaiba218/projects) for most of the patches in there !
