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
    }

    protected void addMissingPrereqForThaumiumBracelet() {
        ResearchItem research = API.getResearch("THAUMICBASES", "TB.Bracelet.Thaumium");
        String[] parentsHidden = deepCopyTabAndAdd(research.parentsHidden, "THAUMIUM");
        research.setParentsHidden(parentsHidden);
    }

    private String[] deepCopyTabAndAdd(String[] tab, String ... newElements) {
        if (tab == null || newElements == null || newElements.length == 0) throw new ParameterIsNullOrEmpty();
        String[] deepCopy = new String[tab.length + newElements.length];
        System.arraycopy(tab, 0, deepCopy, 0, tab.length);
        System.arraycopy(newElements, 0, deepCopy, deepCopy.length - newElements.length - 1, newElements.length);
        return deepCopy;
    }
}
