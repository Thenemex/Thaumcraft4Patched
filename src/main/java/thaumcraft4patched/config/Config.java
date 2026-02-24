package thaumcraft4patched.config;

import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import nemexlib.config.AConfig;

import static thaumcraft4patched.Thaumcraft4Patched.modName;

public class Config extends AConfig {

    public static boolean tc4Enabled, boneBowResearchPatchEnabled, golemLumberCoreWoodHardnessPatchEnabled;
    public static boolean tbEnabled, missingPrereqs_ThaumiumBracelet, missingPrereqs_VoidBracelet, missingPrereqs_VoidWandCore;
    public static boolean wgEnabled, missingPrereqs_WitchingWearables;
    public static boolean txEnabled, blackFloatingCandleRecipePatchEnabled, removeNecroInfusionRecipe;

    public Config(FMLPreInitializationEvent event) {
        super(event, modName, modName, "1.4");
    }

    protected void loadConfig() {
        String mods = "Mods", tc4 = "Thaumcraft-4", tb = "Thaumic-Bases", tx = "Thaumic-Exploration", wg = "Witching-Gadgets";
        config.addCustomCategoryComment(mods, "You can turn off bug-patches for whole mods here");
        tc4Enabled = newEntry(mods, "Thaumcraft 4");
        tbEnabled = newEntry(mods, "Thaumic Bases");
        txEnabled = newEntry(mods, "Thaumic Exploration");
        wgEnabled = newEntry(mods, "Witching Gadgets");
        config.addCustomCategoryComment(tc4, "You can disable/enable bug patches for Thaumcraft 4 here");
        boneBowResearchPatchEnabled = newEntry(tc4,"HiddenBoneBowResearch", "Removes the hidden property of the research -> it will be unlocked when the player discover the Telum aspect");
        golemLumberCoreWoodHardnessPatchEnabled = newEntry(tc4, "GolemLumberBlockHardness", "This patches the issue when Golem with Lumber core cannot drop wood blocks that are too hard to be broken by hand (compatible with HLC)");
        config.addCustomCategoryComment(tb, "You can disable/enable bug patches for Thaumic Bases addon here");
        missingPrereqs_ThaumiumBracelet = newEntry(tb,"MissingPrereqs_ThaumiumBracelet", "Adds the missing prereq(s) for the \"Thaumium Bracelet\" research");
        missingPrereqs_VoidBracelet = newEntry(tb,"MissingPrereqs_VoidBracelet", "Adds the missing prereq(s) for the \"Void Bracelet\" research");
        missingPrereqs_VoidWandCore = newEntry(tb, "MissingPrereqs_VoidWandCore", "Adds the missing prereq(s) for the \"Void Wand Core\" research");
        config.addCustomCategoryComment(tx, "Thaumic Exploration");
        blackFloatingCandleRecipePatchEnabled = newEntry(tx, "BlackFloatingCandle", "This patches the crash caused by trying to craft a Black Floating Candle");
        removeNecroInfusionRecipe = newEntry(tx, "RemoveNecroInfusionRecipe", "Remove the buggy infusion recipe for the \"NecroAltar\" with unregistered/null output itemAlter");
        config.addCustomCategoryComment(wg, "You can disable/enable bug patches for Witching Gadgets addon here");
        missingPrereqs_WitchingWearables = newEntry(wg, "MissingPrereqs_WitchingWearables", "Adds the missing prereq(s) for the \"Witching Wearables\" research");
    }
}
