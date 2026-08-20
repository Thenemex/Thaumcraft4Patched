package thaumcraft4patched.model.patch;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import thaumcraft4patched.config.Config;

/**
 * Restores normal harvesting for Thaumic Exploration's Oblivion Jar, :D.
 */
public final class ThaumicExplorationOblivionJarHarvestPatch {

    private static final String OBLIVION_JAR_CLASS =
            "flaxbeard.thaumicexploration.block.BlockTrashJar";

    private ThaumicExplorationOblivionJarHarvestPatch() {}

    public static ArrayList<ItemStack> getDrops(
            Block block,
            int metadata) {

        ArrayList<ItemStack> drops = new ArrayList<ItemStack>();

        if (Config.txEnabled
                && Config.oblivionJarHarvestPatchEnabled) {

            drops.add(new ItemStack(block, 1, metadata));
        }

        return drops;
    }

    /**
     * Wraps Thaumcraft BlockJar's early drop call.
     *
     * For the Oblivion Jar the early drop is skipped because normal harvest
     * handling will call the patched getDrops() afterwards. All other blocks
     */
    public static void dropBlockAsItem(
            Block block,
            World world,
            int x,
            int y,
            int z,
            int metadata,
            int fortune) {

        if (Config.txEnabled
                && Config.oblivionJarHarvestPatchEnabled
                && OBLIVION_JAR_CLASS.equals(
                block.getClass().getName())) {

            return;
        }

        block.dropBlockAsItem(
                world,
                x,
                y,
                z,
                metadata,
                fortune
        );
    }
}