package thaumcraft4patched.model.patch;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;
import thaumcraft.common.blocks.BlockMagicalLeaves;
import thaumcraft.common.blocks.BlockMagicalLog;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static thaumcraft4patched.Thaumcraft4Patched.logger;

public class FastLeafDecayCompatibilityPatch implements IPatch {

    private static final int LEAF_SCAN_RADIUS = 4;

    private static final String FAST_LEAF_DECAY_HANDLER =
            "com.olafski.fastleafdecay.FldHandler";

    private static final String WARPWOOD_LOG_CLASS =
            "taintedmagic.common.blocks.BlockWarpwoodLog";

    private static final String WARPWOOD_LEAVES_CLASS =
            "taintedmagic.common.blocks.BlockWarpwoodLeaves";

    private final boolean thaumcraftMagicalLeavesEnabled;
    private final boolean taintedMagicWarpwoodLeavesEnabled;

    private final Method handleLeafDecayMethod;

    private boolean invocationFailureLogged;

    public FastLeafDecayCompatibilityPatch(
            boolean thaumcraftMagicalLeavesEnabled,
            boolean taintedMagicWarpwoodLeavesEnabled) {

        this.thaumcraftMagicalLeavesEnabled =
                thaumcraftMagicalLeavesEnabled;

        this.taintedMagicWarpwoodLeavesEnabled =
                taintedMagicWarpwoodLeavesEnabled;

        this.handleLeafDecayMethod = findFastLeafDecayHandler();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onBlockBroken(BlockEvent.BreakEvent event) {
        if (event.isCanceled()
                || event.world.isRemote
                || handleLeafDecayMethod == null) {
            return;
        }

        boolean magicalLogBroken =
                thaumcraftMagicalLeavesEnabled
                        && event.block instanceof BlockMagicalLog;

        boolean warpwoodLogBroken =
                taintedMagicWarpwoodLeavesEnabled
                        && hasClassName(event.block, WARPWOOD_LOG_CLASS);

        if (!magicalLogBroken && !warpwoodLogBroken) {
            return;
        }

        markAndScheduleLeaves(
                event.world,
                event.x,
                event.y,
                event.z,
                magicalLogBroken,
                warpwoodLogBroken
        );
    }

    private void markAndScheduleLeaves(
            World world,
            int logX,
            int logY,
            int logZ,
            boolean scanMagicalLeaves,
            boolean scanWarpwoodLeaves) {

        for (int offsetX = -LEAF_SCAN_RADIUS;
             offsetX <= LEAF_SCAN_RADIUS;
             offsetX++) {

            for (int offsetY = -LEAF_SCAN_RADIUS;
                 offsetY <= LEAF_SCAN_RADIUS;
                 offsetY++) {

                for (int offsetZ = -LEAF_SCAN_RADIUS;
                     offsetZ <= LEAF_SCAN_RADIUS;
                     offsetZ++) {

                    int leafX = logX + offsetX;
                    int leafY = logY + offsetY;
                    int leafZ = logZ + offsetZ;

                    if (!world.blockExists(leafX, leafY, leafZ)) {
                        continue;
                    }

                    Block block = world.getBlock(
                            leafX,
                            leafY,
                            leafZ
                    );

                    if (!isTargetLeaf(
                            block,
                            scanMagicalLeaves,
                            scanWarpwoodLeaves)) {
                        continue;
                    }

                    block.beginLeavesDecay(
                            world,
                            leafX,
                            leafY,
                            leafZ
                    );

                    scheduleFastLeafDecay(
                            world,
                            leafX,
                            leafY,
                            leafZ,
                            block
                    );
                }
            }
        }
    }

    private static boolean isTargetLeaf(
            Block block,
            boolean scanMagicalLeaves,
            boolean scanWarpwoodLeaves) {

        if (scanMagicalLeaves
                && block instanceof BlockMagicalLeaves) {
            return true;
        }

        return scanWarpwoodLeaves
                && hasClassName(block, WARPWOOD_LEAVES_CLASS);
    }

    private void scheduleFastLeafDecay(
            World world,
            int x,
            int y,
            int z,
            Block block) {

        if (invocationFailureLogged) {
            return;
        }

        try {
            handleLeafDecayMethod.invoke(
                    null,
                    world,
                    x,
                    y,
                    z,
                    block
            );
        } catch (IllegalAccessException
                 | InvocationTargetException exception) {

            invocationFailureLogged = true;

            logger.error(
                    "Fast Leaf Decay compatibility could not schedule "
                            + "a leaf update. Further errors will be suppressed.",
                    exception
            );
        }
    }

    private static Method findFastLeafDecayHandler() {
        try {
            Class<?> handlerClass =
                    Class.forName(FAST_LEAF_DECAY_HANDLER);

            return handlerClass.getMethod(
                    "handleLeafDecay",
                    World.class,
                    int.class,
                    int.class,
                    int.class,
                    Block.class
            );
        } catch (ClassNotFoundException
                 | NoSuchMethodException exception) {

            logger.error(
                    "Could not locate Fast Leaf Decay's scheduling handler. "
                            + "The compatibility patch will remain inactive.",
                    exception
            );

            return null;
        }
    }

    private static boolean hasClassName(
            Block block,
            String expectedClassName) {

        return block != null
                && expectedClassName.equals(
                block.getClass().getName()
        );
    }
}