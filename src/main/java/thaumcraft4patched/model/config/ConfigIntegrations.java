package thaumcraft4patched.model.config;

import cpw.mods.fml.common.Loader;
import thaumcraft4patched.config.Config;
import thaumcraft4patched.model.integrations.ThaumicBasesCompat;

public class ConfigIntegrations {

    public static final String tb = "thaumicbases";

    public static void init() {
        if (modIsLoaded(tb, Config.tbEnabled))
            new ThaumicBasesCompat(tb);
    }

    public static boolean modIsLoaded(String mod, boolean config) {
        return Loader.isModLoaded(mod) && config;
    }
}
