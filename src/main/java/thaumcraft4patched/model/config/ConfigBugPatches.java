package thaumcraft4patched.model.config;

import thaumcraft.api.research.ResearchItem;
import thaumcraft4patched.api.thaumcraft.API;
import thaumcraft4patched.api.util.Logger;

import java.lang.reflect.Field;

import static thaumcraft4patched.config.Config.boneBowResearchPatchEnabled;

public class ConfigBugPatches {

    public static void init() {
        if (boneBowResearchPatchEnabled) patchHiddenBoneBowResearch();
    }

    protected static void patchHiddenBoneBowResearch() {
        ResearchItem research = API.getResearch("ARTIFICE", "BONEBOW");
        try {
            Field field = research.getClass().getDeclaredField("isHidden");
            field.setAccessible(true);
            field.set(research, false);
            field.setAccessible(false);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            Logger.logError("Cannot patch the Bone Bow bug / " + e.getClass().getSimpleName() + " : " + e.getLocalizedMessage());
        }
    }
}
