package thaumcraft4patched;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import thaumcraft4patched.config.Config;
import thaumcraft4patched.model.config.ConfigBugPatches;
import thaumcraft4patched.model.config.ConfigIntegrations;

import java.io.File;

import static thaumcraft4patched.Thaumcraft4Patched.dependencies;
import static thaumcraft4patched.Thaumcraft4Patched.modID;

@SuppressWarnings("unused")
@Mod(modid = modID, useMetadata = true, dependencies = dependencies)
public class Thaumcraft4Patched {

    public static final String modID = "TC4Patched", modName = "Thaumcraft4Patched";

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        Config.init(new File(event.getModConfigurationDirectory(), modName)); // Custom folder with mod name
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent ignoredEvent) {}

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent ignoredEvent) {
        // Loading Integrations
        ConfigIntegrations.init();
        // Loading main patches
        if (Config.tc4Enabled) ConfigBugPatches.init();
    }

    public static final String dependencies = "required-after:Thaumcraft@[4.2.3.5,);after:thaumicbases";
}
