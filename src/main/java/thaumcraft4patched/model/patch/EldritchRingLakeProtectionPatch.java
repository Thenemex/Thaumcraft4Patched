package thaumcraft4patched.model.patch;

import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenLakes;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft4patched.config.Config;

import java.util.Random;

public final class EldritchRingLakeProtectionPatch {

    private EldritchRingLakeProtectionPatch() {
    }

    public static boolean generate(
            WorldGenLakes generator,
            World world,
            Random random,
            int x,
            int y,
            int z) {

        if (Config.tc4Enabled
                && Config.eldritchRingLakeProtectionPatchEnabled
                && intersectsEldritchStructure(world, x, y, z)) {

            return false;
        }

        return generator.generate(world, random, x, y, z);
    }

    private static boolean intersectsEldritchStructure(
            World world,
            int x,
            int y,
            int z) {

        int lakeX = x - 8;
        int lakeY = y;
        int lakeZ = z - 8;

        while (lakeY > 5
                && world.isAirBlock(lakeX, lakeY, lakeZ)) {

            lakeY--;
        }

        if (lakeY <= 4) {
            return false;
        }

        lakeY -= 4;

        for (int offsetX = 0; offsetX < 16; offsetX++) {
            for (int offsetZ = 0; offsetZ < 16; offsetZ++) {
                for (int offsetY = 0; offsetY < 8; offsetY++) {
                    int blockX = lakeX + offsetX;
                    int blockY = lakeY + offsetY;
                    int blockZ = lakeZ + offsetZ;

                    Block block =
                            world.getBlock(blockX, blockY, blockZ);

                    if (block == ConfigBlocks.blockEldritch) {
                        return true;
                    }

                    if (block == ConfigBlocks.blockCosmeticSolid
                            && world.getBlockMetadata(
                            blockX,
                            blockY,
                            blockZ
                    ) == 1) {

                        return true;
                    }
                }
            }
        }

        return false;
    }
}
