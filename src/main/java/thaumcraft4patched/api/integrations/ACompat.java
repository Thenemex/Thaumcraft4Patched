package thaumcraft4patched.api.integrations;

import cpw.mods.fml.common.Loader;
import thaumcraft.api.research.ResearchItem;
import thaumcraft4patched.api.thaumcraft.API;
import thaumcraft4patched.api.util.Logger;
import thaumcraft4patched.api.util.exceptions.ParameterIsNullOrEmpty;

import java.util.Objects;

public abstract class ACompat {

    protected final String mod;

    public ACompat(String mod) {
        if (mod == null) throw new ParameterIsNullOrEmpty();
        this.mod = mod;
        init();
        Logger.logInfo("Successfully loaded integration for mod : ".concat(mod));
    }

    protected void init() {
        loadIntegration();
    }

    public abstract void loadIntegration();

    protected void addHiddenPrereq(String tab, String tag, String ... hiddenPrereqs) {
        if (hiddenPrereqs == null || hiddenPrereqs.length == 0) throw new ParameterIsNullOrEmpty();
        ResearchItem research = API.getResearch(tab, tag);
        if (research.parentsHidden == null)
            research.setParentsHidden(hiddenPrereqs);
        else
            research.setParentsHidden(deepCopyTabAndAdd(research.parentsHidden, hiddenPrereqs));
    }
    private String[] deepCopyTabAndAdd(String[] tab, String ... newElements) {
        if (tab == null) throw new ParameterIsNullOrEmpty();
        String[] deepCopy = new String[tab.length + newElements.length];
        System.arraycopy(tab, 0, deepCopy, 0, tab.length);
        System.arraycopy(newElements, 0, deepCopy, deepCopy.length - newElements.length - 1, newElements.length);
        return deepCopy;
    }

    @Override
    public String toString() {
        return "Compat:".concat(mod);
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ACompat)) return false;
        ACompat aCompat = (ACompat) o;
        return Objects.equals(mod, aCompat.mod);
    }
    @Override
    public int hashCode() {
        return Objects.hashCode(mod);
    }

    public static boolean modIsLoaded(String mod, boolean config) {
        return Loader.isModLoaded(mod) && config;
    }
}
