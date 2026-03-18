package thaumcraft4patched.model.config;

import thaumcraft4patched.config.Config;
import thaumcraft4patched.model.integrations.ThaumicBasesCompat;
import thaumcraft4patched.model.integrations.WitchingGadgetsCompat;

import static nemexlib.api.integrations.ACompat.isModLoaded;

public class ConfigIntegrations {

    public static final String tb = "thaumicbases", wg = "WitchingGadgets";

    public static void init() {
        if (isModLoaded(tb, Config.tbEnabled))
            new ThaumicBasesCompat(tb);
        if (isModLoaded(wg, Config.wgEnabled))
            new WitchingGadgetsCompat(wg);
    }
}
