package thaumcraft4patched.model.patch;

import net.minecraft.block.Block;
import net.minecraft.world.World;
import thaumcraft4patched.config.Config;

public final class HungryNodeGravestonePatch {

    private HungryNodeGravestonePatch() {}

    public static boolean breakBlockUnlessGrave(World world, int x, int y, int z, boolean drop) {
        if (Config.gravestoneEnabled && Config.gravestoneHungryNodeProtection) {
            Block block = world.getBlock(x, y, z);
            if (block != null && ("gravestone:gravestone".equals(Block.blockRegistry.getNameForObject(block))
                    || "de.maxhenkel.gravestone.blocks.BlockGraveStone".equals(block.getClass().getName()))) {
                return false;
            }
        }
        return world.func_147480_a(x, y, z, drop);
    }
}
