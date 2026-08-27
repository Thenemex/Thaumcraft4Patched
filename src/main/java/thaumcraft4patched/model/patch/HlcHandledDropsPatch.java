package thaumcraft4patched.model.patch;

import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;

import static thaumcraft4patched.config.Config.hlcEnabled;
import static thaumcraft4patched.config.Config.neighborDropsHlcCompatibilityPatchEnabled;

public final class HlcHandledDropsPatch {

    private static World world;
    private static Block block;
    private static int x;
    private static int y;
    private static int z;
    private static int metadata;
    private static boolean pending;

    private HlcHandledDropsPatch() {}

    public static void rememberBreak(BlockEvent.BreakEvent event) {
        if (!enabled()) return;

        world = event.world;
        block = event.block;
        x = event.x;
        y = event.y;
        z = event.z;
        metadata = event.blockMetadata;
        pending = true;
    }

    public static boolean shouldClearDrops(BlockEvent.HarvestDropsEvent event) {
        if (!enabled()) return true;
        if (!pending) return false;

        if (world != event.world
                || block != event.block
                || x != event.x
                || y != event.y
                || z != event.z
                || metadata != event.blockMetadata) {
            return false;
        }

        pending = false;
        world = null;
        block = null;
        return true;
    }

    private static boolean enabled() {
        return hlcEnabled && neighborDropsHlcCompatibilityPatchEnabled;
    }
}