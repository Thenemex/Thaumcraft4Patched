package thaumcraft4patched.model.patch;

import com.awesomehippo.harvestlevelconfig.util.ConfigLoader;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.world.BlockEvent;
import thaumcraft.common.items.wands.ItemWandCasting;
import thaumcraft.common.items.wands.foci.ItemFocusExcavation;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;

public class HlcExcavationFocusPatch implements IPatch {

    /*
     * Matches diamond and Thaumium tool capability in Thaumcraft 4.
     *
     * A future focus upgrade may raise this capability, but that behavior
     * is deliberately not included in the current compatibility patch.
     */
    private static final int DIAMOND_HARVEST_LEVEL = 3;

    private final ThreadLocal<Deque<CapturedDrops>> capturedDrops = ThreadLocal.withInitial(ArrayDeque::new);

    /*
     * Runs before HLC so the drops calculated by Thaumcraft can be
     * preserved in case HLC rejects the held wand as a mining tool.
     *
     * This only runs while the Excavation Focus's actual magic is
     * harvesting a block. Merely hitting a block with the wand does not
     * activate the harvest context.
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void captureDrops(BlockEvent.HarvestDropsEvent event) {
        if (event.world.isRemote || event.harvester == null) {
            return;
        }

        if (ExcavationFocusHarvestContext.isNotActive()) {
            return;
        }

        /*
         * Defensive verification that the player is still holding a wand
         * with the Excavation Focus equipped.
         */
        if (!isUsingExcavationFocus(event.harvester)) {
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

        ItemStack heldItem = event.harvester.getHeldItem();

        /*
         * Preserve normal behavior when the held item already satisfies
         * HLC without compatibility handling.
         */
        if (canHeldItemHarvest(heldItem, requiredTools)) {
            return;
        }

        /*
         * Excavation Focus magic currently emulates diamond/Thaumium-level
         * pickaxe, axe and shovel capability.
         */
        if (!canVirtualDiamondToolsHarvest(requiredTools)) {
            return;
        }

        if (event.drops.isEmpty()) {
            return;
        }

        List<ItemStack> copiedDrops = new ArrayList<>(event.drops.size());

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
     * Runs after HLC. Drops are restored only when HLC emptied the list
     * during a matching Excavation Focus magic harvest.
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

    private static boolean isUsingExcavationFocus(EntityPlayer player) {
        ItemStack heldItem = player.getHeldItem();

        if (heldItem == null
                || !(heldItem.getItem() instanceof ItemWandCasting)) {
            return false;
        }

        ItemWandCasting wand =
                (ItemWandCasting) heldItem.getItem();

        ItemStack focus = wand.getFocusItem(heldItem);

        return focus != null
                && focus.getItem() instanceof ItemFocusExcavation;
    }

    private static boolean canHeldItemHarvest(
            ItemStack heldItem,
            Map<String, Integer> requiredTools) {

        if (heldItem == null) {
            return requiredTools.containsValue(0);
        }

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

    private static boolean canVirtualDiamondToolsHarvest(
            Map<String, Integer> requiredTools) {

        for (Map.Entry<String, Integer> requirement
                : requiredTools.entrySet()) {

            String tool = requirement.getKey();
            int requiredLevel = requirement.getValue();

            if (isSupportedTool(tool)
                    && requiredLevel <= DIAMOND_HARVEST_LEVEL) {
                return true;
            }
        }

        return false;
    }

    private static boolean isSupportedTool(String tool) {
        return "pickaxe".equals(tool)
                || "axe".equals(tool)
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