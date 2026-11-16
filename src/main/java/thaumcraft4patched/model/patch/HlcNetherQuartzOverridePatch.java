package thaumcraft4patched.model.patch;

import net.minecraft.block.Block;
import net.minecraftforge.event.world.BlockEvent;

import static thaumcraft4patched.config.Config.hlcEnabled;
import static thaumcraft4patched.config.Config.netherQuartzHlcCompatibilityPatchEnabled;

public final class HlcNetherQuartzOverridePatch {

    private static final String QUARTZ_ORE = "minecraft:quartz_ore";
    private static final String OVERRIDE_CLASS =
            "powercrystals.netherores.ores.BlockNetherOverrideOre";

    private HlcNetherQuartzOverridePatch() {}

    public static boolean shouldSkipManualDrops(BlockEvent.BreakEvent event) {
        return isEnabled()
                && event != null
                && isNetherOresOverride(event.block)
                && QUARTZ_ORE.equals(Block.blockRegistry.getNameForObject(event.block));
    }

    public static String resolveHarvestBlockName(
            String registryName, BlockEvent.HarvestDropsEvent event) {
        if (!isEnabled() || event == null || event.harvester == null
                || event.block == null || QUARTZ_ORE.equals(registryName)) {
            return registryName;
        }

        Block quartz = Block.getBlockFromName(QUARTZ_ORE);
        if (isNetherOresOverride(quartz) && quartz.equals(event.block)) {
            return QUARTZ_ORE;
        }
        return registryName;
    }

    private static boolean isEnabled() {
        return hlcEnabled && netherQuartzHlcCompatibilityPatchEnabled;
    }

    private static boolean isNetherOresOverride(Block block) {
        if (block == null) {
            return false;
        }
        for (Class<?> type = block.getClass(); type != null; type = type.getSuperclass()) {
            if (OVERRIDE_CLASS.equals(type.getName())) {
                return true;
            }
        }
        return false;
    }
}
