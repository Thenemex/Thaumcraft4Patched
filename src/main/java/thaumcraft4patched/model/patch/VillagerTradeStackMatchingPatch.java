package thaumcraft4patched.model.patch;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import thaumcraft4patched.config.Config;

public final class VillagerTradeStackMatchingPatch implements IPatch {

    private VillagerTradeStackMatchingPatch() {}

    public static boolean matches(
            ItemStack offered,
            ItemStack required) {

        if (offered.getItem() != required.getItem()) {
            return false;
        }

        if (!Config.mcEnabled
                || !Config.villagerTradeStackMatchingPatchEnabled) {

            return true;
        }

        if (required.getItem().getHasSubtypes()) {
            int requiredMetadata = required.getItemDamage();

            if (requiredMetadata != OreDictionary.WILDCARD_VALUE
                    && offered.getItemDamage() != requiredMetadata) {

                return false;
            }
        }

        if (required.hasTagCompound()) {
            return offered.hasTagCompound()
                    && required.getTagCompound()
                    .equals(offered.getTagCompound());
        }

        return true;
    }
}