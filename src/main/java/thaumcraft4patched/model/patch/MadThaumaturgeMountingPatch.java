package thaumcraft4patched.model.patch;

import net.minecraft.entity.Entity;
import thaumcraft4patched.config.Config;

public final class MadThaumaturgeMountingPatch implements IPatch {

    private MadThaumaturgeMountingPatch() {}

    public static void handleMount(Entity rider, Entity target) {

        if (!Config.tcclmEnabled
                || !Config.madThaumaturgeMountingPatchEnabled) {

            rider.mountEntity(target);
        }


    }
}
