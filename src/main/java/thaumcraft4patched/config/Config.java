package thaumcraft4patched.config;

import net.minecraftforge.common.config.Configuration;

import java.io.File;

import static thaumcraft4patched.Thaumcraft4Patched.modName;

public class Config {

    private static Configuration config;
    public static File configDir;
    public static boolean boneBowResearchPatchEnabled;

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void init(File configFolder) {
        configDir = configFolder;
        configDir.mkdirs();
        config = new Configuration(new File(configDir, modName.concat(".cfg")));
        config.load();
        loadConfig();
        config.save();
    }

    private static void loadConfig() {
        String bugsCat = "Bug-Patches";
        config.addCustomCategoryComment(bugsCat, "You can disable/enable bug patches from the mod here.");
        boneBowResearchPatchEnabled = newEntry(bugsCat,"HiddenBoneBowResearch");
    }

    public static boolean newEntry(String tag, String key) {
        return newEntry(tag, key, true);
    }
    public static boolean newEntry(String tag, String key, boolean enabled) {
        return config.get(tag, key, enabled).getBoolean(enabled);
    }
}
