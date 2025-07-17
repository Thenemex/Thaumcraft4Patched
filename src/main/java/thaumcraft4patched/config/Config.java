package thaumcraft4patched.config;

import net.minecraftforge.common.config.Configuration;

import java.io.File;

import static thaumcraft4patched.Thaumcraft4Patched.modName;

@SuppressWarnings("unused")
public class Config {

    protected static Configuration config;
    public static File configDir;
    public static boolean boneBowResearchPatchEnabled;
    public static boolean tc4Enabled, tbEnabled;

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void init(File configFolder) {
        configDir = configFolder;
        configDir.mkdirs();
        config = new Configuration(new File(configDir, modName.concat(".cfg")));
        config.load();
        loadConfig();
        config.save();
    }

    protected static void loadConfig() {
        String mods = "Mods", tc4 = "Thaumcraft-4", tb = "Thaumic-Bases";
        config.addCustomCategoryComment(mods, "You can turn off bug-patches for whole mods here");
        tc4Enabled = newEntry(mods, "Thaumcraft 4");
        tbEnabled = newEntry(mods, "Thaumic Bases");
        config.addCustomCategoryComment(tc4, "You can disable/enable bug patches from Thaumcraft 4 here");
        boneBowResearchPatchEnabled = newEntry(tc4,"HiddenBoneBowResearch", "Remove the hidden property of the research -> it will be unlocked at spawning");
        config.addCustomCategoryComment(tb, "You can disable/enable bug patches from Thaumic Bases addon here");
    }

    public static boolean newEntry(String category, String key) {
        return newEntry(category, key, true);
    }
    public static boolean newEntry(String category, String key, boolean enabled) {
        return config.get(category, key, enabled).getBoolean(enabled);
    }
    public static boolean newEntry(String category, String key, String comment) {
        return newEntry(category,key, true, comment);
    }
    public static boolean newEntry(String category, String key, boolean enabled, String comment) {
        return config.get(category, key, enabled, comment).getBoolean(enabled);
    }
}
