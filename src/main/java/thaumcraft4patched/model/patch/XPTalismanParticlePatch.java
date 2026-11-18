package thaumcraft4patched.model.patch;

import net.minecraft.world.World;
import thaumcraft.common.CommonProxy;
import thaumcraft4patched.config.Config;

public final class XPTalismanParticlePatch {

    private XPTalismanParticlePatch() {}

    public static void showWisp(CommonProxy proxy, World world, double x, double y, double z,
            float size, float red, float green, float blue) {
        if (Config.ttEnabled && Config.hideXPTalismanWisp) {
            return;
        }
        proxy.wispFX(world, x, y, z, size, red, green, blue);
    }
}
