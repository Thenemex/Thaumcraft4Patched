package thaumcraft4patched.model.config;

import nemexlib.api.thaumcraft.API;
import net.minecraftforge.common.MinecraftForge;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.crafting.InfusionRecipe;
import thaumcraft.api.research.ResearchItem;
import thaumcraft4patched.model.patch.FakePlayerPatch;
import thaumcraft4patched.model.patch.IPatch;

import static thaumcraft.api.aspects.Aspect.WEAPON;
import static thaumcraft4patched.Thaumcraft4Patched.logger;
import static thaumcraft4patched.config.Config.*;

public class ConfigBugPatches {

    public static IPatch golemLumberCoreWoodHardness = null;

    public static void initTC4() {
        if (boneBowResearchPatchEnabled) patchHiddenBoneBowResearch();
        if (golemLumberCoreWoodHardnessPatchEnabled) patchGolemLumberCoreWoodHardness();
    }

    public static void initTX() {
        if (removeNecroInfusionRecipe) removeNecroInfusionRecipe();
    }

    protected static void patchHiddenBoneBowResearch() {
        ResearchItem research = API.getResearch("ARTIFICE", "BONEBOW");
        research.setItemTriggers(); // Cleared all item triggers
        research.setAspectTriggers(WEAPON);
    }

    protected static void patchGolemLumberCoreWoodHardness() {
        golemLumberCoreWoodHardness = new FakePlayerPatch();
        MinecraftForge.EVENT_BUS.register(golemLumberCoreWoodHardness);
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
            logger.info("Successfully removed infusion recipe linked to \"NECROINFUSION\" tag !");
        } else
            logger.info("Could not remove of find infusion recipe linked to \"NECROINFUSION\" tag ...");
    }
}
