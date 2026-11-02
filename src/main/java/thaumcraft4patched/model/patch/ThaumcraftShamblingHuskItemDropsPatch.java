package thaumcraft4patched.model.patch;

import net.minecraft.entity.EntityLiving;
import net.minecraft.world.World;
import thaumcraft4patched.config.Config;

public final class ThaumcraftShamblingHuskItemDropsPatch implements IPatch {

    private ThaumcraftShamblingHuskItemDropsPatch() {}

    public static boolean shouldRestoreDrops(EntityLiving husk) {
        if (!Config.tc4Enabled || !Config.shamblingHuskItemDropsPatchEnabled) {
            return false;
        }
        World world = husk.worldObj;
        return world != null && !world.isRemote
                && world.getGameRules().getGameRuleBooleanValue("doMobLoot");
    }
}
