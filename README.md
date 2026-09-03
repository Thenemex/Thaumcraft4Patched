# Thaumcraft 4 : Patched

An addon for Thaumcraft 4, in Minecraft 1.7.10. Bring bug patches to the mod.

### Patched Bugs :
- Bone Bow research used to be stuck hidden in some cases -> it will now unlock once the player discover the Telum aspect
- Thaumic Exploration used to add a bugged infusion recipe for some non-existent item "Necro Altar" -> it removes that recipe that can make crash other mods like Thaumcraft NEI Plugin : Patched
- Thaumic Exploration add 16 floating candles items/blocks, but the recipe for the black one was crashing the game -> patched !
* Magic Cookies used to give a fully opaque screen when entering a Nether Dark Shrine structure, when using higher Java version
* Magic Cookies entropy fog intensity can now be configured from 0 to 10, with 4 matching the recommended default behaviour.
* Magic Cookies Dark Shrine generation could fill a column of air without an end, when the shrine reached into a chunk that was not generated yet. The server thread then stayed at full load with no crash and no log line. The foundation now stops at a set depth, which you can configure.
* Adds Harvest Level Config compatibility for:
  * Thaumcraft's Excavation Focus
  * Thaumcraft's Primal Crusher
* Adds Fast Leaf Decay compatibility for:
  * Thaumcraft Greatwood Leaves
  * Thaumcraft Silverwood Leaves
  * Tainted Magic Warpwood Leaves
* Gravestone gave a death note after every death while the keepInventory game rule was on, even with "enable_death_note" turned off in its config. The note now follows that entry.
* Prevents Thaumic Concilium Thaumaturges and Witchery Village Guards from attacking each other, while preserving their normal combat behaviour against other mobs.
* Thaumic Tinkerer's six elemental fires now tick and transmute neighbor blocks when the doFireTick game rule is off. Vanilla fire is unchanged.
- Adds the missing prereqs (forgotten by author) for some researches in TC4 addons :
  - Thaumic Bases :
    - Thaumium Bracelet, Void Bracelet and Void Wand Core
  - Witching Gadgets :
    - Witching Wearables 

### Helpers :
- Huge thanks to [**Setokaiba218**](https://www.curseforge.com/members/setokaiba218/projects) for the opaque screen bug patch for Magic Cookies and additional compatibility patches!
