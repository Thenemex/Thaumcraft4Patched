package thaumcraft4patched.model.patch;

import com.awesomehippo.harvestlevelconfig.util.ConfigLoader;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.world.BlockEvent;
import thaumcraft.common.items.equipment.ItemPrimalCrusher;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;

public class HlcPrimalCrusherPatch implements IPatch {

    private static final int PRIMAL_CRUSHER_HARVEST_LEVEL = 5;

    private final ThreadLocal<Deque<CapturedDrops>> capturedDrops =
            new ThreadLocal<Deque<CapturedDrops>>() {
                @Override
                protected Deque<CapturedDrops> initialValue() {
                    return new ArrayDeque<CapturedDrops>();
                }
            };

    /*
     * Captures the drops before HLC checks the held Primal Crusher.
     *
     * The Primal Crusher declares itself as a level-5 pickaxe and shovel,
     * but HLC does not recognise that correctly through getHarvestLevel().
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void captureDrops(BlockEvent.HarvestDropsEvent event) {
        if (event.world.isRemote || event.harvester == null) {
            return;
        }

        ItemStack heldItem = event.harvester.getHeldItem();

        if (heldItem == null
                || !(heldItem.getItem() instanceof ItemPrimalCrusher)) {
            return;
        }

        Map<String, Integer> requiredTools =
                ConfigLoader.getRequiredToolsForBlock(
                        event.block,
                        event.blockMetadata
                );

        if (requiredTools == null || requiredTools.isEmpty()) {
            return;
        }

        /*
         * Do nothing when HLC already recognises the held item.
         * This prevents duplicate drops.
         */
        if (canHeldItemHarvest(heldItem, requiredTools)) {
            return;
        }

        if (!canPrimalCrusherHarvest(requiredTools)) {
            return;
        }

        if (event.drops.isEmpty()) {
            return;
        }

        List<ItemStack> copiedDrops =
                new ArrayList<ItemStack>(event.drops.size());

        for (ItemStack drop : event.drops) {
            if (drop != null) {
                copiedDrops.add(drop.copy());
            }
        }

        if (!copiedDrops.isEmpty()) {
            capturedDrops.get().push(
                    new CapturedDrops(event, copiedDrops)
            );
        }
    }

    /*
     * Runs after HLC and restores the drops only when HLC emptied them.
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void restoreDrops(BlockEvent.HarvestDropsEvent event) {
        Deque<CapturedDrops> captures = capturedDrops.get();

        if (captures.isEmpty()) {
            return;
        }

        CapturedDrops capture = captures.peek();

        if (capture.event != event) {
            return;
        }

        captures.pop();

        if (event.drops.isEmpty()) {
            event.drops.addAll(capture.drops);
        }

        if (captures.isEmpty()) {
            capturedDrops.remove();
        }
    }

    /*
     * Mirrors HLC's normal held-tool check.
     */
    private static boolean canHeldItemHarvest(
            ItemStack heldItem,
            Map<String, Integer> requiredTools) {

        for (Map.Entry<String, Integer> requirement
                : requiredTools.entrySet()) {

            int requiredLevel = requirement.getValue();

            if (requiredLevel == 0) {
                return true;
            }

            int heldLevel = heldItem.getItem().getHarvestLevel(
                    heldItem,
                    requirement.getKey()
            );

            if (heldLevel >= requiredLevel) {
                return true;
            }
        }

        return false;
    }

    /*
     * The Primal Crusher is legitimately a level-5 pickaxe and shovel.
     * It is not given axe compatibility.
     */
    private static boolean canPrimalCrusherHarvest(
            Map<String, Integer> requiredTools) {

        for (Map.Entry<String, Integer> requirement
                : requiredTools.entrySet()) {

            String tool = requirement.getKey();
            int requiredLevel = requirement.getValue();

            if (isSupportedTool(tool)
                    && requiredLevel <= PRIMAL_CRUSHER_HARVEST_LEVEL) {
                return true;
            }
        }

        return false;
    }

    private static boolean isSupportedTool(String tool) {
        return "pickaxe".equals(tool)
                || "shovel".equals(tool);
    }

    private static class CapturedDrops {

        private final BlockEvent.HarvestDropsEvent event;
        private final List<ItemStack> drops;

        private CapturedDrops(
                BlockEvent.HarvestDropsEvent event,
                List<ItemStack> drops) {

            this.event = event;
            this.drops = drops;
        }
    }
}