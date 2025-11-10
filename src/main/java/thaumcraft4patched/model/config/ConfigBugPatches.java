package thaumcraft4patched.model.config;

import nemexlib.api.thaumcraft.API;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.crafting.InfusionRecipe;
import thaumcraft.api.research.ResearchItem;
import thaumcraft4patched.Thaumcraft4Patched;

import static thaumcraft.api.aspects.Aspect.WEAPON;
import static thaumcraft4patched.config.Config.boneBowResearchPatchEnabled;
import static thaumcraft4patched.config.Config.removeNecroInfusionRecipe;

@SuppressWarnings("CommentedOutCode")
public class ConfigBugPatches {

    public static void initTC4() {
        if (boneBowResearchPatchEnabled) patchHiddenBoneBowResearch();
    }

    public static void initTX() {
        if (removeNecroInfusionRecipe) removeNecroInfusionRecipe();
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

    protected static void removeNecroInfusionRecipe() {
        InfusionRecipe recipe, toRemove = null;
        for (Object o : ThaumcraftApi.getCraftingRecipes())
            if (o instanceof InfusionRecipe) {
                recipe = (InfusionRecipe) o;
                if (recipe.getResearch().equals("NECROINFUSION"))
                    toRemove = recipe;
            }
        if (toRemove != null) {
            ThaumcraftApi.getCraftingRecipes().remove(toRemove);
            Thaumcraft4Patched.logger.info("Successfully removed infusion recipe linked to \"NECROINFUSION\" tag !");
        } else
            Thaumcraft4Patched.logger.info("Could not remove of find infusion recipe linked to \"NECROINFUSION\" tag ...");
    }
}
