package thaumcraft4patched.model.config;

import nemexlib.api.thaumcraft.API;
import thaumcraft.api.research.ResearchItem;

import static thaumcraft.api.aspects.Aspect.WEAPON;
import static thaumcraft4patched.config.Config.boneBowResearchPatchEnabled;

@SuppressWarnings("CommentedOutCode")
public class ConfigBugPatches {

    public static void init() {
        if (boneBowResearchPatchEnabled) patchHiddenBoneBowResearch();
    }

    protected static void patchHiddenBoneBowResearch() {
        ResearchItem research = API.getResearch("ARTIFICE", "BONEBOW");
        research.setItemTriggers(); // Cleared all item triggers
        research.setAspectTriggers(WEAPON);
        /*
        try {
            Field field = research.getClass().getDeclaredField("isHidden");
            field.setAccessible(true);
            field.set(research, false);
            field.setAccessible(false);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            logger.error("Cannot patch the Bone Bow bug / " + e.getClass().getSimpleName() + " : " + e.getLocalizedMessage());
        }
        */
    }
}
