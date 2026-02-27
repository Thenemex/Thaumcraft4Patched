package thaumcraft4patched.model.integrations;

import nemexlib.api.integrations.ACompat;
import net.minecraftforge.common.MinecraftForge;
import thaumcraft4patched.config.Config;
import thaumcraft4patched.model.patch.IPatch;
import thaumcraft4patched.model.patch.WitchingGadgetsResearchPatch;

public class WitchingGadgetsCompat extends ACompat {

    public static IPatch wgResearchPatch = null;

    public WitchingGadgetsCompat(String mod) {
        super(mod);
    }

    @Override
    public void loadIntegration() {
        if (Config.missingPrereqs_WitchingWearables) addMissingPrereqForWitchingWearables();
    }

    public void addMissingPrereqForWitchingWearables() {
        wgResearchPatch = new WitchingGadgetsResearchPatch();
        MinecraftForge.EVENT_BUS.register(wgResearchPatch);
    }
}
