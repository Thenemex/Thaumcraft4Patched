package thaumcraft4patched.model.patch;

import net.minecraft.server.MinecraftServer;
import thaumcraft4patched.config.Config;

/**
 * Prevents MineTweaker 3 from attempting to mutate the command registry of an
 * integrated server that has already shut down.
 */
public final class MineTweakerServerCommandRollbackPatch {

    private MineTweakerServerCommandRollbackPatch() {}

    public static boolean shouldSkipCommandRollback() {
        return Config.mtEnabled
                && Config.mineTweakerServerCommandRollbackPatchEnabled
                && MinecraftServer.getServer() == null;
    }
}