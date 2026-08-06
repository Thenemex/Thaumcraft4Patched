package thaumcraft4patched.model.patch;

import cpw.mods.fml.common.Loader;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockGrass;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ForgeHooks;
import thaumcraft.common.items.wands.ItemWandCasting;
import thaumcraft.common.items.wands.foci.ItemFocusExcavation;

import static thaumcraft4patched.config.Config.excavationFocusHlcCompatibilityPatchEnabled;

/**
 * Provides Ender Zoo compatibility while the Excavation Focus is actively
 * using its magic.
 *
 * This does not make the wand itself an effective tool. Physically hitting
 * a block with the wand remains equivalent to mining it by hand.
 */
public final class HlcExcavationFocusToolPatch {

    private HlcExcavationFocusToolPatch() {
    }

    public static boolean isToolEffective(
            ItemStack heldItem,
            Block block,
            int metadata) {

        /*
         * Always preserve Forge's normal result first.
         */
        if (heldItem != null
                && ForgeHooks.isToolEffective(
                heldItem,
                block,
                metadata
        )) {
            return true;
        }

        /*
         * Keep this behaviour tied to the existing HLC Excavation Focus
         * compatibility option.
         */
        if (!excavationFocusHlcCompatibilityPatchEnabled
                || !Loader.isModLoaded("harvestlevelconfig")) {
            return false;
        }

        /*
         * Only genuine Excavation Focus magic receives compatibility.
         * Merely punching a block with the wand does not activate this.
         */
        if (!ExcavationFocusHarvestContext.isActive()) {
            return false;
        }

        /*
         * Ender Zoo only performs its Dire Slime check for dirt and grass.
         */
        if (!(block instanceof BlockDirt)
                && !(block instanceof BlockGrass)) {
            return false;
        }

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
}