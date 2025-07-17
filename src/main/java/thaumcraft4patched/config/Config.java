package thaumcraft4patched.config;

import net.minecraftforge.common.config.Configuration;

import java.io.File;

import static thaumcraft4patched.Thaumcraft4Patched.modName;

@SuppressWarnings("unused")
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
        boneBowResearchPatchEnabled = newEntry(bugsCat,"HiddenBoneBowResearch", "Remove the hidden property of the research -> it will be unlocked at spawning.");
    }

    public static boolean newEntry(String tag, String key) {
        return newEntry(tag, key, true);
    }
    public static boolean newEntry(String tag, String key, boolean enabled) {
        return config.get(tag, key, enabled).getBoolean(enabled);
    }
    public static boolean newEntry(String tag, String key, String comment) {
        return newEntry(tag,key, true, comment);
    }
    public static boolean newEntry(String tag, String key, boolean enabled, String comment) {
        return config.get(tag, key, enabled, comment).getBoolean(enabled);
    }
}
