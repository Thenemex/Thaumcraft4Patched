package thaumcraft4patched.model.config;

import thaumcraft4patched.config.Config;
import thaumcraft4patched.model.integrations.ThaumicBasesCompat;

import static thaumcraft4patched.api.integrations.ACompat.modIsLoaded;

public class ConfigIntegrations {

    public static String tb = "thaumicbases";

    public static void init() {
        if (modIsLoaded(tb, Config.tbEnabled))
            new ThaumicBasesCompat(tb);
    }
}
