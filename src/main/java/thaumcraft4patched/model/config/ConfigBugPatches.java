package thaumcraft4patched.model.config;

import nemexlib.api.items.ItemFinder;
import nemexlib.api.recipes.arcane.ArcaneAdder;
import nemexlib.api.thaumcraft.API;
import nemexlib.api.thaumcraft.aspects.Aspects;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.crafting.InfusionRecipe;
import thaumcraft.api.crafting.ShapelessArcaneRecipe;
import thaumcraft.api.research.ResearchCategoryList;
import thaumcraft.api.research.ResearchItem;
import thaumcraft.common.config.ConfigItems;
import thaumcraft4patched.model.patch.NullParentsPatch;
import thaumcraft4patched.model.patch.FakePlayerPatch;
import thaumcraft4patched.model.patch.IPatch;

import static thaumcraft.api.aspects.Aspect.AIR;
import static thaumcraft.api.aspects.Aspect.WEAPON;
import static thaumcraft.api.research.ResearchCategories.researchCategories;
import static thaumcraft4patched.Thaumcraft4Patched.logger;
import static thaumcraft4patched.config.Config.*;

public class ConfigBugPatches {

    public static IPatch golemLumberCoreWoodHardness, nullResearchParentsPatch;

    public static void initTC4() {
        if (boneBowResearchPatchEnabled) patchHiddenBoneBowResearch();
        if (golemLumberCoreWoodHardnessPatchEnabled) patchGolemLumberCoreWoodHardness();
        if (nullResearchParentsPatchEnabled) patchNullResearchParents();
    }

    public static void initTX() {
        if (removeNecroInfusionRecipe) removeNecroInfusionRecipe();
        if (blackFloatingCandleRecipePatchEnabled) patchBlackFloatingCandleRecipe();
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

    protected static void patchNullResearchParents() {
        int cpt = 0;
        for (ResearchCategoryList rl : researchCategories.values())
            for (ResearchItem ri : rl.research.values()) {
                if (ri.parents == null) {
                    ri.parents = new String[0];
                    cpt++;
                }
                if (ri.parentsHidden == null) {
                    ri.parentsHidden = new String[0];
                    cpt++;
                }
            }
        logger.info("Patched", cpt, "parents that were Null (loading phase 3)");

        nullResearchParentsPatch = new NullParentsPatch();
        MinecraftForge.EVENT_BUS.register(nullResearchParentsPatch);
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
            logger.info("Could not remove or find infusion recipe linked to \"NECROINFUSION\" tag ...");
    }

    protected static void patchBlackFloatingCandleRecipe() {
        ShapelessArcaneRecipe recipeToRemove = null;
        String researchTag = "FLOATCANDLE";
        for (Object o : ThaumcraftApi.getCraftingRecipes())
            if (o instanceof ShapelessArcaneRecipe) {
                ShapelessArcaneRecipe recipe = (ShapelessArcaneRecipe) o;
                if (recipe.research.equals(researchTag) && recipe.getRecipeOutput().getItemDamage() == 32767)
                    recipeToRemove = recipe;
            }
        if (recipeToRemove == null)
            logger.info("Could not patch the recipe for Black Floating Candle / Report to Author");
        else {
            ItemStack blackFloatingCandleTX = ItemFinder.findItem("ThaumicExploration", "floatCandle", 15),
                      blackCandle = ItemFinder.findItemTC("blockCandle", 15);
            blackFloatingCandleTX.stackSize = 3;
            ThaumcraftApi.getCraftingRecipes().remove(recipeToRemove);
            ArcaneAdder.addArcane(
                    researchTag,
                    new Aspects(AIR, 5),
                    true,
                    false,
                    blackFloatingCandleTX,
                    blackCandle, blackCandle, blackCandle, ConfigItems.itemShard);
            logger.info("Successfully patched the recipe for Black Floating Candle [TX] !");
        }
    }
}
