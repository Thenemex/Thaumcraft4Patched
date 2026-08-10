package thaumcraft4patched.config;

import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import nemexlib.config.AConfig;
import thaumcraft4patched.Thaumcraft4Patched;

public class Config extends AConfig {

    public static boolean mcEnabled, oakWoodenSlabBuggedRecipePatchEnabled;
    public static boolean mgckEnabled, opaqueFogNetherDarkShrineJava25PatchEnabled;
    public static int mgckEntropyFogIntensity, mgckEntropyFogCloseness;
    public static boolean hlcEnabled, excavationFocusHlcCompatibilityPatchEnabled, primalCrusherHlcCompatibilityPatchEnabled;
    public static boolean fldEnabled, thaumcraftMagicalLeavesFastDecayPatchEnabled, taintedMagicWarpwoodLeavesFastDecayPatchEnabled;
    public static boolean tcclmEnabled, thaumaturgeWitcheryGuardNonAggressionPatchEnabled;
    public static boolean tc4Enabled, boneBowResearchPatchEnabled, golemLumberCoreWoodHardnessPatchEnabled, nullResearchParentsPatchEnabled;
    public static boolean tbEnabled, missingPrereqs_ThaumiumBracelet, missingPrereqs_VoidBracelet, missingPrereqs_VoidWandCore;
    public static boolean wgEnabled, missingPrereqs_WitchingWearables;
    public static boolean txEnabled, blackFloatingCandleRecipePatchEnabled, removeNecroInfusionRecipe;

    public Config(FMLPreInitializationEvent event) {
        super(Thaumcraft4Patched.modName, event, "1.6.1");
    }

    protected void loadConfig() {
        String mods = "Mods", mc = "Minecraft", mgck = "Magic-Cookies", hlc = "Harvest-Level-Config", tc4 = "Thaumcraft-4", tb = "Thaumic-Bases",
                tx = "Thaumic-Exploration", wg = "Witching-Gadgets", fld = "Fast-Leaf-Decay", tcon = "Thaumic-Concilium";

        comment(mods, "You can turn off bug-patches for whole mods here");
        mcEnabled = newEntry(mods, "Minecraft");
        mgckEnabled = newEntry(mods, "Magic Cookies");
        hlcEnabled = newEntry(mods, "Harvest Level Config");
        fldEnabled = newEntry(mods, "Fast Leaf Decay");
        tcclmEnabled = newEntry(mods, "Thaumic Concilium");
        tc4Enabled = newEntry(mods, "Thaumcraft 4");
        tbEnabled = newEntry(mods, "Thaumic Bases");
        txEnabled = newEntry(mods, "Thaumic Exploration");
        wgEnabled = newEntry(mods, "Witching Gadgets");

        comment(mc, "You can disable/enable bug patches for vanilla Minecraft here");
        oakWoodenSlabBuggedRecipePatchEnabled = newEntry(mc, "OakWoodenSlab", "Removes the recipe that overtake over all other wooden slabs, allowing to make only oak wooden slabs");

        comment(mgck, "You can disable/enable bug patches for Magic Cookies here");
        opaqueFogNetherDarkShrineJava25PatchEnabled = newEntry(mgck, "OpaqueFogNetherDarkShrineJava25", "Removes the opaque fog inside the Dark Shrine, when using higher Java version");
        mgckEntropyFogIntensity = newEntry(mgck, "EntropyFogIntensity", 4, 0, 10, "Controls the Dark Shrine entropy fog intensity. 0 is the lightest, 4 is the recommended default, and 10 is the strongest.");
        mgckEntropyFogCloseness = newEntry(mgck, "EntropyFogCloseness", 5, 0, 10, "Controls how close the Dark Shrine entropy fog appears around the player. 0 keeps the fog furthest away, 5 preserves the normal patched distance, and 10 brings the fog closest.");

        comment(hlc, "You can disable/enable compatibility patches for Harvest Level Config here");
        excavationFocusHlcCompatibilityPatchEnabled = newEntry(hlc, "ExcavationFocusDiamondTools", "While the Excavation Focus magic is actively mining, treats it as a diamond/Thaumium-level pickaxe, axe, and shovel for Harvest Level Config and compatible tool checks. Physically mining with the wand itself remains equivalent to using an empty hand.");
        primalCrusherHlcCompatibilityPatchEnabled = newEntry(hlc, "PrimalCrusherTools", "Makes the Thaumcraft Primal Crusher correctly count as a level-5 pickaxe and shovel for blocks edited by Harvest Level Config");

        comment(fld, "You can disable/enable compatibility patches for Fast Leaf Decay here");
        thaumcraftMagicalLeavesFastDecayPatchEnabled = newEntry(fld, "ThaumcraftMagicalLeaves", "Makes Greatwood and Silverwood leaves use Fast Leaf Decay's configured decay speed");
        taintedMagicWarpwoodLeavesFastDecayPatchEnabled = newEntry(fld, "TaintedMagicWarpwoodLeaves", "Makes Tainted Magic Warpwood leaves use Fast Leaf Decay's configured decay speed");

        comment(tcon, "You can disable/enable compatibility patches for Thaumic Concilium here");
        thaumaturgeWitcheryGuardNonAggressionPatchEnabled = newEntry(tcon, "WitcheryGuardNonAggression", "Stops Thaumic Concilium Thaumaturges and Witchery Village Guards from fighting each other");

        comment(tc4, "You can disable/enable bug patches for Thaumcraft 4 here");
        boneBowResearchPatchEnabled = newEntry(tc4,"HiddenBoneBowResearch", "Removes the hidden property of the research -> it will be unlocked when the player discover the Telum aspect");
        golemLumberCoreWoodHardnessPatchEnabled = newEntry(tc4, "GolemLumberBlockHardness", "This patches the issue when Golem with Lumber core cannot drop wood blocks that are too hard to be broken by hand (compatible with HLC)");
        nullResearchParentsPatchEnabled = newEntry(tc4, "NullResearchParentsPatchedEnabled", false, "Special tweak that will replace any parents/parentsHidden that are Null with an empty array, iterating on all researches registered");

        comment(tb, "You can disable/enable bug patches for Thaumcraft 4 here");
        missingPrereqs_ThaumiumBracelet = newEntry(tb,"MissingPrereqs_ThaumiumBracelet", "Adds the missing prereq(s) for the \"Thaumium Bracelet\" research");
        missingPrereqs_VoidBracelet = newEntry(tb,"MissingPrereqs_VoidBracelet", "Adds the missing prereq(s) for the \"Void Bracelet\" research");
        missingPrereqs_VoidWandCore = newEntry(tb, "MissingPrereqs_VoidWandCore", "Adds the missing prereq(s) for the \"Void Wand Core\" research");

        comment(tx, "You can disable/enable bug patches for Thaumic Exploration here");
        blackFloatingCandleRecipePatchEnabled = newEntry(tx, "BlackFloatingCandle", "This patches the crash caused by trying to craft a Black Floating Candle");
        removeNecroInfusionRecipe = newEntry(tx, "RemoveNecroInfusionRecipe", "Remove the buggy infusion recipe for the \"NecroAltar\" with unregistered/null output itemAlter");

        comment(wg, "You can disable/enable bug patches for Witching Gadgets addon here");
        missingPrereqs_WitchingWearables = newEntry(wg, "MissingPrereqs_WitchingWearables", "Adds the missing prereq(s) for the \"Witching Wearables\" research");
    }
}