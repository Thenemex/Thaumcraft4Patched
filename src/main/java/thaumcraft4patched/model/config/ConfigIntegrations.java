package thaumcraft4patched.model.config;

import cpw.mods.fml.common.Loader;
import thaumcraft4patched.config.Config;
import thaumcraft4patched.model.integrations.ThaumicBasesCompat;
import thaumcraft4patched.model.integrations.WitchingGadgetsCompat;

public class ConfigIntegrations {

    public static final String tb = "thaumicbases", wg = "WitchingGadgets";

    public static void init() {
        if (modIsLoaded(tb, Config.tbEnabled))
            new ThaumicBasesCompat(tb);
        if (modIsLoaded(wg,Config.wgEnabled))
            new WitchingGadgetsCompat(wg);
    }

    public static boolean modIsLoaded(String mod, boolean config) {
        return Loader.isModLoaded(mod) && config;
    }
}
