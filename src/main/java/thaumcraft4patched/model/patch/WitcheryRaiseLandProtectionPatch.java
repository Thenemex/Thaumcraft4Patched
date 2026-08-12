package thaumcraft4patched.model.patch;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import thaumcraft4patched.config.Config;

/**
 * Protects blocks that Witchery's Raise Land brew must never move.
 *
 * The original Raise Land implementation removes a source block and then
 * places the remembered block higher in the column. Both operations are
 * routed through this class so protected blocks are neither removed nor
 * duplicated.
 */
public final class WitcheryRaiseLandProtectionPatch {

    private WitcheryRaiseLandProtectionPatch() {}

    public static boolean setBlockToAir(
            World world,
            int x,
            int y,
            int z) {

        Block block = world.getBlock(x, y, z);

        if (shouldProtectBlock(block)) {
            return false;
        }

        return world.setBlockToAir(x, y, z);
    }

    public static boolean setBlock(
            World world,
            int x,
            int y,
            int z,
            Block block,
            int metadata,
            int flags) {

        if (shouldProtectBlock(block)) {
            return false;
        }

        return world.setBlock(
                x,
                y,
                z,
                block,
                metadata,
                flags
        );
    }

    private static boolean shouldProtectBlock(Block block) {
        return Config.witcheryEnabled
                && Config.witcheryRaiseLandBedrockProtectionPatchEnabled
                && block == Blocks.bedrock;
    }
}