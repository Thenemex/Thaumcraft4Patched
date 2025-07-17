package thaumcraft4patched.model.integrations;

import thaumcraft.api.research.ResearchItem;
import thaumcraft4patched.api.integrations.ACompat;
import thaumcraft4patched.api.thaumcraft.API;
import thaumcraft4patched.api.util.exceptions.ParameterIsNullOrEmpty;
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

    protected void addMissingPrereqForThaumiumBracelet() {
        String newParentsHidden = "THAUMIUM"; // Thaumium
        ResearchItem research = API.getResearch("THAUMICBASES", "TB.Bracelet.Thaumium"); // Thaumium Bracelet
        if (research.parentsHidden == null)
            research.setParentsHidden(newParentsHidden);
        else
            research.setParentsHidden(deepCopyTabAndAdd(research.parentsHidden, newParentsHidden));
    }
    protected void addMissingPrereqForVoidBracelet() {
        // Charged Thaumium Cap, Voidmetal, Salis Mundus Block, Void Wand Core
        String[] newParentsHidden = new String[]{"CAP_thaumium", "VOIDMETAL", "TB.SMB", "ROD_tbvoid", "TB.CrystalBlocks"};
        ResearchItem research = API.getResearch("THAUMICBASES", "TB.Bracelet.Void"); // Void Bracelet
        if (research.parentsHidden == null)
            research.setParentsHidden(newParentsHidden);
        else
            research.setParentsHidden(deepCopyTabAndAdd(research.parentsHidden, newParentsHidden));
    }
    protected void addMissingPrereqsForVoidWandCore() {
        String newParentsHidden = "TB.SMB"; // Salis Mundus Block
        ResearchItem research = API.getResearch("THAUMICBASES", "ROD_tbvoid"); // Void Bracelet
        if (research.parentsHidden == null)
            research.setParentsHidden(newParentsHidden);
        else
            research.setParentsHidden(deepCopyTabAndAdd(research.parentsHidden, newParentsHidden));
    }

    private String[] deepCopyTabAndAdd(String[] tab, String ... newElements) {
        if (tab == null || newElements == null || newElements.length == 0) throw new ParameterIsNullOrEmpty();
        String[] deepCopy = new String[tab.length + newElements.length];
        System.arraycopy(tab, 0, deepCopy, 0, tab.length);
        System.arraycopy(newElements, 0, deepCopy, deepCopy.length - newElements.length - 1, newElements.length);
        return deepCopy;
    }
}
