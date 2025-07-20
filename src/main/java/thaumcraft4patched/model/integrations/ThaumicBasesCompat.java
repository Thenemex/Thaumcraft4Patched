package thaumcraft4patched.model.integrations;

import nemexlib.api.integrations.ACompat;
import nemexlib.api.thaumcraft.API;
import thaumcraft4patched.config.Config;

public class ThaumicBasesCompat extends ACompat {

    public ThaumicBasesCompat(String mod) {
        super(mod);
    }

    @Override
    public void loadIntegration() {
        if (Config.missingPrereqs_ThaumiumBracelet) addMissingPrereqForThaumiumBracelet();
        if (Config.missingPrereqs_VoidBracelet) addMissingPrereqForVoidBracelet();
        if (Config.missingPrereqs_VoidWandCore) addMissingPrereqsForVoidWandCore();
    }

    public void addMissingPrereqForThaumiumBracelet() {
        // Thaumium
        API.addParents("THAUMICBASES", "TB.Bracelet.Thaumium", true, "THAUMIUM");
    }
    public void addMissingPrereqForVoidBracelet() {
        // Charged Thaumium Cap, Voidmetal, Salis Mundus Block, Void Wand Core
        API.addParents("THAUMICBASES", "TB.Bracelet.Void", true, "CAP_thaumium", "VOIDMETAL", "TB.SMB", "ROD_tbvoid", "TB.CrystalBlocks");
    }
    public void addMissingPrereqsForVoidWandCore() {
        // Salis Mundus Block
        API.addParents("THAUMICBASES", "ROD_tbvoid", true, "TB.SMB");
    }
}
