package thaumcraft4patched.model.patch;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.world.BlockEvent;
import thaumcraft4patched.config.Config;

public final class ForbiddenMagicImpactClaimProtectionPatch implements IPatch {

    private ForbiddenMagicImpactClaimProtectionPatch() {}

    public static boolean isEffectiveAndPermitted(
            ItemStack tool,
            Block block,
            int metadata,
            World world,
            EntityLivingBase miner,
            int x,
            int y,
            int z) {

        if (!ForgeHooks.isToolEffective(tool, block, metadata)) {
            return false;
        }

        if (!Config.fmEnabled || !Config.forbiddenMagicImpactClaimProtectionPatchEnabled
                || world.isRemote || !(miner instanceof EntityPlayerMP)) {
            return true;
        }

        EntityPlayerMP player = (EntityPlayerMP) miner;
        BlockEvent.BreakEvent event = ForgeHooks.onBlockBreakEvent(
                world, player.theItemInWorldManager.getGameType(), player, x, y, z);
        return !event.isCanceled();
    }
}
