package thaumcraft4patched.model.patch;

import net.minecraft.entity.Entity;
import thaumcraft4patched.config.Config;

public final class SpecialAIShamblingHuskBlacklistPatch implements IPatch {

    private static final String HUSK_CLASS =
            "thaumcraft.common.entities.monster.EntityInhabitedZombie";

    private SpecialAIShamblingHuskBlacklistPatch() {}

    public static boolean shouldSkip(Entity entity) {
        if (!Config.saiEnabled || !Config.specialAIShamblingHuskBlacklistPatchEnabled
                || entity == null) {
            return false;
        }
        for (Class<?> type = entity.getClass(); type != null; type = type.getSuperclass()) {
            if (HUSK_CLASS.equals(type.getName())) {
                return true;
            }
        }
        return false;
    }
}
