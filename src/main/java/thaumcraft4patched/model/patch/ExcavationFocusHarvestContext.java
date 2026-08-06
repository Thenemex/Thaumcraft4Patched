package thaumcraft4patched.model.patch;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import thaumcraft.common.lib.utils.BlockUtils;

import java.util.ArrayList;

/**
 * Tracks harvest events produced by the Excavation Focus's active magic.
 *
 * Physical block breaking with the wand itself does not use these wrappers
 * and therefore does not activate this context.
 */
public final class ExcavationFocusHarvestContext {

    private static final ThreadLocal<Integer> ACTIVE_DEPTH =
            new ThreadLocal<Integer>();

    private ExcavationFocusHarvestContext() {
    }

    private static void begin() {
        Integer depth = ACTIVE_DEPTH.get();

        if (depth == null) {
            ACTIVE_DEPTH.set(1);
        } else {
            ACTIVE_DEPTH.set(depth + 1);
        }
    }

    private static void end() {
        Integer depth = ACTIVE_DEPTH.get();

        if (depth == null || depth <= 1) {
            ACTIVE_DEPTH.remove();
        } else {
            ACTIVE_DEPTH.set(depth - 1);
        }
    }

    /**
     * True only while the Excavation Focus's magic is generating block drops.
     */
    public static boolean isActive() {
        Integer depth = ACTIVE_DEPTH.get();
        return depth != null && depth > 0;
    }

    /**
     * Wraps Thaumcraft's Silk Touch harvest-event path.
     */
    public static float fireBlockHarvesting(
            ArrayList<ItemStack> drops,
            World world,
            Block block,
            int x,
            int y,
            int z,
            int metadata,
            int fortune,
            float dropChance,
            boolean silkTouch,
            EntityPlayer player) {

        begin();

        try {
            return ForgeEventFactory.fireBlockHarvesting(
                    drops,
                    world,
                    block,
                    x,
                    y,
                    z,
                    metadata,
                    fortune,
                    dropChance,
                    silkTouch,
                    player
            );
        } finally {
            end();
        }
    }

    /**
     * Wraps Thaumcraft's normal Excavation Focus drop path.
     */
    public static void dropBlockAsItemWithChance(
            World world,
            Block block,
            int x,
            int y,
            int z,
            int metadata,
            float dropChance,
            int fortune,
            EntityPlayer player) {

        begin();

        try {
            BlockUtils.dropBlockAsItemWithChance(
                    world,
                    block,
                    x,
                    y,
                    z,
                    metadata,
                    dropChance,
                    fortune,
                    player
            );
        } finally {
            end();
        }
    }
}