package thaumcraft4patched.api.thaumcraft;

import thaumcraft.api.research.ResearchItem;
import thaumcraft4patched.api.util.exceptions.ParameterIsNullOrEmpty;

import static thaumcraft.api.research.ResearchCategories.researchCategories;

public class API {

    /**
     * Get the research from the Thaumcraft 4 registry
     * @param tab Thaumonomicon Tab
     * @param tag Research's Tag
     * @return The research
     */
    public static ResearchItem getResearch(String tab, String tag) {
        if (tab == null || tag == null) throw new ParameterIsNullOrEmpty();
        return researchCategories.get(tab).research.get(tag);
    }
}
