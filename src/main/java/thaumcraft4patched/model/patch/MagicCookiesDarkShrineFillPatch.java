package thaumcraft4patched.model.patch;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import thaumcraft4patched.config.Config;

/**
 * Gives the Magic Cookies Dark Shrine foundation a floor.
 *
 * The shrine generator walks down from the structure and puts dark brick in
 * every position that holds air. It stops at the first position that holds
 * another block. The shrine is wider than one chunk, so the generator can read
 * a position in a chunk that is not generated yet. Some chunk loaders hold that
 * read back while they populate a chunk and give air for each position. The
 * loop then has no end. It keeps the server thread at full load, and the log
 * stays silent.
 *
 * The transformer sends the block read of that loop to this class. The read
 * gives a solid block when the loop is too deep, so the loop always stops.
 */
public final class MagicCookiesDarkShrineFillPatch implements IPatch {

    /**
     * Lowest height the foundation may touch. Layer 0 holds bedrock.
     */
    private static final int MINIMUM_FOUNDATION_HEIGHT = 1;

    private MagicCookiesDarkShrineFillPatch() {}

    /**
     * Replaces the block read inside the foundation loop.
     *
     * @param depthOffset vertical offset of the loop, counted from the shrine
     *                    position. It starts at -8 and goes down by one for
     *                    each layer.
     * @return the block at the position, or a solid block when the loop must
     *         stop. The caller compares this block with air and does nothing
     *         else with it.
     */
    public static Block getBlock(
            World world,
            int x,
            int y,
            int z,
            int depthOffset) {

        if (isFoundationTooDeep(y, depthOffset)) {
            return Blocks.bedrock;
        }

        return world.getBlock(x, y, z);
    }

    private static boolean isFoundationTooDeep(int y, int depthOffset) {
        if (!Config.mgckEnabled
                || !Config.endlessDarkShrineFoundationPatchEnabled) {

            return false;
        }

        if (y <= MINIMUM_FOUNDATION_HEIGHT) {
            return true;
        }

        return depthOffset
                <= -Config.mgckDarkShrineFoundationDepthLimit;
    }
}
