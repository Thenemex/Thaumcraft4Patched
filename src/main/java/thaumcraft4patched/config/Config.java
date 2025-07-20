package thaumcraft4patched.config;

import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import nemexlib.config.AConfig;

import static thaumcraft4patched.Thaumcraft4Patched.modName;

public class Config extends AConfig {

    public static boolean tc4Enabled, boneBowResearchPatchEnabled;
    public static boolean tbEnabled, missingPrereqs_ThaumiumBracelet, missingPrereqs_VoidBracelet, missingPrereqs_VoidWandCore;

    public Config(FMLPreInitializationEvent event, String version) {
        super(event, modName, modName, version);
    }

    protected void loadConfig() {
        String mods = "Mods", tc4 = "Thaumcraft-4", tb = "Thaumic-Bases";
        config.addCustomCategoryComment(mods, "You can turn off bug-patches for whole mods here");
        tc4Enabled = newEntry(mods, "Thaumcraft 4");
        tbEnabled = newEntry(mods, "Thaumic Bases");
        config.addCustomCategoryComment(tc4, "You can disable/enable bug patches from Thaumcraft 4 here");
        boneBowResearchPatchEnabled = newEntry(tc4,"HiddenBoneBowResearch", "Removes the hidden property of the research -> it will be unlocked at spawning");
        config.addCustomCategoryComment(tb, "You can disable/enable bug patches from Thaumic Bases addon here");
        missingPrereqs_ThaumiumBracelet = newEntry(tb,"MissingPrereqs_ThaumiumBracelet", "Adds the missing prereq(s) for the \"Thaumium Bracelet\" research");
        missingPrereqs_VoidBracelet = newEntry(tb,"MissingPrereqs_VoidBracelet", "Adds the missing prereq(s) for the \"Void Bracelet\" research");
        missingPrereqs_VoidWandCore = newEntry(tb, "MissingPrereqs_VoidWandCore", "Adds the missing prereq(s) for the \"Void Wand Core\" research");
    }
}
