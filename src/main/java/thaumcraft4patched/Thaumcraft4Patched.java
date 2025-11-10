package thaumcraft4patched;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import nemexlib.api.util.Logger;
import nemexlib.config.AConfig;
import thaumcraft4patched.config.Config;
import thaumcraft4patched.model.config.ConfigBugPatches;
import thaumcraft4patched.model.config.ConfigIntegrations;

import static thaumcraft4patched.Thaumcraft4Patched.dependencies;
import static thaumcraft4patched.Thaumcraft4Patched.modID;

@SuppressWarnings({"unused", "EmptyMethod"})
@Mod(modid = modID, useMetadata = true, version = "1.4", dependencies = dependencies)
public class Thaumcraft4Patched {

    public static final String modID = "TC4Patched", modName = "Thaumcraft4Patched", configVersion = "1.2";
    public static AConfig config;
    public static final Logger logger = new Logger(modID);

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        config = new Config(event, configVersion).init(); // Init config // Custom folder with mod name
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent ignoredEvent) {}

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent ignoredEvent) {
        // Loading Integrations
        ConfigIntegrations.init();
        // Loading main patches
        if (Config.tc4Enabled) ConfigBugPatches.initTC4();
        if (Config.txEnabled) ConfigBugPatches.initTX();
    }

    public static final String dependencies = "required-after:Thaumcraft@[4.2.3.5,);required-after:NemexLib@[1.0.0.2,);after:thaumicbases;after:WitchingGadgets;after:ThaumicExploration";
}
