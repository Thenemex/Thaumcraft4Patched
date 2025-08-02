package thaumcraft4patched.model.integrations;

import nemexlib.api.integrations.ACompat;
import nemexlib.api.thaumcraft.API;
import thaumcraft4patched.config.Config;

public class WitchingGadgetsCompat extends ACompat {

    public WitchingGadgetsCompat(String mod) {
        super(mod);
    }

    @Override
    public void loadIntegration() {
        if (Config.missingPrereqs_WitchingWearables) addMissingPrereqForWitchingWearables();
    }

    public void addMissingPrereqForWitchingWearables() {
        // Thaumium, Infusion, Primal Arrows
        API.addParents("WITCHGADG", "WGBAUBLES", false, (String) null);
        API.addParents("WITCHGADG", "WGBAUBLES", true, "THAUMIUM", "INFUSION", "PRIMALARROW");
    }
}
