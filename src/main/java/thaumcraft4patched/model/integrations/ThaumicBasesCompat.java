package thaumcraft4patched.model.integrations;

import thaumcraft4patched.api.integrations.ACompat;
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
        addHiddenPrereq("THAUMICBASES", "TB.Bracelet.Thaumium", "THAUMIUM");
    }
    public void addMissingPrereqForVoidBracelet() {
        // Charged Thaumium Cap, Voidmetal, Salis Mundus Block, Void Wand Core
        addHiddenPrereq("THAUMICBASES", "TB.Bracelet.Void", "CAP_thaumium", "VOIDMETAL", "TB.SMB", "ROD_tbvoid", "TB.CrystalBlocks");
    }
    public void addMissingPrereqsForVoidWandCore() {
        // Salis Mundus Block
        addHiddenPrereq("THAUMICBASES", "ROD_tbvoid", "TB.SMB");
    }
}
